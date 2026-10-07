(() => {

    "use strict";

    /*
     * 기존 대시보드에서 사용하는
     * SecureAgent 공통 객체를 가져옵니다.
     */
    const App =
            window.SecureAgent =
                    window.SecureAgent || {};


    /*
     * 조회된 전체 패킷 상세 목록을 저장합니다.
     */
    App.packetDetailList = [];


    /*
     * 현재 패킷 상세 목록을 조회한
     * PC 이름을 저장합니다.
     */
    App.currentPacketComputerName = "";


    /*
     * 현재 적용된 패킷 필터를 저장합니다.
     */
    App.currentPacketFilter = "ALL";


    /*
     * 화면의 숫자를 변경합니다.
     */
    function setCount(elementId, value) {

        const element =
                document.getElementById(
                        elementId
                );

        if (!element) {
            return;
        }

        const numberValue =
                Number(value);

        element.textContent =
                Number.isFinite(numberValue)
                        ? numberValue.toLocaleString(
                                "ko-KR"
                        )
                        : "0";
    }


    /*
     * 모든 패킷 요약 숫자를
     * 0으로 초기화합니다.
     */
    function clearPacketSummary() {

        setCount(
                "packetTotalMetadataCount",
                0
        );

        setCount(
                "packetExternalCommunicationCount",
                0
        );

        setCount(
                "packetDnsDomainCount",
                0
        );

        setCount(
                "packetHttpHostCount",
                0
        );

        setCount(
                "packetTlsServerNameCount",
                0
        );
    }


    /*
     * 선택한 PC의 패킷 메타데이터 요약을
     * STS4 API에서 불러옵니다.
     */
    App.loadPacketSummary =
            async function (computerName) {

        const statusElement =
                document.getElementById(
                        "packetSummaryStatus"
                );

        if (!computerName) {

            clearPacketSummary();

            if (statusElement) {
                statusElement.textContent =
                        "조회할 PC를 선택하세요.";
            }

            return;
        }

        if (statusElement) {
            statusElement.textContent =
                    "패킷 요약 정보를 불러오는 중입니다.";
        }

        try {

            const encodedComputerName =
                    encodeURIComponent(
                            computerName
                    );

            const requestUrl =
                    "/api/agents/"
                    + encodedComputerName
                    + "/packet-metadata/summary";

            const response =
                    await fetch(requestUrl);

            if (!response.ok) {
                throw new Error(
                        "패킷 요약 API 조회 실패: "
                        + response.status
                );
            }

            const summary =
                    await response.json();

            setCount(
                    "packetTotalMetadataCount",
                    summary.totalMetadataCount
            );

            setCount(
                    "packetExternalCommunicationCount",
                    summary.externalCommunicationCount
            );

            setCount(
                    "packetDnsDomainCount",
                    summary.dnsDomainCount
            );

            setCount(
                    "packetHttpHostCount",
                    summary.httpHostCount
            );

            setCount(
                    "packetTlsServerNameCount",
                    summary.tlsServerNameCount
            );

            if (statusElement) {
                statusElement.textContent =
                        computerName
                        + " 패킷 요약 조회 완료";
            }

        } catch (error) {

            console.error(
                    "패킷 요약 조회 오류:",
                    error
            );

            clearPacketSummary();

            if (statusElement) {
                statusElement.textContent =
                        "패킷 요약 정보를 불러오지 못했습니다.";
            }
        }
    };


    /*
     * 선택한 PC의 최근 패킷 메타데이터를
     * STS4 API에서 조회합니다.
     */
    App.loadPacketDetails =
            async function (computerName) {

        const tableBody =
                document.getElementById(
                        "packetDetailTableBody"
                );

        const status =
                document.getElementById(
                        "packetDetailStatus"
                );

        /*
         * 패킷 상세 HTML 영역이 없으면
         * 조회 작업을 실행하지 않습니다.
         */
        if (!tableBody || !status) {
            return;
        }

        /*
         * PC 이름이 없으면 기존 데이터를
         * 초기화하고 안내 문구를 표시합니다.
         */
        if (!computerName) {

            App.packetDetailList = [];
            App.currentPacketComputerName = "";
            App.currentPacketFilter = "ALL";

            tableBody.replaceChildren();

            const emptyRow =
                    document.createElement(
                            "tr"
                    );

            emptyRow.className =
                    "packet-empty-row";

            const emptyCell =
                    document.createElement(
                            "td"
                    );

            emptyCell.colSpan = 8;
            emptyCell.textContent =
                    "PC를 선택하면 패킷 통신 정보가 표시됩니다.";

            emptyRow.appendChild(
                    emptyCell
            );

            tableBody.appendChild(
                    emptyRow
            );

            status.textContent =
                    "패킷 상세 조회 대기";

            return;
        }

        status.textContent =
                "패킷 상세 조회 중...";

        try {

            const encodedComputerName =
                    encodeURIComponent(
                            computerName
                    );

            const requestUrl =
                    "/api/agents/"
                    + encodedComputerName
                    + "/packet-metadata";

            const response =
                    await fetch(requestUrl);

            if (!response.ok) {
                throw new Error(
                        "패킷 상세 API 조회 실패: "
                        + response.status
                );
            }

            const packetList =
                    await response.json();

            /*
             * 필터 버튼과 검색 기능에서 사용할 수 있도록
             * 조회된 전체 패킷 목록을 저장합니다.
             */
            App.packetDetailList =
                    Array.isArray(packetList)
                            ? packetList
                            : [];

            /*
             * 현재 조회한 PC 이름을 저장합니다.
             */
            App.currentPacketComputerName =
                    computerName;

            /*
             * 새로운 PC를 조회하면
             * 검색어를 초기화합니다.
             */
            const searchInput =
                    document.getElementById(
                            "packetSearchInput"
                    );

            if (searchInput) {
                searchInput.value = "";
            }

            /*
             * 새로운 데이터를 조회하면
             * 전체 필터를 적용합니다.
             */
            App.applyPacketFilter(
                    "ALL"
            );
			
			/*
			 * 조회된 전체 패킷 목록을
			 * 패킷 분석 차트에 표시합니다.
			 */
			if (typeof App.renderPacketCharts
			        === "function") {

			    App.renderPacketCharts(
			            App.packetDetailList
			    );
			}

        } catch (error) {

            console.error(
                    "패킷 상세 조회 실패:",
                    error
            );

            App.packetDetailList = [];
            App.currentPacketComputerName =
                    computerName;

            tableBody.replaceChildren();

            const errorRow =
                    document.createElement(
                            "tr"
                    );

            errorRow.className =
                    "packet-empty-row";

            const errorCell =
                    document.createElement(
                            "td"
                    );

            errorCell.colSpan = 8;
            errorCell.textContent =
                    "패킷 통신 정보를 불러오지 못했습니다.";

            errorRow.appendChild(
                    errorCell
            );

            tableBody.appendChild(
                    errorRow
            );

            status.textContent =
                    "패킷 상세 조회 실패";
        }
    };


    /*
     * 서버에서 조회한 패킷 메타데이터를
     * 패킷 통신 상세 표에 출력합니다.
     */
    App.renderPacketDetails =
            function (packetList) {

        const tableBody =
                document.getElementById(
                        "packetDetailTableBody"
                );

        if (!tableBody) {
            return;
        }

        /*
         * 기존에 표시된 행을
         * 모두 제거합니다.
         */
        tableBody.replaceChildren();

        /*
         * 조회 결과가 없을 때
         * 안내 문구를 표시합니다.
         */
        if (!Array.isArray(packetList)
                || packetList.length === 0) {

            const emptyRow =
                    document.createElement(
                            "tr"
                    );

            emptyRow.className =
                    "packet-empty-row";

            const emptyCell =
                    document.createElement(
                            "td"
                    );

            emptyCell.colSpan = 8;
            emptyCell.textContent =
                    "조건에 맞는 패킷 통신 정보가 없습니다.";

            emptyRow.appendChild(
                    emptyCell
            );

            tableBody.appendChild(
                    emptyRow
            );

            return;
        }

        /*
         * 여러 행을 한 번에 추가하기 위한
         * 임시 문서 영역을 생성합니다.
         */
        const fragment =
                document.createDocumentFragment();

        /*
         * 패킷 메타데이터 한 건마다
         * 표의 행 하나를 생성합니다.
         */
        packetList.forEach(
                function (packet) {

            const row =
                    document.createElement(
                            "tr"
                    );

            /*
             * DNS, HTTP Host, TLS SNI 중에서
             * 확인된 도메인을 선택합니다.
             */
            const detectedDomain =
                    packet.dnsDomain
                    || packet.httpHost
                    || packet.tlsServerName
                    || "-";

            const cellValues = [
                App.formatPacketDateTime(
                        packet.receivedAt
                ),

                packet.applicationProtocol
                        || "-",

                packet.direction
                        || "-",

                packet.remoteAddress
                        || "-",

                packet.remotePort
                        ?? "-",

                detectedDomain,

                App.formatPacketNumber(
                        packet.packetCount
                ),

                App.formatPacketBytes(
                        packet.totalBytes
                )
            ];

            /*
             * 위에서 만든 값을 각각
             * 표의 셀로 생성합니다.
             */
            cellValues.forEach(
                    function (value) {

                const cell =
                        document.createElement(
                                "td"
                        );

                /*
                 * textContent를 사용하여
                 * HTML 코드가 실행되지 않게 합니다.
                 */
                cell.textContent =
                        String(value);

                row.appendChild(
                        cell
                );
            });

            fragment.appendChild(
                    row
            );
        });

        tableBody.appendChild(
                fragment
        );
    };


    /*
     * Oracle에서 전달된 날짜를
     * 화면에서 읽기 쉬운 형태로 변경합니다.
     */
    App.formatPacketDateTime =
            function (value) {

        if (!value) {
            return "-";
        }

        return String(value)
                .replace("T", " ")
                .substring(0, 19);
    };


    /*
     * 패킷 개수에 천 단위
     * 구분기호를 추가합니다.
     */
    App.formatPacketNumber =
            function (value) {

        const numberValue =
                Number(value);

        if (!Number.isFinite(
                numberValue
        )) {
            return "0";
        }

        return numberValue.toLocaleString(
                "ko-KR"
        );
    };


    /*
     * 패킷 통신량을 B, KB, MB 단위로
     * 읽기 쉽게 변경합니다.
     */
    App.formatPacketBytes =
            function (value) {

        const bytes =
                Number(value);

        if (!Number.isFinite(bytes)
                || bytes < 0) {

            return "0 B";
        }

        if (bytes < 1024) {
            return bytes.toLocaleString(
                    "ko-KR"
            ) + " B";
        }

        if (bytes < 1024 * 1024) {
            return (
                    bytes / 1024
            ).toFixed(1) + " KB";
        }

        return (
                bytes / (1024 * 1024)
        ).toFixed(1) + " MB";
    };


    /*
     * 패킷 한 건의 검색 대상 문자열을 만듭니다.
     *
     * IP, 포트, 프로토콜, 방향,
     * DNS, HTTP Host, TLS SNI를 검색할 수 있습니다.
     */
    App.createPacketSearchText =
            function (packet) {

        return [
            packet.receivedAt,
            packet.protocol,
            packet.applicationProtocol,
            packet.direction,
            packet.remoteAddress,
            packet.remotePort,
            packet.dnsDomain,
            packet.httpHost,
            packet.tlsServerName,
            packet.packetCount,
            packet.totalBytes
        ]
                .filter(function (value) {
                    return value !== null
                            && value !== undefined;
                })
                .join(" ")
                .toLowerCase();
    };


    /*
     * 전체·DNS·HTTP·HTTPS 필터와
     * 검색어를 패킷 상세 목록에 적용합니다.
     */
    App.applyPacketFilter =
            function (filterValue) {

        const availableFilters = [
            "ALL",
            "DNS",
            "HTTP",
            "HTTPS"
        ];

        const normalizedFilter =
                availableFilters.includes(
                        filterValue
                )
                        ? filterValue
                        : "ALL";

        App.currentPacketFilter =
                normalizedFilter;

        const completeList =
                Array.isArray(
                        App.packetDetailList
                )
                        ? App.packetDetailList
                        : [];

        /*
         * 검색창이 아직 HTML에 없으면
         * 검색어를 빈 문자열로 처리합니다.
         */
        const searchInput =
                document.getElementById(
                        "packetSearchInput"
                );

        const searchKeyword =
                searchInput
                        ? searchInput.value
                                .trim()
                                .toLowerCase()
                        : "";

        /*
         * 프로토콜 필터와 검색 조건을
         * 동시에 적용합니다.
         */
        const filteredList =
                completeList.filter(
                        function (packet) {

            const applicationProtocol =
                    String(
                            packet.applicationProtocol
                            || ""
                    ).toUpperCase();

            const protocolMatched =
                    normalizedFilter === "ALL"
                    || applicationProtocol ===
                            normalizedFilter;

            const searchMatched =
                    searchKeyword === ""
                    || App.createPacketSearchText(
                            packet
                    ).includes(
                            searchKeyword
                    );

            return protocolMatched
                    && searchMatched;
        });

        App.renderPacketDetails(
                filteredList
        );

        /*
         * 현재 선택된 버튼에
         * active 클래스를 적용합니다.
         */
        document.querySelectorAll(
                ".packet-filter-button"
        ).forEach(function (button) {

            const isActive =
                    button.dataset.packetFilter ===
                    normalizedFilter;

            button.classList.toggle(
                    "active",
                    isActive
            );

            button.setAttribute(
                    "aria-pressed",
                    String(isActive)
            );
        });

        /*
         * 필터링된 결과 개수를
         * 상태 영역에 표시합니다.
         */
        const status =
                document.getElementById(
                        "packetDetailStatus"
                );

        if (!status) {
            return;
        }

        const computerName =
                App.currentPacketComputerName;

        if (normalizedFilter === "ALL"
                && searchKeyword === "") {

            status.textContent =
                    computerName
                    + " 패킷 "
                    + completeList.length
                    + "건 조회 완료";

            return;
        }

        status.textContent =
                computerName
                + " 검색 결과 "
                + filteredList.length
                + "건 / 전체 "
                + completeList.length
                + "건";
    };


    /*
     * 패킷 필터 버튼과 검색창의
     * 이벤트를 연결합니다.
     */
    App.initializePacketFilters =
            function () {

        document.querySelectorAll(
                ".packet-filter-button"
        ).forEach(function (button) {

            button.addEventListener(
                    "click",
                    function () {

                App.applyPacketFilter(
                        button.dataset.packetFilter
                );
            });
        });

        /*
         * 검색어가 입력될 때마다
         * 현재 선택된 필터와 함께 적용합니다.
         */
        const searchInput =
                document.getElementById(
                        "packetSearchInput"
                );

        if (searchInput) {
            searchInput.addEventListener(
                    "input",
                    function () {

                App.applyPacketFilter(
                        App.currentPacketFilter
                );
            });
        }

        /*
         * 초기화 버튼을 누르면
         * 검색어와 필터를 모두 초기화합니다.
         */
        const clearButton =
                document.getElementById(
                        "packetSearchClearButton"
                );

        if (clearButton) {
            clearButton.addEventListener(
                    "click",
                    function () {

                if (searchInput) {
                    searchInput.value = "";
                }

                App.applyPacketFilter(
                        "ALL"
                );
            });
        }
    };


    /*
     * HTML이 준비된 후
     * 패킷 필터 기능을 초기화합니다.
     */
    if (document.readyState === "loading") {

        document.addEventListener(
                "DOMContentLoaded",
                App.initializePacketFilters
        );

    } else {

        App.initializePacketFilters();
    }

})();