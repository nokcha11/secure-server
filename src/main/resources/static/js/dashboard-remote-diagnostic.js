(() => {
    "use strict";

    const section = document.getElementById("remoteDiagnosticSection");
    const form = document.getElementById("remoteDiagnosticForm");
    const hostInput = document.getElementById("remoteDiagnosticHost");
    const portInput = document.getElementById("remoteDiagnosticPort");
    const usernameInput = document.getElementById("remoteDiagnosticUsername");
    const commandSelect = document.getElementById("remoteDiagnosticCommand");
    const submitButton = document.getElementById("remoteDiagnosticSubmitButton");
    const statusElement = document.getElementById("remoteDiagnosticStatus");
    const resultMetaElement = document.getElementById("remoteDiagnosticResultMeta");
    const outputElement = document.getElementById("remoteDiagnosticOutput");
    const summaryStatusElement = document.getElementById("remoteDiagnosticSummaryStatus");
    const summaryCardsElement = document.getElementById("remoteDiagnosticSummaryCards");

    if (!section
            || !form
            || !hostInput
            || !portInput
            || !usernameInput
            || !commandSelect
            || !submitButton
            || !statusElement
            || !resultMetaElement
            || !outputElement
            || !summaryStatusElement
            || !summaryCardsElement) {
        return;
    }

    const COMMAND_LABELS = {
        HOSTNAME: "PC 이름 확인",
        CURRENT_USER: "현재 사용자 확인",
        IP_CONFIGURATION: "IP 설정 확인",
        NETWORK_CONNECTIONS: "네트워크 연결 확인"
    };

    function showStatus(message, className) {
        statusElement.textContent = message;
        statusElement.className = "remote-diagnostic-status";

        if (className) {
            statusElement.classList.add(className);
        }
    }

    function moveToLoginWhenRedirected(response) {
        if (response.redirected && response.url.includes("/login")) {
            window.location.href = "/login";
            throw new Error("로그인 세션이 만료되었습니다.");
        }
    }

    async function readResponseBody(response) {
        const responseText = await response.text();

        if (!responseText) {
            return {};
        }

        try {
            return JSON.parse(responseText);
        } catch (error) {
            return {
                message: responseText
            };
        }
    }

    async function loadCurrentAccount() {
        const response = await fetch(
            "/api/account/me",
            {
                method: "GET",
                headers: {
                    Accept: "application/json"
                },
                credentials: "same-origin"
            }
        );

        moveToLoginWhenRedirected(response);

        if (!response.ok) {
            throw new Error("로그인 계정 정보를 확인하지 못했습니다.");
        }

        const account = await response.json();
        section.hidden = account.role !== "ADMIN";
    }

    async function loadCsrfToken() {
        const response = await fetch(
            "/api/account/csrf-token",
            {
                method: "GET",
                headers: {
                    Accept: "application/json"
                },
                credentials: "same-origin"
            }
        );

        moveToLoginWhenRedirected(response);

        if (!response.ok) {
            throw new Error("CSRF 보안 토큰을 가져오지 못했습니다.");
        }

        return response.json();
    }

    function formatExecutedAt(value) {
        if (!value) {
            return "-";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return date.toLocaleString("ko-KR");
    }

    function createSummaryCard(label, value, emphasis = false) {
        const card = document.createElement("div");
        const labelElement = document.createElement("span");
        const valueElement = document.createElement("strong");

        card.className = "remote-summary-card";

        if (emphasis) {
            card.classList.add("emphasis");
        }

        labelElement.textContent = label;
        valueElement.textContent = value || "-";
        card.append(labelElement, valueElement);

        return card;
    }

    function setSummaryStatus(text, className = "") {
        summaryStatusElement.className = "remote-diagnostic-summary-status";
        summaryStatusElement.textContent = text;

        if (className) {
            summaryStatusElement.classList.add(className);
        }
    }

    function firstMeaningfulLine(output) {
        return String(output || "")
            .split(/\r?\n/)
            .map((line) => line.trim())
            .find(Boolean) || "-";
    }

    function extractFirst(output, patterns) {
        const source = String(output || "");

        for (const pattern of patterns) {
            const match = source.match(pattern);

            if (match && match[1] && match[1].trim()) {
                return match[1].trim();
            }
        }

        return "-";
    }

    function createIpConfigurationSummary(output) {
        const ipv4 = extractFirst(output, [
            /IPv4[^:\r\n]*:\s*([^\r\n]+)/i,
            /IPv4\s*주소[^:\r\n]*:\s*([^\r\n]+)/i
        ]);

        const gateway = extractFirst(output, [
            /Default\s+Gateway[^:\r\n]*:\s*([^\r\n]+)/i,
            /기본\s*게이트웨이[^:\r\n]*:\s*([^\r\n]+)/i
        ]);

        const subnet = extractFirst(output, [
            /Subnet\s+Mask[^:\r\n]*:\s*([^\r\n]+)/i,
            /서브넷\s*마스크[^:\r\n]*:\s*([^\r\n]+)/i
        ]);

        const adapterCount = String(output || "")
            .split(/\r?\n/)
            .filter((line) => /adapter|어댑터/i.test(line))
            .length;

        return [
            ["IPv4 주소", ipv4, true],
            ["기본 게이트웨이", gateway, false],
            ["서브넷 마스크", subnet, false],
            ["검색된 어댑터", `${adapterCount}개`, false]
        ];
    }

    function createNetworkConnectionSummary(output) {
        const connectionLines = String(output || "")
            .split(/\r?\n/)
            .map((line) => line.trim())
            .filter((line) => /^(TCP|UDP)\s+/i.test(line));

        const listeningCount = connectionLines.filter(
            (line) => /\bLISTENING\b/i.test(line)
        ).length;

        const establishedCount = connectionLines.filter(
            (line) => /\bESTABLISHED\b/i.test(line)
        ).length;

        const pidSet = new Set();

        connectionLines.forEach((line) => {
            const tokens = line.split(/\s+/);
            const pid = tokens[tokens.length - 1];

            if (/^\d+$/.test(pid)) {
                pidSet.add(pid);
            }
        });

        return [
            ["전체 연결 행", `${connectionLines.length}건`, true],
            ["LISTENING", `${listeningCount}건`, false],
            ["ESTABLISHED", `${establishedCount}건`, false],
            ["관련 PID", `${pidSet.size}개`, false]
        ];
    }

    function createCommandSummary(commandType, output) {
        if (commandType === "IP_CONFIGURATION") {
            return createIpConfigurationSummary(output);
        }

        if (commandType === "NETWORK_CONNECTIONS") {
            return createNetworkConnectionSummary(output);
        }

        if (commandType === "HOSTNAME") {
            return [
                ["PC 이름", firstMeaningfulLine(output), true]
            ];
        }

        if (commandType === "CURRENT_USER") {
            return [
                ["현재 사용자", firstMeaningfulLine(output), true]
            ];
        }

        return [
            ["결과", firstMeaningfulLine(output), true]
        ];
    }

    function renderSummary({
        request,
        output = "",
        status = "waiting",
        message = ""
    }) {
        summaryCardsElement.replaceChildren();

        if (status === "running") {
            setSummaryStatus("진단 중", "is-running");
        } else if (status === "success") {
            setSummaryStatus("정상", "is-success");
        } else if (status === "error") {
            setSummaryStatus("실패", "is-error");
        } else {
            setSummaryStatus("대기");
        }

        if (!request) {
            const empty = document.createElement("div");
            empty.className = "remote-summary-empty";
            empty.textContent = "진단을 실행하면 핵심 결과를 이 영역에서 바로 확인할 수 있습니다.";
            summaryCardsElement.appendChild(empty);
            return;
        }

        summaryCardsElement.append(
            createSummaryCard(
                "대상",
                `${request.host || "-"}:${request.port || "-"}`
            ),
            createSummaryCard(
                "진단 명령",
                COMMAND_LABELS[request.commandType] || request.commandType || "-"
            )
        );

        if (status === "running") {
            summaryCardsElement.appendChild(
                createSummaryCard("진행 상태", "SSH 응답을 기다리는 중입니다.", true)
            );
            return;
        }

        if (status === "error" && !output) {
            summaryCardsElement.appendChild(
                createSummaryCard("오류", message || "진단 실행에 실패했습니다.", true)
            );
            return;
        }

        createCommandSummary(request.commandType, output).forEach(
            ([label, value, emphasis]) => {
                summaryCardsElement.appendChild(
                    createSummaryCard(label, value, emphasis)
                );
            }
        );
    }

    function showResult(result, fallbackRequest, success) {
        const host = result.host || fallbackRequest.host;
        const port = result.port || fallbackRequest.port;
        const username = result.username || fallbackRequest.username;
        const commandType = result.commandType || fallbackRequest.commandType;
        const output = result.output || result.message || "서버가 진단 결과를 반환하지 않았습니다.";

        resultMetaElement.textContent =
            `${host}:${port} · ${username} · ${commandType} · ${formatExecutedAt(result.executedAt)}`;

        outputElement.textContent = output;

        renderSummary({
            request: {
                ...fallbackRequest,
                host,
                port,
                username,
                commandType
            },
            output,
            status: success ? "success" : "error",
            message: result.message || ""
        });
    }

    function validateRequest(requestBody) {
        if (!requestBody.host) {
            hostInput.focus();
            return "대상 서버 주소를 입력하세요.";
        }

        if (!Number.isInteger(requestBody.port)
                || requestBody.port < 1
                || requestBody.port > 65535) {
            portInput.focus();
            return "포트는 1부터 65535 사이로 입력하세요.";
        }

        if (!requestBody.username) {
            usernameInput.focus();
            return "SSH 사용자 이름을 입력하세요.";
        }

        if (!requestBody.commandType) {
            commandSelect.focus();
            return "실행할 진단 명령을 선택하세요.";
        }

        return "";
    }

    async function executeRemoteDiagnostic(event) {
        event.preventDefault();

        const requestBody = {
            host: hostInput.value.trim(),
            port: Number(portInput.value),
            username: usernameInput.value.trim(),
            commandType: commandSelect.value
        };

        const validationMessage = validateRequest(requestBody);

        if (validationMessage) {
            showStatus(validationMessage, "is-error");
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "진단 중...";
        form.setAttribute("aria-busy", "true");

        showStatus(
            "안전한 원격 진단 명령을 실행하고 있습니다.",
            "is-waiting"
        );

        resultMetaElement.textContent = "요청 처리 중";
        outputElement.textContent = "SSH 연결 및 명령 실행 결과를 기다리는 중입니다.";

        renderSummary({
            request: requestBody,
            status: "running"
        });

        try {
            const csrfToken = await loadCsrfToken();

            const response = await fetch(
                "/api/admin/remote-diagnostics",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                        [csrfToken.headerName]: csrfToken.token
                    },
                    credentials: "same-origin",
                    body: JSON.stringify(requestBody)
                }
            );

            moveToLoginWhenRedirected(response);

            const result = await readResponseBody(response);
            const success = response.ok && result.success !== false;

            showResult(result, requestBody, success);

            if (response.status === 403) {
                throw new Error("접근 권한이 없습니다. ADMIN 권한이 필요합니다.");
            }

            if (!success) {
                throw new Error(
                    result.message || "원격 진단 실행에 실패했습니다."
                );
            }

            showStatus(
                result.message || "원격 진단이 정상적으로 완료되었습니다.",
                "is-success"
            );
        } catch (error) {
            console.error("원격 진단 오류:", error);

            showStatus(
                error.message || "원격 진단 실행에 실패했습니다.",
                "is-error"
            );

            if (summaryStatusElement.textContent === "진단 중") {
                renderSummary({
                    request: requestBody,
                    status: "error",
                    message: error.message
                });
            }
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = "진단 실행";
            form.removeAttribute("aria-busy");
        }
    }

    form.addEventListener("submit", executeRemoteDiagnostic);

    loadCurrentAccount().catch((error) => {
        console.error("원격 진단 권한 조회 오류:", error);
        section.hidden = true;
    });
})();
