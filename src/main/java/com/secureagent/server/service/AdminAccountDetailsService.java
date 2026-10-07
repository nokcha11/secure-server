package com.secureagent.server.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureagent.server.entity.AdminAccount;
import com.secureagent.server.repository.AdminAccountRepository;

/*
 * Oracle에 저장된 관리자 계정을 조회하여
 * Spring Security 로그인에 전달합니다.
 */
@Service
public class AdminAccountDetailsService
        implements UserDetailsService {

    private final AdminAccountRepository
            adminAccountRepository;

    /*
     * 관리자 계정 Repository를
     * 생성자 주입으로 연결합니다.
     */
    public AdminAccountDetailsService(
            AdminAccountRepository
                    adminAccountRepository) {

        this.adminAccountRepository =
                adminAccountRepository;
    }

    /*
     * 로그인 화면에서 입력한 아이디를 이용하여
     * Oracle의 관리자 계정을 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        String normalizedUsername =
                normalizeUsername(username);

        AdminAccount adminAccount =
                adminAccountRepository
                        .findByUsername(
                                normalizedUsername
                        )
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "관리자 계정을 찾을 수 없습니다."
                                        )
                        );

        String roleName =
                normalizeRoleName(
                        adminAccount.getRoleName()
                );

        /*
         * Oracle에서 조회한 계정 정보를
         * Spring Security의 UserDetails로 변경합니다.
         */
        return User
                .withUsername(
                        adminAccount.getUsername()
                )
                .password(
                        adminAccount.getPasswordHash()
                )
                .roles(roleName)

                /*
                 * ENABLED_YN이 N이면
                 * 로그인을 차단합니다.
                 */
                .disabled(
                        !"Y".equalsIgnoreCase(
                                adminAccount.getEnabledYn()
                        )
                )

                /*
                 * ACCOUNT_LOCKED_YN이 Y이면
                 * 로그인을 차단합니다.
                 */
                .accountLocked(
                        "Y".equalsIgnoreCase(
                                adminAccount
                                        .getAccountLockedYn()
                        )
                )
                .build();
    }

    /*
     * 로그인 아이디의 앞뒤 공백을 제거하고
     * 소문자로 통일합니다.
     */
    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new UsernameNotFoundException(
                    "관리자 아이디가 비어 있습니다."
            );
        }

        return username
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    /*
     * DB에 권한이 없거나 잘못 저장된 경우
     * 안전하게 조회 전용 권한을 적용합니다.
     */
    private String normalizeRoleName(
            String roleName) {

        if (roleName == null
                || roleName.isBlank()) {

            return "VIEWER";
        }

        return roleName
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }
}