package com.secureagent.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortRiskAnalysisServiceTest {

    private PortRiskAnalysisService riskAnalysisService;

    @BeforeEach
    void setUp() {
        riskAnalysisService =
                new PortRiskAnalysisService();
    }

    /*
     * 21번 FTP 포트는 위험으로 판정되어야 합니다.
     */
    @Test
    void ftpPortShouldBeDanger() {
        var result =
                riskAnalysisService.analyze(
                        21,
                        "ftp-server.exe"
                );

        assertEquals(
                "FTP",
                result.serviceName()
        );

        assertEquals(
                "DANGER",
                result.riskLevel()
        );

        assertEquals(
                "FTP는 계정과 데이터가 평문으로 전송될 수 있습니다.",
                result.riskReason()
        );
    }

    /*
     * 23번 Telnet 포트는 위험으로 판정되어야 합니다.
     */
    @Test
    void telnetPortShouldBeDanger() {
        var result =
                riskAnalysisService.analyze(
                        23,
                        "telnet.exe"
                );

        assertEquals(
                "TELNET",
                result.serviceName()
        );

        assertEquals(
                "DANGER",
                result.riskLevel()
        );

        assertEquals(
                "Telnet은 통신 내용이 암호화되지 않습니다.",
                result.riskReason()
        );
    }

    /*
     * 80번 HTTP 포트는 주의로 판정되어야 합니다.
     */
    @Test
    void httpPortShouldBeCaution() {
        var result =
                riskAnalysisService.analyze(
                        80,
                        "httpd.exe"
                );

        assertEquals(
                "HTTP",
                result.serviceName()
        );

        assertEquals(
                "CAUTION",
                result.riskLevel()
        );

        assertEquals(
                "암호화되지 않은 웹 서비스이거나 관리용 포트일 수 있습니다.",
                result.riskReason()
        );
    }

    /*
     * 123번 포트는 현재 규칙에서 정상으로 판정되어야 합니다.
     */
    @Test
    void port123ShouldBeNormal() {
        var result =
                riskAnalysisService.analyze(
                        123,
                        "svchost.exe"
                );

        assertEquals(
                "알 수 없음",
                result.serviceName()
        );

        assertEquals(
                "NORMAL",
                result.riskLevel()
        );

        assertEquals(
                "현재 기본 위험 규칙에 해당하지 않습니다.",
                result.riskReason()
        );
    }
}