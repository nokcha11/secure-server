package com.secureagent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.secureagent.server.entity.SecurityAuditLog;

/*
 * 보안 감사 기록을
 * Oracle DB에 저장하는 Repository입니다.
 */
@Repository
public interface SecurityAuditLogRepository
        extends JpaRepository<SecurityAuditLog, Long> {
}