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
     * 숫자를 0 이상의 값으로 변환합니다.
     */
    function toNumber(value) {

        const numberValue =
                Number(value);

        return Number.isFinite(numberValue)
                && numberValue > 0
                        ? numberValue
                        : 0;
    }


    /*
     * 숫자에 천 단위 구분기호를 추가합니다.
     */
    function formatNumber(value) {

        return toNumber(value)
                .toLocaleString("ko-KR");
    }


    /*
     * 바이트를 B, KB, MB, GB 단위로 표시합니다.
     */
    function formatBytes(value) {

        const bytes =
                toNumber(value);

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

        if (bytes < 1024 * 1024 * 1024) {
            return (
                    bytes / (1024 * 1024)
            ).toFixed(1) + " MB";
        }

        return (
                bytes
                / (1024 * 1024 * 1024)
        ).toFixed(1) + " GB";
    }


    /*
     * 차트 영역에 데이터 없음
     * 안내 문구를 표시합니다.
     */
    function showEmptyChart(
            elementId,
            message) {

        const container =
                document.getElementById(
                        elementId
                );

        if (!container) {
            return;
        }

        const emptyMessage =
                document.createElement("p");

        emptyMessage.className =
                "packet-chart-empty";

        emptyMessage.textContent =
                message;

        container.replaceChildren(
                emptyMessage
        );
    }


    /*
     * 도넛 차트를 생성합니다.
     */
    function renderDonutChart(
            elementId,
            itemList,
            centerLabel,
            valueFormatter) {

        const container =
                document.getElementById(
                        elementId
                );

        if (!container) {
            return;
        }

        const validItems =
                itemList.filter(
                        function (item) {

            return toNumber(item.value) > 0;
        });

        const total =
                validItems.reduce(
                        function (sum, item) {

            return sum
                    + toNumber(item.value);
        }, 0);

        if (total <= 0) {
            showEmptyChart(
                    elementId,
                    "표시할 데이터가 없습니다."
            );

            return;
        }

        /*
         * conic-gradient에 사용할
         * 각 항목의 시작·끝 각도를 계산합니다.
         */
        let currentDegree = 0;

        const gradientSegments =
                validItems.map(
                        function (item, index) {

            const startDegree =
                    currentDegree;

            const isLast =
                    index ===
                    validItems.length - 1;

            const itemDegree =
                    isLast
                            ? 360 - currentDegree
                            : (
                                toNumber(item.value)
                                / total
                            ) * 360;

            currentDegree += itemDegree;

            return (
                item.color
                + " "
                + startDegree
                + "deg "
                + currentDegree
                + "deg"
            );
        });

        const layout =
                document.createElement("div");

        layout.className =
                "packet-donut-layout";

        const ring =
                document.createElement("div");

        ring.className =
                "packet-donut-ring";

        ring.style.setProperty(
                "--packet-donut-gradient",
                "conic-gradient("
                + gradientSegments.join(", ")
                + ")"
        );

        const center =
                document.createElement("div");

        center.className =
                "packet-donut-center";

        const totalElement =
                document.createElement("strong");

        totalElement.className =
                "packet-donut-total";

        totalElement.textContent =
                valueFormatter(total);

        const labelElement =
                document.createElement("span");

        labelElement.className =
                "packet-donut-label";

        labelElement.textContent =
                centerLabel;

        center.append(
                totalElement,
                labelElement
        );

        ring.appendChild(center);

        /*
         * 도넛 차트 범례를 생성합니다.
         */
        const legend =
                document.createElement("div");

        legend.className =
                "packet-chart-legend";

        validItems.forEach(
                function (item) {

            const legendItem =
                    document.createElement("div");

            legendItem.className =
                    "packet-chart-legend-item";

            const dot =
                    document.createElement("span");

            dot.className =
                    "packet-chart-legend-dot";

            dot.style.setProperty(
                    "--legend-color",
                    item.color
            );

            const label =
                    document.createElement("span");

            label.className =
                    "packet-chart-legend-label";

            label.textContent =
                    item.label;

            const value =
                    document.createElement("strong");

            value.className =
                    "packet-chart-legend-value";

            value.textContent =
                    valueFormatter(item.value);

            legendItem.append(
                    dot,
                    label,
                    value
            );

            legend.appendChild(
                    legendItem
            );
        });

        layout.append(
                ring,
                legend
        );

        container.replaceChildren(
                layout
        );
    }


    /*
     * IP 및 도메인 순위 막대 차트를
     * 생성합니다.
     */
    function renderRankingChart(
            elementId,
            itemList,
            valueFormatter,
            colorList) {

        const container =
                document.getElementById(
                        elementId
                );

        if (!container) {
            return;
        }

        const validItems =
                itemList
                        .filter(
                                function (item) {

            return item.name
                    && toNumber(item.value) > 0;
        })
                        .slice(0, 5);

        if (validItems.length === 0) {
            showEmptyChart(
                    elementId,
                    "표시할 데이터가 없습니다."
            );

            return;
        }

        const maximumValue =
                Math.max(
                        ...validItems.map(
                                function (item) {

            return toNumber(item.value);
        }),
                        0
                );

        const rankingList =
                document.createElement("div");

        rankingList.className =
                "packet-ranking-list";

        validItems.forEach(
                function (item, index) {

            const rankingItem =
                    document.createElement("div");

            rankingItem.className =
                    "packet-ranking-item";

            const header =
                    document.createElement("div");

            header.className =
                    "packet-ranking-header";

            const name =
                    document.createElement("span");

            name.className =
                    "packet-ranking-name";

            name.textContent =
                    item.name;

            name.title =
                    item.name;

            const value =
                    document.createElement("strong");

            value.className =
                    "packet-ranking-value";

            value.textContent =
                    valueFormatter(item.value);

            const track =
                    document.createElement("div");

            track.className =
                    "packet-ranking-track";

            const bar =
                    document.createElement("div");

            bar.className =
                    "packet-ranking-bar";

            const widthPercent =
                    maximumValue === 0
                            ? 0
                            : (
                                toNumber(item.value)
                                / maximumValue
                            ) * 100;

            const color =
                    colorList[
                        index % colorList.length
                    ];

            bar.style.setProperty(
                    "--ranking-color",
                    color
            );

            bar.style.width =
                    Math.max(
                            widthPercent,
                            3
                    ) + "%";

            header.append(
                    name,
                    value
            );

            track.appendChild(
                    bar
            );

            rankingItem.append(
                    header,
                    track
            );

            rankingList.appendChild(
                    rankingItem
            );
        });

        container.replaceChildren(
                rankingList
        );
    }


    /*
     * 프로토콜별 패킷 수를 계산합니다.
     */
    function createProtocolItems(packetList) {

        const protocolTotals = {
            DNS: 0,
            HTTP: 0,
            HTTPS: 0,
            OTHER: 0
        };

        packetList.forEach(
                function (packet) {

            const protocol =
                    String(
                            packet.applicationProtocol
                            || "OTHER"
                    ).toUpperCase();

            const protocolName =
                    Object.hasOwn(
                            protocolTotals,
                            protocol
                    )
                            ? protocol
                            : "OTHER";

            protocolTotals[protocolName] +=
                    toNumber(
                            packet.packetCount
                    );
        });

        return [
            {
                label: "DNS",
                value: protocolTotals.DNS,
                color: "#34d399"
            },
            {
                label: "HTTP",
                value: protocolTotals.HTTP,
                color: "#fbbf24"
            },
            {
                label: "HTTPS",
                value: protocolTotals.HTTPS,
                color: "#a78bfa"
            },
            {
                label: "OTHER",
                value: protocolTotals.OTHER,
                color: "#60a5fa"
            }
        ];
    }


    /*
     * 통신 방향별 전체 바이트를 계산합니다.
     */
    function createDirectionItems(packetList) {

        const directionTotals = {
            INBOUND: 0,
            OUTBOUND: 0,
            OTHER: 0
        };

        packetList.forEach(
                function (packet) {

            const direction =
                    String(
                            packet.direction
                            || "OTHER"
                    ).toUpperCase();

            const directionName =
                    Object.hasOwn(
                            directionTotals,
                            direction
                    )
                            ? direction
                            : "OTHER";

            directionTotals[directionName] +=
                    toNumber(
                            packet.totalBytes
                    );
        });

        return [
            {
                label: "INBOUND",
                value: directionTotals.INBOUND,
                color: "#38bdf8"
            },
            {
                label: "OUTBOUND",
                value: directionTotals.OUTBOUND,
                color: "#fb7185"
            },
            {
                label: "OTHER",
                value: directionTotals.OTHER,
                color: "#94a3b8"
            }
        ];
    }


    /*
     * 상대방 IP별 통신량을 계산하고
     * 상위 5개를 반환합니다.
     */
    function createTopIpItems(packetList) {

        const ipTotals =
                new Map();

        packetList.forEach(
                function (packet) {

            const remoteAddress =
                    String(
                            packet.remoteAddress
                            || ""
                    ).trim();

            if (!remoteAddress) {
                return;
            }

            const currentBytes =
                    ipTotals.get(
                            remoteAddress
                    ) || 0;

            ipTotals.set(
                    remoteAddress,
                    currentBytes
                    + toNumber(
                            packet.totalBytes
                    )
            );
        });

        return Array.from(
                ipTotals.entries()
        )
                .map(function (entry) {
                    return {
                        name: entry[0],
                        value: entry[1]
                    };
                })
                .sort(function (first, second) {
                    return second.value
                            - first.value;
                })
                .slice(0, 5);
    }


    /*
     * DNS, HTTP Host, TLS SNI에서 확인된
     * 도메인별 패킷 수를 계산합니다.
     */
    function createTopDomainItems(packetList) {

        const domainTotals =
                new Map();

        packetList.forEach(
                function (packet) {

            const domainCandidates = [
                packet.dnsDomain,
                packet.httpHost,
                packet.tlsServerName
            ];

            /*
             * 한 메타데이터에 같은 도메인이
             * 중복돼도 한 번만 계산합니다.
             */
            const uniqueDomains =
                    new Map();

            domainCandidates.forEach(
                    function (domainValue) {

                const domain =
                        String(
                                domainValue || ""
                        ).trim();

                if (!domain) {
                    return;
                }

                const normalizedDomain =
                        domain.toLowerCase();

                if (!uniqueDomains.has(
                        normalizedDomain
                )) {
                    uniqueDomains.set(
                            normalizedDomain,
                            domain
                    );
                }
            });

            const packetCount =
                    Math.max(
                            toNumber(
                                    packet.packetCount
                            ),
                            1
                    );

            uniqueDomains.forEach(
                    function (
                            originalDomain,
                            normalizedDomain) {

                const oldItem =
                        domainTotals.get(
                                normalizedDomain
                        );

                if (oldItem) {
                    oldItem.value +=
                            packetCount;

                    return;
                }

                domainTotals.set(
                        normalizedDomain,
                        {
                            name:
                                originalDomain,
                            value:
                                packetCount
                        }
                );
            });
        });

        return Array.from(
                domainTotals.values()
        )
                .sort(function (first, second) {
                    return second.value
                            - first.value;
                })
                .slice(0, 5);
    }


    /*
     * 패킷 메타데이터를 분석해
     * 네 개의 차트를 표시합니다.
     */
    App.renderPacketCharts =
            function (packetList) {

        const status =
                document.getElementById(
                        "packetChartStatus"
                );

        if (!Array.isArray(packetList)
                || packetList.length === 0) {

            showEmptyChart(
                    "packetProtocolChart",
                    "프로토콜 데이터가 없습니다."
            );

            showEmptyChart(
                    "packetDirectionChart",
                    "통신 방향 데이터가 없습니다."
            );

            showEmptyChart(
                    "packetTopIpChart",
                    "상대방 IP 데이터가 없습니다."
            );

            showEmptyChart(
                    "packetTopDomainChart",
                    "확인된 도메인이 없습니다."
            );

            if (status) {
                status.textContent =
                        "패킷 차트 데이터 없음";
            }

            return;
        }

        renderDonutChart(
                "packetProtocolChart",
                createProtocolItems(
                        packetList
                ),
                "패킷",
                formatNumber
        );

        renderDonutChart(
                "packetDirectionChart",
                createDirectionItems(
                        packetList
                ),
                "통신량",
                formatBytes
        );

        renderRankingChart(
                "packetTopIpChart",
                createTopIpItems(
                        packetList
                ),
                formatBytes,
                [
                    "#22d3ee",
                    "#38bdf8",
                    "#60a5fa",
                    "#818cf8",
                    "#a78bfa"
                ]
        );

        renderRankingChart(
                "packetTopDomainChart",
                createTopDomainItems(
                        packetList
                ),
                function (value) {
                    return formatNumber(value)
                            + "개";
                },
                [
                    "#34d399",
                    "#2dd4bf",
                    "#22d3ee",
                    "#38bdf8",
                    "#60a5fa"
                ]
        );

        if (status) {

            const computerName =
                    App.currentPacketComputerName
                    || "선택한 PC";

            status.textContent =
                    computerName
                    + " 최근 패킷 "
                    + packetList.length
                    + "건 분석 완료";
        }
    };

})();