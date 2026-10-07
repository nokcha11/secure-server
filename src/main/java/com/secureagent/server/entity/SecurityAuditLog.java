package com.secureagent.server.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/*
 * 로그인, 비밀번호 변경, 계정 잠금 등
 * 보안상 중요한 행동을 기록합니다.
 */
@Entity
@Table(
        name = "SECURITY_AUDIT_LOG",
        indexes = {
                @Index(
                        name = "IDX_AUDIT_CREATED_AT",
                        columnList = "CREATED_AT"
                ),
                @Index(
                        name = "IDX_AUDIT_ACTOR",
                        columnList = "ACTOR_USERNAME"
                )
        }
)
public class SecurityAuditLog {

    /*
     * 감사 기록 기본키입니다.
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "securityAuditLogSequence"
    )
    @SequenceGenerator(
            name = "securityAuditLogSequence",
            sequenceName = "SEQ_SECURITY_AUDIT_LOG",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    /*
     * 발생한 보안 행동의 종류입니다.
     *
     * LOGIN_SUCCESS
     * LOGIN_FAILURE
     * LOGOUT
     * PASSWORD_CHANGE
     * ADMIN_PASSWORD_RESET
     * ACCOUNT_LOCK
     * ACCOUNT_UNLOCK
     */
    @Column(
            name = "ACTION_TYPE",
            nullable = false,
            length = 50
    )
    private String actionType;

    /*
     * 행동을 실행한 사용자입니다.
     *
     * 로그인 실패의 경우
     * 로그인 화면에 입력한 아이디를 저장합니다.
     */
    @Column(
            name = "ACTOR_USERNAME",
            nullable = false,
            length = 50
    )
    private String actorUsername;

    /*
     * 행동의 대상이 된 사용자입니다.
     *
     * ADMIN이 user의 비밀번호를 초기화하거나
     * 계정 잠금을 해제할 때 사용합니다.
     */
    @Column(
            name = "TARGET_USERNAME",
            length = 50
    )
    private String targetUsername;

    /*
     * 행동의 성공 여부입니다.
     *
     * Y : 성공
     * N : 실패
     */
    @Column(
            name = "SUCCESS_YN",
            nullable = false,
            length = 1
    )
    private String successYn;

    /*
     * 요청을 보낸 클라이언트의 IP 주소입니다.
     *
     * IPv4와 IPv6 주소를 모두 저장할 수 있도록
     * 최대 길이를 45자로 지정합니다.
     */
    @Column(
            name = "CLIENT_IP",
            length = 45
    )
    private String clientIp;

    /*
     * 감사 기록에 대한 추가 설명입니다.
     *
     * 비밀번호 원문과 API Key 같은
     * 민감정보는 저장하지 않습니다.
     */
    @Column(
            name = "DETAIL",
            length = 500
    )
    private String detail;

    /*
     * 보안 행동이 발생한 시각입니다.
     */
    @Column(
            name = "CREATED_AT",
            nullable = false
    )
    private LocalDateTime createdAt;

    /*
     * 감사 기록이 처음 저장되기 전에
     * 생성 시각과 기본값을 설정합니다.
     */
    @PrePersist
    public void beforeInsert() {

        if (actorUsername == null
                || actorUsername.isBlank()) {

            actorUsername = "UNKNOWN";
        }

        if (successYn == null
                || successYn.isBlank()) {

            successYn = "N";
        }

        createdAt = LocalDateTime.now();
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getActionType() {

        return actionType;
    }

    public void setActionType(
            String actionType) {

        this.actionType = actionType;
    }

    public String getActorUsername() {

        return actorUsername;
    }

    public void setActorUsername(
            String actorUsername) {

        this.actorUsername = actorUsername;
    }

    public String getTargetUsername() {

        return targetUsername;
    }

    public void setTargetUsername(
            String targetUsername) {

        this.targetUsername = targetUsername;
    }

    public String getSuccessYn() {

        return successYn;
    }

    public void setSuccessYn(
            String successYn) {

        this.successYn = successYn;
    }

    public String getClientIp() {

        return clientIp;
    }

    public void setClientIp(
            String clientIp) {

        this.clientIp = clientIp;
    }

    public String getDetail() {

        return detail;
    }

    public void setDetail(
            String detail) {

        this.detail = detail;
    }

    public LocalDateTime getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}