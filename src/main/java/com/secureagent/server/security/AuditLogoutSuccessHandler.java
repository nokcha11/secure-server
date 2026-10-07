package com.secureagent.server.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.secureagent.server.service.SecurityAuditLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * 사용자가 정상적으로 로그아웃했을 때
 * 감사 기록을 저장합니다.
 */
@Component
public class AuditLogoutSuccessHandler
        implements LogoutSuccessHandler {

    private final SecurityAuditLogService
            securityAuditLogService;

    /*
     * 감사 기록 Service를
     * 생성자 주입으로 연결합니다.
     */
    public AuditLogoutSuccessHandler(
            SecurityAuditLogService
                    securityAuditLogService) {

        this.securityAuditLogService =
                securityAuditLogService;
    }

    /*
     * 로그아웃 성공 기록을 저장하고
     * 로그인 화면으로 이동합니다.
     */
    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        /*
         * 로그아웃한 사용자 아이디를 가져옵니다.
         */
        String username = null;

        if (authentication != null) {

            username =
                    authentication.getName();
        }

        /*
         * 로그아웃 내용을
         * 감사 기록에 저장합니다.
         */
        securityAuditLogService.record(
                SecurityAuditLogService.LOGOUT,
                username,
                null,
                true,
                request.getRemoteAddr(),
                "사용자 로그아웃"
        );

        /*
         * 로그아웃 완료 후
         * 로그인 화면으로 이동합니다.
         */
        response.sendRedirect(
                request.getContextPath()
                + "/login"
        );
    }
}