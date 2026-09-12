package com.filehider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResponseDto {
    private Integer id;
    private String fileName;
    private String path;
    private String email;
    private long size;
    private String checksum;
    private String contentType;
    private String cipherAlgorithm = "AES-256-GCM";
    private LocalDateTime createdAt;

    public FileResponseDto() {
    }

    public FileResponseDto(Integer id, String fileName, String path, String email, long size) {
        this.id = id;
        this.fileName = fileName;
        this.path = path;
        this.email = email;
        this.size = size;
    }

    public FileResponseDto(Integer id, String fileName, String path, String email, long size, String checksum, String contentType, LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.path = path;
        this.email = email;
        this.size = size;
        this.checksum = checksum;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public void setCipherAlgorithm(String cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
