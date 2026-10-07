(() => {
    "use strict";

    const App = window.SecureAgent;

    App.loadNetworkConnections = async (computerName) => {
        if (!computerName) {
            return;
        }

        App.ensureNetworkSelectionComputer(computerName);

        App.showMessage(
            App.el.networkConnectionTableBody,
            9,
            "네트워크 연결 정보를 불러오는 중입니다."
        );

        try {
            const encodedName = encodeURIComponent(computerName);

            App.data.networkConnectionList = await App.fetchJson(
                `/api/agents/${encodedName}/network-connections`
            );

            App.filterNetworkConnections();
        } catch (error) {
            console.error(error);
            App.data.networkConnectionList = [];

            App.showMessage(
                App.el.networkConnectionTableBody,
                9,
                "네트워크 연결 정보를 불러오지 못했습니다.",
                true
            );
        }
    };

    App.filterNetworkConnections = () => {
        const filter = App.normalize(
            App.el.networkStatusFilter.value
        );

        const keyword = App.el.networkSearchInput.value
            .trim()
            .toLowerCase();

        const filteredConnections = App.data.networkConnectionList.filter(
            (connection) => {
                let filterMatches = true;

                if (
                    filter === "ESTABLISHED" ||
                    filter === "LISTENING"
                ) {
                    filterMatches =
                        App.normalize(connection.state) === filter;
                } else if (filter === "SUSPICIOUS") {
                    filterMatches =
                        App.normalize(connection.suspiciousYn) === "Y";
                } else if (filter === "EXTERNAL") {
                    filterMatches =
                        App.normalize(connection.externalYn) === "Y";
                }

                const keywordMatches =
                    keyword === "" ||
                    App.connectionSearchText(connection).includes(keyword);

                return filterMatches && keywordMatches;
            }
        );

        App.renderNetworkTable(filteredConnections);
    };

    App.renderNetworkTable = (connections) => {
        const tbody = App.el.networkConnectionTableBody;
        tbody.replaceChildren();

        if (connections.length === 0) {
            const selectedFilter = App.normalize(
                App.el.networkStatusFilter.value
            );

            const message =
                selectedFilter === "SUSPICIOUS"
                    ? "현재 탐지된 의심 연결이 없습니다."
                    : "조건에 맞는 네트워크 연결이 없습니다.";

            App.showMessage(tbody, 9, message);
            App.updateNetworkMasterCheckbox(
                "networkSelectAllCheckbox",
                "network-current-checkbox"
            );
            return;
        }

        connections.forEach((connection) => {
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
                false
            );

            row.append(
                App.createNetworkSelectionCell(
                    connection,
                    "current",
                    "network-current-checkbox",
                    "networkSelectAllCheckbox"
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
            "networkSelectAllCheckbox",
            "network-current-checkbox"
        );
    };

    App.bindNetworkMasterCheckbox(
        "networkSelectAllCheckbox",
        "network-current-checkbox"
    );
})();
