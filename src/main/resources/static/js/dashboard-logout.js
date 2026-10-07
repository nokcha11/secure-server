(() => {

    "use strict";

    const logoutButton =
            document.getElementById(
                    "logoutButton"
            );

    if (!logoutButton) {
        return;
    }

    /*
     * Spring Security의 CSRF 토큰을 가져옵니다.
     */
    async function loadCsrfToken() {

        const response =
                await fetch(
                        "/api/account/csrf-token",
                        {
                            method: "GET",
                            headers: {
                                "Accept":
                                        "application/json"
                            },
                            credentials:
                                    "same-origin"
                        }
                );

        if (response.redirected
                && response.url.includes(
                        "/login"
                )) {

            window.location.href =
                    "/login";

            throw new Error(
                    "로그인 세션이 만료되었습니다."
            );
        }

        if (!response.ok) {

            throw new Error(
                    "CSRF 보안 토큰을 가져오지 못했습니다."
            );
        }

        return response.json();
    }

    /*
     * 숨겨진 POST 폼을 만들어
     * Spring Security에 로그아웃을 요청합니다.
     */
    async function logout() {

        logoutButton.disabled = true;
        logoutButton.textContent =
                "로그아웃 중...";

        try {

            const csrfToken =
                    await loadCsrfToken();

            /*
             * fetch 대신 일반 HTML Form을 제출합니다.
             *
             * 이렇게 하면 로그아웃 성공 후
             * 서버의 리다이렉트 주소로
             * 브라우저가 직접 이동합니다.
             */
            const logoutForm =
                    document.createElement(
                            "form"
                    );

            logoutForm.method =
                    "POST";

            logoutForm.action =
                    "/logout";

            logoutForm.hidden =
                    true;

            /*
             * POST 로그아웃 요청에 필요한
             * CSRF 토큰을 hidden 값으로 추가합니다.
             */
            const csrfInput =
                    document.createElement(
                            "input"
                    );

            csrfInput.type =
                    "hidden";

            csrfInput.name =
                    csrfToken.parameterName;

            csrfInput.value =
                    csrfToken.token;

            logoutForm.appendChild(
                    csrfInput
            );

            document.body.appendChild(
                    logoutForm
            );

            /*
             * 브라우저가 직접 POST /logout을 요청합니다.
             */
            logoutForm.submit();

        } catch (error) {

            console.error(
                    "로그아웃 오류:",
                    error
            );

            alert(
                    error.message
                    || "로그아웃에 실패했습니다."
            );

            logoutButton.disabled = false;
            logoutButton.textContent =
                    "로그아웃";
        }
    }

    logoutButton.addEventListener(
            "click",
            logout
    );

})();