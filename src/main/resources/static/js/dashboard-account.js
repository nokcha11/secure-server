(() => {

    "use strict";

    /*
     * 비밀번호 변경 화면의
     * HTML 요소를 가져옵니다.
     */
    const modal =
            document.getElementById(
                    "passwordChangeModal"
            );

    const openButton =
            document.getElementById(
                    "passwordChangeOpenButton"
            );

    const closeButton =
            document.getElementById(
                    "passwordChangeCloseButton"
            );

    const cancelButton =
            document.getElementById(
                    "passwordChangeCancelButton"
            );

    const form =
            document.getElementById(
                    "passwordChangeForm"
            );

    const currentPasswordInput =
            document.getElementById(
                    "currentPassword"
            );

    const newPasswordInput =
            document.getElementById(
                    "newPassword"
            );

    const confirmPasswordInput =
            document.getElementById(
                    "confirmPassword"
            );

    const statusElement =
            document.getElementById(
                    "passwordChangeStatus"
            );

    const submitButton =
            document.getElementById(
                    "passwordChangeSubmitButton"
            );

    /*
     * 필요한 HTML 요소가 없으면
     * 비밀번호 변경 기능을 실행하지 않습니다.
     */
    if (!modal
            || !openButton
            || !closeButton
            || !cancelButton
            || !form
            || !currentPasswordInput
            || !newPasswordInput
            || !confirmPasswordInput
            || !statusElement
            || !submitButton) {

        return;
    }

    /*
     * 비밀번호 변경 결과 메시지를 표시합니다.
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
     * 비밀번호 변경 창을 엽니다.
     */
    function openModal() {

        form.reset();

        showStatus("", "");

        modal.hidden = false;

        document.body.classList.add(
                "password-modal-open"
        );

        currentPasswordInput.focus();
    }

    /*
     * 비밀번호 변경 창을 닫습니다.
     */
    function closeModal() {

        /*
         * 서버 요청이 처리 중일 때는
         * 창을 닫지 않습니다.
         */
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
     * 서버에서 CSRF 토큰을 가져옵니다.
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

        /*
         * 로그인 세션이 만료된 경우
         * 로그인 화면으로 이동합니다.
         */
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
     * 비밀번호 변경 요청을 서버에 전송합니다.
     */
    async function changePassword(event) {

        event.preventDefault();

        const currentPassword =
                currentPasswordInput.value;

        const newPassword =
                newPasswordInput.value;

        const confirmPassword =
                confirmPasswordInput.value;

        /*
         * 화면에서 먼저 기본 입력값을 검사합니다.
         */
        if (!currentPassword) {

            showStatus(
                    "현재 비밀번호를 입력하세요.",
                    "is-error"
            );

            currentPasswordInput.focus();

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
                "변경 중...";

        showStatus(
                "비밀번호를 변경하는 중입니다.",
                ""
        );

        try {

            const csrfToken =
                    await loadCsrfToken();

            const response =
                    await fetch(
                            "/api/account/password",
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
                                            currentPassword:
                                                    currentPassword,

                                            newPassword:
                                                    newPassword,

                                            confirmPassword:
                                                    confirmPassword
                                        }
                                )
                            }
                    );

            const responseMessage =
                    await response.text();

            if (!response.ok) {

                throw new Error(
                        responseMessage
                        || "비밀번호 변경에 실패했습니다."
                );
            }

            form.reset();

            showStatus(
                    responseMessage
                    || "비밀번호가 변경되었습니다. 다시 로그인하세요.",
                    "is-success"
            );

            /*
             * 서버에서 기존 로그인 세션을 종료했으므로
             * 잠시 후 로그인 화면으로 이동합니다.
             */
            window.setTimeout(
                    () => {

                        window.location.href =
                                "/login";
                    },
                    1200
            );

        } catch (error) {

            console.error(
                    "비밀번호 변경 오류:",
                    error
            );

            showStatus(
                    error.message
                    || "비밀번호 변경에 실패했습니다.",
                    "is-error"
            );

        } finally {

            submitButton.disabled =
                    false;

            submitButton.textContent =
                    "비밀번호 변경";
        }
    }

    /*
     * 비밀번호 변경 버튼을 누르면
     * 변경 창을 엽니다.
     */
    openButton.addEventListener(
            "click",
            openModal
    );

    /*
     * 닫기 버튼과 취소 버튼을 누르면
     * 변경 창을 닫습니다.
     */
    closeButton.addEventListener(
            "click",
            closeModal
    );

    cancelButton.addEventListener(
            "click",
            closeModal
    );

    /*
     * 변경 창 바깥쪽 배경을 누르면
     * 변경 창을 닫습니다.
     */
    modal.addEventListener(
            "click",
            (event) => {

                if (event.target === modal) {

                    closeModal();
                }
            }
    );

    /*
     * Esc 키를 누르면
     * 변경 창을 닫습니다.
     */
    document.addEventListener(
            "keydown",
            (event) => {

                if (event.key === "Escape"
                        && !modal.hidden) {

                    closeModal();
                }
            }
    );

    /*
     * 비밀번호 변경 폼을 제출하면
     * 서버에 변경 요청을 보냅니다.
     */
    form.addEventListener(
            "submit",
            changePassword
    );

})();