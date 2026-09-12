package com.filehider.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HidePathRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Must provide a valid email format")
    private String email;

    @NotBlank(message = "File path is required")
    @Size(min = 1, max = 1024, message = "File path exceeds maximum length")
    private String path;

    public HidePathRequest() {
    }

    public HidePathRequest(String email, String path) {
        this.email = email;
        this.path = path;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
