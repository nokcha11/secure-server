package com.secureagent.server.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.model.OpenPortRequest;
import com.secureagent.model.OpenPortResponse;
import com.secureagent.server.entity.AgentOpenPort;
import com.secureagent.server.repository.AgentOpenPortRepository;
import com.secureagent.server.service.PortRiskAnalysisService.PortRiskResult;

@Service
public class OpenPortService {

    private final AgentOpenPortRepository agentOpenPortRepository;

    private final PortRiskAnalysisService portRiskAnalysisService;


    public OpenPortService(
            AgentOpenPortRepository agentOpenPortRepository,
            PortRiskAnalysisService portRiskAnalysisService) {

        this.agentOpenPortRepository =
                agentOpenPortRepository;

        this.portRiskAnalysisService =
                portRiskAnalysisService;
    }


    /*
     * 특정 PC의 기존 포트 목록을 삭제하고
     * 새로 수집한 포트 목록을 저장합니다.
     */
    @Transactional
    public void replaceOpenPorts(
            String computerName,
            List<OpenPortRequest> portRequests) {

        agentOpenPortRepository
                .deleteByComputerName(computerName);

        if (portRequests == null ||
                portRequests.isEmpty()) {

            return;
        }

        List<AgentOpenPort> openPorts =
                portRequests.stream()
                        .map(request -> {
                            return convertToEntity(
                                    computerName,
                                    request
                            );
                        })
                        .toList();

        agentOpenPortRepository.saveAll(openPorts);
    }


    /*
     * 기존 엔티티 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<AgentOpenPort> getOpenPorts(
            String computerName) {

        return agentOpenPortRepository
                .findByComputerNameOrderByLocalPortAsc(
                        computerName
                );
    }


    /*
     * 열린 포트와 위험 분석 결과를 함께 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<OpenPortResponse> getAnalyzedOpenPorts(
            String computerName) {

        List<AgentOpenPort> openPorts =
                agentOpenPortRepository
                        .findByComputerNameOrderByLocalPortAsc(
                                computerName
                        );

        return openPorts.stream()
                .map(this::convertToResponse)
                .toList();
    }


    /*
     * 요청 데이터를 DB 엔티티로 변환합니다.
     */
    private AgentOpenPort convertToEntity(
            String computerName,
            OpenPortRequest request) {

        AgentOpenPort openPort =
                new AgentOpenPort();

        openPort.setComputerName(computerName);
        openPort.setProtocol(request.getProtocol());
        openPort.setLocalAddress(
                request.getLocalAddress()
        );
        openPort.setLocalPort(
                request.getLocalPort()
        );
        openPort.setState(request.getState());
        openPort.setPid(request.getPid());
        openPort.setProcessName(
                request.getProcessName()
        );

        return openPort;
    }


    /*
     * DB 엔티티에 위험 분석 결과를 추가해
     * API 응답 객체로 변환합니다.
     */
    private OpenPortResponse convertToResponse(
            AgentOpenPort openPort) {

        PortRiskResult riskResult =
                portRiskAnalysisService.analyze(
                        openPort.getLocalPort(),
                        openPort.getProcessName()
                );

        return new OpenPortResponse(
                openPort.getId(),
                openPort.getComputerName(),
                openPort.getProtocol(),
                openPort.getLocalAddress(),
                openPort.getLocalPort(),
                openPort.getState(),
                openPort.getPid(),
                openPort.getProcessName(),
                openPort.getReceivedAt(),
                riskResult.serviceName(),
                riskResult.riskLevel(),
                riskResult.riskReason()
        );
    }
}