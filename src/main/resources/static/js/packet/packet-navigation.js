(() => {
    "use strict";

    const App = window.SecureAgent = window.SecureAgent || {};

    App.currentPacketView = App.currentPacketView || "summary";

    App.showPacketView = (
        viewName,
        { scroll = true } = {}
    ) => {
        const panels = document.querySelectorAll(
            "[data-packet-view-panel]"
        );

        const tabButtons = document.querySelectorAll(
            "[data-packet-view]"
        );

        App.currentPacketView = viewName;

        panels.forEach((panel) => {
            panel.hidden =
                panel.dataset.packetViewPanel !== viewName;
        });

        tabButtons.forEach((button) => {
            const active =
                button.dataset.packetView === viewName;

            button.classList.toggle("active", active);
            button.setAttribute(
                "aria-current",
                active ? "page" : "false"
            );
        });

        if (
            viewName === "analysis" &&
            typeof App.renderPacketCharts === "function" &&
            Array.isArray(App.packetDetailList)
        ) {
            requestAnimationFrame(() => {
                App.renderPacketCharts(
                    App.packetDetailList
                );
            });
        }

        if (
            viewName === "detail" &&
            typeof App.applyPacketFilter === "function"
        ) {
            App.applyPacketFilter(
                App.currentPacketFilter || "ALL"
            );
        }

        if (scroll) {
            const workspace = document.querySelector(
                ".packet-workspace"
            );

            if (workspace) {
                workspace.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            }
        }
    };

    App.bindPacketNavigation = () => {
        document
            .querySelectorAll("[data-packet-view]")
            .forEach((button) => {
                if (button.dataset.bound === "true") {
                    return;
                }

                button.dataset.bound = "true";

                button.addEventListener("click", () => {
                    App.showPacketView(
                        button.dataset.packetView
                    );
                });
            });
    };

    App.bindPacketNavigation();
    App.showPacketView("summary", { scroll: false });
})();
