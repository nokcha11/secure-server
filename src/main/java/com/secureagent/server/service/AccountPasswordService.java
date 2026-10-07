package com.secureagent.server.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.dto.AdminPasswordResetRequest;
import com.secureagent.server.dto.PasswordChangeRequest;
import com.secureagent.server.entity.AdminAccount;
import com.secureagent.server.repository.AdminAccountRepository;

/*
 * 로그인 계정의 비밀번호 변경과
 * ADMIN의 사용자 비밀번호 초기화를 처리합니다.
 */
@Service
public class AccountPasswordService {

    private final AdminAccountRepository
            adminAccountRepository;

    private final PasswordEncoder
            passwordEncoder;

    public AccountPasswordService(
            AdminAccountRepository adminAccountRepository,
            PasswordEncoder passwordEncoder) {

        this.adminAccountRepository =
                adminAccountRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    /*
     * 현재 로그인한 사용자가
     * 자신의 비밀번호를 변경합니다.
     */
    @Transactional
    public void changeOwnPassword(
            String username,
            PasswordChangeRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "비밀번호 변경 정보가 없습니다."
            );
        }

        AdminAccount account =
                findAccount(username);

        validateAvailableAccount(account);

        String currentPassword =
                request.getCurrentPassword();

        /*
         * 입력한 현재 비밀번호와
         * DB에 저장된 BCrypt 해시를 비교합니다.
         */
        if (currentPassword == null
                || currentPassword.isBlank()
                || !passwordEncoder.matches(
                        currentPassword,
                        account.getPasswordHash()
                )) {

            throw new IllegalArgumentException(
                    "현재 비밀번호가 올바르지 않습니다."
            );
        }

        validateNewPassword(
                request.getNewPassword(),
                request.getConfirmPassword(),
                account.getPasswordHash()
        );

        account.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        account.setFailedLoginCount(0);
        account.setAccountLockedYn("N");

        adminAccountRepository.save(account);
    }

    /*
     * ADMIN이 VIEWER 사용자의 비밀번호를
     * 새로운 비밀번호로 초기화합니다.
     */
    @Transactional
    public void resetUserPasswordByAdmin(
            String adminUsername,
            String targetUsername,
            AdminPasswordResetRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "비밀번호 초기화 정보가 없습니다."
            );
        }

        AdminAccount adminAccount =
                findAccount(adminUsername);

        validateAvailableAccount(adminAccount);

        /*
         * 실제 로그인 계정의 DB 권한도
         * 다시 한번 ADMIN인지 검사합니다.
         */
        if (!"ADMIN".equalsIgnoreCase(
                adminAccount.getRoleName()
        )) {

            throw new IllegalStateException(
                    "ADMIN 권한이 필요한 기능입니다."
            );
        }

        AdminAccount targetAccount =
                findAccount(targetUsername);

        /*
         * 이 API로 자신의 비밀번호를 우회하여
         * 변경하지 못하게 제한합니다.
         *
         * 본인 비밀번호는 현재 비밀번호를 확인하는
         * 기존 비밀번호 변경 기능을 사용해야 합니다.
         */
        if (adminAccount.getUsername().equalsIgnoreCase(
                targetAccount.getUsername()
        )) {

            throw new IllegalArgumentException(
                    "본인 비밀번호는 비밀번호 변경 기능을 이용하세요."
            );
        }

        /*
         * 이번 단계에서는 VIEWER 권한을 가진
         * 일반 사용자만 초기화할 수 있습니다.
         */
        if (!"VIEWER".equalsIgnoreCase(
                targetAccount.getRoleName()
        )) {

            throw new IllegalArgumentException(
                    "VIEWER 사용자의 비밀번호만 초기화할 수 있습니다."
            );
        }

        validateNewPassword(
                request.getNewPassword(),
                request.getConfirmPassword(),
                targetAccount.getPasswordHash()
        );

        targetAccount.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        /*
         * 비밀번호를 초기화하면서
         * 로그인 실패 횟수와 잠금 상태도 복구합니다.
         */
        targetAccount.setFailedLoginCount(0);
        targetAccount.setAccountLockedYn("N");

        adminAccountRepository.save(
                targetAccount
        );
    }

    /*
     * 사용자 아이디로 계정을 조회합니다.
     */
    private AdminAccount findAccount(
            String username) {

        String normalizedUsername =
                normalizeUsername(username);

        return adminAccountRepository
                .findByUsername(normalizedUsername)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "계정을 찾을 수 없습니다: "
                                + normalizedUsername
                        )
                );
    }

    /*
     * 사용 중지 또는 잠긴 계정인지 확인합니다.
     */
    private void validateAvailableAccount(
            AdminAccount account) {

        if (!"Y".equalsIgnoreCase(
                account.getEnabledYn()
        )) {

            throw new IllegalStateException(
                    "사용이 중지된 계정입니다."
            );
        }

        if ("Y".equalsIgnoreCase(
                account.getAccountLockedYn()
        )) {

            throw new IllegalStateException(
                    "잠긴 계정입니다."
            );
        }
    }

    /*
     * 새 비밀번호의 길이와 확인값을 검사합니다.
     */
    private void validateNewPassword(
            String newPassword,
            String confirmPassword,
            String currentPasswordHash) {

        if (newPassword == null
                || newPassword.isBlank()
                || newPassword.length() < 12) {

            throw new IllegalArgumentException(
                    "새 비밀번호는 12자 이상이어야 합니다."
            );
        }

        if (!newPassword.equals(
                confirmPassword
        )) {

            throw new IllegalArgumentException(
                    "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."
            );
        }

        if (passwordEncoder.matches(
                newPassword,
                currentPasswordHash
        )) {

            throw new IllegalArgumentException(
                    "기존 비밀번호와 다른 비밀번호를 사용하세요."
            );
        }
    }

    /*
     * 아이디의 앞뒤 공백을 제거하고
     * 소문자로 통일합니다.
     */
    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "사용자 아이디가 없습니다."
            );
        }

        return username
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}