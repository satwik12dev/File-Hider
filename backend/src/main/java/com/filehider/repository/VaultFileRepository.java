package com.filehider.repository;

import com.filehider.entity.VaultFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultFileRepository extends JpaRepository<VaultFile, Integer> {
    List<VaultFile> findByEmailOrderByIdDesc(String email);
    List<VaultFile> findByEmail(String email);
    Optional<VaultFile> findByIdAndEmail(Integer id, String email);
    boolean existsByIdAndEmail(Integer id, String email);
    void deleteByIdAndEmail(Integer id, String email);
}
