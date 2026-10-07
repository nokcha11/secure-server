package com.secureagent.server.service;

import java.util.List;

import com.secureagent.server.dto.PacketMetadataRequest;
import com.secureagent.server.dto.PacketMetadataSummaryResponse;
import com.secureagent.server.entity.AgentPacketMetadata;

public interface PacketMetadataService {

    /*
     * 에이전트가 보낸 패킷 메타데이터 목록을
     * Oracle DB에 저장합니다.
     */
    void savePacketMetadata(
            String computerName,
            List<PacketMetadataRequest> requestList
    );

    /*
     * 선택한 PC의 최근 패킷 메타데이터를
     * 최대 500개까지 조회합니다.
     */
    List<AgentPacketMetadata>
            getRecentPacketMetadata(
                    String computerName
            );

    /*
     * 선택한 PC의 외부 통신 메타데이터만
     * 최대 500개까지 조회합니다.
     */
    List<AgentPacketMetadata>
            getRecentExternalPacketMetadata(
                    String computerName
            );

    /*
     * 선택한 PC에 저장된 전체
     * 패킷 메타데이터 개수를 조회합니다.
     */
    long countPacketMetadata(
            String computerName
    );

    /*
     * 대시보드에 표시할 패킷·도메인
     * 요약 정보를 조회합니다.
     */
    PacketMetadataSummaryResponse
            getPacketMetadataSummary(
                    String computerName
            );
}