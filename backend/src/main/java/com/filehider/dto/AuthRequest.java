package com.filehider.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Must provide a valid email format")
    private String email;

    private String name;

    @NotBlank(message = "OTP security code is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "OTP must be 4 to 6 numeric digits")
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
