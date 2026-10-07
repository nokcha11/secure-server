package com.secureagent.server.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "AGENT_SYSTEM_INFO")
public class AgentSystemInfo {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "agentSystemInfoSeq"
    )
    @SequenceGenerator(
        name = "agentSystemInfoSeq",
        sequenceName = "SEQ_AGENT_SYSTEM_INFO",
        allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "COMPUTER_NAME", nullable = false, length = 100)
    private String computerName;

    @Column(name = "OS_NAME", nullable = false, length = 100)
    private String osName;

    @Column(name = "OS_VERSION", nullable = false, length = 100)
    private String osVersion;

    @Column(name = "USER_NAME", nullable = false, length = 100)
    private String userName;

    @Column(name = "RECEIVED_AT", nullable = false)
    private LocalDateTime receivedAt;

    public AgentSystemInfo() {
    }

    public AgentSystemInfo(
            String computerName,
            String osName,
            String osVersion,
            String userName) {

        this.computerName = computerName;
        this.osName = osName;
        this.osVersion = osVersion;
        this.userName = userName;
    }

    @PrePersist
    @PreUpdate
    protected void onCreate() {
        this.receivedAt = LocalDateTime.now();
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

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

	public void setReceivedAt(LocalDateTime receivedAt) {
	    this.receivedAt = receivedAt;

	}
}