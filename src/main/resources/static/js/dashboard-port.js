(() => {
    "use strict";

    const App = window.SecureAgent;

    App.loadOpenPorts = async (computerName) => {
        if (!computerName) {
            return;
        }

        App.showMessage(
            App.el.portTableBody,
            10,
            "열린 포트 정보를 불러오는 중입니다."
        );

        try {
            const encodedName = encodeURIComponent(computerName);

            App.data.openPortList = await App.fetchJson(
                `/api/agents/${encodedName}/open-ports`
            );

            App.updatePortSummary();
            App.updateLastCollectedAt();
            App.filterPorts();
        } catch (error) {
            console.error(error);
            App.data.openPortList = [];
            App.updatePortSummary();
            App.showMessage(
                App.el.portTableBody,
                10,
                "열린 포트 정보를 불러오지 못했습니다.",
                true
            );
        }
    };

    App.updatePortSummary = () => {
        const ports = App.data.openPortList;
        const tcpCount = ports.filter((port) => {
            return App.normalize(port.protocol) === "TCP";
        }).length;
        const udpCount = ports.filter((port) => {
            return App.normalize(port.protocol) === "UDP";
        }).length;
        const warningCount = ports.filter((port) => {
            const riskLevel = App.normalize(port.riskLevel);
            return riskLevel === "CAUTION" || riskLevel === "DANGER";
        }).length;

        App.el.totalPortCount.textContent = ports.length;
        App.el.tcpPortCount.textContent = tcpCount;
        App.el.udpPortCount.textContent = udpCount;
        App.el.warningPortCount.textContent = warningCount;
    };

    App.updateLastCollectedAt = () => {
        const collectedDates = App.data.openPortList
            .map((port) => App.parseDate(port.receivedAt))
            .filter(Boolean);

        if (collectedDates.length === 0) {
            App.el.lastCollectedAt.textContent = "마지막 수집 시각: -";
            return;
        }

        const latestDate = collectedDates.reduce((latest, current) => {
            return latest > current ? latest : current;
        });

        App.el.lastCollectedAt.textContent =
            "마지막 수집 시각: " + App.formatDate(latestDate);
    };

    App.filterPorts = () => {
        const protocol = App.normalize(App.el.protocolFilter.value);
        const keyword = App.el.searchInput.value.trim().toLowerCase();

        const filteredPorts = App.data.openPortList.filter((port) => {
            const protocolMatches =
                protocol === "ALL" ||
                App.normalize(port.protocol) === protocol;

            const searchableText = App.searchable([
                port.protocol,
                port.serviceName,
                port.localAddress,
                port.localPort,
                port.state,
                port.pid,
                port.processName,
                port.riskLevel,
                App.riskLabel(port.riskLevel),
                port.riskReason
            ]);

            return protocolMatches && (
                keyword === "" || searchableText.includes(keyword)
            );
        });

        App.renderPortTable(filteredPorts);
    };

    App.renderPortTable = (ports) => {
        const tbody = App.el.portTableBody;
        tbody.replaceChildren();

        if (ports.length === 0) {
            App.showMessage(
                tbody,
                10,
                "조건에 맞는 포트 정보가 없습니다."
            );
            return;
        }

        ports.forEach((port) => {
            const row = document.createElement("tr");
            const riskLevel = App.normalize(port.riskLevel || "NORMAL");

            if (riskLevel === "CAUTION") {
                row.classList.add("risk-row-caution");
            }

            if (riskLevel === "DANGER") {
                row.classList.add("risk-row-danger");
            }

            row.append(
                App.badge(port.protocol, "protocol-badge"),
                App.styledCell(port.serviceName, "service-cell"),
                App.cell(port.localAddress),
                App.cell(port.localPort),
                App.badge(port.state, "status-badge"),
                App.cell(port.pid),
                App.cell(port.processName),
                App.badge(
                    App.riskLabel(riskLevel),
                    "risk-badge " + App.riskClass(riskLevel)
                ),
                App.styledCell(port.riskReason, "risk-reason-cell"),
                App.styledCell(
                    App.formatDate(port.receivedAt),
                    "date-cell"
                )
            );

            tbody.appendChild(row);
        });
    };
})();