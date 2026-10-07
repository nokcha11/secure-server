package com.secureagent.server.security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.secureagent.server.service.LoginAttemptService;
import com.secureagent.server.service.SecurityAuditLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * 로그인에 실패했을 때 실행됩니다.
 */
@Component
public class LoginFailureHandler
        implements AuthenticationFailureHandler {

    private final LoginAttemptService
            loginAttemptService;

    private final SecurityAuditLogService
            securityAuditLogService;

    /*
     * 로그인 처리 Service와
     * 감사 기록 Service를 연결합니다.
     */
    public LoginFailureHandler(
            LoginAttemptService loginAttemptService,
            SecurityAuditLogService
                    securityAuditLogService) {

        this.loginAttemptService =
                loginAttemptService;

        this.securityAuditLogService =
                securityAuditLogService;
    }

    /*
     * 로그인 실패 처리를 수행합니다.
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        /*
         * 로그인 화면에 입력된
         * 사용자 아이디를 가져옵니다.
         */
        String username =
                request.getParameter("username");

        /*
         * 로그인 실패 횟수를 증가하고
         * 5회 이상이면 계정을 잠급니다.
         */
        boolean accountLocked =
                loginAttemptService.recordLoginFailure(
                        username
                );

        String failureDetail;

        if (exception instanceof LockedException) {

            failureDetail =
                    "잠긴 계정으로 로그인 시도";

        } else {

            failureDetail =
                    "아이디 또는 비밀번호 불일치";
        }

        /*
         * 로그인 실패 내용을
         * 감사 기록에 저장합니다.
         *
         * 사용자가 입력한 비밀번호는
         * 절대로 저장하지 않습니다.
         */
        securityAuditLogService.record(
                SecurityAuditLogService.LOGIN_FAILURE,
                username,
                username,
                false,
                request.getRemoteAddr(),
                failureDetail
        );

        /*
         * 이번 로그인 실패로 계정이 새로 잠겼다면
         * 계정 잠금 기록도 함께 저장합니다.
         *
         * 이미 잠긴 계정의 반복 요청에는
         * 잠금 기록을 중복 저장하지 않습니다.
         */
        if (accountLocked
                && exception
                        instanceof BadCredentialsException) {

            securityAuditLogService.record(
                    SecurityAuditLogService.ACCOUNT_LOCK,
                    "system",
                    username,
                    true,
                    request.getRemoteAddr(),
                    "로그인 5회 실패로 계정 자동 잠금"
            );
        }

        /*
         * 잠금된 계정이면 locked 정보를 포함하여
         * 로그인 화면으로 돌려보냅니다.
         */
        String redirectUrl =
                request.getContextPath()
                + "/login?error";

        if (accountLocked) {

            redirectUrl += "&locked";
        }

        response.sendRedirect(redirectUrl);
    }
}