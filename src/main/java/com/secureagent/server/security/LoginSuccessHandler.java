package com.secureagent.server.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.secureagent.server.service.LoginAttemptService;
import com.secureagent.server.service.SecurityAuditLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * 로그인에 성공했을 때 실행됩니다.
 */
@Component
public class LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final LoginAttemptService
            loginAttemptService;

    private final SecurityAuditLogService
            securityAuditLogService;

    /*
     * 로그인 처리 Service와
     * 감사 기록 Service를 연결합니다.
     */
    public LoginSuccessHandler(
            LoginAttemptService loginAttemptService,
            SecurityAuditLogService
                    securityAuditLogService) {

        this.loginAttemptService =
                loginAttemptService;

        this.securityAuditLogService =
                securityAuditLogService;
    }

    /*
     * 로그인 성공 처리를 수행합니다.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String username =
                authentication.getName();

        /*
         * 로그인 실패 횟수를 0으로 초기화하고
         * 마지막 로그인 성공 시각을 저장합니다.
         */
        loginAttemptService.recordLoginSuccess(
                username
        );

        /*
         * 로그인 성공 내용을
         * 감사 기록에 저장합니다.
         */
        securityAuditLogService.record(
                SecurityAuditLogService.LOGIN_SUCCESS,
                username,
                username,
                true,
                request.getRemoteAddr(),
                "로그인 성공"
        );

        /*
         * 로그인 성공 후
         * 대시보드로 이동합니다.
         */
        response.sendRedirect(
                request.getContextPath() + "/"
        );
    }
}