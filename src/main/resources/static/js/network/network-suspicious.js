(() => {
    "use strict";

    const App = window.SecureAgent;

    App.loadSuspiciousConnections = async (computerName) => {
        if (!computerName) {
            return;
        }

        App.ensureNetworkSelectionComputer(computerName);

        try {
            const encodedName = encodeURIComponent(computerName);

            const connections = await App.fetchJson(
                `/api/agents/${encodedName}/network-connections/suspicious`
            );

            App.data.suspiciousConnectionList = connections;
            App.renderSuspiciousConnections(connections);
        } catch (error) {
            console.error(error);
            App.data.suspiciousConnectionList = [];

            App.el.suspiciousConnectionCount.textContent = "-";
            App.el.suspiciousStatusMessage.textContent =
                "의심 연결 정보를 불러오지 못했습니다.";
            App.el.suspiciousTableWrapper.hidden = true;
        }
    };

    App.renderSuspiciousConnections = (connections) => {
        const { el } = App;

        el.suspiciousConnectionTableBody.replaceChildren();
        el.suspiciousConnectionCount.textContent =
            `${connections.length}건`;

        if (connections.length === 0) {
            el.suspiciousSection.classList.remove("has-threat");
            el.suspiciousSection.classList.add("has-no-threat");

            el.suspiciousStatusMessage.className =
                "security-status-message status-safe";
            el.suspiciousStatusMessage.textContent =
                "현재 탐지된 의심 연결이 없습니다.";

            el.suspiciousTableWrapper.hidden = true;

            App.updateNetworkMasterCheckbox(
                "suspiciousSelectAllCheckbox",
                "network-suspicious-checkbox"
            );
            return;
        }

        el.suspiciousSection.classList.remove("has-no-threat");
        el.suspiciousSection.classList.add("has-threat");

        el.suspiciousStatusMessage.className =
            "security-status-message status-danger";
        el.suspiciousStatusMessage.textContent =
            `주의가 필요한 연결 ${connections.length}건이 탐지되었습니다.`;

        el.suspiciousTableWrapper.hidden = false;

        connections.forEach((connection) => {
            const active =
                App.normalize(connection.activeYn) === "Y";

            const external =
                App.normalize(connection.externalYn) === "Y";

            const row = document.createElement("tr");
            row.className =
                "network-summary-row network-row-suspicious";

            const detailRow = App.createNetworkDetailRow(
                connection,
                9,
                Boolean(connection.endedAt)
            );

            row.append(
                App.createNetworkSelectionCell(
                    connection,
                    "suspicious",
                    "network-suspicious-checkbox",
                    "suspiciousSelectAllCheckbox"
                ),
                App.badge(
                    active ? "현재 연결" : "종료",
                    active
                        ? "active-connection-badge"
                        : "ended-connection-badge"
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
                    external ? "외부" : "내부",
                    external ? "external-badge" : "internal-badge"
                ),
                App.createNetworkDetailButtonCell(detailRow)
            );

            el.suspiciousConnectionTableBody.append(
                row,
                detailRow
            );
        });

        App.syncNetworkSelectionCheckboxes();
        App.updateNetworkMasterCheckbox(
            "suspiciousSelectAllCheckbox",
            "network-suspicious-checkbox"
        );
    };

    App.bindNetworkMasterCheckbox(
        "suspiciousSelectAllCheckbox",
        "network-suspicious-checkbox"
    );
})();
