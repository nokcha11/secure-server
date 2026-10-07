package com.secureagent.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.model.OpenPortRequest;
import com.secureagent.model.OpenPortResponse;
import com.secureagent.server.service.OpenPortService;

@RestController
@RequestMapping("/api/agents")
public class OpenPortController {

    private final OpenPortService openPortService;


    public OpenPortController(
            OpenPortService openPortService) {

        this.openPortService = openPortService;
    }


    /*
     * 에이전트가 수집한 현재 열린 포트 목록을
     * DB에 저장합니다.
     */
    @PostMapping("/{computerName}/open-ports")
    public ResponseEntity<String> replaceOpenPorts(
            @PathVariable("computerName")
            String computerName,

            @RequestBody
            List<OpenPortRequest> portRequests) {

        openPortService.replaceOpenPorts(
                computerName,
                portRequests
        );

        return ResponseEntity.ok(
                "열린 포트 목록 저장 완료"
        );
    }


    /*
     * 특정 PC의 열린 포트와
     * 위험 분석 결과를 함께 조회합니다.
     */
    @GetMapping("/{computerName}/open-ports")
    public ResponseEntity<List<OpenPortResponse>>
            getOpenPorts(

            @PathVariable("computerName")
            String computerName) {

        List<OpenPortResponse> openPorts =
                openPortService.getAnalyzedOpenPorts(
                        computerName
                );

        return ResponseEntity.ok(openPorts);
    }
}