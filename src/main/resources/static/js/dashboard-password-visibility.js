(() => {

    "use strict";

    /*
     * 비밀번호 변경 및 초기화 창에 있는
     * 모든 비밀번호 입력칸을 가져옵니다.
     */
    const passwordInputs =
            document.querySelectorAll(
                    ".password-change-modal input[type='password']"
            );

    passwordInputs.forEach((input) => {

        const wrapper =
                document.createElement("div");

        const toggleButton =
                document.createElement("button");

        wrapper.className =
                "password-input-wrapper";

        toggleButton.type = "button";
        toggleButton.className =
                "password-visibility-button";

        toggleButton.textContent = "보기";

        toggleButton.setAttribute(
                "aria-label",
                "입력한 비밀번호 보기"
        );

        toggleButton.setAttribute(
                "aria-pressed",
                "false"
        );

        /*
         * 기존 입력칸을 보기 버튼과 함께
         * 표시할 수 있는 영역으로 감쌉니다.
         */
        input.parentNode.insertBefore(
                wrapper,
                input
        );

        wrapper.append(
                input,
                toggleButton
        );

        /*
         * 비밀번호를 다시 숨깁니다.
         */
        function hidePassword() {

            input.type = "password";
            toggleButton.textContent = "보기";

            toggleButton.setAttribute(
                    "aria-label",
                    "입력한 비밀번호 보기"
            );

            toggleButton.setAttribute(
                    "aria-pressed",
                    "false"
            );
        }

        /*
         * 보기 버튼을 누르면 입력값을 표시하고,
         * 숨기기 버튼을 누르면 다시 가립니다.
         */
        toggleButton.addEventListener(
                "click",
                () => {

                    const shouldShow =
                            input.type === "password";

                    if (shouldShow) {

                        input.type = "text";
                        toggleButton.textContent =
                                "숨기기";

                        toggleButton.setAttribute(
                                "aria-label",
                                "입력한 비밀번호 숨기기"
                        );

                        toggleButton.setAttribute(
                                "aria-pressed",
                                "true"
                        );

                    } else {
                        hidePassword();
                    }

                    input.focus();
                }
        );

        /*
         * 창을 닫거나 입력값을 초기화하면
         * 비밀번호도 자동으로 다시 가립니다.
         */
        const form = input.closest("form");

        if (form) {

            form.addEventListener(
                    "reset",
                    () => {

                        window.setTimeout(
                                hidePassword,
                                0
                        );
                    }
            );
        }
    });

})();