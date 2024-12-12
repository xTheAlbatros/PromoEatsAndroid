package com.example.promoeatsandroid.models;

public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmationPassword;

    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmationPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmationPassword = confirmationPassword;
    }

    // Gettery
    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmationPassword() {
        return confirmationPassword;
    }
}
