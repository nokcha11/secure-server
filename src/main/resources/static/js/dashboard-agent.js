(() => {

    "use strict";


    const App = window.SecureAgent;


    if (!App) {

        console.error(
            "SecureAgent 공통 객체를 찾지 못했습니다."
        );

        return;
    }


    const byId = (id) => {

        return document.getElementById(id);
    };


    const safeArray = (value) => {

        return Array.isArray(value)
            ? value
            : [];
    };


    /* ========================================
       가장 최근 날짜 찾기
       ======================================== */

    const latestDateFrom = (
        list,
        fieldNames
    ) => {

        const times = [];


        safeArray(list).forEach(
            (item) => {

                fieldNames.forEach(
                    (fieldName) => {

                        const value =
                            item?.[fieldName];


                        const date =
                            App.parseDate(value);


                        if (date) {

                            times.push(
                                date.getTime()
                            );
                        }

                    }
                );

            }
        );


        if (times.length === 0) {

            return null;
        }


        return new Date(
            Math.max(...times)
        );
    };


    /* ========================================
       상대 시간
       ======================================== */

    const relativeTime = (date) => {

        if (!date) {

            return "수집 정보 없음";
        }


        let diff =
            Date.now()
            -
            date.getTime();


        if (diff < 0) {

            diff = 0;
        }


        const seconds =
            Math.floor(
                diff / 1000
            );


        if (seconds < 60) {

            return "방금 전";
        }


        const minutes =
            Math.floor(
                seconds / 60
            );


        if (minutes < 60) {

            return `${minutes}분 전`;
        }


        const hours =
            Math.floor(
                minutes / 60
            );


        if (hours < 24) {

            return `${hours}시간 전`;
        }


        const days =
            Math.floor(
                hours / 24
            );


        return `${days}일 전`;
    };


    /* ========================================
       선택 PC 시스템 정보
       ======================================== */

    const getSelectedSystemInfo = (
        computerName
    ) => {

        if (!computerName) {

            return null;
        }


        const list =
            safeArray(
                App.data?.systemInfoList
            )
            .filter(
                (item) => {

                    return (
                        item.computerName
                        ===
                        computerName
                    );
                }
            );


        if (list.length === 0) {

            return null;
        }


        return list
            .slice()
            .sort(
                (left, right) => {

                    const leftDate =
                        App.parseDate(
                            left.receivedAt
                        );


                    const rightDate =
                        App.parseDate(
                            right.receivedAt
                        );


                    return (

                        (
                            rightDate?.getTime()
                            ?? 0
                        )

                        -

                        (
                            leftDate?.getTime()
                            ?? 0
                        )

                    );
                }
            )[0];
    };


    /* ========================================
       패킷 건수
       ======================================== */

    const getPacketCount = () => {

        const detailCount =
            safeArray(
                App.packetDetailList
            ).length;


        const summary =
            byId(
                "packetTotalMetadataCount"
            );


        if (!summary) {

            return detailCount;
        }


        const value =
            Number(

                String(
                    summary.textContent || ""
                )
                .replace(
                    /[^0-9]/g,
                    ""
                )

            );


        if (!Number.isFinite(value)) {

            return detailCount;
        }


        return Math.max(
            value,
            detailCount
        );
    };


    /* ========================================
       카드 상태
       ======================================== */

    const setState = (
        elementId,
        hasData,
        successText = "수집됨",
        emptyText = "데이터 없음"
    ) => {

        const element =
            byId(elementId);


        if (!element) {

            return;
        }


        element.classList.remove(
            "is-waiting",
            "has-data",
            "no-data"
        );


        if (hasData) {

            element.classList.add(
                "has-data"
            );


            element.textContent =
                successText;

        } else {

            element.classList.add(
                "no-data"
            );


            element.textContent =
                emptyText;
        }
    };


    /* ========================================
       최근 수집 시간
       ======================================== */

    const renderCollectionTime = (
        timeId,
        relativeId,
        date
    ) => {

        const timeElement =
            byId(timeId);


        const relativeElement =
            byId(relativeId);


        if (timeElement) {

            timeElement.textContent =
                date
                    ? App.formatDate(date)
                    : "-";
        }


        if (relativeElement) {

            relativeElement.textContent =
                relativeTime(date);
        }
    };


    /* ========================================
       에이전트 상태 화면 갱신
       ======================================== */

    App.updateAgentStatus = (
        requestedComputerName
    ) => {

        const computerName =

            requestedComputerName

            ||

            App.el
                ?.computerSelect
                ?.value

            ||

            "";


        const systemInfo =
            getSelectedSystemInfo(
                computerName
            );


        const portList =
            safeArray(
                App.data?.openPortList
            );


        const networkList =
            safeArray(
                App.data
                    ?.networkConnectionList
            );


        const packetList =
            safeArray(
                App.packetDetailList
            );


        const packetCount =
            getPacketCount();


        /* ========================================
           최근 날짜 계산
           ======================================== */

        const systemDate =

            systemInfo

                ? App.parseDate(
                    systemInfo.receivedAt
                )

                : null;


        const portDate =
            latestDateFrom(
                portList,
                [
                    "receivedAt"
                ]
            );


        const networkDate =
            latestDateFrom(
                networkList,
                [
                    "lastSeenAt",
                    "receivedAt",
                    "firstSeenAt"
                ]
            );


        const packetDate =
            latestDateFrom(
                packetList,
                [
                    "receivedAt"
                ]
            );


        const latestDates =
            [
                systemDate,
                portDate,
                networkDate,
                packetDate
            ]
            .filter(Boolean);


        const overallLatest =

            latestDates.length > 0

                ? new Date(

                    Math.max(
                        ...latestDates.map(
                            (date) =>
                                date.getTime()
                        )
                    )

                )

                : null;


        const hasAnyData =

            Boolean(systemInfo)

            ||

            portList.length > 0

            ||

            networkList.length > 0

            ||

            packetCount > 0;


        /* ========================================
           전체 수집 상태
           ======================================== */

        const badge =
            byId(
                "agentCollectionBadge"
            );


        if (badge) {

            badge.classList.remove(
                "is-waiting",
                "has-data",
                "no-data"
            );


            badge.classList.add(

                hasAnyData
                    ? "has-data"
                    : "no-data"

            );


            badge.innerHTML =
                `

                <span
                    class="agent-collection-badge-dot">
                </span>

                ${
                    hasAnyData
                        ? "수집 데이터 확인"
                        : "데이터 없음"
                }

                `;
        }


        setState(
            "agentDataState",
            hasAnyData,
            "수집 확인",
            "데이터 없음"
        );


        const status =
            byId(
                "agentCollectionStatus"
            );


        if (status) {

            status.textContent =

                hasAnyData
                    ? "최근 수집 확인"
                    : "수집 데이터 없음";
        }


        const message =
            byId(
                "agentCollectionMessage"
            );


        if (message) {

            message.textContent =

                overallLatest

                    ? (
                        "최근 데이터 "
                        +
                        relativeTime(
                            overallLatest
                        )
                    )

                    :
                    "저장된 수집 데이터가 없습니다.";
        }


        const latestRelative =
            byId(
                "agentLatestRelative"
            );


        if (latestRelative) {

            latestRelative.textContent =

                overallLatest
                    ? relativeTime(
                        overallLatest
                    )
                    : "-";
        }


        /* ========================================
           열린 포트
           ======================================== */

        const portCount =
            byId(
                "agentPortCount"
            );


        if (portCount) {

            portCount.textContent =
                `${portList.length}건`;
        }


        setState(
            "agentPortState",
            portList.length > 0
        );


        /* ========================================
           네트워크 연결
           ======================================== */

        const networkCount =
            byId(
                "agentNetworkCount"
            );


        if (networkCount) {

            networkCount.textContent =
                `${networkList.length}건`;
        }


        setState(
            "agentNetworkState",
            networkList.length > 0
        );


        /* ========================================
           패킷 메타데이터
           ======================================== */

        const packetCountElement =
            byId(
                "agentPacketCount"
            );


        if (packetCountElement) {

            packetCountElement.textContent =
                `${packetCount}건`;
        }


        setState(
            "agentPacketState",
            packetCount > 0
        );


        /* ========================================
           최근 수집 시간
           ======================================== */

        renderCollectionTime(
            "agentSystemCollectedAt",
            "agentSystemRelative",
            systemDate
        );


        renderCollectionTime(
            "agentPortCollectedAt",
            "agentPortRelative",
            portDate
        );


        renderCollectionTime(
            "agentNetworkCollectedAt",
            "agentNetworkRelative",
            networkDate
        );


        renderCollectionTime(
            "agentPacketCollectedAt",
            "agentPacketRelative",
            packetDate
        );
    };


    /* ========================================
       카드 바로가기
       ======================================== */

    const bindAgentNavigation = () => {

        document
            .querySelectorAll(
                "[data-agent-page-target]"
            )
            .forEach(
                (button) => {

                    if (
                        button.dataset
                            .agentNavigationBound
                        === "true"
                    ) {

                        return;
                    }


                    button.dataset
                        .agentNavigationBound =
                        "true";


                    button.addEventListener(
                        "click",
                        () => {

                            const target =
                                button.dataset
                                    .agentPageTarget;


                            if (
                                !target
                                ||
                                !window
                                    .SecureAgentNavigation
                            ) {

                                return;
                            }


                            window
                                .SecureAgentNavigation
                                .showPage(
                                    target
                                );


                            if (
                                target
                                === "connections"
                            ) {

                                requestAnimationFrame(
                                    () => {

                                        if (
                                            typeof App
                                                .showConnectionView
                                            === "function"
                                        ) {

                                            App.showConnectionView(
                                                "current",
                                                {
                                                    scroll:
                                                        false
                                                }
                                            );
                                        }

                                    }
                                );
                            }


                            if (
                                target
                                === "packets"
                            ) {

                                requestAnimationFrame(
                                    () => {

                                        if (
                                            typeof App
                                                .showPacketView
                                            === "function"
                                        ) {

                                            App.showPacketView(
                                                "summary",
                                                {
                                                    scroll:
                                                        false
                                                }
                                            );
                                        }

                                    }
                                );
                            }

                        }
                    );

                }
            );
    };


    /* ========================================
       PC 데이터 조회 완료 후 상태 갱신
       ======================================== */

    const originalLoadSelectedComputerData =
        App.loadSelectedComputerData;


    if (
        typeof originalLoadSelectedComputerData
        === "function"
    ) {

        App.loadSelectedComputerData =
            async (
                computerName
            ) => {

                const result =
                    await originalLoadSelectedComputerData(
                        computerName
                    );


                App.updateAgentStatus(
                    computerName
                );


                return result;
            };
    }


    bindAgentNavigation();


    App.updateAgentStatus();

})();