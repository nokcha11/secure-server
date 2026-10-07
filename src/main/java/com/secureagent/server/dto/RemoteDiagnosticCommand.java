package com.secureagent.server.dto;

/*
 * 원격 PC에서 실행할 수 있는
 * 안전한 진단 명령 목록입니다.
 *
 * 사용자가 명령어 원문을 직접 입력하지 않고
 * 아래에 등록된 명령만 선택할 수 있습니다.
 */
public enum RemoteDiagnosticCommand {

    /*
     * 대상 PC의 이름을 확인합니다.
     */
    HOSTNAME(
            "hostname",
            "PC 이름 확인"
    ),

    /*
     * SSH로 접속한 Windows 사용자 계정을
     * 확인합니다.
     */
    CURRENT_USER(
            "whoami",
            "현재 사용자 확인"
    ),

    /*
     * 대상 PC의 네트워크 어댑터와
     * IP 정보를 확인합니다.
     */
    IP_CONFIGURATION(
            "ipconfig",
            "IP 구성 확인"
    ),

    /*
     * 대상 PC의 네트워크 연결과
     * 사용 중인 포트를 확인합니다.
     */
    NETWORK_CONNECTIONS(
            "netstat -ano",
            "네트워크 연결 확인"
    );

    /*
     * 실제 SSH에서 실행할
     * 고정된 명령어입니다.
     */
    private final String commandText;

    /*
     * 화면에 표시할 진단 항목 설명입니다.
     */
    private final String displayName;

    RemoteDiagnosticCommand(
            String commandText,
            String displayName) {

        this.commandText = commandText;
        this.displayName = displayName;
    }

    public String getCommandText() {

        return commandText;
    }

    public String getDisplayName() {

        return displayName;
    }
}