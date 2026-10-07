package com.secureagent.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secureagent.server.entity.AdminAccount;

/*
 * 관리자 계정 정보를 Oracle DB에서
 * 저장하고 조회하는 Repository입니다.
 */
public interface AdminAccountRepository
        extends JpaRepository<AdminAccount, Long> {

    /*
     * 로그인 아이디로 관리자 계정을 조회합니다.
     *
     * 아이디가 존재하지 않을 수도 있으므로
     * Optional 형태로 반환합니다.
     */
    Optional<AdminAccount> findByUsername(
            String username);

    /*
     * 동일한 로그인 아이디가 이미 등록되어 있는지
     * 확인할 때 사용합니다.
     */
    boolean existsByUsername(
            String username);
}