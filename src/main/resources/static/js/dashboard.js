function initializeDashboard() {

    "use strict";


    const App =
        window.SecureAgent;


    /*
     * HTML 요소 연결
     */
    App.initializeElements();


    /*
     * PC 선택 변경
     */
    App.bind(
        App.el.computerSelect,
        "change",
        async () => {

            const computerName =
                App.el.computerSelect.value;


            App.displaySystemInfo(
                computerName
            );


            await App.loadSelectedComputerData(
                computerName
            );
        }
    );


    /*
     * 새로고침 버튼
     */
    App.bind(
        App.el.refreshButton,
        "click",
        async () => {

            const button =
                App.el.refreshButton;


            button.disabled =
                true;


            button.textContent =
                "새로고침 중...";


            try {

                await App.loadSystemInfo();

            } finally {

                button.disabled =
                    false;


                button.textContent =
                    "새로고침";
            }
        }
    );


    /*
     * 열린 포트 필터
     */
    App.bind(
        App.el.protocolFilter,
        "change",
        App.filterPorts
    );


    App.bind(
        App.el.searchInput,
        "input",
        App.filterPorts
    );


    /*
     * 현재 네트워크 연결 필터
     */
    App.bind(
        App.el.networkStatusFilter,
        "change",
        App.filterNetworkConnections
    );


    App.bind(
        App.el.networkSearchInput,
        "input",
        App.filterNetworkConnections
    );


    /*
     * 종료된 연결 이력 필터
     */
    App.bind(
        App.el.historyFilter,
        "change",
        App.filterHistory
    );


    App.bind(
        App.el.historySearchInput,
        "input",
        App.filterHistory
    );


    /*
     * 화면 최초 데이터 조회
     */
    App.loadSystemInfo();
}


/*
 * 기존 index 방식에서도 동작하고,
 * 분리 HTML을 나중에 삽입한 방식에서도
 * 동작할 수 있도록 처리합니다.
 */
if (
    document.readyState
        === "loading"
) {

    document.addEventListener(
        "DOMContentLoaded",
        initializeDashboard,
        {
            once: true
        }
    );

} else {

    initializeDashboard();
}