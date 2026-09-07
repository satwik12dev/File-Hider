package com.filehider.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "data")
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

    public VaultFile() {
    }

    public VaultFile(String fileName, String path, String email, byte[] binData) {
        this.fileName = fileName;
        this.path = path;
        this.email = email;
        this.binData = binData;
    }

    public VaultFile(Integer id, String fileName, String path, String email, byte[] binData) {
        this.id = id;
        this.fileName = fileName;
        this.path = path;
        this.email = email;
        this.binData = binData;
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
}
