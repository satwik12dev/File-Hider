package com.filehider.dto;

public class FileResponseDto {
    private Integer id;
    private String fileName;
    private String path;
    private String email;
    private long size;

    public FileResponseDto() {
    }

    public FileResponseDto(Integer id, String fileName, String path, String email, long size) {
        this.id = id;
        this.fileName = fileName;
        this.path = path;
        this.email = email;
        this.size = size;
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
}
