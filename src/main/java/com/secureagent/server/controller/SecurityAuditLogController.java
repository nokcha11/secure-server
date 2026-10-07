package com.secureagent.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secureagent.server.dto.SecurityAuditLogResponse;
import com.secureagent.server.entity.AdminAccount;
import com.secureagent.server.entity.SecurityAuditLog;
import com.secureagent.server.repository.AdminAccountRepository;
import com.secureagent.server.repository.SecurityAuditLogRepository;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class SecurityAuditLogController {

    private static final int MAXIMUM_AUDIT_LOG_COUNT = 500;

    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final AdminAccountRepository adminAccountRepository;

    public SecurityAuditLogController(
            SecurityAuditLogRepository securityAuditLogRepository,
            AdminAccountRepository adminAccountRepository) {

        this.securityAuditLogRepository = securityAuditLogRepository;
        this.adminAccountRepository = adminAccountRepository;
    }

    @GetMapping
    public List<SecurityAuditLogResponse> findRecentAuditLogs() {

        List<SecurityAuditLog> auditLogs =
                securityAuditLogRepository.findAll(
                        PageRequest.of(
                                0,
                                MAXIMUM_AUDIT_LOG_COUNT,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                ).getContent();

        Map<String, String> roleByUsername = createRoleMap();
        List<SecurityAuditLogResponse> responses = new ArrayList<>();

        for (SecurityAuditLog auditLog : auditLogs) {
            responses.add(
                    createResponse(
                            auditLog,
                            roleByUsername
                    )
            );
        }

        return responses;
    }

    private Map<String, String> createRoleMap() {

        Map<String, String> roleByUsername = new HashMap<>();

        for (AdminAccount account : adminAccountRepository.findAll()) {
            if (account.getUsername() == null) {
                continue;
            }

            roleByUsername.put(
                    account.getUsername()
                            .trim()
                            .toLowerCase(Locale.ROOT),
                    account.getRoleName()
            );
        }

        return roleByUsername;
    }

    private SecurityAuditLogResponse createResponse(
            SecurityAuditLog auditLog,
            Map<String, String> roleByUsername) {

        SecurityAuditLogResponse response =
                new SecurityAuditLogResponse();

        response.setId(auditLog.getId());
        response.setActionType(auditLog.getActionType());
        response.setActorUsername(auditLog.getActorUsername());
        response.setActorRole(
                findRole(
                        auditLog.getActorUsername(),
                        roleByUsername
                )
        );
        response.setTargetUsername(auditLog.getTargetUsername());
        response.setSuccessYn(auditLog.getSuccessYn());
        response.setClientIp(auditLog.getClientIp());
        response.setDetail(auditLog.getDetail());
        response.setCreatedAt(auditLog.getCreatedAt());

        return response;
    }

    private String findRole(
            String username,
            Map<String, String> roleByUsername) {

        if (username == null || username.isBlank()) {
            return "-";
        }

        return roleByUsername.getOrDefault(
                username.trim().toLowerCase(Locale.ROOT),
                "-"
        );
    }
}
