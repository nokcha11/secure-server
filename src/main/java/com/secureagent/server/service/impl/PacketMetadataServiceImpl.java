package com.secureagent.server.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.dto.PacketMetadataRequest;
import com.secureagent.server.dto.PacketMetadataSummaryResponse;
import com.secureagent.server.entity.AgentPacketMetadata;
import com.secureagent.server.repository.AgentPacketMetadataRepository;
import com.secureagent.server.service.PacketMetadataService;

@Service
@Transactional
public class PacketMetadataServiceImpl
        implements PacketMetadataService {

    private final AgentPacketMetadataRepository
            packetMetadataRepository;

    /*
     * 생성자 주입으로 Repository를 연결합니다.
     */
    public PacketMetadataServiceImpl(
            AgentPacketMetadataRepository
                    packetMetadataRepository) {

        this.packetMetadataRepository =
                packetMetadataRepository;
    }

    /*
     * STS3에서 받은 DTO 목록을
     * Oracle 저장용 Entity 목록으로 변환한 뒤
     * 한 번에 저장합니다.
     */
    @Override
    public void savePacketMetadata(
            String computerName,
            List<PacketMetadataRequest> requestList) {

        if (computerName == null
                || computerName.isBlank()) {

            throw new IllegalArgumentException(
                    "PC 이름은 비어 있을 수 없습니다."
            );
        }

        if (requestList == null
                || requestList.isEmpty()) {

            return;
        }

        String normalizedComputerName =
                computerName.trim();

        List<AgentPacketMetadata> entityList =
                new ArrayList<>();

        for (PacketMetadataRequest request
                : requestList) {

            if (request == null) {
                continue;
            }

            AgentPacketMetadata entity =
                    new AgentPacketMetadata();

            entity.setComputerName(
                    normalizedComputerName
            );

            entity.setProtocol(
                    request.getProtocol()
            );

            entity.setApplicationProtocol(
                    request.getApplicationProtocol()
            );

            entity.setDirection(
                    request.getDirection()
            );

            entity.setLocalAddress(
                    request.getLocalAddress()
            );

            entity.setLocalPort(
                    request.getLocalPort()
            );

            entity.setRemoteAddress(
                    request.getRemoteAddress()
            );

            entity.setRemotePort(
                    request.getRemotePort()
            );

            entity.setPacketCount(
                    request.getPacketCount()
            );

            entity.setTotalBytes(
                    request.getTotalBytes()
            );

            entity.setFirstSeenAt(
                    request.getFirstSeenAt()
            );

            entity.setLastSeenAt(
                    request.getLastSeenAt()
            );

            entity.setExternalYn(
                    request.getExternalYn()
            );

            entity.setDnsDomain(
                    request.getDnsDomain()
            );

            entity.setHttpHost(
                    request.getHttpHost()
            );

            entity.setTlsServerName(
                    request.getTlsServerName()
            );

            entityList.add(entity);
        }

        if (!entityList.isEmpty()) {
            packetMetadataRepository.saveAll(
                    entityList
            );
        }
    }

    /*
     * 선택한 PC의 최근 메타데이터를
     * 최대 500개까지 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AgentPacketMetadata>
            getRecentPacketMetadata(
                    String computerName) {

        return packetMetadataRepository
                .findTop500ByComputerNameOrderByReceivedAtDesc(
                        computerName
                );
    }

    /*
     * 선택한 PC의 외부 통신만
     * 최대 500개까지 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AgentPacketMetadata>
            getRecentExternalPacketMetadata(
                    String computerName) {

        return packetMetadataRepository
                .findTop500ByComputerNameAndExternalYnOrderByReceivedAtDesc(
                        computerName,
                        "Y"
                );
    }

    /*
     * 선택한 PC에 누적된
     * 전체 메타데이터 개수를 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public long countPacketMetadata(
            String computerName) {

        return packetMetadataRepository
                .countByComputerName(
                        computerName
                );
    }

    /*
     * 대시보드에 표시할 패킷·도메인
     * 요약 정보를 계산합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public PacketMetadataSummaryResponse
            getPacketMetadataSummary(
                    String computerName) {

        if (computerName == null
                || computerName.isBlank()) {

            throw new IllegalArgumentException(
                    "PC 이름은 비어 있을 수 없습니다."
            );
        }

        String normalizedComputerName =
                computerName.trim();

        long totalMetadataCount =
                packetMetadataRepository
                        .countByComputerName(
                                normalizedComputerName
                        );

        long externalCommunicationCount =
                packetMetadataRepository
                        .countByComputerNameAndExternalYn(
                                normalizedComputerName,
                                "Y"
                        );

        long dnsDomainCount =
                packetMetadataRepository
                        .countByComputerNameAndDnsDomainIsNotNull(
                                normalizedComputerName
                        );

        long httpHostCount =
                packetMetadataRepository
                        .countByComputerNameAndHttpHostIsNotNull(
                                normalizedComputerName
                        );

        long tlsServerNameCount =
                packetMetadataRepository
                        .countByComputerNameAndTlsServerNameIsNotNull(
                                normalizedComputerName
                        );

        return new PacketMetadataSummaryResponse(
                normalizedComputerName,
                totalMetadataCount,
                externalCommunicationCount,
                dnsDomainCount,
                httpHostCount,
                tlsServerNameCount
        );
    }
}