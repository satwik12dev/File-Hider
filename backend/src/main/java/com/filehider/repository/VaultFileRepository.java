package com.filehider.repository;

import com.filehider.entity.VaultFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaultFileRepository extends JpaRepository<VaultFile, Integer> {
    List<VaultFile> findByEmailOrderByIdDesc(String email);
    List<VaultFile> findByEmail(String email);
}
