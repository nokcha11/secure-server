(() => {
    "use strict";

    const App = window.SecureAgent;

    function byId(id) {
        return document.getElementById(id);
    }

    function setText(id, value) {
        const element = byId(id);
        if (element) {
            element.textContent = String(value ?? "-");
        }
    }

    function numberFromText(id) {
        const element = byId(id);
        if (!element) {
            return 0;
        }

        const value = Number(
            String(element.textContent || "0").replace(/[^0-9.-]/g, "")
        );

        return Number.isFinite(value) ? value : 0;
    }

    function setStateBadge(id, state, label) {
        const badge = byId(id);
        if (!badge) {
            return;
        }

        badge.className = "overview-state-badge";
        badge.textContent = label;

        if (state) {
            badge.classList.add(`is-${state}`);
        }
    }

    App.syncOverviewComputerSelect = () => {
        const select = byId("overviewComputerSelect");
        if (!select) {
            return;
        }

        const currentName = App.el?.computerSelect?.value || select.value;
        const list = Array.isArray(App.data.systemInfoList)
            ? App.data.systemInfoList
            : [];

        select.replaceChildren();

        if (list.length === 0) {
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "등록된 PC가 없습니다.";
            select.appendChild(option);
            return;
        }

        list.forEach((info) => {
            const option = document.createElement("option");
            option.value = info.computerName || "";
            option.textContent = info.computerName || "-";
            select.appendChild(option);
        });

        const exists = list.some(
            (info) => info.computerName === currentName
        );

        if (exists) {
            select.value = currentName;
        } else if (App.el?.computerSelect?.value) {
            select.value = App.el.computerSelect.value;
        }
    };

    App.updateOverview = () => {
        App.syncOverviewComputerSelect();

        const selectedComputer =
            App.el?.computerSelect?.value ||
            byId("overviewComputerSelect")?.value ||
            "";

        const systemInfo = (App.data.systemInfoList || []).find(
            (item) => item.computerName === selectedComputer
        );

        const ports = Array.isArray(App.data.openPortList)
            ? App.data.openPortList
            : [];

        const connections = Array.isArray(App.data.networkConnectionList)
            ? App.data.networkConnectionList
            : [];

        const normalPorts = ports.filter((port) => {
            const level = App.normalize(port.riskLevel || "NORMAL");
            return level === "NORMAL" || level === "SAFE" || level === "";
        });

        const cautionPorts = ports.filter(
            (port) => App.normalize(port.riskLevel) === "CAUTION"
        );

        const dangerPorts = ports.filter(
            (port) => App.normalize(port.riskLevel) === "DANGER"
        );

        const externalConnections = connections.filter(
            (connection) => App.normalize(connection.externalYn) === "Y"
        );

        const suspiciousConnections = connections.filter(
            (connection) => App.normalize(connection.suspiciousYn) === "Y"
        );

        const packetTotal = numberFromText("packetTotalMetadataCount") ||
            (Array.isArray(App.packetDetailList) ? App.packetDetailList.length : 0);

        const packetExternal = numberFromText("packetExternalCommunicationCount");
        const packetDomains = numberFromText("packetDnsDomainCount");

        setText("overviewAgentStatus", systemInfo ? "정상 수집" : "정보 없음");
        setText("overviewAgentComputerName", selectedComputer || "-");
        setText("overviewOpenPortCount", ports.length);
        setText("overviewRiskPortCount", cautionPorts.length + dangerPorts.length);
        setText("overviewConnectionCount", connections.length);
        setText("overviewSuspiciousCount", suspiciousConnections.length);
        setText("overviewPacketCount", packetTotal);

        setText("overviewNormalPortCount", normalPorts.length);
        setText("overviewCautionPortCount", cautionPorts.length);
        setText("overviewDangerPortCount", dangerPorts.length);

        setText("overviewCurrentConnectionCount", connections.length);
        setText("overviewExternalConnectionCount", externalConnections.length);
        setText("overviewNetworkSuspiciousCount", suspiciousConnections.length);

        setText("overviewPacketMetadataCount", packetTotal);
        setText("overviewPacketExternalCount", packetExternal);
        setText("overviewPacketDomainCount", packetDomains);

        const lastCollected = App.el?.lastCollectedAt?.textContent;
        setText(
            "overviewLastCollectedAt",
            lastCollected || "마지막 수집 시각: -"
        );

        if (dangerPorts.length > 0) {
            setStateBadge("overviewPortStateBadge", "danger", "위험 확인");
        } else if (cautionPorts.length > 0) {
            setStateBadge("overviewPortStateBadge", "warning", "주의 확인");
        } else {
            setStateBadge("overviewPortStateBadge", "safe", "정상");
        }

        if (suspiciousConnections.length > 0) {
            setStateBadge("overviewNetworkStateBadge", "danger", "의심 탐지");
        } else if (externalConnections.length > 0) {
            setStateBadge("overviewNetworkStateBadge", "warning", "외부 통신");
        } else {
            setStateBadge("overviewNetworkStateBadge", "safe", "정상");
        }

        setStateBadge(
            "overviewPacketStateBadge",
            packetTotal > 0 ? "safe" : "warning",
            packetTotal > 0 ? "수집 중" : "데이터 없음"
        );

        const riskItems = [];

        dangerPorts.forEach((port) => {
            riskItems.push({
                severity: "danger",
                label: "위험",
                title: `${port.protocol || "-"} ${port.localPort ?? "-"} / ${port.processName || "-"}`,
                reason: port.riskReason || "위험 포트로 판정되었습니다."
            });
        });

        cautionPorts.forEach((port) => {
            riskItems.push({
                severity: "caution",
                label: "주의",
                title: `${port.protocol || "-"} ${port.localPort ?? "-"} / ${port.processName || "-"}`,
                reason: port.riskReason || "주의 포트로 판정되었습니다."
            });
        });

        suspiciousConnections.forEach((connection) => {
            riskItems.push({
                severity: "danger",
                label: "의심",
                title: `${connection.remoteAddress || "-"}:${connection.remotePort ?? "-"} / ${connection.processName || "-"}`,
                reason: connection.riskReason || "의심 네트워크 연결로 판정되었습니다."
            });
        });

        const list = byId("overviewRiskList");
        setText("overviewRiskItemCount", `${riskItems.length}건`);

        if (list) {
            list.replaceChildren();

            if (riskItems.length === 0) {
                const empty = document.createElement("div");
                empty.className = "overview-empty";
                empty.textContent = "현재 표시할 위험 항목이 없습니다.";
                list.appendChild(empty);
            } else {
                riskItems.slice(0, 8).forEach((item) => {
                    const row = document.createElement("article");
                    const severity = document.createElement("span");
                    const main = document.createElement("div");
                    const title = document.createElement("strong");
                    const reason = document.createElement("span");

                    row.className = "overview-risk-item";
                    severity.className = `overview-risk-severity ${item.severity}`;
                    severity.textContent = item.label;
                    main.className = "overview-risk-main";
                    title.textContent = item.title;
                    reason.textContent = item.reason;
                    main.append(title, reason);
                    row.append(severity, main);
                    list.appendChild(row);
                });
            }
        }
    };

    const originalLoadSelectedComputerData = App.loadSelectedComputerData;

    if (typeof originalLoadSelectedComputerData === "function") {
        App.loadSelectedComputerData = async (computerName) => {
            const result = await originalLoadSelectedComputerData(computerName);
            App.updateOverview();
            return result;
        };
    }

    const overviewSelect = byId("overviewComputerSelect");
    if (overviewSelect) {
        overviewSelect.addEventListener("change", async () => {
            const computerName = overviewSelect.value;

            if (!computerName || !App.el?.computerSelect) {
                return;
            }

            App.el.computerSelect.value = computerName;
            App.displaySystemInfo(computerName);
            await App.loadSelectedComputerData(computerName);
        });
    }

    const refreshButton = byId("overviewRefreshButton");
    if (refreshButton) {
        refreshButton.addEventListener("click", async () => {
            refreshButton.disabled = true;
            refreshButton.textContent = "새로고침 중...";

            try {
                await App.loadSystemInfo();
                App.updateOverview();
            } finally {
                refreshButton.disabled = false;
                refreshButton.textContent = "새로고침";
            }
        });
    }

    document.querySelectorAll("[data-overview-target]").forEach((button) => {
        button.addEventListener("click", () => {
            const target = button.dataset.overviewTarget;
            const navigation = window.SecureAgentNavigation;

            if (navigation && typeof navigation.showPage === "function") {
                navigation.showPage(target);
            }
        });
    });
})();
