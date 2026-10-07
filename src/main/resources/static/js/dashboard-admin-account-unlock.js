(() => {

    "use strict";

    const openButton =
            document.getElementById(
                    "adminAccountUnlockOpenButton"
            );

    const modal =
            document.getElementById(
                    "adminAccountUnlockModal"
            );

    const closeButton =
            document.getElementById(
                    "adminAccountUnlockCloseButton"
            );

    const cancelButton =
            document.getElementById(
                    "adminAccountUnlockCancelButton"
            );

    const form =
            document.getElementById(
                    "adminAccountUnlockForm"
            );

    const targetUsernameInput =
            document.getElementById(
                    "unlockTargetUsername"
            );

    const statusElement =
            document.getElementById(
                    "adminAccountUnlockStatus"
            );

    const submitButton =
            document.getElementById(
                    "adminAccountUnlockSubmitButton"
            );

    if (!openButton
            || !modal
            || !closeButton
            || !cancelButton
            || !form
            || !targetUsernameInput
            || !statusElement
            || !submitButton) {

        return;
    }

    function showStatus(message, className) {

        statusElement.textContent = message;
        statusElement.className =
                "password-change-status";

        if (className) {

            statusElement.classList.add(
                    className
            );
        }
    }

    /*
     * ADMIN으로 로그인했을 때만
     * 잠금 해제 버튼을 표시합니다.
     */
    async function loadCurrentAccount() {

        const response =
                await fetch(
                        "/api/account/me",
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

        if (!response.ok) {

            throw new Error(
                    "로그인 계정 정보를 확인하지 못했습니다."
            );
        }

        const account =
                await response.json();

        openButton.hidden =
                account.role !== "ADMIN";
    }

    /*
     * 잠금 해제 PUT 요청에 사용할
     * CSRF 토큰을 가져옵니다.
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
                && response.url.includes("/login")) {

            window.location.href = "/login";

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

    function openModal() {

        form.reset();
        showStatus("", "");

        modal.hidden = false;

        document.body.classList.add(
                "password-modal-open"
        );

        targetUsernameInput.focus();
    }

    function closeModal() {

        if (submitButton.disabled) {
            return;
        }

        modal.hidden = true;

        document.body.classList.remove(
                "password-modal-open"
        );

        form.reset();
        showStatus("", "");
    }

    /*
     * ADMIN 권한으로 대상 사용자의
     * 계정 잠금을 해제합니다.
     */
    async function unlockAccount(event) {

        event.preventDefault();

        const targetUsername =
                targetUsernameInput.value.trim();

        if (!targetUsername) {

            showStatus(
                    "사용자 아이디를 입력하세요.",
                    "is-error"
            );

            targetUsernameInput.focus();
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent =
                "해제 중...";

        showStatus(
                "사용자 계정 잠금을 해제하는 중입니다.",
                ""
        );

        try {

            const csrfToken =
                    await loadCsrfToken();

            const requestUrl =
                    "/api/admin/accounts/"
                    + encodeURIComponent(targetUsername)
                    + "/unlock";

            const response =
                    await fetch(
                            requestUrl,
                            {
                                method: "PUT",
                                headers: {
                                    "Accept":
                                            "text/plain",

                                    [csrfToken.headerName]:
                                            csrfToken.token
                                },
                                credentials:
                                        "same-origin"
                            }
                    );

            if (response.redirected
                    && response.url.includes("/login")) {

                window.location.href = "/login";

                throw new Error(
                        "로그인 세션이 만료되었습니다."
                );
            }

            const responseMessage =
                    await response.text();

            if (response.status === 403) {

                throw new Error(
                        responseMessage
                        || "접근 권한이 없습니다. "
                        + "ADMIN만 사용할 수 있습니다."
                );
            }

            if (!response.ok) {

                throw new Error(
                        responseMessage
                        || "계정 잠금 해제에 실패했습니다."
                );
            }

            showStatus(
                    responseMessage
                    || targetUsername
                    + " 사용자의 계정 잠금을 해제했습니다.",
                    "is-success"
            );

        } catch (error) {

            console.error(
                    "사용자 계정 잠금 해제 오류:",
                    error
            );

            showStatus(
                    error.message
                    || "계정 잠금 해제에 실패했습니다.",
                    "is-error"
            );

        } finally {

            submitButton.disabled = false;
            submitButton.textContent =
                    "계정 잠금 해제";
        }
    }

    openButton.addEventListener(
            "click",
            openModal
    );

    closeButton.addEventListener(
            "click",
            closeModal
    );

    cancelButton.addEventListener(
            "click",
            closeModal
    );

    modal.addEventListener(
            "click",
            (event) => {

                if (event.target === modal) {
                    closeModal();
                }
            }
    );

    document.addEventListener(
            "keydown",
            (event) => {

                if (event.key === "Escape"
                        && !modal.hidden) {

                    closeModal();
                }
            }
    );

    form.addEventListener(
            "submit",
            unlockAccount
    );

    loadCurrentAccount()
            .catch(
                    (error) => {

                        console.error(
                                "로그인 계정 권한 조회 오류:",
                                error
                        );

                        openButton.hidden = true;
                    }
            );

})();