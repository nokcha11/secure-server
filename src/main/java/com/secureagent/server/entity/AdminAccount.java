package com.secureagent.server.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/*
 * SecureAgent 관리 화면에 로그인하는
 * 관리자 계정 정보를 저장합니다.
 */
@Entity
@Table(
        name = "AGENT_ADMIN_ACCOUNT",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_AGENT_ADMIN_USERNAME",
                        columnNames = "USERNAME"
                )
        }
)
public class AdminAccount {

    /*
     * 관리자 계정 기본키입니다.
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "adminAccountSequence"
    )
    @SequenceGenerator(
            name = "adminAccountSequence",
            sequenceName = "SEQ_AGENT_ADMIN_ACCOUNT",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    /*
     * 로그인할 때 사용하는 관리자 아이디입니다.
     */
    @Column(
            name = "USERNAME",
            nullable = false,
            length = 50
    )
    private String username;

    /*
     * 암호화된 비밀번호를 저장합니다.
     *
     * 실제 비밀번호 원문은 저장하지 않습니다.
     */
    @Column(
            name = "PASSWORD_HASH",
            nullable = false,
            length = 100
    )
    private String passwordHash;

    /*
     * 사용자의 권한을 저장합니다.
     *
     * ADMIN  : 모든 관리 기능 사용
     * VIEWER : 조회 기능만 사용
     */
    @Column(
            name = "ROLE_NAME",
            nullable = false,
            length = 30
    )
    private String roleName;

    /*
     * 계정 사용 여부입니다.
     *
     * Y : 사용 가능
     * N : 사용 중지
     */
    @Column(
            name = "ENABLED_YN",
            nullable = false,
            length = 1
    )
    private String enabledYn;

    /*
     * 연속 로그인 실패 횟수입니다.
     */
    @Column(
            name = "FAILED_LOGIN_COUNT",
            nullable = false
    )
    private int failedLoginCount;

    /*
     * 계정 잠금 여부입니다.
     *
     * Y : 잠금
     * N : 정상
     */
    @Column(
            name = "ACCOUNT_LOCKED_YN",
            nullable = false,
            length = 1
    )
    private String accountLockedYn;

    /*
     * 마지막 로그인 성공 시각입니다.
     */
    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    /*
     * 계정 생성 시각입니다.
     */
    @Column(
            name = "CREATED_AT",
            nullable = false
    )
    private LocalDateTime createdAt;

    /*
     * 계정 정보가 마지막으로 변경된 시각입니다.
     */
    @Column(
            name = "UPDATED_AT",
            nullable = false
    )
    private LocalDateTime updatedAt;

    /*
     * 관리자 계정이 처음 저장되기 전에
     * 기본값과 생성 시각을 설정합니다.
     */
    @PrePersist
    public void beforeInsert() {

        LocalDateTime currentTime =
                LocalDateTime.now();

        if (enabledYn == null) {
            enabledYn = "Y";
        }

        if (roleName == null) {
            roleName = "ADMIN";
        }

        if (accountLockedYn == null) {
            accountLockedYn = "N";
        }

        createdAt = currentTime;
        updatedAt = currentTime;
    }

    /*
     * 관리자 계정 정보가 변경될 때
     * 수정 시각을 새로 저장합니다.
     */
    @PreUpdate
    public void beforeUpdate() {

        updatedAt = LocalDateTime.now();
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPasswordHash() {

        return passwordHash;
    }

    public void setPasswordHash(
            String passwordHash) {

        this.passwordHash = passwordHash;
    }

    public String getRoleName() {

        return roleName;
    }

    public void setRoleName(String roleName) {

        this.roleName = roleName;
    }

    public String getEnabledYn() {

        return enabledYn;
    }

    public void setEnabledYn(String enabledYn) {

        this.enabledYn = enabledYn;
    }

    public int getFailedLoginCount() {

        return failedLoginCount;
    }

    public void setFailedLoginCount(
            int failedLoginCount) {

        this.failedLoginCount =
                failedLoginCount;
    }

    public String getAccountLockedYn() {

        return accountLockedYn;
    }

    public void setAccountLockedYn(
            String accountLockedYn) {

        this.accountLockedYn =
                accountLockedYn;
    }

    public LocalDateTime getLastLoginAt() {

        return lastLoginAt;
    }

    public void setLastLoginAt(
            LocalDateTime lastLoginAt) {

        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {

        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}