package com.filehider.dto;

public class UnhideRequest {
    private Integer id;

    public UnhideRequest() {
    }

    public UnhideRequest(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
