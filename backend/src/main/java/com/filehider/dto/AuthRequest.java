package com.filehider.dto;

public class AuthRequest {
    private String email;
    private String name;
    private String otp;

    public AuthRequest() {
    }

    public AuthRequest(String email, String name, String otp) {
        this.email = email;
        this.name = name;
        this.otp = otp;
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

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
