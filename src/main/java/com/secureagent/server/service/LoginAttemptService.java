package com.secureagent.server.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.entity.AdminAccount;
import com.secureagent.server.repository.AdminAccountRepository;

/*
 * 로그인 성공과 실패 횟수를 관리하고
 * 5회 연속 실패한 계정을 잠급니다.
 */
@Service
public class LoginAttemptService {

    /*
     * 계정을 잠그는 최대 연속 실패 횟수입니다.
     */
    private static final int MAX_FAILED_LOGIN_COUNT =
            5;

    private final AdminAccountRepository
            adminAccountRepository;

    /*
     * 관리자 계정 Repository를
     * 생성자 주입으로 연결합니다.
     */
    public LoginAttemptService(
            AdminAccountRepository
                    adminAccountRepository) {

        this.adminAccountRepository =
                adminAccountRepository;
    }

    /*
     * 로그인 성공 결과를 저장합니다.
     *
     * 연속 실패 횟수를 0으로 초기화하고
     * 마지막 로그인 성공 시각을 저장합니다.
     */
    @Transactional
    public void recordLoginSuccess(
            String username) {

        String normalizedUsername =
                normalizeUsername(username);

        if (normalizedUsername.isBlank()) {
            return;
        }

        Optional<AdminAccount> accountOptional =
                adminAccountRepository
                        .findByUsername(
                                normalizedUsername
                        );

        if (accountOptional.isEmpty()) {
            return;
        }

        AdminAccount account =
                accountOptional.get();

        account.setFailedLoginCount(0);

        account.setLastLoginAt(
                LocalDateTime.now()
        );

        adminAccountRepository.save(
                account
        );
    }

    /*
     * 로그인 실패 결과를 저장합니다.
     *
     * 실패 횟수가 5회에 도달하면
     * 계정 잠금 여부를 Y로 변경합니다.
     *
     * 반환값:
     * true  -> 계정이 잠긴 상태
     * false -> 아직 잠기지 않은 상태
     */
    @Transactional
    public boolean recordLoginFailure(
            String username) {

        String normalizedUsername =
                normalizeUsername(username);

        if (normalizedUsername.isBlank()) {
            return false;
        }

        Optional<AdminAccount> accountOptional =
                adminAccountRepository
                        .findByUsername(
                                normalizedUsername
                        );

        /*
         * 존재하지 않는 아이디는
         * 실패 횟수를 저장할 계정이 없습니다.
         */
        if (accountOptional.isEmpty()) {
            return false;
        }

        AdminAccount account =
                accountOptional.get();

        /*
         * 이미 잠긴 계정이면
         * 실패 횟수를 더 증가시키지 않습니다.
         */
        if ("Y".equalsIgnoreCase(
                account.getAccountLockedYn()
        )) {

            return true;
        }

        int nextFailedLoginCount =
                account.getFailedLoginCount() + 1;

        /*
         * 실패 횟수는 최대 5까지만
         * 저장하도록 제한합니다.
         */
        if (nextFailedLoginCount
                > MAX_FAILED_LOGIN_COUNT) {

            nextFailedLoginCount =
                    MAX_FAILED_LOGIN_COUNT;
        }

        account.setFailedLoginCount(
                nextFailedLoginCount
        );

        boolean accountLocked =
                nextFailedLoginCount
                        >= MAX_FAILED_LOGIN_COUNT;

        if (accountLocked) {

            account.setAccountLockedYn(
                    "Y"
            );
        }

        adminAccountRepository.save(
                account
        );

        return accountLocked;
    }

    /*
     * 로그인 아이디의 앞뒤 공백을 제거하고
     * 소문자로 통일합니다.
     */
    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            return "";
        }

        return username
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}