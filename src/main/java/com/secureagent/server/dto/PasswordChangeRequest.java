package com.secureagent.server.dto;

/*
 * 로그인한 사용자가 자신의 비밀번호를
 * 변경할 때 전달하는 요청 정보입니다.
 */
public class PasswordChangeRequest {

    /*
     * 현재 사용 중인 비밀번호입니다.
     *
     * 실제 계정 소유자인지 확인할 때 사용합니다.
     */
    private String currentPassword;

    /*
     * 새로 사용할 비밀번호입니다.
     */
    private String newPassword;

    /*
     * 새 비밀번호를 정확하게 입력했는지
     * 다시 확인하기 위한 값입니다.
     */
    private String confirmPassword;

    public String getCurrentPassword() {

        return currentPassword;
    }

    public void setCurrentPassword(
            String currentPassword) {

        this.currentPassword =
                currentPassword;
    }

    public String getNewPassword() {

        return newPassword;
    }

    public void setNewPassword(
            String newPassword) {

        this.newPassword =
                newPassword;
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