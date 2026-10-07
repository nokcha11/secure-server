package com.secureagent.server.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.model.SystemInfoRequest;
import com.secureagent.server.entity.AgentSystemInfo;
import com.secureagent.server.repository.AgentSystemInfoRepository;

@Service
@Transactional(readOnly = true)
public class SystemInfoService {

    private final AgentSystemInfoRepository agentSystemInfoRepository;

    public SystemInfoService(
            AgentSystemInfoRepository agentSystemInfoRepository) {

        this.agentSystemInfoRepository = agentSystemInfoRepository;
    }

    @Transactional
    public AgentSystemInfo addOrUpdateSystemInfo(
            SystemInfoRequest request) {

        AgentSystemInfo systemInfo =
                agentSystemInfoRepository
                        .findFirstByComputerNameOrderByIdDesc(
                                request.getComputerName())
                        .orElseGet(AgentSystemInfo::new);

        applyRequest(systemInfo, request);

        return agentSystemInfoRepository.save(systemInfo);
    }

    public List<AgentSystemInfo> getAllSystemInfo() {
        return agentSystemInfoRepository.findAll();
    }

    public Optional<AgentSystemInfo> getSystemInfoById(Long id) {
        return agentSystemInfoRepository.findById(id);
    }

    public List<AgentSystemInfo> getSystemInfoByComputerName(
            String computerName) {

        return agentSystemInfoRepository
                .findByComputerName(computerName);
    }

    @Transactional
    public Optional<AgentSystemInfo> updateSystemInfo(
            Long id,
            SystemInfoRequest request) {

        return agentSystemInfoRepository.findById(id)
                .map(systemInfo -> {
                    applyRequest(systemInfo, request);
                    return agentSystemInfoRepository.save(systemInfo);
                });
    }

    @Transactional
    public boolean deleteSystemInfo(Long id) {

        if (!agentSystemInfoRepository.existsById(id)) {
            return false;
        }

        agentSystemInfoRepository.deleteById(id);
        return true;
    }

    private void applyRequest(
            AgentSystemInfo systemInfo,
            SystemInfoRequest request) {

        systemInfo.setComputerName(request.getComputerName());
        systemInfo.setOsName(request.getOsName());
        systemInfo.setOsVersion(request.getOsVersion());
        systemInfo.setUserName(request.getUserName());
    }
}