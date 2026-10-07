(() => {
    "use strict";

    const App = window.SecureAgent;

    App.loadConnectionHistory = async (computerName) => {
        if (!computerName) {
            return;
        }

        App.ensureNetworkSelectionComputer(computerName);

        App.showMessage(
            App.el.connectionHistoryTableBody,
            9,
            "종료된 연결 이력을 불러오는 중입니다."
        );

        try {
            const encodedName = encodeURIComponent(computerName);

            const connections = await App.fetchJson(
                `/api/agents/${encodedName}/network-connections/history`
            );

            App.data.connectionHistoryList = connections.filter(
                (connection) => {
                    return (
                        App.normalize(connection.activeYn) === "N" ||
                        Boolean(connection.endedAt)
                    );
                }
            );

            App.filterHistory();
        } catch (error) {
            console.error(error);
            App.data.connectionHistoryList = [];

            App.showMessage(
                App.el.connectionHistoryTableBody,
                9,
                "종료된 연결 이력을 불러오지 못했습니다.",
                true
            );
        }
    };

    App.filterHistory = () => {
        const filter = App.normalize(
            App.el.historyFilter.value
        );

        const keyword = App.el.historySearchInput.value
            .trim()
            .toLowerCase();

        const filteredHistory = App.data.connectionHistoryList.filter(
            (connection) => {
                let filterMatches = true;

                if (filter === "EXTERNAL") {
                    filterMatches =
                        App.normalize(connection.externalYn) === "Y";
                } else if (filter === "SUSPICIOUS") {
                    filterMatches =
                        App.normalize(connection.suspiciousYn) === "Y";
                }

                const keywordMatches =
                    keyword === "" ||
                    App.connectionSearchText(connection).includes(keyword);

                return filterMatches && keywordMatches;
            }
        );

        App.renderHistoryTable(filteredHistory);
    };

    App.renderHistoryTable = (history) => {
        const tbody = App.el.connectionHistoryTableBody;
        tbody.replaceChildren();

        if (history.length === 0) {
            const selectedFilter = App.normalize(
                App.el.historyFilter.value
            );

            const message =
                selectedFilter === "SUSPICIOUS"
                    ? "종료된 의심 연결 이력이 없습니다."
                    : "조건에 맞는 종료 이력이 없습니다.";

            App.showMessage(tbody, 9, message);
            App.updateNetworkMasterCheckbox(
                "historySelectAllCheckbox",
                "network-history-checkbox"
            );
            return;
        }

        history.forEach((connection) => {
            const isExternal =
                App.normalize(connection.externalYn) === "Y";

            const isSuspicious =
                App.normalize(connection.suspiciousYn) === "Y";

            const row = document.createElement("tr");
            row.className = "network-summary-row";

            App.applyNetworkRowState(row, connection);

            const detailRow = App.createNetworkDetailRow(
                connection,
                9,
                true
            );

            row.append(
                App.createNetworkSelectionCell(
                    connection,
                    "history",
                    "network-history-checkbox",
                    "historySelectAllCheckbox"
                ),
                App.badge(connection.protocol, "protocol-badge"),
                App.styledCell(
                    connection.remoteAddress,
                    "network-address-cell"
                ),
                App.styledCell(
                    connection.remotePort,
                    "network-port-cell"
                ),
                App.badge(connection.state, "status-badge"),
                App.styledCell(
                    connection.processName,
                    "network-process-cell"
                ),
                App.badge(
                    isExternal ? "외부" : "내부",
                    isExternal ? "external-badge" : "internal-badge"
                ),
                App.badge(
                    isSuspicious ? "의심" : "정상",
                    isSuspicious ? "suspicious-badge" : "safe-badge"
                ),
                App.createNetworkDetailButtonCell(detailRow)
            );

            tbody.append(row, detailRow);
        });

        App.syncNetworkSelectionCheckboxes();
        App.updateNetworkMasterCheckbox(
            "historySelectAllCheckbox",
            "network-history-checkbox"
        );
    };

    App.bindNetworkMasterCheckbox(
        "historySelectAllCheckbox",
        "network-history-checkbox"
    );
})();
