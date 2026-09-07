package com.filehider.dto;

public class HidePathRequest {
    private String email;
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
