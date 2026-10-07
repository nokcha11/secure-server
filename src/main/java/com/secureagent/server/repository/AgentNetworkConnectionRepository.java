package com.secureagent.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secureagent.server.entity.AgentNetworkConnection;

public interface AgentNetworkConnectionRepository
        extends JpaRepository<AgentNetworkConnection, Long> {

    /*
     * 특정 PC의 전체 연결 이력을
     * 최근 확인 시각 순으로 조회합니다.
     */
    List<AgentNetworkConnection>
            findByComputerNameOrderByLastSeenAtDesc(
                    String computerName
            );

    /*
     * 특정 PC의 현재 활성 연결만 조회합니다.
     *
     * activeYn에 "Y"를 전달하면
     * 현재 연결된 항목만 반환합니다.
     */
    List<AgentNetworkConnection>
            findByComputerNameAndActiveYnOrderByLastSeenAtDesc(
                    String computerName,
                    String activeYn
            );

    /*
     * 특정 PC의 의심 연결만 조회합니다.
     *
     * suspiciousYn에 "Y"를 전달하면
     * 의심스러운 연결만 반환합니다.
     */
    List<AgentNetworkConnection>
            findByComputerNameAndSuspiciousYnOrderByLastSeenAtDesc(
                    String computerName,
                    String suspiciousYn
            );
}