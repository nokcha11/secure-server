package com.secureagent.server.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "AGENT_NETWORK_CONNECTION")
public class AgentNetworkConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "COMPUTER_NAME",
            nullable = false,
            length = 100
    )
    private String computerName;

    @Column(
            name = "PROTOCOL",
            nullable = false,
            length = 10
    )
    private String protocol;

    @Column(
            name = "LOCAL_ADDRESS",
            length = 45
    )
    private String localAddress;

    @Column(name = "LOCAL_PORT")
    private Integer localPort;

    @Column(
            name = "REMOTE_ADDRESS",
            length = 45
    )
    private String remoteAddress;

    @Column(name = "REMOTE_PORT")
    private Integer remotePort;

    @Column(
            name = "CONNECTION_STATE",
            length = 30
    )
    private String state;

    @Column(name = "PID")
    private Long pid;

    @Column(
            name = "PROCESS_NAME",
            length = 255
    )
    private String processName;

    /*
     * 이 연결을 처음 발견한 시각
     */
    @Column(
            name = "FIRST_SEEN_AT",
            nullable = false
    )
    private LocalDateTime firstSeenAt;

    /*
     * 이 연결을 가장 최근에 확인한 시각
     */
    @Column(
            name = "LAST_SEEN_AT",
            nullable = false
    )
    private LocalDateTime lastSeenAt;

    /*
     * 연결이 사라진 것을 확인한 시각
     */
    @Column(name = "ENDED_AT")
    private LocalDateTime endedAt;

    /*
     * 같은 연결이 다시 나타난 횟수
     */
    @Column(
            name = "CONNECTION_COUNT",
            nullable = false
    )
    private Integer connectionCount;

    /*
     * 현재 연결 여부
     * Y = 현재 연결
     * N = 종료된 연결
     */
    @Column(
            name = "ACTIVE_YN",
            nullable = false,
            length = 1
    )
    private String activeYn;

    /*
     * 외부 공인 IP 여부
     * Y = 외부 공인 IP
     * N = 내부·로컬 IP 또는 상대방 없음
     *
     * 기존 데이터가 있는 테이블에 안전하게
     * 열을 추가하기 위해 처음에는 nullable로 둡니다.
     */
    @Column(
            name = "EXTERNAL_YN",
            length = 1
    )
    private String externalYn;

    /*
     * 의심스러운 연결 여부
     * Y = 의심 연결
     * N = 현재 규칙상 정상
     */
    @Column(
            name = "SUSPICIOUS_YN",
            nullable = false,
            length = 1
    )
    private String suspiciousYn;

    /*
     * 의심 여부를 판정한 이유
     */
    @Column(
            name = "RISK_REASON",
            length = 1000
    )
    private String riskReason;

    @PrePersist
    public void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        if (firstSeenAt == null) {
            firstSeenAt = now;
        }

        if (lastSeenAt == null) {
            lastSeenAt = now;
        }

        if (connectionCount == null) {
            connectionCount = 1;
        }

        if (activeYn == null) {
            activeYn = "Y";
        }

        if (externalYn == null) {
            externalYn = "N";
        }

        if (suspiciousYn == null) {
            suspiciousYn = "N";
        }
    }

    public Long getId() {
        return id;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(
            String computerName) {

        this.computerName =
                computerName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(
            String protocol) {

        this.protocol =
                protocol;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(
            String localAddress) {

        this.localAddress =
                localAddress;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(
            Integer localPort) {

        this.localPort =
                localPort;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(
            String remoteAddress) {

        this.remoteAddress =
                remoteAddress;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(
            Integer remotePort) {

        this.remotePort =
                remotePort;
    }

    public String getState() {
        return state;
    }

    public void setState(
            String state) {

        this.state =
                state;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(
            Long pid) {

        this.pid =
                pid;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(
            String processName) {

        this.processName =
                processName;
    }

    public LocalDateTime getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(
            LocalDateTime firstSeenAt) {

        this.firstSeenAt =
                firstSeenAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(
            LocalDateTime lastSeenAt) {

        this.lastSeenAt =
                lastSeenAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(
            LocalDateTime endedAt) {

        this.endedAt =
                endedAt;
    }

    public Integer getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(
            Integer connectionCount) {

        this.connectionCount =
                connectionCount;
    }

    public String getActiveYn() {
        return activeYn;
    }

    public void setActiveYn(
            String activeYn) {

        this.activeYn =
                activeYn;
    }

    public String getExternalYn() {
        return externalYn;
    }

    public void setExternalYn(
            String externalYn) {

        this.externalYn =
                externalYn;
    }

    public String getSuspiciousYn() {
        return suspiciousYn;
    }

    public void setSuspiciousYn(
            String suspiciousYn) {

        this.suspiciousYn =
                suspiciousYn;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(
            String riskReason) {

        this.riskReason =
                riskReason;
    }
}