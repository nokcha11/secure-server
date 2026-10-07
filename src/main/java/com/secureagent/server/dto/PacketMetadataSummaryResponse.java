package com.secureagent.server.dto;

public class PacketMetadataSummaryResponse {

    /*
     * 조회한 PC 이름
     */
    private String computerName;

    /*
     * DB에 저장된 전체 패킷 메타데이터 수
     */
    private long totalMetadataCount;

    /*
     * 외부 공인 IP와 통신한 메타데이터 수
     */
    private long externalCommunicationCount;

    /*
     * DNS 도메인이 확인된 메타데이터 수
     */
    private long dnsDomainCount;

    /*
     * 일반 HTTP Host가 확인된 메타데이터 수
     */
    private long httpHostCount;

    /*
     * HTTPS TLS SNI가 확인된 메타데이터 수
     */
    private long tlsServerNameCount;

    public PacketMetadataSummaryResponse() {
    }

    public PacketMetadataSummaryResponse(
            String computerName,
            long totalMetadataCount,
            long externalCommunicationCount,
            long dnsDomainCount,
            long httpHostCount,
            long tlsServerNameCount) {

        this.computerName = computerName;
        this.totalMetadataCount =
                totalMetadataCount;
        this.externalCommunicationCount =
                externalCommunicationCount;
        this.dnsDomainCount =
                dnsDomainCount;
        this.httpHostCount =
                httpHostCount;
        this.tlsServerNameCount =
                tlsServerNameCount;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(
            String computerName) {

        this.computerName = computerName;
    }

    public long getTotalMetadataCount() {
        return totalMetadataCount;
    }

    public void setTotalMetadataCount(
            long totalMetadataCount) {

        this.totalMetadataCount =
                totalMetadataCount;
    }

    public long getExternalCommunicationCount() {
        return externalCommunicationCount;
    }

    public void setExternalCommunicationCount(
            long externalCommunicationCount) {

        this.externalCommunicationCount =
                externalCommunicationCount;
    }

    public long getDnsDomainCount() {
        return dnsDomainCount;
    }

    public void setDnsDomainCount(
            long dnsDomainCount) {

        this.dnsDomainCount = dnsDomainCount;
    }

    public long getHttpHostCount() {
        return httpHostCount;
    }

    public void setHttpHostCount(
            long httpHostCount) {

        this.httpHostCount = httpHostCount;
    }

    public long getTlsServerNameCount() {
        return tlsServerNameCount;
    }

    public void setTlsServerNameCount(
            long tlsServerNameCount) {

        this.tlsServerNameCount =
                tlsServerNameCount;
    }
}