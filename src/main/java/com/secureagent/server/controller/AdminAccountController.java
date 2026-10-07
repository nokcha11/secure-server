package com.secureagent.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.server.dto.AdminPasswordResetRequest;
import com.secureagent.server.service.AccountLockService;
import com.secureagent.server.service.AccountPasswordService;
import com.secureagent.server.service.SecurityAuditLogService;

import jakarta.servlet.http.HttpServletRequest;

/*
 * ADMIN 권한이 필요한
 * 계정 관리 요청을 처리합니다.
 */
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AccountPasswordService
            accountPasswordService;

    private final AccountLockService
            accountLockService;

    private final SecurityAuditLogService
            securityAuditLogService;

    /*
     * 비밀번호 관리 Service,
     * 계정 잠금 Service,
     * 감사 기록 Service를 연결합니다.
     */
    public AdminAccountController(
            AccountPasswordService accountPasswordService,
            AccountLockService accountLockService,
            SecurityAuditLogService
                    securityAuditLogService) {

        this.accountPasswordService =
                accountPasswordService;

        this.accountLockService =
                accountLockService;

        this.securityAuditLogService =
                securityAuditLogService;
    }

    /*
     * ADMIN이 VIEWER 사용자의 비밀번호를
     * 새로운 비밀번호로 초기화합니다.
     *
     * PUT /api/admin/accounts/{username}/password-reset
     */
    @PutMapping("/{username}/password-reset")
    public ResponseEntity<String> resetUserPassword(
            @PathVariable("username")
            String username,

            @RequestBody
            AdminPasswordResetRequest request,

            Authentication authentication,

            HttpServletRequest httpServletRequest) {

        /*
         * 로그인 정보가 없는 요청은 거부합니다.
         */
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "로그인이 필요합니다."
                    );
        }

        String adminUsername =
                authentication.getName();

        String clientIp =
                httpServletRequest.getRemoteAddr();

        try {
            /*
             * 대상 사용자의 비밀번호를
             * 새로운 BCrypt 해시로 초기화합니다.
             */
            accountPasswordService
                    .resetUserPasswordByAdmin(
                            adminUsername,
                            username,
                            request
                    );

            /*
             * 비밀번호 초기화 성공을
             * 감사 기록에 저장합니다.
             *
             * 새 비밀번호 원문은 저장하지 않습니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService
                            .ADMIN_PASSWORD_RESET,
                    adminUsername,
                    username,
                    true,
                    clientIp,
                    "ADMIN이 사용자 비밀번호를 초기화함"
            );

            return ResponseEntity.ok(
                    username
                    + " 사용자의 비밀번호를 "
                    + "초기화했습니다."
            );

        } catch (IllegalArgumentException exception) {

            /*
             * 비밀번호 초기화 실패도
             * 감사 기록에 저장합니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService
                            .ADMIN_PASSWORD_RESET,
                    adminUsername,
                    username,
                    false,
                    clientIp,
                    "사용자 비밀번호 초기화 실패"
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );

        } catch (IllegalStateException exception) {

            securityAuditLogService.record(
                    SecurityAuditLogService
                            .ADMIN_PASSWORD_RESET,
                    adminUsername,
                    username,
                    false,
                    clientIp,
                    "사용자 비밀번호 초기화 거부"
            );

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            exception.getMessage()
                    );
        }
    }

    /*
     * ADMIN이 잠긴 사용자 계정의
     * 잠금을 해제합니다.
     *
     * PUT /api/admin/accounts/{username}/unlock
     */
    @PutMapping("/{username}/unlock")
    public ResponseEntity<String> unlockAccount(
            @PathVariable("username")
            String username,

            Authentication authentication,

            HttpServletRequest httpServletRequest) {

        /*
         * 로그인 정보가 없는 요청은 거부합니다.
         */
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "로그인이 필요합니다."
                    );
        }

        String adminUsername =
                authentication.getName();

        String clientIp =
                httpServletRequest.getRemoteAddr();

        try {
            /*
             * 실패 횟수를 0으로 초기화하고
             * 대상 계정의 잠금을 해제합니다.
             */
            accountLockService
                    .unlockAccountByAdmin(
                            adminUsername,
                            username
                    );

            /*
             * 계정 잠금 해제 성공을
             * 감사 기록에 저장합니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService.ACCOUNT_UNLOCK,
                    adminUsername,
                    username,
                    true,
                    clientIp,
                    "ADMIN이 사용자 계정 잠금을 해제함"
            );

            return ResponseEntity.ok(
                    username
                    + " 사용자의 계정 잠금을 "
                    + "해제했습니다."
            );

        } catch (IllegalArgumentException exception) {

            /*
             * 존재하지 않는 사용자 등의
             * 잠금 해제 실패를 기록합니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService.ACCOUNT_UNLOCK,
                    adminUsername,
                    username,
                    false,
                    clientIp,
                    "사용자 계정 잠금 해제 실패"
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );

        } catch (IllegalStateException exception) {

            /*
             * 이미 잠금 해제된 계정이거나
             * 권한이 없는 요청을 기록합니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService.ACCOUNT_UNLOCK,
                    adminUsername,
                    username,
                    false,
                    clientIp,
                    "사용자 계정 잠금 해제 거부"
            );

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            exception.getMessage()
                    );
        }
    }
}