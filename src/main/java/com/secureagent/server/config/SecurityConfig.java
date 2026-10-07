package com.secureagent.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.secureagent.server.security.AgentApiKeyFilter;
import com.secureagent.server.security.AuditLogoutSuccessHandler;
import com.secureagent.server.security.LoginFailureHandler;
import com.secureagent.server.security.LoginSuccessHandler;

/*
 * SecureAgent 서버의 로그인과
 * URL 접근 권한을 설정합니다.
 */
@Configuration
public class SecurityConfig {

    /*
     * 계정 비밀번호를 BCrypt 방식으로
     * 암호화하고 비교합니다.
     */
    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /*
     * 웹 요청별 접근 권한과
     * 로그인·로그아웃 방식을 설정합니다.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AgentApiKeyFilter agentApiKeyFilter,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            AuditLogoutSuccessHandler
                    auditLogoutSuccessHandler)
            throws Exception {

        /*
         * STS3 에이전트가 서버로 전송하는
         * POST 요청을 구분합니다.
         *
         * 이 요청은 로그인 세션 대신
         * AgentApiKeyFilter가 API Key를 검사합니다.
         */
        RequestMatcher agentPostRequest =
                request ->
                        "POST".equalsIgnoreCase(
                                request.getMethod()
                        )
                        && request.getRequestURI()
                                .startsWith(
                                        "/api/agents/"
                                );

        http
                /*
                 * 일반 로그인 인증 Filter보다 먼저
                 * 에이전트 API Key를 검사합니다.
                 */
                .addFilterBefore(
                        agentApiKeyFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                /*
                 * URL별 접근 권한을 설정합니다.
                 */
                .authorizeHttpRequests(
                        authorization ->
                                authorization

                                        /*
                                         * 로그인 화면과 정적 파일은
                                         * 로그인하지 않아도 접근할 수 있습니다.
                                         */
                                        .requestMatchers(
                                                "/login",
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/favicon.ico",
                                                "/error"
                                        )
                                        .permitAll()

                                        /*
                                         * ADMIN과 VIEWER 모두
                                         * 자신의 비밀번호를 변경할 수 있습니다.
                                         */
                                        .requestMatchers(
                                                HttpMethod.PUT,
                                                "/api/account/password"
                                        )
                                        .hasAnyRole(
                                                "ADMIN",
                                                "VIEWER"
                                        )

                                        /*
                                         * API Key 검사를 통과한
                                         * STS3 에이전트 POST 요청입니다.
                                         *
                                         * 에이전트는 관리자 로그인 세션을
                                         * 사용하지 않으므로 permitAll로 연결합니다.
                                         */
                                        .requestMatchers(
                                                agentPostRequest
                                        )
                                        .permitAll()

                                        /*
                                         * 에이전트 데이터 조회 요청은
                                         * ADMIN과 VIEWER 모두 허용합니다.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/agents/**"
                                        )
                                        .hasAnyRole(
                                                "ADMIN",
                                                "VIEWER"
                                        )

                                        /*
                                         * 에이전트 데이터 전체 수정은
                                         * ADMIN만 사용할 수 있습니다.
                                         */
                                        .requestMatchers(
                                                HttpMethod.PUT,
                                                "/api/agents/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        /*
                                         * 에이전트 데이터 일부 수정도
                                         * ADMIN만 사용할 수 있습니다.
                                         */
                                        .requestMatchers(
                                                HttpMethod.PATCH,
                                                "/api/agents/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        /*
                                         * 에이전트 데이터 삭제는
                                         * ADMIN만 사용할 수 있습니다.
                                         */
                                        .requestMatchers(
                                                HttpMethod.DELETE,
                                                "/api/agents/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        /*
                                         * 관리자 전용 API는
                                         * ADMIN만 접근할 수 있습니다.
                                         *
                                         * 사용자 비밀번호 초기화와
                                         * 계정 잠금 해제 기능이 포함됩니다.
                                         */
                                        .requestMatchers(
                                                "/api/admin/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        /*
                                         * 위에서 구체적으로 지정하지 않은
                                         * 나머지 에이전트 API는
                                         * ADMIN만 접근할 수 있습니다.
                                         */
                                        .requestMatchers(
                                                "/api/agents/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        /*
                                         * 대시보드와 나머지 요청은
                                         * 로그인한 사용자만 접근할 수 있습니다.
                                         */
                                        .anyRequest()
                                        .authenticated()
                )

                /*
                 * 일반 웹 요청에는 CSRF 보호를 유지합니다.
                 *
                 * 로그인 세션을 사용하지 않는
                 * STS3 에이전트 POST 요청만
                 * CSRF 검사에서 제외합니다.
                 */
                .csrf(
                        csrf ->
                                csrf.ignoringRequestMatchers(
                                        agentPostRequest
                                )
                )

                /*
                 * Spring Security 기본 로그인 화면을 사용합니다.
                 *
                 * 로그인 성공과 실패 결과는
                 * 각각 전용 Handler가 처리합니다.
                 */
                .formLogin(
                        form ->
                                form
                                        /*
                                         * 로그인 성공 시:
                                         *
                                         * 실패 횟수를 0으로 초기화하고
                                         * 마지막 로그인 성공 시각과
                                         * 감사 기록을 저장합니다.
                                         */
                                        .successHandler(
                                                loginSuccessHandler
                                        )

                                        /*
                                         * 로그인 실패 시:
                                         *
                                         * 실패 횟수를 증가시키고
                                         * 5회 도달 시 계정을 잠그며
                                         * 감사 기록을 저장합니다.
                                         */
                                        .failureHandler(
                                                loginFailureHandler
                                        )
                                        .permitAll()
                )

                /*
                 * Spring Security 로그아웃 설정입니다.
                 */
                .logout(
                        logout ->
                                logout

                                        /*
                                         * 로그아웃 요청 주소입니다.
                                         */
                                        .logoutUrl(
                                                "/logout"
                                        )

                                        /*
                                         * 로그아웃 성공 시
                                         * 감사 기록 Handler를 실행합니다.
                                         *
                                         * Handler가 기록 저장 후
                                         * /login?logout으로 이동시킵니다.
                                         */
                                        .logoutSuccessHandler(
                                                auditLogoutSuccessHandler
                                        )

                                        /*
                                         * 로그인 세션과
                                         * 인증 정보를 제거합니다.
                                         */
                                        .invalidateHttpSession(
                                                true
                                        )
                                        .clearAuthentication(
                                                true
                                        )

                                        /*
                                         * 브라우저의 세션 쿠키를 제거합니다.
                                         */
                                        .deleteCookies(
                                                "JSESSIONID"
                                        )
                                        .permitAll()
                );

        return http.build();
    }
}