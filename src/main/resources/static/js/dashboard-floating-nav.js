(() => {
    "use strict";

    if (document.getElementById("secureFloatingDock")) {
        return;
    }

    const dock = document.createElement("aside");

    dock.id = "secureFloatingDock";
    dock.className = "secure-floating-dock";
    dock.setAttribute(
        "aria-label",
        "연결 분석 빠른 이동"
    );

    dock.innerHTML = `
        <div class="secure-floating-main">
            <button
                type="button"
                class="secure-floating-connection-button"
                data-floating-connection-view="current"
                title="현재 네트워크 연결">
                <span class="secure-floating-icon" aria-hidden="true">↔</span>
                <span class="secure-floating-label">현재 연결</span>
            </button>

            <button
                type="button"
                class="secure-floating-connection-button"
                data-floating-connection-view="history"
                title="종료된 연결 이력">
                <span class="secure-floating-icon" aria-hidden="true">↶</span>
                <span class="secure-floating-label">종료 이력</span>
            </button>

            <button
                type="button"
                class="secure-floating-connection-button secure-floating-analysis-button"
                data-floating-connection-view="selection"
                title="선택 항목 분석">
                <span class="secure-floating-icon" aria-hidden="true">◎</span>
                <span class="secure-floating-label">선택 항목 분석</span>
                <span
                    id="networkSelectedCount"
                    class="secure-floating-count"
                    aria-label="선택 항목 수">
                    0건
                </span>
            </button>
        </div>
    `;

    document.body.appendChild(dock);

    const connectionButtons = Array.from(
        dock.querySelectorAll(
            "[data-floating-connection-view]"
        )
    );

    function setConnectionView(viewName) {
        connectionButtons.forEach((button) => {
            const active =
                button.dataset.floatingConnectionView
                === viewName;

            button.classList.toggle(
                "active",
                active
            );

            button.setAttribute(
                "aria-current",
                active ? "page" : "false"
            );
        });
    }

    function openConnectionView(viewName) {
        const pageNavigation =
            window.SecureAgentNavigation;

        if (
            pageNavigation
            && typeof pageNavigation.showPage === "function"
        ) {
            pageNavigation.showPage("connections");
        }

        requestAnimationFrame(() => {
            const App = window.SecureAgent;

            if (
                App
                && typeof App.showConnectionView === "function"
            ) {
                App.showConnectionView(viewName);
            }
        });
    }

    connectionButtons.forEach((button) => {
        button.addEventListener("click", () => {
            openConnectionView(
                button.dataset.floatingConnectionView
            );
        });
    });

    /*
     * bootstrap의 기존 호출과 호환하기 위해 남겨둡니다.
     * 플로팅 메뉴는 어느 페이지에서도 계속 표시됩니다.
     */
    function setActivePage(pageName) {
        dock.classList.toggle(
            "is-connections-page",
            pageName === "connections"
        );

        if (
            pageName === "connections"
            && window.SecureAgent
        ) {
            setConnectionView(
                window.SecureAgent.currentConnectionView
                || "current"
            );
        } else {
            connectionButtons.forEach((button) => {
                button.classList.remove("active");
                button.setAttribute(
                    "aria-current",
                    "false"
                );
            });
        }
    }

    window.SecureAgentFloatingNav = {
        setActivePage,
        setConnectionView,
        openConnectionView
    };
})();
