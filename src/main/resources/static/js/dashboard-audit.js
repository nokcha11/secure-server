(() => {
    "use strict";

    const tableBody = document.getElementById("auditTableBody");

    if (!tableBody) {
        return;
    }

    const refreshButton = document.getElementById("auditRefreshButton");
    const searchInput = document.getElementById("auditSearchInput");
    const actionFilter = document.getElementById("auditActionFilter");
    const resultFilter = document.getElementById("auditResultFilter");
    const periodFilter = document.getElementById("auditPeriodFilter");
    const statusMessage = document.getElementById("auditStatusMessage");

    const totalCount = document.getElementById("auditTotalCount");
    const loginCount = document.getElementById("auditLoginCount");
    const adminActionCount = document.getElementById("auditAdminActionCount");
    const failureCount = document.getElementById("auditFailureCount");

    const state = {
        auditLogs: []
    };

    const LOGIN_ACTIONS = new Set([
        "LOGIN_SUCCESS",
        "LOGIN_FAILURE",
        "LOGOUT"
    ]);

    const ACCOUNT_ACTIONS = new Set([
        "PASSWORD_CHANGE",
        "ADMIN_PASSWORD_RESET",
        "ACCOUNT_LOCK",
        "ACCOUNT_UNLOCK"
    ]);

    const ADMIN_ACTIONS = new Set([
        "ADMIN_PASSWORD_RESET",
        "ACCOUNT_UNLOCK",
        "REMOTE_DIAGNOSTIC"
    ]);

    const ACTION_LABELS = {
        LOGIN_SUCCESS: "로그인 성공",
        LOGIN_FAILURE: "로그인 실패",
        LOGOUT: "로그아웃",
        PASSWORD_CHANGE: "비밀번호 변경",
        ADMIN_PASSWORD_RESET: "사용자 비밀번호 초기화",
        ACCOUNT_LOCK: "계정 잠금",
        ACCOUNT_UNLOCK: "계정 잠금 해제",
        REMOTE_DIAGNOSTIC: "원격 진단"
    };

    function formatDateTime(value) {
        if (!value) {
            return "-";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return date.toLocaleString("ko-KR");
    }

    function normalize(value) {
        return String(value || "")
            .trim()
            .toUpperCase();
    }

    function actionLabel(actionType) {
        return ACTION_LABELS[normalize(actionType)] || actionType || "-";
    }

    function isWithinPeriod(createdAt, period) {
        if (period === "ALL") {
            return true;
        }

        const value = new Date(createdAt);

        if (Number.isNaN(value.getTime())) {
            return false;
        }

        const now = new Date();

        if (period === "TODAY") {
            return value.getFullYear() === now.getFullYear()
                && value.getMonth() === now.getMonth()
                && value.getDate() === now.getDate();
        }

        const days = period === "7D" ? 7 : 30;
        const boundary = new Date(now);
        boundary.setDate(boundary.getDate() - days);

        return value >= boundary;
    }

    function matchesActionFilter(log, selectedFilter) {
        const action = normalize(log.actionType);

        if (selectedFilter === "ALL") {
            return true;
        }

        if (selectedFilter === "LOGIN") {
            return LOGIN_ACTIONS.has(action);
        }

        if (selectedFilter === "ACCOUNT") {
            return ACCOUNT_ACTIONS.has(action);
        }

        if (selectedFilter === "REMOTE") {
            return action === "REMOTE_DIAGNOSTIC";
        }

        return true;
    }

    function matchesResultFilter(log, selectedFilter) {
        if (selectedFilter === "ALL") {
            return true;
        }

        const success = normalize(log.successYn) === "Y";

        return selectedFilter === "SUCCESS"
            ? success
            : !success;
    }

    function matchesKeyword(log, keyword) {
        if (!keyword) {
            return true;
        }

        return [
            log.actorUsername,
            log.actorRole,
            log.targetUsername,
            log.actionType,
            actionLabel(log.actionType),
            log.clientIp,
            log.detail
        ]
            .map((value) => String(value || "").toLowerCase())
            .join(" ")
            .includes(keyword);
    }

    function updateSummary() {
        const logs = state.auditLogs;

        totalCount.textContent = logs.length;
        loginCount.textContent = logs.filter(
            (log) => LOGIN_ACTIONS.has(normalize(log.actionType))
        ).length;
        adminActionCount.textContent = logs.filter(
            (log) => ADMIN_ACTIONS.has(normalize(log.actionType))
        ).length;
        failureCount.textContent = logs.filter(
            (log) => normalize(log.successYn) !== "Y"
        ).length;
    }

    function createBadge(text, className) {
        const span = document.createElement("span");
        span.className = className;
        span.textContent = text;
        return span;
    }

    function createCell(text, className = "") {
        const cell = document.createElement("td");
        cell.textContent = text || "-";

        if (className) {
            cell.className = className;
        }

        return cell;
    }

    function createDetailItem(label, value, wide = false) {
        const item = document.createElement("div");
        const title = document.createElement("span");
        const content = document.createElement("strong");

        item.className = "audit-detail-item";

        if (wide) {
            item.classList.add("wide");
        }

        title.textContent = label;
        content.textContent = value || "-";

        item.append(title, content);
        return item;
    }

    function createRows(log) {
        const fragment = document.createDocumentFragment();
        const row = document.createElement("tr");
        const detailRow = document.createElement("tr");
        const detailCell = document.createElement("td");
        const detailGrid = document.createElement("div");
        const success = normalize(log.successYn) === "Y";

        row.className = "audit-summary-row";
        detailRow.className = "audit-detail-row";
        detailRow.hidden = true;
        detailCell.colSpan = 7;
        detailGrid.className = "audit-detail-grid";

        row.appendChild(
            createCell(formatDateTime(log.createdAt), "audit-time-cell")
        );
        row.appendChild(
            createCell(log.actorUsername || "-", "audit-user-cell")
        );

        const roleCell = document.createElement("td");
        roleCell.appendChild(
            createBadge(log.actorRole || "-", "audit-role-badge")
        );
        row.appendChild(roleCell);

        const actionCell = document.createElement("td");
        actionCell.appendChild(
            createBadge(actionLabel(log.actionType), "audit-action-badge")
        );
        row.appendChild(actionCell);

        row.appendChild(
            createCell(log.targetUsername || "-", "audit-target-cell")
        );

        const resultCell = document.createElement("td");
        resultCell.appendChild(
            createBadge(
                success ? "성공" : "실패",
                `audit-result-badge ${success ? "success" : "failure"}`
            )
        );
        row.appendChild(resultCell);

        const buttonCell = document.createElement("td");
        const detailButton = document.createElement("button");
        buttonCell.className = "audit-detail-button-cell";
        detailButton.type = "button";
        detailButton.className = "audit-detail-button";
        detailButton.textContent = "상세 ▼";
        detailButton.setAttribute("aria-expanded", "false");

        detailButton.addEventListener("click", () => {
            const open = detailRow.hidden;
            detailRow.hidden = !open;
            detailButton.textContent = open ? "상세 ▲" : "상세 ▼";
            detailButton.setAttribute("aria-expanded", open ? "true" : "false");
        });

        buttonCell.appendChild(detailButton);
        row.appendChild(buttonCell);

        detailGrid.append(
            createDetailItem("기록 ID", String(log.id ?? "-")),
            createDetailItem("클라이언트 IP", log.clientIp || "-"),
            createDetailItem("원본 작업 코드", log.actionType || "-"),
            createDetailItem("상세 내용", log.detail || "추가 상세 내용이 없습니다.", true),
            createDetailItem("기록 시각", formatDateTime(log.createdAt))
        );

        detailCell.appendChild(detailGrid);
        detailRow.appendChild(detailCell);
        fragment.append(row, detailRow);

        return fragment;
    }

    function render() {
        const keyword = searchInput.value.trim().toLowerCase();
        const selectedAction = actionFilter.value;
        const selectedResult = resultFilter.value;
        const selectedPeriod = periodFilter.value;

        const filtered = state.auditLogs.filter((log) => {
            return matchesKeyword(log, keyword)
                && matchesActionFilter(log, selectedAction)
                && matchesResultFilter(log, selectedResult)
                && isWithinPeriod(log.createdAt, selectedPeriod);
        });

        tableBody.replaceChildren();

        if (filtered.length === 0) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");
            cell.colSpan = 7;
            cell.className = "audit-empty-cell";
            cell.textContent = "조건에 맞는 감사 기록이 없습니다.";
            row.appendChild(cell);
            tableBody.appendChild(row);
        } else {
            filtered.forEach((log) => {
                tableBody.appendChild(createRows(log));
            });
        }

        statusMessage.textContent =
            `전체 ${state.auditLogs.length}건 중 ${filtered.length}건을 표시하고 있습니다.`;
    }

    async function loadAuditLogs() {
        refreshButton.disabled = true;
        refreshButton.textContent = "조회 중...";
        statusMessage.textContent = "감사 기록을 불러오는 중입니다.";

        try {
            const response = await fetch("/api/admin/audit-logs", {
                method: "GET",
                headers: {
                    Accept: "application/json"
                },
                credentials: "same-origin"
            });

            if (response.redirected && response.url.includes("/login")) {
                window.location.href = "/login";
                return;
            }

            if (response.status === 403) {
                throw new Error("감사 기록은 ADMIN만 조회할 수 있습니다.");
            }

            if (!response.ok) {
                throw new Error(`감사 기록 조회 실패 (${response.status})`);
            }

            const data = await response.json();
            state.auditLogs = Array.isArray(data) ? data : [];

            updateSummary();
            render();
        } catch (error) {
            console.error("감사 기록 조회 오류:", error);
            state.auditLogs = [];
            updateSummary();
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="audit-empty-cell">
                        ${error.message || "감사 기록을 불러오지 못했습니다."}
                    </td>
                </tr>
            `;
            statusMessage.textContent =
                error.message || "감사 기록을 불러오지 못했습니다.";
        } finally {
            refreshButton.disabled = false;
            refreshButton.textContent = "새로고침";
        }
    }

    [searchInput, actionFilter, resultFilter, periodFilter].forEach(
        (element) => {
            const eventName = element === searchInput ? "input" : "change";
            element.addEventListener(eventName, render);
        }
    );

    refreshButton.addEventListener("click", loadAuditLogs);

    window.SecureAgentAudit = {
        refresh: loadAuditLogs
    };

    loadAuditLogs();
})();
