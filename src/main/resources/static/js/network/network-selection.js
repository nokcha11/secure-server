(() => {
    "use strict";

    const App = window.SecureAgent;

    App.isHistoryConnection = (connection) => {
        return (
            App.normalize(connection.activeYn) === "N" ||
            Boolean(connection.endedAt)
        );
    };

    App.createNetworkAnalysisItem = (entry) => {
        const connection = entry.connection;
        const wrapper = document.createElement("article");
        const header = document.createElement("div");
        const sourceBadge = document.createElement("span");
        const title = document.createElement("strong");
        const status = document.createElement("span");
        const grid = document.createElement("div");

        const suspicious =
            App.normalize(connection.suspiciousYn) === "Y";

        const external =
            App.normalize(connection.externalYn) === "Y";

        const history = App.isHistoryConnection(connection);

        wrapper.className = "network-analysis-item";

        if (suspicious) {
            wrapper.classList.add("is-suspicious");
        }

        header.className = "network-analysis-item-header";

        sourceBadge.className = "network-analysis-source";
        sourceBadge.textContent = history
            ? "종료 이력"
            : "현재 연결";

        title.className = "network-analysis-item-title";
        title.textContent =
            `${connection.remoteAddress || "-"}:` +
            `${connection.remotePort ?? "-"}`;

        status.className = "network-analysis-item-status";

        if (suspicious) {
            status.classList.add("is-danger");
            status.textContent = "의심";
        } else {
            status.classList.add("is-safe");
            status.textContent = "정상";
        }

        header.append(sourceBadge, title, status);

        grid.className = "network-analysis-item-grid";

        grid.append(
            App.createNetworkDetailItem(
                "프로토콜",
                connection.protocol
            ),
            App.createNetworkDetailItem(
                "프로세스",
                connection.processName
            ),
            App.createNetworkDetailItem(
                "상태",
                connection.state
            ),
            App.createNetworkDetailItem(
                "통신 구분",
                external ? "외부 연결" : "내부 연결"
            ),
            App.createNetworkDetailItem(
                "내 IP / 포트",
                `${connection.localAddress || "-"}:` +
                `${connection.localPort ?? "-"}`
            ),
            App.createNetworkDetailItem(
                "PID",
                connection.pid
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

        if (connection.endedAt) {
            grid.appendChild(
                App.createNetworkDetailItem(
                    "종료 시각",
                    App.formatDate(connection.endedAt)
                )
            );
        }

        wrapper.append(header, grid);
        return wrapper;
    };

    App.renderNetworkSelectionAnalysis = () => {
        const empty = document.getElementById(
            "networkSelectionEmpty"
        );

        const result = document.getElementById(
            "networkSelectionAnalysisResult"
        );

        if (!empty || !result) {
            return;
        }

        const selected = Array.from(
            App.networkSelectionStore.values()
        );

        if (selected.length === 0) {
            empty.hidden = false;
            result.hidden = true;
            return;
        }

        empty.hidden = true;
        result.hidden = false;

        const currentItems = selected.filter(
            (entry) => !App.isHistoryConnection(entry.connection)
        );

        const historyItems = selected.filter(
            (entry) => App.isHistoryConnection(entry.connection)
        );

        const suspiciousItems = selected.filter(
            (entry) =>
                App.normalize(entry.connection.suspiciousYn) === "Y"
        );

        const externalItems = selected.filter(
            (entry) =>
                App.normalize(entry.connection.externalYn) === "Y"
        );

        const remoteIps = new Set();

        selected.forEach((entry) => {
            const ip = String(
                entry.connection.remoteAddress || ""
            ).trim();

            if (ip && ip !== "-") {
                remoteIps.add(ip);
            }
        });

        const setText = (id, value) => {
            const element = document.getElementById(id);

            if (element) {
                element.textContent = String(value);
            }
        };

        setText(
            "networkAnalysisSelectedCount",
            selected.length
        );

        setText(
            "networkAnalysisCurrentCount",
            currentItems.length
        );

        setText(
            "networkAnalysisHistoryCount",
            historyItems.length
        );

        setText(
            "networkAnalysisSuspiciousCount",
            suspiciousItems.length
        );

        setText(
            "networkAnalysisExternalCount",
            externalItems.length
        );

        setText(
            "networkAnalysisRemoteIpCount",
            remoteIps.size
        );

        const verdict = document.getElementById(
            "networkAnalysisVerdict"
        );

        const summary = document.getElementById(
            "networkAnalysisSummaryText"
        );

        if (verdict) {
            verdict.className = "network-analysis-verdict";

            if (suspiciousItems.length > 0) {
                verdict.textContent = "주의 필요";
                verdict.classList.add("is-danger");
            } else if (externalItems.length > 0) {
                verdict.textContent = "외부 통신 확인";
                verdict.classList.add("is-warning");
            } else {
                verdict.textContent = "특이사항 없음";
                verdict.classList.add("is-safe");
            }
        }

        if (summary) {
            if (suspiciousItems.length > 0) {
                summary.textContent =
                    `선택한 ${selected.length}건 중 ` +
                    `${suspiciousItems.length}건이 의심 연결입니다. ` +
                    "상대방 IP, 포트, 프로세스와 판정 이유를 우선 확인하세요.";
            } else if (externalItems.length > 0) {
                summary.textContent =
                    `선택한 ${selected.length}건 중 ` +
                    `${externalItems.length}건이 외부 연결입니다. ` +
                    "업무상 필요한 통신인지 확인할 수 있습니다.";
            } else {
                summary.textContent =
                    `선택한 ${selected.length}건에서 현재 보안 규칙상 ` +
                    "의심 연결은 확인되지 않았습니다.";
            }
        }

        const list = document.getElementById(
            "networkAnalysisItemList"
        );

        if (list) {
            list.replaceChildren();

            selected
                .sort((left, right) => {
                    const leftHistory = App.isHistoryConnection(
                        left.connection
                    );

                    const rightHistory = App.isHistoryConnection(
                        right.connection
                    );

                    return Number(leftHistory) - Number(rightHistory);
                })
                .forEach((entry) => {
                    list.appendChild(
                        App.createNetworkAnalysisItem(entry)
                    );
                });
        }
    };
})();
