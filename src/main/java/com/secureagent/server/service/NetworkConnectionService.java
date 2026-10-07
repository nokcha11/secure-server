package com.secureagent.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.model.NetworkConnectionRequest;
import com.secureagent.server.entity.AgentNetworkConnection;
import com.secureagent.server.repository.AgentNetworkConnectionRepository;

@Service
public class NetworkConnectionService {

    private final AgentNetworkConnectionRepository repository;
    private final NetworkRiskAnalysisService networkRiskAnalysisService;

    public NetworkConnectionService(
            AgentNetworkConnectionRepository repository,
            NetworkRiskAnalysisService networkRiskAnalysisService) {

        this.repository = repository;
        this.networkRiskAnalysisService =
                networkRiskAnalysisService;
    }

    @Transactional
    public void updateConnections(
            String computerName,
            List<NetworkConnectionRequest> requestList) {

        LocalDateTime now = LocalDateTime.now();

        List<AgentNetworkConnection> existingConnections =
                repository
                        .findByComputerNameOrderByLastSeenAtDesc(
                                computerName);

        /*
         * 기존 활성 연결을 우선 종료 후보로 표시합니다.
         * 이번 수집 목록에서 다시 확인되면 Y로 되돌립니다.
         */
        for (AgentNetworkConnection existing
                : existingConnections) {

            if ("Y".equals(existing.getActiveYn())) {
                existing.setActiveYn("N");
                existing.setEndedAt(now);
            }
        }

        if (requestList != null) {
            for (NetworkConnectionRequest request
                    : requestList) {

                Optional<AgentNetworkConnection> matched =
                        findMatchedConnection(
                                existingConnections,
                                request
                        );

                AgentNetworkConnection connection;

                if (matched.isPresent()) {
                    connection = matched.get();

                    /*
                     * 종료되었던 연결이 다시 발견되면
                     * 새로운 접속으로 계산합니다.
                     */
                    if (!"Y".equals(connection.getActiveYn())) {
                        int previousCount =
                                connection.getConnectionCount();

                        connection.setConnectionCount(
                                previousCount + 1);
                    }

                } else {
                    connection =
                            new AgentNetworkConnection();

                    connection.setComputerName(computerName);
                    connection.setFirstSeenAt(now);
                    connection.setConnectionCount(1);

                    existingConnections.add(connection);
                }

                connection.setProtocol(
                        request.getProtocol());
                connection.setLocalAddress(
                        request.getLocalAddress());
                connection.setLocalPort(
                        request.getLocalPort());
                connection.setRemoteAddress(
                        request.getRemoteAddress());
                connection.setRemotePort(
                        request.getRemotePort());
                connection.setState(
                        request.getState());
                connection.setPid(
                        request.getPid());
                connection.setProcessName(
                        request.getProcessName());

                connection.setLastSeenAt(now);
                connection.setEndedAt(null);
                connection.setActiveYn("Y");

                /*
                 * 외부 IP 여부와 의심 여부를 판정합니다.
                 */
                NetworkRiskAnalysisService.NetworkRiskResult
                        riskResult =
                        networkRiskAnalysisService.analyze(
                                request.getRemoteAddress(),
                                request.getRemotePort(),
                                request.getState(),
                                request.getProcessName(),
                                connection.getConnectionCount()
                        );

                connection.setExternalYn(
                        riskResult.externalYn());

                connection.setSuspiciousYn(
                        riskResult.suspiciousYn());

                connection.setRiskReason(
                        riskResult.riskReason());
            }
        }

        repository.saveAll(existingConnections);
    }

    private Optional<AgentNetworkConnection>
            findMatchedConnection(
                    List<AgentNetworkConnection> existingConnections,
                    NetworkConnectionRequest request) {

        return existingConnections.stream()
                .filter(connection ->
                        isSameConnection(
                                connection,
                                request))
                .findFirst();
    }

    private boolean isSameConnection(
            AgentNetworkConnection connection,
            NetworkConnectionRequest request) {

        return Objects.equals(
                        connection.getProtocol(),
                        request.getProtocol())
                && Objects.equals(
                        connection.getLocalAddress(),
                        request.getLocalAddress())
                && Objects.equals(
                        connection.getLocalPort(),
                        request.getLocalPort())
                && Objects.equals(
                        connection.getRemoteAddress(),
                        request.getRemoteAddress())
                && Objects.equals(
                        connection.getRemotePort(),
                        request.getRemotePort())
                && Objects.equals(
                        connection.getPid(),
                        request.getPid());
    }

    public List<AgentNetworkConnection>
            getActiveConnections(String computerName) {

        return repository
                .findByComputerNameAndActiveYnOrderByLastSeenAtDesc(
                        computerName,
                        "Y"
                );
    }

    public List<AgentNetworkConnection>
            getConnectionHistory(String computerName) {

        return repository
                .findByComputerNameOrderByLastSeenAtDesc(
                        computerName);
    }

    public List<AgentNetworkConnection>
            getSuspiciousConnections(String computerName) {

        return repository
                .findByComputerNameAndSuspiciousYnOrderByLastSeenAtDesc(
                        computerName,
                        "Y"
                );
    }
}