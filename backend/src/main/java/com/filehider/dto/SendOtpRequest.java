package com.filehider.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SendOtpRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Must provide a valid email format")
    private String email;

    private String name;

    @Pattern(regexp = "(?i)^(login|signup)$", message = "Mode must be either 'login' or 'signup'")
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
