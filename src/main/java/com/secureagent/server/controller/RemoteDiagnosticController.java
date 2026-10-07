package com.secureagent.server.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.server.dto.RemoteDiagnosticRequest;
import com.secureagent.server.dto.RemoteDiagnosticResponse;
import com.secureagent.server.service.RemoteDiagnosticService;
import com.secureagent.server.service.SecurityAuditLogService;

import jakarta.servlet.http.HttpServletRequest;

/*
 * ADMIN이 요청하는 OpenSSH 원격 진단을
 * 처리하는 REST API Controller입니다.
 */
@RestController
@RequestMapping("/api/admin/remote-diagnostics")
public class RemoteDiagnosticController {

    private final RemoteDiagnosticService
            remoteDiagnosticService;

    private final SecurityAuditLogService
            securityAuditLogService;

    public RemoteDiagnosticController(
            RemoteDiagnosticService
                    remoteDiagnosticService,

            SecurityAuditLogService
                    securityAuditLogService) {

        this.remoteDiagnosticService =
                remoteDiagnosticService;

        this.securityAuditLogService =
                securityAuditLogService;
    }

    /*
     * 허용된 원격 진단 명령을
     * SSH를 통해 실행합니다.
     *
     * POST /api/admin/remote-diagnostics
     */
    @PostMapping
    public ResponseEntity<RemoteDiagnosticResponse>
            executeDiagnostic(

            @RequestBody
            RemoteDiagnosticRequest request,

            Authentication authentication,

            HttpServletRequest httpServletRequest) {

        /*
         * 로그인하지 않은 요청을
         * 한 번 더 방어적으로 차단합니다.
         */
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return createErrorResponse(
                    HttpStatus.UNAUTHORIZED,
                    request,
                    "로그인이 필요합니다."
            );
        }

        String adminUsername =
                authentication.getName();

        String clientIp =
                httpServletRequest.getRemoteAddr();

        try {
            RemoteDiagnosticResponse response =
                    remoteDiagnosticService
                            .executeDiagnostic(request);

            /*
             * 명령의 출력 원문은 감사 기록에
             * 저장하지 않습니다.
             *
             * 대상, 명령 종류, 성공 여부만
             * 감사 기록에 저장합니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService
                            .REMOTE_DIAGNOSTIC,
                    adminUsername,
                    getTargetUsername(request),
                    response.isSuccess(),
                    clientIp,
                    createAuditDetail(
                            request,
                            response.isSuccess()
                    )
            );

            if (response.isSuccess()) {

                return ResponseEntity.ok(response);
            }

            /*
             * SSH 접속 또는 원격 명령은 실행됐지만
             * 정상 종료되지 않은 경우입니다.
             */
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(response);

        } catch (IllegalArgumentException exception) {

            /*
             * 대상 주소, 포트, 사용자 이름,
             * 명령 종류가 잘못된 경우입니다.
             *
             * 검증되지 않은 입력 원문은
             * 감사 기록에 저장하지 않습니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService
                            .REMOTE_DIAGNOSTIC,
                    adminUsername,
                    getTargetUsername(request),
                    false,
                    clientIp,
                    "원격 진단 요청값 검증 실패"
            );

            return createErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    request,
                    exception.getMessage()
            );

        } catch (IllegalStateException exception) {

            /*
             * SSH 개인키, known_hosts, 허용 대상,
             * OpenSSH Client 설정 등이
             * 준비되지 않은 경우입니다.
             */
            securityAuditLogService.record(
                    SecurityAuditLogService
                            .REMOTE_DIAGNOSTIC,
                    adminUsername,
                    getTargetUsername(request),
                    false,
                    clientIp,
                    "원격 진단 실행 환경 또는 "
                    + "SSH 연결 실패"
            );

            return createErrorResponse(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    request,
                    exception.getMessage()
            );
        }
    }

    /*
     * 감사 기록의 대상 사용자 이름을
     * 안전하게 가져옵니다.
     */
    private String getTargetUsername(
            RemoteDiagnosticRequest request) {

        if (request == null) {

            return null;
        }

        return request.getUsername();
    }

    /*
     * 감사 기록에는 원격 명령 결과를 저장하지 않고
     * 대상 주소와 명령 종류만 저장합니다.
     */
    private String createAuditDetail(
            RemoteDiagnosticRequest request,
            boolean success) {

        String commandType = "UNKNOWN";

        if (request.getCommandType() != null) {

            commandType =
                    request.getCommandType().name();
        }

        return "원격 진단 "
                + (success ? "성공" : "실패")
                + ": 대상="
                + request.getHost()
                + ", 명령="
                + commandType;
    }

    /*
     * 원격 진단 실패 응답을 만듭니다.
     */
    private ResponseEntity<RemoteDiagnosticResponse>
            createErrorResponse(

            HttpStatus status,
            RemoteDiagnosticRequest request,
            String message) {

        RemoteDiagnosticResponse response =
                new RemoteDiagnosticResponse();

        response.setSuccess(false);
        response.setMessage(message);
        response.setExecutedAt(
                LocalDateTime.now()
        );

        if (request != null) {

            response.setHost(
                    request.getHost()
            );

            response.setPort(
                    request.getPort()
            );

            response.setUsername(
                    request.getUsername()
            );

            response.setCommandType(
                    request.getCommandType()
            );
        }

        return ResponseEntity
                .status(status)
                .body(response);
    }
}