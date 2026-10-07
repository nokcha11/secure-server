(() => {
    "use strict";

    const App = window.SecureAgent;

    App.loadHourlyStats = async (computerName) => {
        if (!computerName) {
            return;
        }

        try {
            const encodedName = encodeURIComponent(computerName);

            const stats = await App.fetchJson(
                `/api/agents/${encodedName}/network-connections/hourly-stats`
            );

            App.data.hourlyStats = Array.isArray(stats) ? stats : [];
            App.renderHourlyChart(App.data.hourlyStats);
        } catch (error) {
            console.error(error);
            App.data.hourlyStats = [];
            App.showChartEmpty("그래프를 표시할 수 없습니다.");
        }
    };

    App.renderHourlyChart = (stats) => {
        const chart = App.el.hourlyConnectionChart;

        if (!chart) {
            return;
        }

        chart.replaceChildren();

        if (!Array.isArray(stats) || stats.length === 0) {
            App.showChartEmpty("시간대별 연결 데이터가 없습니다.");
            return;
        }

        const counts = stats.map((item) => {
            const count = Number(item.connectionCount);
            return Number.isFinite(count) ? count : 0;
        });

        const totalCount = counts.reduce(
            (sum, count) => sum + count,
            0
        );

        const maximumCount = Math.max(...counts, 0);
        const peakIndex = counts.indexOf(maximumCount);

        App.el.hourlyTotalCount.textContent =
            totalCount.toLocaleString("ko-KR");

        App.el.hourlyPeakTime.textContent =
            maximumCount > 0
                ? stats[peakIndex].timeLabel
                : "-";

        stats.forEach((item, index) => {
            const count = counts[index];

            const heightPercent =
                maximumCount === 0
                    ? 0
                    : (count / maximumCount) * 100;

            const chartItem = document.createElement("div");
            const value = document.createElement("div");
            const barArea = document.createElement("div");
            const bar = document.createElement("div");
            const label = document.createElement("div");

            chartItem.className = "hourly-bar-item";
            value.className = "hourly-bar-value";
            barArea.className = "hourly-bar-area";
            bar.className = "hourly-bar";
            label.className = "hourly-bar-label";

            value.textContent = count.toLocaleString("ko-KR");
            label.textContent = item.timeLabel || "-";

            bar.style.height =
                count === 0
                    ? "3px"
                    : `${Math.max(heightPercent, 4)}%`;

            bar.title = `${item.timeLabel} / 연결 ${count}개`;

            if (count === maximumCount && maximumCount > 0) {
                bar.classList.add("is-peak");
            }

            barArea.appendChild(bar);
            chartItem.append(value, barArea, label);
            chart.appendChild(chartItem);
        });

        requestAnimationFrame(() => {
            App.drawHourlyLine(counts, maximumCount);
        });

        App.el.hourlyChartStatus.className =
            "chart-status chart-success";

        App.el.hourlyChartStatus.textContent =
            "최근 24시간의 실제 연결 통계입니다. 막대와 직선형 추이선으로 표시합니다.";
    };

    App.drawHourlyLine = (counts, maximumCount) => {
        const chart = App.el.hourlyConnectionChart;

        if (!chart || chart.offsetParent === null) {
            return;
        }

        const barAreas = chart.querySelectorAll(
            ".hourly-bar-area"
        );

        if (barAreas.length === 0) {
            return;
        }

        const oldLine = chart.querySelector(
            ".hourly-line-overlay"
        );

        if (oldLine) {
            oldLine.remove();
        }

        const chartRect = chart.getBoundingClientRect();

        const points = Array.from(barAreas).map(
            (area, index) => {
                const areaRect = area.getBoundingClientRect();

                const ratio =
                    maximumCount === 0
                        ? 0
                        : counts[index] / maximumCount;

                return {
                    x:
                        areaRect.left -
                        chartRect.left +
                        areaRect.width / 2,
                    y:
                        areaRect.top -
                        chartRect.top +
                        areaRect.height -
                        ratio * areaRect.height,
                    count: counts[index]
                };
            }
        );

        const namespace = "http://www.w3.org/2000/svg";
        const svg = document.createElementNS(namespace, "svg");
        const path = document.createElementNS(namespace, "path");

        const pathData = points
            .map((point, index) => {
                const command = index === 0 ? "M" : "L";
                return `${command} ${point.x} ${point.y}`;
            })
            .join(" ");

        svg.classList.add("hourly-line-overlay");
        svg.setAttribute(
            "viewBox",
            `0 0 ${chart.scrollWidth} ${chart.scrollHeight}`
        );
        svg.setAttribute("preserveAspectRatio", "none");

        path.setAttribute("d", pathData);
        path.classList.add("hourly-line-path");
        svg.appendChild(path);

        points.forEach((point) => {
            const circle = document.createElementNS(
                namespace,
                "circle"
            );

            const isPeak =
                point.count === maximumCount && maximumCount > 0;

            circle.setAttribute("cx", point.x);
            circle.setAttribute("cy", point.y);
            circle.setAttribute("r", isPeak ? "6" : "4");
            circle.classList.add("hourly-line-point");

            if (isPeak) {
                circle.classList.add("is-peak");
            }

            svg.appendChild(circle);
        });

        chart.appendChild(svg);
    };

    App.showChartEmpty = (message) => {
        const chart = App.el.hourlyConnectionChart;

        if (!chart) {
            return;
        }

        const emptyMessage = document.createElement("div");
        emptyMessage.className = "hourly-chart-empty";
        emptyMessage.textContent = message;

        chart.replaceChildren(emptyMessage);

        if (App.el.hourlyTotalCount) {
            App.el.hourlyTotalCount.textContent = "0";
        }

        if (App.el.hourlyPeakTime) {
            App.el.hourlyPeakTime.textContent = "-";
        }

        if (App.el.hourlyChartStatus) {
            App.el.hourlyChartStatus.className =
                "chart-status chart-error";
            App.el.hourlyChartStatus.textContent = message;
        }
    };
})();
