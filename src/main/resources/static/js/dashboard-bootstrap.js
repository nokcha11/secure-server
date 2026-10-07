(() => {

    "use strict";


    const content =
        document.getElementById(
            "dashboardContent"
        );


    const menuButtons =
        Array.from(

            document.querySelectorAll(
                ".sidebar-menu[data-page]"
            )

        );


    if (!content) {

        console.error(
            "dashboardContent 영역을 찾지 못했습니다."
        );

        return;
    }



    /* ========================================
       메인 페이지 정의
       ======================================== */

    const pageDefinitions =
        [

            {
                name:
                    "overview",

                url:
                    "/pages/overview.html"
            },

            {
                name:
                    "agent",

                url:
                    "/pages/agent.html"
            },

            {
                name:
                    "ports",

                url:
                    "/pages/ports.html"
            },

            {
                name:
                    "connections",

                url:
                    "/pages/connections.html"
            },

            {
                name:
                    "packets",

                url:
                    "/pages/packets.html"
            },

            {
                name:
                    "remote-diagnostic",

                url:
                    "/pages/remote-diagnostic.html"
            },

            {
                name:
                    "audit",

                url:
                    "/pages/audit.html"
            },

            {
                name:
                    "account",

                url:
                    "/pages/account.html"
            },

            {
                name:
                    "user-management",

                url:
                    "/pages/user-management.html"
            }

        ];



    /* ========================================
       연결 분석 내부 화면
       ======================================== */

    const connectionFragmentDefinitions =
        [

            {
                targetId:
                    "connectionViewCurrent",

                url:
                    "/pages/connections/current.html"
            },

            {
                targetId:
                    "connectionViewHistory",

                url:
                    "/pages/connections/history.html"
            },

            {
                targetId:
                    "connectionViewSuspicious",

                url:
                    "/pages/connections/suspicious.html"
            },

            {
                targetId:
                    "connectionViewHourly",

                url:
                    "/pages/connections/hourly.html"
            },

            {
                targetId:
                    "connectionViewRelation",

                url:
                    "/pages/connections/relation.html"
            },

            {
                targetId:
                    "connectionViewSelection",

                url:
                    "/pages/connections/selection.html"
            }

        ];



    /* ========================================
       패킷 분석 내부 화면
       ======================================== */

    const packetFragmentDefinitions =
        [

            {
                targetId:
                    "packetViewSummary",

                url:
                    "/pages/packets/summary.html"
            },

            {
                targetId:
                    "packetViewAnalysis",

                url:
                    "/pages/packets/analysis.html"
            },

            {
                targetId:
                    "packetViewDetail",

                url:
                    "/pages/packets/detail.html"
            }

        ];



    /* ========================================
       JS 로딩 순서
       ======================================== */

    const scriptUrls =
        [

            "/js/dashboard-common.js?v=3",

            "/js/dashboard-floating-nav.js?v=2",

            "/js/dashboard-port.js?v=1",


            "/js/network/network-common.js?v=2",

            "/js/network/network-selection.js?v=2",

            "/js/network/network-current.js?v=2",

            "/js/network/network-history.js?v=2",

            "/js/network/network-suspicious.js?v=2",

            "/js/network/network-relation.js?v=2",

            "/js/network/network-navigation.js?v=3",


            "/js/dashboard-chart.js?v=3",

            "/js/dashboard-packet-chart.js?v=1",

            "/js/dashboard-packet.js?v=4",

            "/js/packet/packet-navigation.js?v=1",


            /*
             * 에이전트 수집 상태
             */
            "/js/dashboard-agent.js?v=1",


            "/js/dashboard-overview.js?v=2",

            "/js/dashboard-current-account.js?v=4",

            "/js/dashboard-account.js?v=1",

            "/js/dashboard-admin-password-reset.js?v=1",

            "/js/dashboard-admin-account-unlock.js?v=1",

            "/js/dashboard-remote-diagnostic.js?v=2",

            "/js/dashboard-audit.js?v=1",

            "/js/dashboard-logout.js?v=2",

            "/js/dashboard-password-visibility.js?v=1",

            "/js/dashboard.js?v=4"

        ];



    /* ========================================
       CSS 동적 로딩
       ======================================== */

    function ensureStylesheet(
        href,
        marker
    ) {

        if (
            document.querySelector(
                `link[data-style-marker="${marker}"]`
            )
        ) {

            return;
        }


        const link =
            document.createElement(
                "link"
            );


        link.rel =
            "stylesheet";


        link.href =
            href;


        link.dataset
            .styleMarker =
            marker;


        document.head
            .appendChild(
                link
            );
    }



    /* ========================================
       HTML 조회
       ======================================== */

    async function fetchHtml(
        url
    ) {

        const response =
            await fetch(
                url,
                {
                    credentials:
                        "same-origin"
                }
            );


        if (!response.ok) {

            throw new Error(

                `화면 파일 로드 실패: ` +
                `${response.status} ${url}`

            );
        }


        return response.text();
    }



    /* ========================================
       메인 페이지 로드
       ======================================== */

    async function loadPage(
        definition
    ) {

        const html =
            await fetchHtml(
                definition.url
            );


        const page =
            document.createElement(
                "section"
            );


        page.className =
            "dashboard-page";


        page.dataset
            .dashboardPage =
            definition.name;


        page.innerHTML =
            html;


        page.hidden =
            true;


        content.appendChild(
            page
        );
    }



    /* ========================================
       내부 Fragment 로드
       ======================================== */

    async function loadFragments(
        definitions,
        label
    ) {

        for (
            const definition
            of definitions
        ) {

            const target =
                document.getElementById(
                    definition.targetId
                );


            if (!target) {

                throw new Error(

                    `${label} 영역을 찾지 못했습니다: ` +
                    `${definition.targetId}`

                );
            }


            target.innerHTML =
                await fetchHtml(
                    definition.url
                );
        }
    }



    /* ========================================
       JS 파일 순차 로드
       ======================================== */

    function loadScript(
        src
    ) {

        return new Promise(
            (
                resolve,
                reject
            ) => {

                const script =
                    document.createElement(
                        "script"
                    );


                script.src =
                    src;


                script.async =
                    false;


                script.addEventListener(
                    "load",
                    resolve,
                    {
                        once:
                            true
                    }
                );


                script.addEventListener(
                    "error",
                    () => {

                        reject(

                            new Error(
                                `스크립트 로드 실패: ${src}`
                            )

                        );
                    },
                    {
                        once:
                            true
                    }
                );


                document.body
                    .appendChild(
                        script
                    );
            }
        );
    }



    /* ========================================
       화면 이동
       ======================================== */

    function showPage(
        pageName
    ) {

        const pages =
            document.querySelectorAll(
                "[data-dashboard-page]"
            );


        pages.forEach(
            (page) => {

                page.hidden =
                    (
                        page.dataset
                            .dashboardPage
                        !==
                        pageName
                    );
            }
        );



        /* 사이드바 활성 메뉴 */

        menuButtons.forEach(
            (button) => {

                const active =
                    (
                        button.dataset.page
                        ===
                        pageName
                    );


                button.classList
                    .toggle(
                        "active",
                        active
                    );


                button.setAttribute(
                    "aria-current",
                    active
                        ? "page"
                        : "false"
                );
            }
        );



        /* 오른쪽 플로팅 메뉴 */

        if (
            window.SecureAgentFloatingNav
            &&
            typeof window
                .SecureAgentFloatingNav
                .setActivePage
            === "function"
        ) {

            window
                .SecureAgentFloatingNav
                .setActivePage(
                    pageName
                );
        }


        const App =
            window.SecureAgent;



        /* ========================================
           통합 관제
           ======================================== */

        if (
            pageName
            === "overview"
            &&
            App
        ) {

            if (
                typeof App
                    .syncOverviewComputerSelect
                === "function"
            ) {

                App.syncOverviewComputerSelect();
            }


            if (
                typeof App
                    .updateOverview
                === "function"
            ) {

                App.updateOverview();
            }
        }



        /* ========================================
           에이전트 상태
           ======================================== */

        if (
            pageName
            === "agent"
            &&
            App
            &&
            typeof App
                .updateAgentStatus
            === "function"
        ) {

            App.updateAgentStatus(
                App.el
                    ?.computerSelect
                    ?.value
            );
        }



        /* ========================================
           연결 분석
           ======================================== */

        if (
            pageName
            === "connections"
            &&
            App
        ) {

            requestAnimationFrame(
                () => {

                    if (
                        App.currentConnectionView
                        === "hourly"
                        &&
                        Array.isArray(
                            App.data
                                ?.hourlyStats
                        )
                        &&
                        typeof App
                            .renderHourlyChart
                        === "function"
                    ) {

                        App.renderHourlyChart(
                            App.data.hourlyStats
                        );
                    }


                    if (
                        App.currentConnectionView
                        === "relation"
                        &&
                        typeof App
                            .drawRelationGraph
                        === "function"
                    ) {

                        App.drawRelationGraph();
                    }
                }
            );
        }



        /* ========================================
           패킷 분석
           ======================================== */

        if (
            pageName
            === "packets"
            &&
            App
        ) {

            requestAnimationFrame(
                () => {

                    if (
                        typeof App
                            .showPacketView
                        === "function"
                    ) {

                        App.showPacketView(

                            App.currentPacketView
                            ||
                            "summary",

                            {
                                scroll:
                                    false
                            }

                        );


                        return;
                    }


                    if (
                        typeof App
                            .renderPacketCharts
                        === "function"
                        &&
                        Array.isArray(
                            App.packetDetailList
                        )
                    ) {

                        App.renderPacketCharts(
                            App.packetDetailList
                        );
                    }
                }
            );
        }



        /* ========================================
           감사 기록
           ======================================== */

        if (
            pageName
            === "audit"
            &&
            window.SecureAgentAudit
            &&
            typeof window
                .SecureAgentAudit
                .refresh
            === "function"
        ) {

            window
                .SecureAgentAudit
                .refresh();
        }



        /* 화면 최상단 */

        window.scrollTo(
            {
                top:
                    0,

                behavior:
                    "auto"
            }
        );
    }



    /* 다른 JS에서 사용하는 화면 이동 API */

    window.SecureAgentNavigation =
        {
            showPage
        };



    /* ========================================
       사이드바 메뉴 이벤트
       ======================================== */

    function bindMenu() {

        menuButtons.forEach(
            (button) => {

                button.addEventListener(
                    "click",
                    () => {

                        if (
                            button.hidden
                        ) {

                            return;
                        }


                        showPage(
                            button.dataset.page
                        );
                    }
                );
            }
        );
    }



    /* ========================================
       Bootstrap 시작
       ======================================== */

    async function bootstrap() {

        try {


            /* 왼쪽 사이드바 */

            ensureStylesheet(
                "/css/dashboard-sticky.css?v=3",
                "dashboard-sticky"
            );


            /* 통합 관제 */

            ensureStylesheet(
                "/css/dashboard-overview.css?v=2",
                "dashboard-overview"
            );


            /* 오른쪽 Glass 메뉴 */

            ensureStylesheet(
                "/css/dashboard-floating-nav.css?v=2",
                "dashboard-floating-nav"
            );


            /* 에이전트 상태 */

            ensureStylesheet(
                "/css/dashboard-agent.css?v=1",
                "dashboard-agent"
            );


            /* 감사 기록 */

            ensureStylesheet(
                "/css/dashboard-audit.css?v=1",
                "dashboard-audit"
            );


            /* 연결 시간대별 그래프 */

            ensureStylesheet(
                "/css/network/network-hourly-accent.css?v=1",
                "network-hourly-accent"
            );


            /* 패킷 내부 메뉴 */

            ensureStylesheet(
                "/css/packet/packet-navigation.css?v=1",
                "packet-navigation"
            );



            /*
             * index.html에 있던
             * 초기 로딩 화면 제거
             */

            content.innerHTML =
                "";



            /* 메인 페이지 로드 */

            for (
                const definition
                of pageDefinitions
            ) {

                await loadPage(
                    definition
                );
            }



            /* 연결 분석 6개 화면 */

            await loadFragments(

                connectionFragmentDefinitions,

                "연결 분석"

            );



            /* 패킷 분석 3개 화면 */

            await loadFragments(

                packetFragmentDefinitions,

                "패킷 분석"

            );



            /* JS 순차 로드 */

            for (
                const src
                of scriptUrls
            ) {

                await loadScript(
                    src
                );
            }



            /* 사이드바 연결 */

            bindMenu();



            /* 최초 화면 */

            showPage(
                "overview"
            );


        } catch (
            error
        ) {

            console.error(
                "대시보드 초기화 실패:",
                error
            );


            content.innerHTML =
                `
                    <section
                        class="panel dashboard-placeholder-panel">

                        <div class="panel-header">

                            <div>

                                <h2>
                                    화면을 불러오지 못했습니다.
                                </h2>

                                <p>
                                    ${error.message}
                                </p>

                            </div>

                        </div>

                    </section>
                `;
        }
    }



    bootstrap();

})();