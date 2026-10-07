package com.secureagent.server.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.server.dto.PasswordChangeRequest;
import com.secureagent.server.service.AccountPasswordService;
import com.secureagent.server.service.SecurityAuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/*
 * 로그인한 사용자의
 * 계정 관련 요청을 처리합니다.
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountPasswordService
            accountPasswordService;

    private final SecurityAuditLogService
            securityAuditLogService;

    /*
     * 비밀번호 변경 Service와
     * 감사 기록 Service를 연결합니다.
     */
    public AccountController(
            AccountPasswordService accountPasswordService,
            SecurityAuditLogService
                    securityAuditLogService) {

        this.accountPasswordService =
                accountPasswordService;

        this.securityAuditLogService =
                securityAuditLogService;
    }

    /*
     * 현재 로그인한 계정의
     * 아이디와 권한을 반환합니다.
     *
     * GET /api/account/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>>
            getCurrentAccount(
                    Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        String roleName =
                authentication
                        .getAuthorities()
                        .stream()
                        .map(
                                authority ->
                                        authority.getAuthority()
                        )
                        .filter(
                                authority ->
                                        authority.startsWith(
                                                "ROLE_"
                                        )
                        )
                        .map(
                                authority ->
                                        authority.substring(5)
                        )
                        .findFirst()
                        .orElse("UNKNOWN");

        return ResponseEntity.ok(
                Map.of(
                        "username",
                        authentication.getName(),

                        "role",
                        roleName
                )
        );
    }

    /*
     * 비밀번호 변경 요청에 사용할
     * CSRF 토큰을 반환합니다.
     *
     * GET /api/account/csrf-token
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>>
            getCsrfToken(
                    CsrfToken csrfToken) {

        return ResponseEntity.ok(
                Map.of(
                        "headerName",
                        csrfToken.getHeaderName(),

                        "parameterName",
                        csrfToken.getParameterName(),

                        "token",
                        csrfToken.getToken()
                )
        );
    }

    /*
     * 현재 로그인한 사용자가
     * 자신의 비밀번호를 변경합니다.
     *
     * PUT /api/account/password
     */
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody
            PasswordChangeRequest request,

            Authentication authentication,

            HttpServletRequest httpRequest) {

        /*
         * 로그인 정보가 없으면
         * 비밀번호 변경을 거부합니다.
         */
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "로그인이 필요합니다."
                    );
        }

        String username =
                authentication.getName();

        String clientIp =
                httpRequest.getRemoteAddr();

        try {
            /*
             * 현재 로그인한 사용자의
             * 비밀번호를 변경합니다.
             */
            accountPasswordService.changeOwnPassword(
                    username,
                    request
            );

            /*
             * 비밀번호 변경 성공 내용을
             * 감사 기록에 저장합니다.
             *
             * 비밀번호 원문은 저장하지 않습니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService.PASSWORD_CHANGE,
                    username,
                    username,
                    true,
                    clientIp,
                    "본인 비밀번호 변경 성공"
            );

            /*
             * 비밀번호 변경 후 기존 세션을 종료하여
             * 새 비밀번호로 다시 로그인하게 합니다.
             */
            HttpSession session =
                    httpRequest.getSession(false);

            if (session != null) {

                session.invalidate();
            }

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(
                    "비밀번호가 변경되었습니다. 다시 로그인하세요."
            );

        } catch (IllegalArgumentException exception) {

            /*
             * 현재 비밀번호 불일치,
             * 새 비밀번호 검증 실패 등을 기록합니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService.PASSWORD_CHANGE,
                    username,
                    username,
                    false,
                    clientIp,
                    "본인 비밀번호 변경 실패"
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );

        } catch (IllegalStateException exception) {

            /*
             * 중지되거나 잠긴 계정의
             * 비밀번호 변경 요청을 기록합니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService.PASSWORD_CHANGE,
                    username,
                    username,
                    false,
                    clientIp,
                    "계정 상태로 인한 비밀번호 변경 거부"
            );

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            exception.getMessage()
                    );
        }
    }
}