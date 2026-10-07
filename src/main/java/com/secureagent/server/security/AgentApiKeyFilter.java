package com.secureagent.server.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * STS3 에이전트 전송 요청에 포함된
 * API Key를 검사합니다.
 */
@Component
public class AgentApiKeyFilter
        extends OncePerRequestFilter {

    /*
     * STS3 에이전트가 API Key를
     * 전달할 때 사용하는 HTTP 헤더 이름입니다.
     */
    public static final String API_KEY_HEADER =
            "X-Agent-Api-Key";

    /*
     * STS4 실행 환경변수에서 전달받은
     * 서버 측 API Key입니다.
     */
    private final String configuredApiKey;

    /*
     * application.properties의
     * secureagent.agent.api-key 값을 주입받습니다.
     *
     * 실제 값은 SECURE_AGENT_API_KEY
     * 환경변수에 저장되어 있습니다.
     */
    public AgentApiKeyFilter(
            @Value("${secureagent.agent.api-key:}")
            String configuredApiKey) {

        this.configuredApiKey =
                configuredApiKey;
    }

    /*
     * STS3 에이전트의 POST 전송 요청에만
     * API Key Filter를 적용합니다.
     *
     * 그 밖의 로그인, 화면 조회 등의 요청에는
     * 이 Filter를 적용하지 않습니다.
     */
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        boolean isAgentPostRequest =
                "POST".equalsIgnoreCase(
                        request.getMethod()
                )
                && request.getRequestURI()
                        .startsWith(
                                "/api/agents/"
                        );

        /*
         * true를 반환하면 Filter를 실행하지 않습니다.
         *
         * 따라서 에이전트 POST 요청일 때만
         * false를 반환하여 API Key를 검사합니다.
         */
        return !isAgentPostRequest;
    }

    /*
     * HTTP 요청에 포함된 API Key를 검사합니다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        /*
         * 서버에 API Key 환경변수가 없다면
         * 에이전트 전송 API를 열어두지 않고 차단합니다.
         */
        if (configuredApiKey == null
                || configuredApiKey.isBlank()) {

            writeErrorResponse(
                    response,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "서버에 에이전트 API Key가 설정되지 않았습니다."
            );

            return;
        }

        /*
         * STS3 에이전트가 전송한
         * X-Agent-Api-Key 헤더를 읽습니다.
         */
        String providedApiKey =
                request.getHeader(
                        API_KEY_HEADER
                );

        /*
         * 헤더가 없거나 API Key가 일치하지 않으면
         * 인증 실패로 요청을 차단합니다.
         */
        if (providedApiKey == null
                || providedApiKey.isBlank()
                || !isSameApiKey(
                        configuredApiKey,
                        providedApiKey
                )) {

            writeErrorResponse(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "에이전트 API Key가 없거나 올바르지 않습니다."
            );

            return;
        }

        /*
         * API Key가 일치하면 다음 Spring Security
         * Filter와 Controller로 요청을 전달합니다.
         */
        filterChain.doFilter(
                request,
                response
        );
    }

    /*
     * 일반 문자열 equals() 대신
     * MessageDigest.isEqual()을 사용합니다.
     *
     * 비교 시간 차이를 이용하여 API Key를
     * 추측하는 공격 가능성을 줄입니다.
     */
    private boolean isSameApiKey(
            String expectedApiKey,
            String providedApiKey) {

        byte[] expectedBytes =
                expectedApiKey.getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] providedBytes =
                providedApiKey.getBytes(
                        StandardCharsets.UTF_8
                );

        return MessageDigest.isEqual(
                expectedBytes,
                providedBytes
        );
    }

    /*
     * API Key 검사 실패 결과를
     * JSON 형식으로 반환합니다.
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message)
            throws IOException {

        response.setStatus(
                status.value()
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.getWriter().write(
                "{\"message\":\""
                + message
                + "\"}"
        );
    }
}