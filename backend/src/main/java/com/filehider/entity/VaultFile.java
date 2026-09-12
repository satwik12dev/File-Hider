package com.filehider.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "data", indexes = {
        @Index(name = "idx_vault_email", columnList = "email")
})
public class VaultFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nameoffile", nullable = false)
    private String fileName;

    @Column(name = "path")
    private String path;

    @Column(name = "email", nullable = false)
    private String email;

    @Lob
    @Column(name = "bin_data", columnDefinition = "LONGBLOB")
    private byte[] binData;

    @Column(name = "iv")
    private byte[] iv;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public VaultFile() {
    }

    public VaultFile(String fileName, String path, String email, byte[] binData) {
        this.fileName = fileName;
        this.path = path;
        this.email = email;
        this.binData = binData;
    }

    public VaultFile(String fileName, String path, String email, byte[] binData, byte[] iv, String checksum, Long fileSize, String contentType) {
        this.fileName = fileName;
        this.path = path;
        this.email = email;
        this.binData = binData;
        this.iv = iv;
        this.checksum = checksum;
        this.fileSize = fileSize;
        this.contentType = contentType;
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

    public byte[] getBinData() {
        return binData;
    }

    public void setBinData(byte[] binData) {
        this.binData = binData;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
