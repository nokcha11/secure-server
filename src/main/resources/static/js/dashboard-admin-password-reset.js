(() => {

    "use strict";

    const openButton =
            document.getElementById(
                    "adminPasswordResetOpenButton"
            );

    const modal =
            document.getElementById(
                    "adminPasswordResetModal"
            );

    const closeButton =
            document.getElementById(
                    "adminPasswordResetCloseButton"
            );

    const cancelButton =
            document.getElementById(
                    "adminPasswordResetCancelButton"
            );

    const form =
            document.getElementById(
                    "adminPasswordResetForm"
            );

    const targetUsernameInput =
            document.getElementById(
                    "resetTargetUsername"
            );

    const newPasswordInput =
            document.getElementById(
                    "resetNewPassword"
            );

    const confirmPasswordInput =
            document.getElementById(
                    "resetConfirmPassword"
            );

    const statusElement =
            document.getElementById(
                    "adminPasswordResetStatus"
            );

    const submitButton =
            document.getElementById(
                    "adminPasswordResetSubmitButton"
            );

    if (!openButton
            || !modal
            || !closeButton
            || !cancelButton
            || !form
            || !targetUsernameInput
            || !newPasswordInput
            || !confirmPasswordInput
            || !statusElement
            || !submitButton) {

        return;
    }

    /*
     * 결과 안내 문구를 표시합니다.
     */
    function showStatus(
            message,
            className) {

        statusElement.textContent =
                message;

        statusElement.className =
                "password-change-status";

        if (className) {
            statusElement.classList.add(
                    className
            );
        }
    }

    /*
     * 로그인한 계정의 권한을 조회합니다.
     *
     * ADMIN일 때만 초기화 버튼을 표시합니다.
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
     * 비밀번호 초기화 창을 엽니다.
     */
    function openModal() {

        form.reset();
        showStatus("", "");

        modal.hidden = false;

        document.body.classList.add(
                "password-modal-open"
        );

        targetUsernameInput.focus();
    }

    /*
     * 비밀번호 초기화 창을 닫습니다.
     */
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
     * VIEWER 사용자의 비밀번호를 초기화합니다.
     */
    async function resetUserPassword(event) {

        event.preventDefault();

        const targetUsername =
                targetUsernameInput.value.trim();

        const newPassword =
                newPasswordInput.value;

        const confirmPassword =
                confirmPasswordInput.value;

        if (!targetUsername) {

            showStatus(
                    "사용자 아이디를 입력하세요.",
                    "is-error"
            );

            targetUsernameInput.focus();
            return;
        }

        if (newPassword.length < 12) {

            showStatus(
                    "새 비밀번호는 12자 이상이어야 합니다.",
                    "is-error"
            );

            newPasswordInput.focus();
            return;
        }

        if (newPassword !== confirmPassword) {

            showStatus(
                    "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.",
                    "is-error"
            );

            confirmPasswordInput.focus();
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent =
                "초기화 중...";

        showStatus(
                "사용자 비밀번호를 초기화하는 중입니다.",
                ""
        );

        try {

            const csrfToken =
                    await loadCsrfToken();

            const requestUrl =
                    "/api/admin/accounts/"
                    + encodeURIComponent(
                            targetUsername
                    )
                    + "/password-reset";

            const response =
                    await fetch(
                            requestUrl,
                            {
                                method: "PUT",
                                headers: {
                                    "Content-Type":
                                            "application/json",

                                    "Accept":
                                            "text/plain",

                                    [csrfToken.headerName]:
                                            csrfToken.token
                                },
                                credentials:
                                        "same-origin",

                                body: JSON.stringify(
                                        {
                                            newPassword:
                                                    newPassword,

                                            confirmPassword:
                                                    confirmPassword
                                        }
                                )
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

            const responseMessage =
                    await response.text();

            if (response.status === 403) {

                throw new Error(
                        "접근 권한이 없습니다. "
                        + "ADMIN 권한이 필요한 기능입니다."
                );
            }

            if (!response.ok) {

                throw new Error(
                        responseMessage
                        || "비밀번호 초기화에 실패했습니다."
                );
            }

            form.reset();

            showStatus(
                    responseMessage
                    || "사용자 비밀번호가 초기화되었습니다.",
                    "is-success"
            );

        } catch (error) {

            console.error(
                    "사용자 비밀번호 초기화 오류:",
                                       error
            );

            showStatus(
                    error.message
                    || "비밀번호 초기화에 실패했습니다.",
                    "is-error"
            );

        } finally {

            submitButton.disabled =
                    false;

            submitButton.textContent =
                    "비밀번호 초기화";
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
            resetUserPassword
    );

    /*
     * 화면을 열 때 로그인 권한을 확인합니다.
     */
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