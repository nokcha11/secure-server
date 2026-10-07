package com.secureagent.server.dto;

/*
 * ADMIN이 원격 진단을 요청할 때
 * 서버로 전달하는 정보입니다.
 */
public class RemoteDiagnosticRequest {

    /*
     * SSH로 접속할 대상 PC의
     * IP 주소 또는 호스트 이름입니다.
     */
    private String host;

    /*
     * SSH 접속 포트입니다.
     * 기본 SSH 포트는 22번입니다.
     */
    private int port = 22;

    /*
     * 대상 Windows PC에서 사용할
     * SSH 사용자 이름입니다.
     */
    private String username;

    /*
     * 실행할 안전한 진단 명령 종류입니다.
     */
    private RemoteDiagnosticCommand commandType;

    public String getHost() {

        return host;
    }

    public void setHost(
            String host) {

        this.host = host;
    }

    public int getPort() {

        return port;
    }

    public void setPort(
            int port) {

        this.port = port;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public RemoteDiagnosticCommand getCommandType() {

        return commandType;
    }

    public void setCommandType(
            RemoteDiagnosticCommand commandType) {

        this.commandType = commandType;
    }
}