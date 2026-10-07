package com.secureagent.server.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class PortRiskAnalysisService {

    /*
     * 포트 번호를 사람이 이해하기 쉬운 서비스 이름으로 변환합니다.
     */
    private static final Map<Integer, String> SERVICE_NAMES = Map.ofEntries(
            Map.entry(21, "FTP"),
            Map.entry(22, "SSH"),
            Map.entry(23, "TELNET"),
            Map.entry(25, "SMTP"),
            Map.entry(53, "DNS"),
            Map.entry(80, "HTTP"),
            Map.entry(110, "POP3"),
            Map.entry(135, "RPC"),
            Map.entry(137, "NETBIOS"),
            Map.entry(138, "NETBIOS"),
            Map.entry(139, "NETBIOS"),
            Map.entry(143, "IMAP"),
            Map.entry(443, "HTTPS"),
            Map.entry(445, "SMB"),
            Map.entry(1433, "MSSQL"),
            Map.entry(1521, "ORACLE"),
            Map.entry(3306, "MYSQL"),
            Map.entry(3389, "RDP"),
            Map.entry(5432, "POSTGRESQL"),
            Map.entry(5900, "VNC"),
            Map.entry(6379, "REDIS"),
            Map.entry(8080, "HTTP-ALT"),
            Map.entry(8081, "HTTP-ALT"),
            Map.entry(27017, "MONGODB")
    );

    /*
     * 서비스 포트에서 일반적으로 사용되는 프로세스 이름입니다.
     * 알려진 프로세스와 다르면 의심 프로세스로 판정합니다.
     */
    private static final Map<Integer, Set<String>> EXPECTED_PROCESSES =
            Map.ofEntries(
                    Map.entry(22, Set.of("sshd.exe", "sshd")),
                    Map.entry(1521, Set.of("tnslsnr.exe", "tnslsnr")),
                    Map.entry(3306, Set.of("mysqld.exe", "mysqld")),
                    Map.entry(5432, Set.of("postgres.exe", "postgres")),
                    Map.entry(6379, Set.of(
                            "redis-server.exe",
                            "redis-server"
                    )),
                    Map.entry(27017, Set.of("mongod.exe", "mongod"))
            );

    /*
     * 암호화되지 않은 원격 접속 또는 파일 전송 포트입니다.
     */
    private static final Set<Integer> DANGER_PORTS =
            Set.of(21, 23);

    /*
     * 외부 노출 시 점검이 필요한 원격 접속, 공유, DB, 웹 포트입니다.
     */
    private static final Set<Integer> CAUTION_PORTS = Set.of(
            22, 25, 53, 80, 110, 135, 137, 138, 139, 143,
            445, 1433, 1521, 3306, 3389, 5432, 5900,
            6379, 8080, 8081, 27017
    );

    /**
     * 포트 번호와 프로세스 이름을 이용해 서비스와 위험도를 판정합니다.
     */
    public PortRiskResult analyze(
            Integer localPort,
            String processName) {

        if (localPort == null) {
            return new PortRiskResult(
                    "알 수 없음",
                    "NORMAL",
                    "포트 정보가 없습니다."
            );
        }

        String serviceName = SERVICE_NAMES.getOrDefault(
                localPort,
                "알 수 없음"
        );

        PortRiskResult processRisk = analyzeProcess(
                localPort,
                processName,
                serviceName
        );

        if (processRisk != null) {
            return processRisk;
        }

        if (DANGER_PORTS.contains(localPort)) {
            return new PortRiskResult(
                    serviceName,
                    "DANGER",
                    createDangerReason(localPort)
            );
        }

        if (CAUTION_PORTS.contains(localPort)) {
            return new PortRiskResult(
                    serviceName,
                    "CAUTION",
                    createCautionReason(localPort)
            );
        }

        return new PortRiskResult(
                serviceName,
                "NORMAL",
                "현재 기본 위험 규칙에 해당하지 않습니다."
        );
    }

    /**
     * 알려진 서비스 포트와 실제 프로세스가 일치하는지 검사합니다.
     */
    private PortRiskResult analyzeProcess(
            Integer localPort,
            String processName,
            String serviceName) {

        Set<String> expectedProcesses =
                EXPECTED_PROCESSES.get(localPort);

        if (expectedProcesses == null) {
            return null;
        }

        if (processName == null || processName.isBlank()) {
            return new PortRiskResult(
                    serviceName,
                    "CAUTION",
                    "프로세스 정보를 확인할 수 없습니다."
            );
        }

        String normalizedProcessName =
                processName.trim().toLowerCase();

        boolean expectedProcess = expectedProcesses.stream()
                .anyMatch(normalizedProcessName::endsWith);

        if (!expectedProcess) {
            return new PortRiskResult(
                    serviceName,
                    "DANGER",
                    serviceName + " 포트를 예상하지 못한 프로세스가 사용 중입니다: "
                            + processName
            );
        }

        return null;
    }

    private String createDangerReason(Integer localPort) {
        return switch (localPort) {
            case 21 -> "FTP는 계정과 데이터가 평문으로 전송될 수 있습니다.";
            case 23 -> "Telnet은 통신 내용이 암호화되지 않습니다.";
            default -> "위험 포트가 열려 있습니다.";
        };
    }

    private String createCautionReason(Integer localPort) {
        return switch (localPort) {
            case 22 -> "SSH 원격 접속 포트입니다. 접근 허용 대상을 확인하세요.";
            case 80, 8080, 8081 ->
                    "암호화되지 않은 웹 서비스이거나 관리용 포트일 수 있습니다.";
            case 135, 137, 138, 139, 445 ->
                    "Windows 원격 통신 또는 파일 공유 포트입니다.";
            case 1433, 1521, 3306, 5432, 6379, 27017 ->
                    "데이터베이스 포트입니다. 외부 노출 여부를 확인하세요.";
            case 3389, 5900 ->
                    "원격 제어 포트입니다. 허용된 사용자만 접근해야 합니다.";
            default -> "외부 노출 여부와 사용 목적을 확인하세요.";
        };
    }

    /**
     * API와 대시보드에 전달할 위험 판정 결과입니다.
     */
    public record PortRiskResult(
            String serviceName,
            String riskLevel,
            String riskReason) {
    }
}
