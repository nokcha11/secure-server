package com.secureagent.server.dto;

import java.time.LocalDateTime;

/*
 * 원격 진단 실행 결과를
 * 관리자 화면에 반환하는 응답 정보입니다.
 */
public class RemoteDiagnosticResponse {

    /*
     * 원격 진단 성공 여부입니다.
     */
    private boolean success;

    /*
     * 진단 대상 PC의 IP 주소 또는
     * 호스트 이름입니다.
     */
    private String host;

    /*
     * SSH 접속 포트입니다.
     */
    private int port;

    /*
     * SSH 접속에 사용한 사용자 이름입니다.
     */
    private String username;

    /*
     * 실행한 진단 명령의 종류입니다.
     */
    private RemoteDiagnosticCommand commandType;

    /*
     * 원격 명령 실행 결과입니다.
     */
    private String output;

    /*
     * 성공 또는 실패 설명입니다.
     */
    private String message;

    /*
     * 원격 진단을 실행한 시각입니다.
     */
    private LocalDateTime executedAt;

    public boolean isSuccess() {

        return success;
    }

    public void setSuccess(
            boolean success) {

        this.success = success;
    }

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

    public String getOutput() {

        return output;
    }

    public void setOutput(
            String output) {

        this.output = output;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }

    public LocalDateTime getExecutedAt() {

        return executedAt;
    }

    public void setExecutedAt(
            LocalDateTime executedAt) {

        this.executedAt = executedAt;
    }
}