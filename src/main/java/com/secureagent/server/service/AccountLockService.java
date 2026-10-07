package com.secureagent.server.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.entity.AdminAccount;
import com.secureagent.server.repository.AdminAccountRepository;

/*
 * ADMIN이 잠긴 사용자 계정을
 * 정상 상태로 복구합니다.
 */
@Service
public class AccountLockService {

    private final AdminAccountRepository
            adminAccountRepository;

    /*
     * 계정 Repository를
     * 생성자 주입으로 연결합니다.
     */
    public AccountLockService(
            AdminAccountRepository
                    adminAccountRepository) {

        this.adminAccountRepository =
                adminAccountRepository;
    }

    /*
     * ADMIN이 대상 계정의 잠금을 해제합니다.
     *
     * FAILED_LOGIN_COUNT를 0으로 초기화하고
     * ACCOUNT_LOCKED_YN을 N으로 변경합니다.
     */
    @Transactional
    public void unlockAccountByAdmin(
            String adminUsername,
            String targetUsername) {

        String normalizedAdminUsername =
                normalizeUsername(
                        adminUsername
                );

        String normalizedTargetUsername =
                normalizeUsername(
                        targetUsername
                );

        /*
         * 요청을 실행한 관리자 계정을 확인합니다.
         */
        AdminAccount adminAccount =
                adminAccountRepository
                        .findByUsername(
                                normalizedAdminUsername
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "관리자 계정을 찾을 수 없습니다."
                                        )
                        );

        /*
         * 실제 ADMIN 권한을 가진 계정인지
         * Service에서도 다시 확인합니다.
         */
        if (!"ADMIN".equalsIgnoreCase(
                adminAccount.getRoleName())) {

            throw new IllegalStateException(
                    "ADMIN 권한이 필요합니다."
            );
        }

        if (!"Y".equalsIgnoreCase(
                adminAccount.getEnabledYn())) {

            throw new IllegalStateException(
                    "사용할 수 없는 관리자 계정입니다."
            );
        }

        /*
         * 잠금을 해제할 대상 계정을 확인합니다.
         */
        AdminAccount targetAccount =
                adminAccountRepository
                        .findByUsername(
                                normalizedTargetUsername
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "대상 사용자를 찾을 수 없습니다."
                                        )
                        );

        /*
         * 이미 정상 상태인 계정이면
         * 중복 잠금 해제를 거부합니다.
         */
        if (!"Y".equalsIgnoreCase(
                targetAccount.getAccountLockedYn())) {

            throw new IllegalStateException(
                    "이미 잠금 해제된 계정입니다."
            );
        }

        /*
         * 로그인 실패 횟수를 초기화하고
         * 계정 잠금을 해제합니다.
         */
        targetAccount.setFailedLoginCount(0);
        targetAccount.setAccountLockedYn("N");

        adminAccountRepository.save(
                targetAccount
        );
    }

    /*
     * 사용자 아이디의 앞뒤 공백을 제거하고
     * 소문자로 통일합니다.
     */
    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "사용자 아이디가 필요합니다."
            );
        }

        return username
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}