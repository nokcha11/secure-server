(() => {
    "use strict";

    const App = window.SecureAgent;

    App.relationType = (node) => {
        if (node.suspicious) {
            return "suspicious";
        }

        if (node.external) {
            return "external";
        }

        return "internal";
    };

    App.shortenIp = (ip) => {
        const value = String(ip || "");

        if (value.length > 17) {
            return `${value.slice(0, 15)}...`;
        }

        return value;
    };

    App.showRelationDetails = (node) => {
        if (!App.el.relationGraphDetails || !node) {
            return;
        }

        let connectionType = "내부 연결";

        if (node.suspicious) {
            connectionType = "의심 연결";
        } else if (node.external) {
            connectionType = "외부 연결";
        }

        const portText = Array.from(node.ports).join(", ") || "-";
        const processText =
            Array.from(node.processes).join(", ") || "-";
        const stateText = Array.from(node.states).join(", ") || "-";

        App.el.relationGraphDetails.textContent =
            `상대방 IP: ${node.ip}` +
            ` ｜ 연결 유형: ${connectionType}` +
            ` ｜ 접속 횟수: ${node.count}회` +
            ` ｜ 포트: ${portText}` +
            ` ｜ 프로세스: ${processText}` +
            ` ｜ 상태: ${stateText}`;
    };

    App.appendRelationNode = (
        svg,
        namespace,
        node,
        centerX,
        centerY
    ) => {
        const type = App.relationType(node);
        const group = document.createElementNS(namespace, "g");
        const circle = document.createElementNS(namespace, "circle");
        const nodeTypeLabel = document.createElementNS(
            namespace,
            "text"
        );
        const ipLabel = document.createElementNS(namespace, "text");

        group.style.cursor = "pointer";

        circle.setAttribute("cx", node.x);
        circle.setAttribute("cy", node.y);
        circle.setAttribute("r", "40");
        circle.setAttribute(
            "class",
            `relation-node remote ${type}`
        );

        nodeTypeLabel.setAttribute("x", node.x);
        nodeTypeLabel.setAttribute("y", node.y + 5);
        nodeTypeLabel.setAttribute(
            "class",
            "relation-node-sub-label"
        );

        nodeTypeLabel.textContent =
            type === "suspicious"
                ? "의심"
                : type === "external"
                    ? "외부"
                    : "내부";

        const distance = Math.hypot(
            node.x - centerX,
            node.y - centerY
        );

        const unitX = (node.x - centerX) / distance;
        const unitY = (node.y - centerY) / distance;

        ipLabel.setAttribute("x", node.x + unitX * 56);
        ipLabel.setAttribute("y", node.y + unitY * 56);
        ipLabel.setAttribute("class", "relation-node-label");

        if (unitX > 0.25) {
            ipLabel.setAttribute("text-anchor", "start");
        } else if (unitX < -0.25) {
            ipLabel.setAttribute("text-anchor", "end");
        } else {
            ipLabel.setAttribute("text-anchor", "middle");
        }

        ipLabel.textContent = App.shortenIp(node.ip);

        group.append(circle, nodeTypeLabel, ipLabel);

        group.addEventListener("click", () => {
            App.showRelationDetails(node);
        });

        svg.appendChild(group);
    };

    App.drawRelationGraph = () => {
        const { el, data } = App;
        const svg = el.relationGraphSvg;

        if (!svg) {
            return;
        }

        svg.replaceChildren();
        svg.setAttribute("viewBox", "0 0 1000 560");

        const groupedNodes = new Map();

        data.networkConnectionList.forEach((connection) => {
            const ip = String(
                connection.remoteAddress || ""
            ).trim();

            if (["", "-", "*", "0.0.0.0", "::"].includes(ip)) {
                return;
            }

            if (!groupedNodes.has(ip)) {
                groupedNodes.set(ip, {
                    ip,
                    count: 0,
                    external: false,
                    suspicious: false,
                    ports: new Set(),
                    processes: new Set(),
                    states: new Set()
                });
            }

            const node = groupedNodes.get(ip);
            const connectionCount = Number(
                connection.connectionCount
            );

            node.count +=
                Number.isFinite(connectionCount) &&
                connectionCount > 0
                    ? connectionCount
                    : 1;

            node.external =
                node.external ||
                App.normalize(connection.externalYn) === "Y";

            node.suspicious =
                node.suspicious ||
                App.normalize(connection.suspiciousYn) === "Y";

            if (connection.remotePort) {
                node.ports.add(String(connection.remotePort));
            }

            if (connection.processName) {
                node.processes.add(String(connection.processName));
            }

            if (connection.state) {
                node.states.add(String(connection.state));
            }
        });

        const allNodes = Array.from(groupedNodes.values()).sort(
            (left, right) => right.count - left.count
        );

        const nodes = allNodes.slice(0, 16);

        if (el.relationNodeCount) {
            el.relationNodeCount.textContent =
                allNodes.length > nodes.length
                    ? `${allNodes.length}개 중 ${nodes.length}개`
                    : `${nodes.length}개`;
        }

        if (nodes.length === 0) {
            if (el.relationGraphEmpty) {
                el.relationGraphEmpty.hidden = false;
                el.relationGraphEmpty.classList.add("is-visible");
            }

            if (el.relationGraphStatus) {
                el.relationGraphStatus.className = "chart-status";
                el.relationGraphStatus.textContent =
                    "표시할 네트워크 연결 관계가 없습니다.";
            }

            if (el.relationGraphDetails) {
                el.relationGraphDetails.textContent =
                    "상세 정보가 없습니다.";
            }

            return;
        }

        if (el.relationGraphEmpty) {
            el.relationGraphEmpty.hidden = true;
            el.relationGraphEmpty.classList.remove("is-visible");
        }

        if (el.relationGraphStatus) {
            el.relationGraphStatus.className =
                "chart-status chart-success";
            el.relationGraphStatus.textContent =
                `접속 횟수가 많은 상위 ${nodes.length}개 상대방 IP를 표시합니다. ` +
                "IP를 클릭하면 아래에서 상세정보를 확인할 수 있습니다.";
        }

        const namespace = "http://www.w3.org/2000/svg";
        const centerX = 500;
        const centerY = 280;

        let graphRadius = 215;

        if (nodes.length <= 8) {
            graphRadius = 180;
        } else if (nodes.length <= 12) {
            graphRadius = 200;
        }

        nodes.forEach((node, index) => {
            const angle =
                (index / nodes.length) * Math.PI * 2 - Math.PI / 2;

            node.x = centerX + Math.cos(angle) * graphRadius;
            node.y = centerY + Math.sin(angle) * graphRadius;

            const edge = document.createElementNS(namespace, "line");

            edge.setAttribute("x1", centerX);
            edge.setAttribute("y1", centerY);
            edge.setAttribute("x2", node.x);
            edge.setAttribute("y2", node.y);
            edge.setAttribute(
                "class",
                `relation-edge ${App.relationType(node)}`
            );

            edge.style.strokeWidth =
                `${Math.min(8, Math.max(2, node.count))}px`;

            svg.appendChild(edge);
        });

        const localNode = document.createElementNS(
            namespace,
            "circle"
        );

        localNode.setAttribute("cx", centerX);
        localNode.setAttribute("cy", centerY);
        localNode.setAttribute("r", "76");
        localNode.setAttribute("class", "relation-node local");
        svg.appendChild(localNode);

        const localLabel = document.createElementNS(
            namespace,
            "text"
        );

        localLabel.setAttribute("x", centerX);
        localLabel.setAttribute("y", centerY + 5);
        localLabel.setAttribute("class", "relation-local-label");
        localLabel.textContent =
            el.computerSelect.value || "내 PC";
        svg.appendChild(localLabel);

        nodes.forEach((node) => {
            App.appendRelationNode(
                svg,
                namespace,
                node,
                centerX,
                centerY
            );
        });

        App.showRelationDetails(nodes[0]);
    };
})();
