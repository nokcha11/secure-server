(() => {
    "use strict";

    const App = window.SecureAgent = window.SecureAgent || {};

    App.data = {
        systemInfoList: [],
        openPortList: [],
        networkConnectionList: [],
        connectionHistoryList: []
    };

    App.initializeElements = () => {
        const byId = (id) => document.getElementById(id);

        App.el = {
            computerSelect: byId("computerSelect"),
            refreshButton: byId("refreshButton"),
            lastCollectedAt: byId("lastCollectedAt"),
            computerName: byId("computerName"),
            osName: byId("osName"),
            osVersion: byId("osVersion"),
            userName: byId("userName"),

            totalPortCount: byId("totalPortCount"),
            tcpPortCount: byId("tcpPortCount"),
            udpPortCount: byId("udpPortCount"),
            warningPortCount: byId("warningPortCount"),
            protocolFilter: byId("protocolFilter"),
            searchInput: byId("searchInput"),
            portTableBody: byId("portTableBody"),

            networkSearchInput: byId("networkSearchInput"),
            networkStatusFilter: byId("networkStatusFilter"),
            networkConnectionTableBody: byId(
                "networkConnectionTableBody"
            ),

            historySearchInput: byId("historySearchInput"),
            historyFilter: byId("historyFilter"),
            connectionHistoryTableBody: byId(
                "connectionHistoryTableBody"
            ),

            suspiciousConnectionCount: byId(
                "suspiciousConnectionCount"
            ),
            suspiciousStatusMessage: byId(
                "suspiciousStatusMessage"
            ),
            suspiciousConnectionTableBody: byId(
                "suspiciousConnectionTableBody"
            ),
            suspiciousSection: document.querySelector(
                ".suspicious-section"
            ),
            suspiciousTableWrapper: document.querySelector(
                ".suspicious-table-wrapper"
            ),

            hourlyConnectionChart: byId("hourlyConnectionChart"),
            hourlyChartStatus: byId("hourlyChartStatus"),
            hourlyTotalCount: byId("hourlyTotalCount"),
            hourlyPeakTime: byId("hourlyPeakTime"),

            relationGraphSvg: byId("relationGraphSvg"),
            relationGraphStatus: byId("relationGraphStatus"),
            relationNodeCount: byId("relationNodeCount"),
            relationGraphEmpty: byId("relationGraphEmpty"),
            relationGraphDetails: byId("relationGraphDetails")
        };
    };

    App.bind = (element, eventName, listener) => {
        if (element) {
            element.addEventListener(eventName, listener);
        }
    };

    App.fetchJson = async (url) => {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`API 조회 실패: ${response.status} ${url}`);
        }

        return response.json();
    };

    App.normalize = (value) => String(value || "").toUpperCase();

    App.isEmpty = (value) => {
        return value === null || value === undefined || value === "";
    };

    App.searchable = (values) => {
        return values
            .map((value) => value ?? "")
            .join(" ")
            .toLowerCase();
    };

    App.parseDate = (value) => {
        if (!value) {
            return null;
        }

        const text = String(value).replace(/(\.\d{3})\d+/, "$1");
        const date = new Date(text);

        return Number.isNaN(date.getTime()) ? null : date;
    };

    App.formatDate = (value) => {
        const date = App.parseDate(value);

        if (!date) {
            return value ? String(value) : "-";
        }

        return date.toLocaleString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit"
        });
    };

    App.riskLabel = (level) => {
        switch (App.normalize(level)) {
            case "DANGER":
                return "위험";
            case "CAUTION":
                return "주의";
            case "NORMAL":
            case "SAFE":
                return "정상";
            default:
                return "미분류";
        }
    };

    App.riskClass = (level) => {
        switch (App.normalize(level)) {
            case "DANGER":
                return "risk-danger";
            case "CAUTION":
                return "risk-caution";
            case "NORMAL":
            case "SAFE":
                return "risk-normal";
            default:
                return "risk-unknown";
        }
    };

    App.cell = (value) => {
        const td = document.createElement("td");
        td.textContent = App.isEmpty(value) ? "-" : value;
        return td;
    };

    App.styledCell = (value, className) => {
        const td = App.cell(value);
        td.classList.add(className);
        return td;
    };

    App.badge = (value, className) => {
        const td = document.createElement("td");
        const span = document.createElement("span");

        span.className = className;
        span.textContent = App.isEmpty(value) ? "-" : value;
        td.appendChild(span);

        return td;
    };

    App.showMessage = (tbody, columns, message, isError = false) => {
        if (!tbody) {
            return;
        }

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = columns;
        cell.className = "empty-message";
        cell.textContent = message;

        if (isError) {
            cell.classList.add("error-message");
        }

        row.appendChild(cell);
        tbody.replaceChildren(row);
    };

    App.fillComputerSelect = () => {
        const { computerSelect } = App.el;

        if (!computerSelect) {
            return;
        }

        const previousComputerName = computerSelect.value;

        computerSelect.replaceChildren();

        App.data.systemInfoList.forEach((info) => {
            const option = document.createElement("option");
            option.value = info.computerName;
            option.textContent = info.computerName;
            computerSelect.appendChild(option);
        });

        const previousStillExists = App.data.systemInfoList.some((info) => {
            return info.computerName === previousComputerName;
        });

        if (previousStillExists) {
            computerSelect.value = previousComputerName;
        }
    };

    App.clearSystemInfo = () => {
        const { computerName, osName, osVersion, userName, lastCollectedAt } =
            App.el;

        if (computerName) computerName.textContent = "-";
        if (osName) osName.textContent = "-";
        if (osVersion) osVersion.textContent = "-";
        if (userName) userName.textContent = "-";
        if (lastCollectedAt) {
            lastCollectedAt.textContent = "마지막 수집 시각: -";
        }
    };

    App.displaySystemInfo = (computerName) => {
        const info = App.data.systemInfoList.find((item) => {
            return item.computerName === computerName;
        });

        if (!info) {
            App.clearSystemInfo();
            return;
        }

        App.el.computerName.textContent = info.computerName || "-";
        App.el.osName.textContent = info.osName || "-";
        App.el.osVersion.textContent = info.osVersion || "-";
        App.el.userName.textContent = info.userName || "-";
    };

	App.loadSelectedComputerData =
	        async (computerName) => {

	    if (!computerName) {
	        return;
	    }

	    await Promise.all([

	        App.loadOpenPorts(
	                computerName
	        ),

	        App.loadNetworkConnections(
	                computerName
	        ),

	        App.loadConnectionHistory(
	                computerName
	        ),

	        App.loadSuspiciousConnections(
	                computerName
	        ),

	        App.loadHourlyStats(
	                computerName
	        ),

	        App.loadPacketSummary(
	                computerName
	        ), 
			App.loadPacketDetails(
				computerName
			)
	    ]);

	    App.drawRelationGraph();
	};

    App.loadSystemInfo = async () => {
        App.showMessage(
            App.el.portTableBody,
            10,
            "PC 정보를 불러오는 중입니다."
        );

        try {
            App.data.systemInfoList = await App.fetchJson(
                "/api/agents/system-info"
            );

            if (App.data.systemInfoList.length === 0) {
                App.el.computerSelect.innerHTML =
                    '<option value="">등록된 PC가 없습니다.</option>';

                App.clearSystemInfo();
                App.showMessage(
                    App.el.portTableBody,
                    10,
                    "등록된 PC 정보가 없습니다."
                );
                return;
            }

            App.fillComputerSelect();

            const computerName = App.el.computerSelect.value;
            App.displaySystemInfo(computerName);
            await App.loadSelectedComputerData(computerName);
        } catch (error) {
            console.error(error);
            App.clearSystemInfo();
            App.showMessage(
                App.el.portTableBody,
                10,
                "시스템 정보를 불러오지 못했습니다.",
                true
            );
        }
    };
})();