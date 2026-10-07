package com.secureagent.server.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.entity.SecurityAuditLog;
import com.secureagent.server.repository.SecurityAuditLogRepository;

@Service
public class SecurityAuditLogService {

    public static final String LOGIN_SUCCESS =
            "LOGIN_SUCCESS";

    public static final String LOGIN_FAILURE =
            "LOGIN_FAILURE";

    public static final String LOGOUT =
            "LOGOUT";

    public static final String PASSWORD_CHANGE =
            "PASSWORD_CHANGE";

    public static final String ADMIN_PASSWORD_RESET =
            "ADMIN_PASSWORD_RESET";

    public static final String ACCOUNT_LOCK =
            "ACCOUNT_LOCK";

    public static final String ACCOUNT_UNLOCK =
            "ACCOUNT_UNLOCK";

    /*
     * OpenSSH를 이용한
     * 원격 진단 실행 기록입니다.
     */
    public static final String REMOTE_DIAGNOSTIC =
            "REMOTE_DIAGNOSTIC";
    
    private final SecurityAuditLogRepository
            securityAuditLogRepository;

    public SecurityAuditLogService(
            SecurityAuditLogRepository
                    securityAuditLogRepository) {

        this.securityAuditLogRepository =
                securityAuditLogRepository;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void record(
            String actionType,
            String actorUsername,
            String targetUsername,
            boolean success,
            String clientIp,
            String detail) {

        SecurityAuditLog securityAuditLog =
                new SecurityAuditLog();

        securityAuditLog.setActionType(
                normalizeActionType(actionType)
        );

        securityAuditLog.setActorUsername(
                normalizeActorUsername(
                        actorUsername
                )
        );

        securityAuditLog.setTargetUsername(
                normalizeTargetUsername(
                        targetUsername
                )
        );

        securityAuditLog.setSuccessYn(
                success ? "Y" : "N"
        );

        securityAuditLog.setClientIp(
                limitLength(
                        clientIp,
                        45
                )
        );

        securityAuditLog.setDetail(
                limitLength(
                        detail,
                        500
                )
        );

        securityAuditLogRepository.save(
                securityAuditLog
        );
    }

    private String normalizeActionType(
            String actionType) {

        if (actionType == null
                || actionType.isBlank()) {

            return "UNKNOWN_ACTION";
        }

        return limitLength(
                actionType
                        .trim()
                        .toUpperCase(Locale.ROOT),
                50
        );
    }

    private String normalizeActorUsername(
            String actorUsername) {

        if (actorUsername == null
                || actorUsername.isBlank()) {

            return "unknown";
        }

        return limitLength(
                actorUsername
                        .trim()
                        .toLowerCase(Locale.ROOT),
                50
        );
    }

    private String normalizeTargetUsername(
            String targetUsername) {

        if (targetUsername == null
                || targetUsername.isBlank()) {

            return null;
        }

        return limitLength(
                targetUsername
                        .trim()
                        .toLowerCase(Locale.ROOT),
                50
        );
    }

    private String limitLength(
            String value,
            int maximumLength) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        String trimmedValue =
                value.trim();

        if (trimmedValue.length()
                <= maximumLength) {

            return trimmedValue;
        }

        return trimmedValue.substring(
                0,
                maximumLength
        );
    }
}