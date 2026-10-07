package com.secureagent.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.model.NetworkConnectionRequest;
import com.secureagent.server.entity.AgentNetworkConnection;
import com.secureagent.server.service.NetworkConnectionService;

@RestController
@RequestMapping("/api/agents")
public class NetworkConnectionController {

    private final NetworkConnectionService
            networkConnectionService;

    public NetworkConnectionController(
            NetworkConnectionService
                    networkConnectionService) {

        this.networkConnectionService =
                networkConnectionService;
    }

    /*
     * 에이전트가 수집한 현재 네트워크 연결 목록을 받습니다.
     */
    @PostMapping(
            "/{computerName}/network-connections"
    )
    public ResponseEntity<String> updateConnections(
            @PathVariable String computerName,
            @RequestBody
            List<NetworkConnectionRequest>
                    connectionRequests) {

        networkConnectionService.updateConnections(
                computerName,
                connectionRequests
        );

        return ResponseEntity.ok(
                "네트워크 연결 목록 저장 완료"
        );
    }

    /*
     * 특정 PC의 현재 활성 연결 목록을 조회합니다.
     */
    @GetMapping(
            "/{computerName}/network-connections"
    )
    public ResponseEntity<
            List<AgentNetworkConnection>>
            getActiveConnections(
                    @PathVariable
                    String computerName) {

        List<AgentNetworkConnection> connections =
                networkConnectionService
                        .getActiveConnections(
                                computerName
                        );

        return ResponseEntity.ok(
                connections
        );
    }

    /*
     * 특정 PC의 전체 연결 이력을 조회합니다.
     */
    @GetMapping(
            "/{computerName}/network-connections/history"
    )
    public ResponseEntity<
            List<AgentNetworkConnection>>
            getConnectionHistory(
                    @PathVariable
                    String computerName) {

        List<AgentNetworkConnection> history =
                networkConnectionService
                        .getConnectionHistory(
                                computerName
                        );

        return ResponseEntity.ok(
                history
        );
    }

    /*
     * 특정 PC의 의심 연결만 조회합니다.
     */
    @GetMapping(
            "/{computerName}/network-connections/suspicious"
    )
    public ResponseEntity<
            List<AgentNetworkConnection>>
            getSuspiciousConnections(
                    @PathVariable
                    String computerName) {

        List<AgentNetworkConnection> connections =
                networkConnectionService
                        .getSuspiciousConnections(
                                computerName
                        );

        return ResponseEntity.ok(
                connections
        );
    }
}