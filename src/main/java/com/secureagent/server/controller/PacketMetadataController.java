package com.secureagent.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.server.dto.PacketMetadataRequest;
import com.secureagent.server.dto.PacketMetadataSummaryResponse;
import com.secureagent.server.entity.AgentPacketMetadata;
import com.secureagent.server.service.PacketMetadataService;

@RestController
@RequestMapping("/api/agents")
public class PacketMetadataController {

    private final PacketMetadataService
            packetMetadataService;

    /*
     * Service 인터페이스를 생성자 주입으로 연결합니다.
     */
    public PacketMetadataController(
            PacketMetadataService
                    packetMetadataService) {

        this.packetMetadataService =
                packetMetadataService;
    }

    /*
     * STS3 에이전트가 수집한 패킷 메타데이터
     * 목록을 받아 Oracle DB에 저장합니다.
     */
    @PostMapping(
            "/{computerName}/packet-metadata"
    )
    public ResponseEntity<String>
            savePacketMetadata(
                    @PathVariable("computerName")
                    String computerName,

                    @RequestBody
                    List<PacketMetadataRequest>
                            requestList) {

        packetMetadataService
                .savePacketMetadata(
                        computerName,
                        requestList
                );

        return ResponseEntity.ok(
                "패킷 메타데이터 목록 저장 완료"
        );
    }

    /*
     * 선택한 PC의 최근 패킷 메타데이터를
     * 최대 500개까지 조회합니다.
     */
    @GetMapping(
            "/{computerName}/packet-metadata"
    )
    public ResponseEntity<
            List<AgentPacketMetadata>>
            getRecentPacketMetadata(
                    @PathVariable("computerName")
                    String computerName) {

        List<AgentPacketMetadata> result =
                packetMetadataService
                        .getRecentPacketMetadata(
                                computerName
                        );

        return ResponseEntity.ok(result);
    }

    /*
     * 선택한 PC의 외부 통신 메타데이터만
     * 최대 500개까지 조회합니다.
     */
    @GetMapping(
            "/{computerName}/packet-metadata/external"
    )
    public ResponseEntity<
            List<AgentPacketMetadata>>
            getRecentExternalPacketMetadata(
                    @PathVariable("computerName")
                    String computerName) {

        List<AgentPacketMetadata> result =
                packetMetadataService
                        .getRecentExternalPacketMetadata(
                                computerName
                        );

        return ResponseEntity.ok(result);
    }

    /*
     * 선택한 PC에 저장된 패킷 메타데이터의
     * 전체 개수를 조회합니다.
     */
    @GetMapping(
            "/{computerName}/packet-metadata/count"
    )
    public ResponseEntity<Long>
            countPacketMetadata(
                    @PathVariable("computerName")
                    String computerName) {

        long count =
                packetMetadataService
                        .countPacketMetadata(
                                computerName
                        );

        return ResponseEntity.ok(count);
    }

    /*
     * 선택한 PC의 패킷·도메인 요약 정보를
     * 대시보드에 전달합니다.
     */
    @GetMapping(
            "/{computerName}/packet-metadata/summary"
    )
    public ResponseEntity<
            PacketMetadataSummaryResponse>
            getPacketMetadataSummary(
                    @PathVariable("computerName")
                    String computerName) {

        PacketMetadataSummaryResponse result =
                packetMetadataService
                        .getPacketMetadataSummary(
                                computerName
                        );

        return ResponseEntity.ok(result);
    }
}