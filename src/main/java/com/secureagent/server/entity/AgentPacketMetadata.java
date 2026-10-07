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

@Entity
@Table(name = "AGENT_PACKET_METADATA")
public class AgentPacketMetadata {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "agent_packet_metadata_seq_generator"
    )
    @SequenceGenerator(
            name = "agent_packet_metadata_seq_generator",
            sequenceName = "AGENT_PACKET_METADATA_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID")
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
            length = 20
    )
    private String protocol;

    @Column(
            name = "APPLICATION_PROTOCOL",
            length = 30
    )
    private String applicationProtocol;

    @Column(
            name = "DIRECTION",
            nullable = false,
            length = 20
    )
    private String direction;

    @Column(
            name = "LOCAL_ADDRESS",
            nullable = false,
            length = 64
    )
    private String localAddress;

    @Column(name = "LOCAL_PORT")
    private int localPort;

    @Column(
            name = "REMOTE_ADDRESS",
            nullable = false,
            length = 64
    )
    private String remoteAddress;

    @Column(name = "REMOTE_PORT")
    private int remotePort;

    @Column(
            name = "PACKET_COUNT",
            nullable = false
    )
    private long packetCount;

    @Column(
            name = "TOTAL_BYTES",
            nullable = false
    )
    private long totalBytes;

    @Column(
            name = "FIRST_SEEN_AT",
            nullable = false
    )
    private LocalDateTime firstSeenAt;

    @Column(
            name = "LAST_SEEN_AT",
            nullable = false
    )
    private LocalDateTime lastSeenAt;

    @Column(
            name = "EXTERNAL_YN",
            nullable = false,
            length = 1
    )
    private String externalYn;

    @Column(
            name = "DNS_DOMAIN",
            length = 255
    )
    private String dnsDomain;

    @Column(
            name = "HTTP_HOST",
            length = 255
    )
    private String httpHost;

    /*
     * HTTPS TLS ClientHello에서 확인한
     * SNI 서버 도메인입니다.
     */
    @Column(
            name = "TLS_SERVER_NAME",
            length = 255
    )
    private String tlsServerName;

    @Column(
            name = "RECEIVED_AT",
            nullable = false
    )
    private LocalDateTime receivedAt;

    public AgentPacketMetadata() {
    }

    @PrePersist
    public void prePersist() {

        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(
            String computerName) {

        this.computerName = computerName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getApplicationProtocol() {
        return applicationProtocol;
    }

    public void setApplicationProtocol(
            String applicationProtocol) {

        this.applicationProtocol =
                applicationProtocol;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(
            String localAddress) {

        this.localAddress = localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(
            String remoteAddress) {

        this.remoteAddress = remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public long getPacketCount() {
        return packetCount;
    }

    public void setPacketCount(
            long packetCount) {

        this.packetCount = packetCount;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(
            long totalBytes) {

        this.totalBytes = totalBytes;
    }

    public LocalDateTime getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(
            LocalDateTime firstSeenAt) {

        this.firstSeenAt = firstSeenAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(
            LocalDateTime lastSeenAt) {

        this.lastSeenAt = lastSeenAt;
    }

    public String getExternalYn() {
        return externalYn;
    }

    public void setExternalYn(
            String externalYn) {

        this.externalYn = externalYn;
    }

    public String getDnsDomain() {
        return dnsDomain;
    }

    public void setDnsDomain(
            String dnsDomain) {

        this.dnsDomain = dnsDomain;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(
            String httpHost) {

        this.httpHost = httpHost;
    }

    public String getTlsServerName() {
        return tlsServerName;
    }

    public void setTlsServerName(
            String tlsServerName) {

        this.tlsServerName =
                tlsServerName;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(
            LocalDateTime receivedAt) {

        this.receivedAt = receivedAt;
    }
}
