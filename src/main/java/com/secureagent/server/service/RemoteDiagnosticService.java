package com.secureagent.server.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.secureagent.server.dto.RemoteDiagnosticRequest;
import com.secureagent.server.dto.RemoteDiagnosticResponse;

/*
 * OpenSSH Client를 이용하여
 * 원격 Windows PC에 접속하고,
 * 허용된 안전한 진단 명령만 실행합니다.
 */
@Service
public class RemoteDiagnosticService {

    /*
     * SSH 연결 제한 시간입니다.
     */
    private static final int CONNECT_TIMEOUT_SECONDS = 5;

    /*
     * 전체 명령 실행 제한 시간입니다.
     */
    private static final int COMMAND_TIMEOUT_SECONDS = 15;

    /*
     * 관리자 화면에 반환할 수 있는
     * 최대 출력 문자 수입니다.
     */
    private static final int MAXIMUM_OUTPUT_LENGTH = 20000;

    /*
     * IP 주소 또는 일반 호스트 이름에
     * 사용할 수 있는 문자만 허용합니다.
     */
    private static final Pattern HOST_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9][A-Za-z0-9.-]{0,252}$"
            );

    /*
     * SSH 사용자 이름에 사용할 수 있는
     * 안전한 문자만 허용합니다.
     */
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._-]{1,64}$"
            );

    /*
     * SSH 인증에 사용할 개인키 파일 경로입니다.
     * 실제 경로는 환경변수에서 받습니다.
     */
    private final String privateKeyPath;

    /*
     * 신뢰하는 대상 PC의 SSH Host Key가
     * 저장된 known_hosts 파일 경로입니다.
     */
    private final String knownHostsPath;

    /*
     * SSH 접속을 허용할 대상 PC 목록입니다.
     */
    private final Set<String> allowedHosts;

    public RemoteDiagnosticService(

            @Value("${secure.ssh.private-key-path:}")
            String privateKeyPath,

            @Value("${secure.ssh.known-hosts-path:}")
            String knownHostsPath,

            @Value("${secure.ssh.allowed-hosts:}")
            String allowedHostsText) {

        this.privateKeyPath = privateKeyPath;
        this.knownHostsPath = knownHostsPath;
        this.allowedHosts =
                parseAllowedHosts(allowedHostsText);
    }

    /*
     * 요청값을 검사하고 SSH를 통해
     * 안전한 진단 명령을 실행합니다.
     */
    public RemoteDiagnosticResponse executeDiagnostic(
            RemoteDiagnosticRequest request) {

        validateRequest(request);

        Path privateKeyFile =
                resolveRequiredFile(
                        privateKeyPath,
                        "SSH 개인키"
                );

        Path knownHostsFile =
                resolveRequiredFile(
                        knownHostsPath,
                        "known_hosts"
                );

        List<String> sshCommand =
                buildSshCommand(
                        request,
                        privateKeyFile,
                        knownHostsFile
                );

        Process sshProcess = null;

        ExecutorService outputReaderExecutor = null;

        try {
            /*
             * cmd.exe나 PowerShell을 거치지 않고
             * ssh.exe를 직접 실행합니다.
             *
             * 요청값이 운영체제 셸에서
             * 다시 해석되지 않도록 합니다.
             */
            ProcessBuilder processBuilder =
                    new ProcessBuilder(sshCommand);

            /*
             * 오류 출력도 일반 출력과 함께
             * 안전하게 수집합니다.
             */
            processBuilder.redirectErrorStream(true);

            sshProcess = processBuilder.start();

            Process runningProcess = sshProcess;

            /*
             * 출력이 많아 프로세스가 멈추지 않도록
             * 별도 작업에서 출력을 계속 읽습니다.
             */
            outputReaderExecutor =
                    Executors.newSingleThreadExecutor();

            Future<String> outputFuture =
                    outputReaderExecutor.submit(
                            () -> readOutput(
                                    runningProcess
                                            .getInputStream()
                            )
                    );

            /*
             * 정해진 시간 안에 명령이
             * 종료되는지 확인합니다.
             */
            boolean completed =
                    sshProcess.waitFor(
                            COMMAND_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            if (!completed) {

                sshProcess.destroyForcibly();
                outputFuture.cancel(true);

                return createResponse(
                        request,
                        false,
                        null,
                        "원격 진단 실행 시간이 "
                        + COMMAND_TIMEOUT_SECONDS
                        + "초를 초과했습니다."
                );
            }

            String output =
                    outputFuture.get(
                            2,
                            TimeUnit.SECONDS
                    );

            int exitCode =
                    sshProcess.exitValue();

            if (exitCode != 0) {

                return createResponse(
                        request,
                        false,
                        output,
                        "SSH 원격 진단에 실패했습니다. "
                        + "종료 코드: "
                        + exitCode
                );
            }

            return createResponse(
                    request,
                    true,
                    output,
                    "원격 진단을 완료했습니다."
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "SSH Client를 실행하지 못했습니다. "
                    + "SecureServer PC에 OpenSSH Client가 "
                    + "설치되어 있는지 확인하세요.",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "SSH 원격 진단 작업이 중단되었습니다.",
                    exception
            );

        } catch (ExecutionException exception) {

            throw new IllegalStateException(
                    "SSH 실행 결과를 읽지 못했습니다.",
                    exception
            );

        } catch (TimeoutException exception) {

            throw new IllegalStateException(
                    "SSH 실행 결과를 읽는 시간이 "
                    + "초과되었습니다.",
                    exception
            );

        } finally {

            if (sshProcess != null
                    && sshProcess.isAlive()) {

                sshProcess.destroyForcibly();
            }

            if (outputReaderExecutor != null) {

                outputReaderExecutor.shutdownNow();
            }
        }
    }

    /*
     * ssh.exe에 전달할 실행 인수를 만듭니다.
     */
    private List<String> buildSshCommand(
            RemoteDiagnosticRequest request,
            Path privateKeyFile,
            Path knownHostsFile) {

        List<String> command =
                new ArrayList<>();

        command.add("ssh");

        /*
         * 비밀번호 입력과 사용자 확인창을
         * 표시하지 않는 자동 실행 모드입니다.
         */
        command.add("-o");
        command.add("BatchMode=yes");

        /*
         * 비밀번호 인증을 사용하지 않고
         * 공개키 인증만 허용합니다.
         */
        command.add("-o");
        command.add("PasswordAuthentication=no");

        command.add("-o");
        command.add("KbdInteractiveAuthentication=no");

        command.add("-o");
        command.add("PubkeyAuthentication=yes");

        command.add("-o");
        command.add("IdentitiesOnly=yes");

        /*
         * 대상 PC의 Host Key가 known_hosts와
         * 일치하지 않으면 접속을 거부합니다.
         */
        command.add("-o");
        command.add("StrictHostKeyChecking=yes");

        command.add("-o");
        command.add(
                "UserKnownHostsFile="
                + knownHostsFile
                        .toAbsolutePath()
                        .normalize()
        );

        /*
         * SSH 연결 시간을 제한합니다.
         */
        command.add("-o");
        command.add(
                "ConnectTimeout="
                + CONNECT_TIMEOUT_SECONDS
        );

        /*
         * Agent·X11·포트 전달 기능을
         * 사용하지 않습니다.
         */
        command.add("-o");
        command.add("ForwardAgent=no");

        command.add("-o");
        command.add("ForwardX11=no");

        command.add("-o");
        command.add("ClearAllForwardings=yes");

        /*
         * 대화형 터미널과 표준 입력을
         * 사용하지 않습니다.
         */
        command.add("-T");
        command.add("-n");

        /*
         * SSH 개인키를 지정합니다.
         */
        command.add("-i");
        command.add(
                privateKeyFile
                        .toAbsolutePath()
                        .normalize()
                        .toString()
        );

        /*
         * SSH 포트를 지정합니다.
         */
        command.add("-p");
        command.add(
                String.valueOf(
                        request.getPort()
                )
        );

        /*
         * 사용자명과 대상 호스트를 지정합니다.
         */
        command.add(
                request.getUsername()
                + "@"
                + request.getHost()
        );

        /*
         * 사용자가 전달한 문자열이 아니라
         * Enum에 등록된 고정 명령만 실행합니다.
         */
        command.add(
                request
                    .getCommandType()
                    .getCommandText()
        );

        return command;
    }

    /*
     * 원격 진단 요청값을 검사합니다.
     */
    private void validateRequest(
            RemoteDiagnosticRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "원격 진단 요청 정보가 없습니다."
            );
        }

        String host =
                normalizeRequiredText(
                        request.getHost(),
                        "대상 PC 주소"
                );

        String username =
                normalizeRequiredText(
                        request.getUsername(),
                        "SSH 사용자 이름"
                );

        if (!HOST_PATTERN.matcher(host).matches()
                || host.contains("..")
                || host.endsWith(".")) {

            throw new IllegalArgumentException(
                    "대상 PC 주소 형식이 올바르지 않습니다."
            );
        }

        if (!USERNAME_PATTERN
                .matcher(username)
                .matches()) {

            throw new IllegalArgumentException(
                    "SSH 사용자 이름 형식이 "
                    + "올바르지 않습니다."
            );
        }

        if (request.getPort() < 1
                || request.getPort() > 65535) {

            throw new IllegalArgumentException(
                    "SSH 포트는 1부터 65535 "
                    + "사이여야 합니다."
            );
        }

        if (request.getCommandType() == null) {

            throw new IllegalArgumentException(
                    "원격 진단 명령 종류가 없습니다."
            );
        }

        /*
         * 환경변수에 등록되지 않은 PC로
         * 접속하는 요청은 거부합니다.
         */
        if (allowedHosts.isEmpty()) {

            throw new IllegalStateException(
                    "서버에 SSH 접속 허용 대상이 "
                    + "설정되지 않았습니다."
            );
        }

        if (!allowedHosts.contains(
                host.toLowerCase(Locale.ROOT))) {

            throw new IllegalArgumentException(
                    "SSH 접속이 허용되지 않은 "
                    + "대상 PC입니다."
            );
        }

        request.setHost(host);
        request.setUsername(username);
    }

    /*
     * 필수 문자열의 null, 공백을 검사합니다.
     */
    private String normalizeRequiredText(
            String value,
            String fieldName) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + "이(가) 없습니다."
            );
        }

        return value.trim();
    }

    /*
     * SSH 설정 파일이 실제로 존재하는지
     * 확인합니다.
     */
    private Path resolveRequiredFile(
            String configuredPath,
            String description) {

        if (configuredPath == null
                || configuredPath.isBlank()) {

            throw new IllegalStateException(
                    description
                    + " 파일 경로가 설정되지 않았습니다."
            );
        }

        try {
            Path path =
            		Path.of(configuredPath.strip())
                        .toAbsolutePath()
                        .normalize();

            if (!Files.isRegularFile(path)) {

                throw new IllegalStateException(
                        description
                        + " 파일을 찾을 수 없습니다."
                );
            }

            return path;

        } catch (InvalidPathException exception) {

            throw new IllegalStateException(
                    description
                    + " 파일 경로가 올바르지 않습니다.",
                    exception
            );
        }
    }

    /*
     * SSH가 반환하는 출력을 계속 읽으면서
     * 최대 길이까지만 저장합니다.
     */
    private String readOutput(
            InputStream inputStream)
            throws IOException {

        StringBuilder outputBuilder =
                new StringBuilder();

        boolean truncated = false;

        try (BufferedReader reader =
                new BufferedReader(
                		new InputStreamReader(
                		        inputStream,
                		        Charset.forName("MS949")
                        )
                )) {

            String line;

            while ((line = reader.readLine())
                    != null) {

                String outputLine =
                        line
                        + System.lineSeparator();

                int remainingLength =
                        MAXIMUM_OUTPUT_LENGTH
                        - outputBuilder.length();

                if (remainingLength > 0) {

                    int appendLength =
                            Math.min(
                                    remainingLength,
                                    outputLine.length()
                            );

                    outputBuilder.append(
                            outputLine,
                            0,
                            appendLength
                    );
                }

                if (outputLine.length()
                        > remainingLength) {

                    truncated = true;
                }
            }
        }

        String output =
                outputBuilder
                    .toString()
                    .trim();

        if (truncated) {

            output += System.lineSeparator()
                    + "[출력 길이 제한으로 "
                    + "나머지 결과는 생략했습니다.]";
        }

        return output;
    }

    /*
     * Controller에 반환할 응답 객체를 만듭니다.
     */
    private RemoteDiagnosticResponse createResponse(
            RemoteDiagnosticRequest request,
            boolean success,
            String output,
            String message) {

        RemoteDiagnosticResponse response =
                new RemoteDiagnosticResponse();

        response.setSuccess(success);
        response.setHost(request.getHost());
        response.setPort(request.getPort());
        response.setUsername(request.getUsername());
        response.setCommandType(
                request.getCommandType()
        );
        response.setOutput(output);
        response.setMessage(message);
        response.setExecutedAt(
                LocalDateTime.now()
        );

        return response;
    }

    /*
     * 쉼표로 구분된 SSH 허용 대상 목록을
     * Set으로 변환합니다.
     */
    private Set<String> parseAllowedHosts(
            String allowedHostsText) {

        if (allowedHostsText == null
                || allowedHostsText.isBlank()) {

            return Set.of();
        }

        return Arrays.stream(
                        allowedHostsText.split(",")
                )
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value ->
                        value.toLowerCase(
                                Locale.ROOT
                        )
                )
                .collect(
                        Collectors.toUnmodifiableSet()
                );
    }
}