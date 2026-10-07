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
@Table(name = "AGENT_OPEN_PORT")
public class AgentOpenPort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COMPUTER_NAME", nullable = false, length = 100)
    private String computerName;

    @Column(name = "PROTOCOL", nullable = false, length = 10)
    private String protocol;

    @Column(name = "LOCAL_ADDRESS", length = 45)
    private String localAddress;

    @Column(name = "LOCAL_PORT", nullable = false)
    private Integer localPort;

    @Column(name = "PORT_STATE", length = 30)
    private String state;

    @Column(name = "PID")
    private Long pid;

    @Column(name = "PROCESS_NAME", length = 255)
    private String processName;

    @Column(name = "RECEIVED_AT", nullable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    public void prePersist() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}