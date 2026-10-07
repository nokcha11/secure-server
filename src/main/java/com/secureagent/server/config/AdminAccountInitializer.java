package com.secureagent.server.config;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.entity.AdminAccount;
import com.secureagent.server.repository.AdminAccountRepository;

/*
 * SecureAgent 서버의 최초 관리자 계정과
 * 최초 일반 사용자 계정을 생성합니다.
 */
@Component
public class AdminAccountInitializer
        implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminAccountInitializer.class
            );

    private final AdminAccountRepository
            adminAccountRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final String initialAdminUsername;

    private final String initialAdminPassword;

    private final String initialUserUsername;

    private final String initialUserPassword;

    /*
     * 계정 Repository, PasswordEncoder,
     * 환경변수에 설정된 최초 계정 정보를 주입받습니다.
     */
    public AdminAccountInitializer(
            AdminAccountRepository
                    adminAccountRepository,

            PasswordEncoder passwordEncoder,

            @Value(
                    "${secureagent.admin.username:admin}"
            )
            String initialAdminUsername,

            @Value(
                    "${secureagent.admin.initial-password:}"
            )
            String initialAdminPassword,

            @Value(
                    "${secureagent.user.username:user}"
            )
            String initialUserUsername,

            @Value(
                    "${secureagent.user.initial-password:}"
            )
            String initialUserPassword) {

        this.adminAccountRepository =
                adminAccountRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.initialAdminUsername =
                initialAdminUsername;

        this.initialAdminPassword =
                initialAdminPassword;

        this.initialUserUsername =
                initialUserUsername;

        this.initialUserPassword =
                initialUserPassword;
    }

    /*
     * Spring Boot 서버가 시작된 후
     * 관리자와 일반 사용자 계정을 각각 확인합니다.
     */
    @Override
    @Transactional
    public void run(
            ApplicationArguments arguments) {

        /*
         * 관리자 계정을 확인하고 생성합니다.
         */
        createAccountIfMissing(
                normalizeUsername(
                        initialAdminUsername,
                        "admin"
                ),
                initialAdminPassword,
                "ADMIN",
                "관리자",
                "SECURE_ADMIN_PASSWORD"
        );

        /*
         * 일반 사용자 계정을 확인하고 생성합니다.
         */
        createAccountIfMissing(
                normalizeUsername(
                        initialUserUsername,
                        "user"
                ),
                initialUserPassword,
                "VIEWER",
                "일반 사용자",
                "SECURE_USER_PASSWORD"
        );
    }

    /*
     * 동일한 아이디의 계정이 없는 경우에만
     * 새로운 계정을 생성합니다.
     */
    private void createAccountIfMissing(
            String username,
            String initialPassword,
            String roleName,
            String accountDescription,
            String passwordEnvironmentName) {

        /*
         * 동일한 계정이 이미 존재하면
         * 비밀번호를 변경하거나 다시 생성하지 않습니다.
         */
        if (adminAccountRepository
                .existsByUsername(username)) {

            logger.info(
                    "{} 계정이 이미 존재합니다: {}",
                    accountDescription,
                    username
            );

            return;
        }

        /*
         * 환경변수에 비밀번호가 없으면
         * 해당 계정을 생성하지 않습니다.
         */
        if (initialPassword == null
                || initialPassword.isBlank()) {

            logger.warn(
                    "{}가 없어 {} 계정을 생성하지 않았습니다.",
                    passwordEnvironmentName,
                    accountDescription
            );

            return;
        }

        /*
         * 너무 짧은 비밀번호는
         * 사용하지 못하도록 제한합니다.
         */
        if (initialPassword.length() < 12) {

            logger.warn(
                    "{} 계정의 비밀번호는 12자 이상이어야 합니다.",
                    accountDescription
            );

            return;
        }

        AdminAccount account =
                new AdminAccount();

        account.setUsername(username);

        /*
         * 실제 비밀번호가 아니라
         * BCrypt로 변환한 해시를 저장합니다.
         */
        account.setPasswordHash(
                passwordEncoder.encode(
                        initialPassword
                )
        );

        account.setRoleName(roleName);
        account.setEnabledYn("Y");
        account.setFailedLoginCount(0);
        account.setAccountLockedYn("N");

        adminAccountRepository.save(account);

        logger.info(
                "{} 계정 생성 완료: {} / 권한: {}",
                accountDescription,
                username,
                roleName
        );
    }

    /*
     * 로그인 아이디의 앞뒤 공백을 제거하고
     * 소문자로 통일합니다.
     */
    private String normalizeUsername(
            String username,
            String defaultUsername) {

        if (username == null
                || username.isBlank()) {

            return defaultUsername;
        }

        return username
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}