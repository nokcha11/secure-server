(() => {
    "use strict";

    const statusElement = document.getElementById("currentAccountStatus");
    const roleElement = document.getElementById("currentAccountRole");
    const usernameElement = document.getElementById("currentAccountUsername");

    const toggleButton = document.getElementById("currentAccountToggle");
    const dropdown = document.getElementById("currentAccountDropdown");
    const dropdownRole = document.getElementById("currentAccountDropdownRole");
    const dropdownUsername = document.getElementById("currentAccountDropdownUsername");

    const myAccountButton = document.getElementById("accountMenuMyAccountButton");
    const userManagementButton = document.getElementById("accountMenuUserManagementButton");
    const accountPagePasswordChangeButton = document.getElementById("accountPagePasswordChangeButton");
    const passwordChangeOpenButton = document.getElementById("passwordChangeOpenButton");

    if (!statusElement || !roleElement || !usernameElement) {
        return;
    }

    function setText(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    }

    function normalizeRole(role) {
        return String(role || "").toUpperCase();
    }

    function applyRoleVisibility(role) {
        const normalizedRole = normalizeRole(role);
        const isAdmin = normalizedRole === "ADMIN";

        document
            .querySelectorAll(".admin-only-menu, .admin-only-action")
            .forEach((element) => {
                element.hidden = !isAdmin;
            });

        const adminResponseGroup = document.getElementById("adminResponseGroup");
        if (adminResponseGroup) {
            adminResponseGroup.hidden = !isAdmin;
        }

        if (userManagementButton) {
            userManagementButton.hidden = !isAdmin;
        }
    }

    function closeDropdown() {
        if (!dropdown || !toggleButton) {
            return;
        }

        dropdown.hidden = true;
        toggleButton.setAttribute("aria-expanded", "false");
    }

    function openDropdown() {
        if (!dropdown || !toggleButton) {
            return;
        }

        dropdown.hidden = false;
        toggleButton.setAttribute("aria-expanded", "true");
    }

    function navigateToPage(pageName) {
        const navigation = window.SecureAgentNavigation;

        if (navigation && typeof navigation.showPage === "function") {
            navigation.showPage(pageName);
        }

        closeDropdown();
    }

    function updateAccountUi(account) {
        const role = normalizeRole(account.role) || "UNKNOWN";
        const username = account.username || "-";

        statusElement.textContent = "로그인 중";
        roleElement.textContent = role;
        usernameElement.textContent = username;

        if (dropdownRole) {
            dropdownRole.textContent = role;
        }

        if (dropdownUsername) {
            dropdownUsername.textContent = username;
        }

        setText("accountPageUsername", username);
        setText("accountPageRole", role);
        setText("accountPageLoginStatus", "로그인 중");

        document.body.dataset.currentRole = role;
        document.body.dataset.currentUsername = username;

        applyRoleVisibility(role);
    }

    async function loadCurrentAccount() {
        try {
            const response = await fetch("/api/account/me", {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                },
                credentials: "same-origin"
            });

            if (response.redirected && response.url.includes("/login")) {
                window.location.href = "/login";
                return;
            }

            if (!response.ok) {
                throw new Error("로그인 계정 정보를 확인하지 못했습니다.");
            }

            const account = await response.json();
            updateAccountUi(account);
        } catch (error) {
            console.error("현재 계정 조회 실패:", error);

            statusElement.textContent = "계정 확인 실패";
            roleElement.textContent = "-";
            usernameElement.textContent = "-";

            if (dropdownRole) {
                dropdownRole.textContent = "-";
            }

            if (dropdownUsername) {
                dropdownUsername.textContent = "-";
            }

            setText("accountPageUsername", "-");
            setText("accountPageRole", "-");
            setText("accountPageLoginStatus", "계정 확인 실패");

            applyRoleVisibility("");
        }
    }

    if (toggleButton && dropdown) {
        toggleButton.addEventListener("click", (event) => {
            event.stopPropagation();

            if (dropdown.hidden) {
                openDropdown();
            } else {
                closeDropdown();
            }
        });

        dropdown.addEventListener("click", (event) => {
            event.stopPropagation();
        });

        document.addEventListener("click", closeDropdown);

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                closeDropdown();
            }
        });
    }

    if (myAccountButton) {
        myAccountButton.addEventListener("click", () => {
            navigateToPage("account");
        });
    }

    if (userManagementButton) {
        userManagementButton.addEventListener("click", () => {
            if (normalizeRole(document.body.dataset.currentRole) === "ADMIN") {
                navigateToPage("user-management");
            }
        });
    }

    if (accountPagePasswordChangeButton && passwordChangeOpenButton) {
        accountPagePasswordChangeButton.addEventListener("click", () => {
            passwordChangeOpenButton.click();
        });
    }

    loadCurrentAccount();
})();
