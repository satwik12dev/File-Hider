package com.filehider.dto;

public class SendOtpRequest {
    private String email;
    private String name;
    private String mode; // "login" or "signup"

    public SendOtpRequest() {
    }

    public SendOtpRequest(String email, String name, String mode) {
        this.email = email;
        this.name = name;
        this.mode = mode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
