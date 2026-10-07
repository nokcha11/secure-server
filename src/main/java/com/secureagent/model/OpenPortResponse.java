package com.secureagent.model;

import java.time.LocalDateTime;

/*
 * 열린 포트 정보와 위험 분석 결과를
 * 대시보드에 전달하는 응답 객체입니다.
 */
public record OpenPortResponse(

        Long id,

        String computerName,

        String protocol,

        String localAddress,

        Integer localPort,

        String state,

        Long pid,

        String processName,

        LocalDateTime receivedAt,

        String serviceName,

        String riskLevel,

        String riskReason

) {
}