package com.secureagent.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secureagent.server.entity.AgentPacketMetadata;

public interface AgentPacketMetadataRepository
        extends JpaRepository<
                AgentPacketMetadata,
                Long
        > {

    /*
     * 선택한 PC의 최근 패킷 메타데이터를
     * 최대 500개까지 조회합니다.
     */
    List<AgentPacketMetadata>
            findTop500ByComputerNameOrderByReceivedAtDesc(
                    String computerName
            );

    /*
     * 선택한 PC의 외부 통신 메타데이터를
     * 최대 500개까지 조회합니다.
     */
    List<AgentPacketMetadata>
            findTop500ByComputerNameAndExternalYnOrderByReceivedAtDesc(
                    String computerName,
                    String externalYn
            );

    /*
     * 선택한 PC에 저장된 전체
     * 패킷 메타데이터 개수입니다.
     */
    long countByComputerName(
            String computerName
    );

    /*
     * 외부 공인 IP와 통신한
     * 패킷 메타데이터 개수입니다.
     */
    long countByComputerNameAndExternalYn(
            String computerName,
            String externalYn
    );

    /*
     * DNS 도메인이 확인된
     * 패킷 메타데이터 개수입니다.
     */
    long countByComputerNameAndDnsDomainIsNotNull(
            String computerName
    );

    /*
     * 일반 HTTP Host가 확인된
     * 패킷 메타데이터 개수입니다.
     */
    long countByComputerNameAndHttpHostIsNotNull(
            String computerName
    );

    /*
     * HTTPS TLS SNI가 확인된
     * 패킷 메타데이터 개수입니다.
     */
    long countByComputerNameAndTlsServerNameIsNotNull(
            String computerName
    );
}