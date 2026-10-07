(() => {
    "use strict";

    const App = window.SecureAgent;

    App.currentConnectionView = App.currentConnectionView || "current";

    App.updateNetworkSelectionNavigation = () => {
        const count = App.networkSelectionStore.size;
        const countElement = document.getElementById("networkSelectedCount");
        const clearButton = document.getElementById(
            "clearSelectedNetworkButton"
        );

        if (countElement) {
            countElement.textContent = `${count}건`;
        }

        if (clearButton) {
            clearButton.disabled = count === 0;
        }
    };

    App.showConnectionView = (
        viewName,
        { scroll = true } = {}
    ) => {
        const panels = document.querySelectorAll(
            "[data-connection-view-panel]"
        );

        const tabButtons = document.querySelectorAll(
            "[data-connection-view]"
        );

        const quickButtons = document.querySelectorAll(
            "[data-connection-quick-view]"
        );

        App.currentConnectionView = viewName;

        if (
            window.SecureAgentFloatingNav
            && typeof window.SecureAgentFloatingNav.setConnectionView === "function"
        ) {
            window.SecureAgentFloatingNav.setConnectionView(viewName);
        }

        panels.forEach((panel) => {
            panel.hidden =
                panel.dataset.connectionViewPanel !== viewName;
        });

        tabButtons.forEach((button) => {
            const active = button.dataset.connectionView === viewName;
            button.classList.toggle("active", active);
            button.setAttribute(
                "aria-current",
                active ? "page" : "false"
            );
        });

        quickButtons.forEach((button) => {
            button.classList.toggle(
                "active",
                button.dataset.connectionQuickView === viewName
            );
        });

        if (
            viewName === "hourly" &&
            Array.isArray(App.data.hourlyStats) &&
            typeof App.renderHourlyChart === "function"
        ) {
            requestAnimationFrame(() => {
                App.renderHourlyChart(App.data.hourlyStats);
            });
        }

        if (
            viewName === "relation" &&
            typeof App.drawRelationGraph === "function"
        ) {
            requestAnimationFrame(() => {
                App.drawRelationGraph();
            });
        }

        if (
            viewName === "selection" &&
            typeof App.renderNetworkSelectionAnalysis === "function"
        ) {
            App.renderNetworkSelectionAnalysis();
        }

        if (scroll) {
            const workspace = document.querySelector(
                ".connection-workspace"
            );

            if (workspace) {
                workspace.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            }
        }
    };

    App.bindConnectionNavigation = () => {
        document
            .querySelectorAll("[data-connection-view]")
            .forEach((button) => {
                if (button.dataset.bound === "true") {
                    return;
                }

                button.dataset.bound = "true";

                button.addEventListener("click", () => {
                    App.showConnectionView(
                        button.dataset.connectionView
                    );
                });
            });

        document
            .querySelectorAll("[data-connection-quick-view]")
            .forEach((button) => {
                if (button.dataset.bound === "true") {
                    return;
                }

                button.dataset.bound = "true";

                button.addEventListener("click", () => {
                    App.showConnectionView(
                        button.dataset.connectionQuickView
                    );
                });
            });

        const clearButton = document.getElementById(
            "clearSelectedNetworkButton"
        );

        if (clearButton && clearButton.dataset.bound !== "true") {
            clearButton.dataset.bound = "true";

            clearButton.addEventListener("click", () => {
                App.clearAllNetworkSelections();
            });
        }
    };

    document.addEventListener(
        "secureagent:network-selection-changed",
        () => {
            App.updateNetworkSelectionNavigation();

            if (
                App.currentConnectionView === "selection" &&
                typeof App.renderNetworkSelectionAnalysis === "function"
            ) {
                App.renderNetworkSelectionAnalysis();
            }
        }
    );

    App.bindConnectionNavigation();
    App.updateNetworkSelectionNavigation();
    App.showConnectionView("current", { scroll: false });
})();
