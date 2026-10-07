(() => {
    "use strict";

    const App = window.SecureAgent;

    App.networkSelectionStore = App.networkSelectionStore || new Map();
    App.networkCheckboxMeta = App.networkCheckboxMeta || new WeakMap();
    App.networkSelectionComputerName = App.networkSelectionComputerName || "";

    App.connectionSearchText = (connection) => {
        return App.searchable([
            connection.protocol,
            connection.localAddress,
            connection.localPort,
            connection.remoteAddress,
            connection.remotePort,
            connection.state,
            connection.pid,
            connection.processName,
            connection.connectionCount,
            connection.externalYn,
            connection.suspiciousYn,
            connection.riskReason,
            connection.firstSeenAt,
            connection.lastSeenAt,
            connection.endedAt
        ]);
    };

    App.connectionSelectionKey = (connection) => {
        if (!App.isEmpty(connection.id)) {
            return `id:${connection.id}`;
        }

        return `connection:${App.searchable([
            connection.computerName,
            connection.protocol,
            connection.localAddress,
            connection.localPort,
            connection.remoteAddress,
            connection.remotePort,
            connection.pid,
            connection.firstSeenAt,
            connection.endedAt
        ])}`;
    };

    App.sourceLabel = (source) => {
        switch (source) {
            case "current":
                return "현재 연결";
            case "history":
                return "종료 이력";
            case "suspicious":
                return "의심 연결";
            default:
                return "연결";
        }
    };

    App.emitNetworkSelectionChanged = () => {
        document.dispatchEvent(
            new CustomEvent("secureagent:network-selection-changed", {
                detail: {
                    count: App.networkSelectionStore.size
                }
            })
        );
    };

    App.syncNetworkSelectionCheckboxes = () => {
        document
            .querySelectorAll(".network-row-checkbox[data-selection-key]")
            .forEach((checkbox) => {
                checkbox.checked = App.networkSelectionStore.has(
                    checkbox.dataset.selectionKey
                );
            });
    };

    App.setNetworkSelected = (connection, source, selected) => {
        const key = App.connectionSelectionKey(connection);

        if (selected) {
            if (!App.networkSelectionStore.has(key)) {
                App.networkSelectionStore.set(key, {
                    key,
                    source,
                    connection: { ...connection }
                });
            } else {
                const existing = App.networkSelectionStore.get(key);
                existing.connection = { ...connection };
            }
        } else {
            App.networkSelectionStore.delete(key);
        }

        App.syncNetworkSelectionCheckboxes();
        App.emitNetworkSelectionChanged();
    };

    App.clearAllNetworkSelections = () => {
        App.networkSelectionStore.clear();
        App.syncNetworkSelectionCheckboxes();

        document
            .querySelectorAll(".network-master-checkbox")
            .forEach((checkbox) => {
                checkbox.checked = false;
                checkbox.indeterminate = false;
            });

        App.emitNetworkSelectionChanged();
    };

    App.ensureNetworkSelectionComputer = (computerName) => {
        if (!computerName) {
            return;
        }

        if (App.networkSelectionComputerName === computerName) {
            return;
        }

        App.networkSelectionComputerName = computerName;
        App.clearAllNetworkSelections();
    };

    App.applyNetworkRowState = (row, connection) => {
        const isExternal = App.normalize(connection.externalYn) === "Y";
        const isSuspicious = App.normalize(connection.suspiciousYn) === "Y";

        if (isSuspicious) {
            row.classList.add("network-row-suspicious");
        } else if (isExternal) {
            row.classList.add("network-row-external");
        }
    };

    App.createNetworkSelectionCell = (
        connection,
        source,
        checkboxClass,
        masterCheckboxId
    ) => {
        const td = document.createElement("td");
        const checkbox = document.createElement("input");
        const key = App.connectionSelectionKey(connection);

        td.className = "network-select-cell";

        checkbox.type = "checkbox";
        checkbox.className = `network-row-checkbox ${checkboxClass}`;
        checkbox.dataset.selectionKey = key;
        checkbox.checked = App.networkSelectionStore.has(key);
        checkbox.setAttribute("aria-label", "연결 항목 선택");

        App.networkCheckboxMeta.set(checkbox, {
            connection,
            source
        });

        checkbox.addEventListener("change", () => {
            App.setNetworkSelected(
                connection,
                source,
                checkbox.checked
            );

            App.updateNetworkMasterCheckbox(
                masterCheckboxId,
                checkboxClass
            );
        });

        td.appendChild(checkbox);
        return td;
    };

    App.updateNetworkMasterCheckbox = (
        masterCheckboxId,
        checkboxClass
    ) => {
        const master = document.getElementById(masterCheckboxId);

        if (!master) {
            return;
        }

        const checkboxes = Array.from(
            document.querySelectorAll(`.${checkboxClass}`)
        );

        if (checkboxes.length === 0) {
            master.checked = false;
            master.indeterminate = false;
            return;
        }

        const checkedCount = checkboxes.filter(
            (checkbox) => checkbox.checked
        ).length;

        master.checked = checkedCount === checkboxes.length;
        master.indeterminate =
            checkedCount > 0 && checkedCount < checkboxes.length;
    };

    App.bindNetworkMasterCheckbox = (
        masterCheckboxId,
        checkboxClass
    ) => {
        const master = document.getElementById(masterCheckboxId);

        if (!master || master.dataset.bound === "true") {
            return;
        }

        master.dataset.bound = "true";

        master.addEventListener("change", () => {
            const checkboxes = Array.from(
                document.querySelectorAll(`.${checkboxClass}`)
            );

            checkboxes.forEach((checkbox) => {
                const meta = App.networkCheckboxMeta.get(checkbox);

                checkbox.checked = master.checked;

                if (!meta) {
                    return;
                }

                const key = App.connectionSelectionKey(meta.connection);

                if (master.checked) {
                    if (!App.networkSelectionStore.has(key)) {
                        App.networkSelectionStore.set(key, {
                            key,
                            source: meta.source,
                            connection: { ...meta.connection }
                        });
                    }
                } else {
                    App.networkSelectionStore.delete(key);
                }
            });

            master.indeterminate = false;
            App.syncNetworkSelectionCheckboxes();
            App.emitNetworkSelectionChanged();
        });
    };

    App.createNetworkDetailItem = (
        label,
        value,
        className = ""
    ) => {
        const item = document.createElement("div");
        const labelElement = document.createElement("span");
        const valueElement = document.createElement("strong");

        item.className = "network-detail-item";

        if (className) {
            item.classList.add(className);
        }

        labelElement.className = "network-detail-label";
        labelElement.textContent = label;

        valueElement.className = "network-detail-value";
        valueElement.textContent = App.isEmpty(value)
            ? "-"
            : String(value);

        item.append(labelElement, valueElement);
        return item;
    };

    App.createNetworkDetailRow = (
        connection,
        columnCount,
        includeEndedAt = false
    ) => {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        const grid = document.createElement("div");

        row.className = "network-detail-row";
        row.hidden = true;

        cell.colSpan = columnCount;
        cell.className = "network-detail-cell";

        grid.className = "network-detail-grid";

        grid.append(
            App.createNetworkDetailItem("내 IP", connection.localAddress),
            App.createNetworkDetailItem("내 포트", connection.localPort),
            App.createNetworkDetailItem("PID", connection.pid),
            App.createNetworkDetailItem(
                "접속 횟수",
                connection.connectionCount
            ),
            App.createNetworkDetailItem(
                "판정 이유",
                connection.riskReason || "특이사항 없음",
                "network-detail-reason"
            ),
            App.createNetworkDetailItem(
                "최초 발견",
                App.formatDate(connection.firstSeenAt)
            ),
            App.createNetworkDetailItem(
                "마지막 확인",
                App.formatDate(connection.lastSeenAt)
            )
        );

        if (includeEndedAt) {
            grid.appendChild(
                App.createNetworkDetailItem(
                    "종료 시각",
                    App.formatDate(connection.endedAt)
                )
            );
        }

        cell.appendChild(grid);
        row.appendChild(cell);
        return row;
    };

    App.createNetworkDetailButtonCell = (detailRow) => {
        const td = document.createElement("td");
        const button = document.createElement("button");

        td.className = "network-detail-toggle-cell";

        button.type = "button";
        button.className = "network-detail-toggle";
        button.textContent = "상세 ▼";
        button.setAttribute("aria-expanded", "false");

        button.addEventListener("click", () => {
            const willOpen = detailRow.hidden;

            detailRow.hidden = !willOpen;
            button.textContent = willOpen ? "상세 ▲" : "상세 ▼";
            button.setAttribute(
                "aria-expanded",
                willOpen ? "true" : "false"
            );
        });

        td.appendChild(button);
        return td;
    };
})();
