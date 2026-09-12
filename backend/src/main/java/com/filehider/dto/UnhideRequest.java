package com.filehider.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UnhideRequest {

    @NotNull(message = "File identifier is required")
    @Positive(message = "File identifier must be positive")
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
