package com.secureagent.server.dto;

/*
 * ADMIN이 일반 사용자의 비밀번호를
 * 초기화할 때 전달받는 요청 정보입니다.
 */
public class AdminPasswordResetRequest {

    /*
     * 새로 설정할 비밀번호입니다.
     */
    private String newPassword;

    /*
     * 새 비밀번호 확인값입니다.
     */
    private String confirmPassword;

    public String getNewPassword() {

        return newPassword;
    }

    public void setNewPassword(
            String newPassword) {

        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {

        return confirmPassword;
    }

    public void setConfirmPassword(
            String confirmPassword) {

        this.confirmPassword =
                confirmPassword;
    }
}