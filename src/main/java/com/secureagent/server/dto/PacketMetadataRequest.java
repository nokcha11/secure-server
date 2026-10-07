package com.secureagent.server.dto;

import java.time.LocalDateTime;

public class PacketMetadataRequest {

    private String protocol;

    private String applicationProtocol;

    private String direction;

    private String localAddress;

    private int localPort;

    private String remoteAddress;

    private int remotePort;

    private long packetCount;

    private long totalBytes;

    private LocalDateTime firstSeenAt;

    private LocalDateTime lastSeenAt;

    private String externalYn;

    private String dnsDomain;

    private String httpHost;

    /*
     * HTTPS TLS ClientHello에서 확인한
     * SNI 서버 도메인입니다.
     */
    private String tlsServerName;

    public PacketMetadataRequest() {
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
}
