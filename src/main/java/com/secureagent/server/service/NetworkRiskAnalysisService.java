package com.secureagent.server.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class NetworkRiskAnalysisService {

    /*
     * 외부 연결 시 특히 확인해야 하는 위험 포트
     */
    private static final Set<Integer>
            DANGER_REMOTE_PORTS = Set.of(
                    21,     // FTP
                    23,     // Telnet
                    445,    // SMB
                    3389,   // RDP
                    5900    // VNC
            );

    /*
     * 외부 연결 시 주의가 필요한
     * 데이터베이스 및 관리 포트
     */
    private static final Set<Integer>
            CAUTION_REMOTE_PORTS = Set.of(
                    135,
                    139,
                    1433,
                    1521,
                    3306,
                    5432,
                    6379,
                    8080,
                    8081,
                    27017
            );

    /*
     * 상대방 IP와 포트, 연결 상태, 프로세스를
     * 이용하여 의심 여부를 판단합니다.
     */
    public NetworkRiskResult analyze(
            String remoteAddress,
            Integer remotePort,
            String state,
            String processName,
            Integer connectionCount) {

        /*
         * LISTENING 또는 UDP처럼
         * 실제 상대방이 없는 경우
         */
        if (hasNoRemoteEndpoint(
                remoteAddress,
                remotePort)) {

            return new NetworkRiskResult(
                    "N",
                    "N",
                    "실제 상대방 연결이 없는 대기 또는 UDP 상태입니다."
            );
        }

        InetAddress address =
                parseAddress(remoteAddress);

        /*
         * IP 형식을 해석하지 못한 경우
         */
        if (address == null) {
            return new NetworkRiskResult(
                    "N",
                    "Y",
                    "상대방 IP 주소 형식을 확인할 수 없습니다."
            );
        }

        /*
         * 내 PC, 내부망, 사설 IP
         */
        if (isInternalAddress(address)) {
            return new NetworkRiskResult(
                    "N",
                    "N",
                    "로컬 또는 내부 네트워크 연결입니다."
            );
        }

        /*
         * 여기부터는 외부 공인 IP
         */
        if (DANGER_REMOTE_PORTS.contains(
                remotePort)) {

            return new NetworkRiskResult(
                    "Y",
                    "Y",
                    "외부 IP가 위험 원격 접속 또는 파일 공유 포트를 사용합니다."
            );
        }

        if (CAUTION_REMOTE_PORTS.contains(
                remotePort)) {

            return new NetworkRiskResult(
                    "Y",
                    "Y",
                    "외부 IP가 관리 또는 데이터베이스 포트를 사용합니다."
            );
        }

        /*
         * 실제 연결 중인데 프로세스를
         * 확인할 수 없는 경우
         */
        boolean established =
                "ESTABLISHED".equalsIgnoreCase(
                        state
                );

        boolean unknownProcess =
                processName == null ||
                processName.isBlank() ||
                "알 수 없음".equals(processName);

        if (established && unknownProcess) {
            return new NetworkRiskResult(
                    "Y",
                    "Y",
                    "외부 연결을 사용하는 프로세스를 확인할 수 없습니다."
            );
        }

        /*
         * 같은 연결이 반복적으로 다시 나타난 경우
         */
        if (connectionCount != null &&
                connectionCount >= 10) {

            return new NetworkRiskResult(
                    "Y",
                    "Y",
                    "같은 외부 연결이 반복적으로 발생했습니다."
            );
        }

        /*
         * 일반적인 외부 연결
         *
         * 외부 IP라는 이유만으로 위험 처리하지 않습니다.
         */
        return new NetworkRiskResult(
                "Y",
                "N",
                "외부 공인 IP 연결이지만 현재 의심 규칙에는 해당하지 않습니다."
        );
    }

    /*
     * 실제 상대방이 없는 주소인지 확인합니다.
     */
    private boolean hasNoRemoteEndpoint(
            String remoteAddress,
            Integer remotePort) {

        if (remoteAddress == null ||
                remoteAddress.isBlank()) {

            return true;
        }

        String address =
                remoteAddress.trim();

        return "*".equals(address) ||
                "0.0.0.0".equals(address) ||
                "::".equals(address) ||
                remotePort == null ||
                remotePort == 0;
    }

    /*
     * 문자열 IP를 InetAddress로 변환합니다.
     */
    private InetAddress parseAddress(
            String remoteAddress) {

        if (remoteAddress == null) {
            return null;
        }

        String address =
                remoteAddress.trim();

        /*
         * IPv6 영역 번호 제거
         *
         * 예: fe80::1234%7
         */
        int scopeIndex =
                address.indexOf('%');

        if (scopeIndex >= 0) {
            address =
                    address.substring(
                            0,
                            scopeIndex
                    );
        }

        /*
         * IPv6 대괄호 제거
         *
         * 예: [::1]
         */
        if (address.startsWith("[") &&
                address.endsWith("]")) {

            address =
                    address.substring(
                            1,
                            address.length() - 1
                    );
        }

        try {
            return InetAddress.getByName(
                    address
            );

        } catch (UnknownHostException e) {
            return null;
        }
    }

    /*
     * 로컬·사설·링크 로컬·멀티캐스트 주소를
     * 내부 연결로 분류합니다.
     */
    private boolean isInternalAddress(
            InetAddress address) {

        if (address.isAnyLocalAddress() ||
                address.isLoopbackAddress() ||
                address.isSiteLocalAddress() ||
                address.isLinkLocalAddress() ||
                address.isMulticastAddress()) {

            return true;
        }

        byte[] addressBytes =
                address.getAddress();

        /*
         * IPv6 Unique Local Address
         * fc00::/7
         */
        if (addressBytes.length == 16) {
            int firstByte =
                    addressBytes[0] & 0xFF;

            if ((firstByte & 0xFE) == 0xFC) {
                return true;
            }
        }

        /*
         * IPv4 CGNAT 주소
         * 100.64.0.0/10
         */
        if (addressBytes.length == 4) {
            int firstByte =
                    addressBytes[0] & 0xFF;

            int secondByte =
                    addressBytes[1] & 0xFF;

            if (firstByte == 100 &&
                    secondByte >= 64 &&
                    secondByte <= 127) {

                return true;
            }
        }

        return false;
    }

    /*
     * externalYn:
     * Y = 외부 공인 IP
     * N = 로컬·내부 IP 또는 상대방 없음
     *
     * suspiciousYn:
     * Y = 의심 연결
     * N = 현재 규칙상 정상
     */
    public record NetworkRiskResult(
            String externalYn,
            String suspiciousYn,
            String riskReason) {
    }
}