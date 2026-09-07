package com.filehider.service;

import com.filehider.dto.FileResponseDto;
import com.filehider.entity.VaultFile;
import com.filehider.repository.VaultFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VaultService {

    private static final Logger log = LoggerFactory.getLogger(VaultService.class);

    private final VaultFileRepository vaultFileRepository;

    @Autowired
    public VaultService(VaultFileRepository vaultFileRepository) {
        this.vaultFileRepository = vaultFileRepository;
    }

    public List<FileResponseDto> getFilesByEmail(String email) {
        List<VaultFile> files = vaultFileRepository.findByEmailOrderByIdDesc(email);
        return files.stream().map(f -> new FileResponseDto(
                f.getId(),
                f.getFileName(),
                f.getPath(),
                f.getEmail(),
                f.getBinData() != null ? f.getBinData().length : 0)).collect(Collectors.toList());
    }

    @Transactional
    public VaultFile hideUploadedFile(MultipartFile multipartFile, String email, String customPath) throws IOException {
        String fileName = multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename()
                : "vault_file.bin";
        byte[] bytes = multipartFile.getBytes();

        // Check if this uploaded file exists locally on user's machine (Desktop,
        // Downloads, Documents, etc.)
        File localFile = resolveLocalFile(fileName);
        String path = (localFile != null) ? localFile.getAbsolutePath()
                : ((customPath != null && !customPath.trim().isEmpty()) ? customPath : "VaultStorage://" + fileName);

        VaultFile vaultFile = new VaultFile(fileName, path, email, bytes);
        VaultFile saved = vaultFileRepository.save(vaultFile);

        // If matching local original file was found, wipe/delete it from disk!
        if (localFile != null && localFile.exists() && localFile.isFile()) {
            wipeFileFromDisk(localFile);
        }

        return saved;
    }

    @Transactional
    public VaultFile hideLocalFile(String inputPath, String email) throws IOException {
        File file = resolveLocalFile(inputPath);
        if (file == null) {
            throw new IllegalArgumentException(
                    "File not found: \"" + inputPath + "\". Please ensure the path or file name is correct.");
        }

        String resolvedAbsolutePath = file.getAbsolutePath();
        byte[] bytes = Files.readAllBytes(file.toPath());
        String fileName = file.getName();

        VaultFile vaultFile = new VaultFile(fileName, resolvedAbsolutePath, email, bytes);
        VaultFile saved = vaultFileRepository.save(vaultFile);

        // Delete original file from disk after saving to database
        wipeFileFromDisk(file);

        return saved;
    }

    private boolean wipeFileFromDisk(File file) {
        if (file == null || !file.exists()) return true;
        String absPath = file.getAbsolutePath();
        try {
            file.setWritable(true, false);
            file.setReadable(true, false);
            file.setExecutable(true, false);

            boolean deleted = file.delete();
            if (!deleted) {
                try {
                    deleted = Files.deleteIfExists(file.toPath());
                } catch (Exception ignored) {}
            }

            if (!deleted) {
                // Windows force delete via cmd.exe
                try {
                    Process p = new ProcessBuilder("cmd.exe", "/c", "del", "/f", "/q", "\"" + absPath + "\"").start();
                    p.waitFor();
                    deleted = !file.exists();
                } catch (Exception ignored) {}
            }

            if (deleted) {
                log.info("Original file successfully wiped from PC disk: {}", absPath);
                return true;
            } else {
                log.warn("Could not wipe file from disk after all attempts: {}", absPath);
                return false;
            }
        } catch (Exception e) {
            log.error("Exception while wiping file from disk ({}): {}", absPath, e.getMessage());
            return false;
        }
    }

    private File resolveLocalFile(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty())
            return null;
        String path = rawPath.trim();
        // Strip leading/trailing quotes if user copied path from Windows Explorer ("Copy as path")
        if ((path.startsWith("\"") && path.endsWith("\"")) || (path.startsWith("'") && path.endsWith("'"))) {
            path = path.substring(1, path.length() - 1).trim();
        }

        File direct = new File(path);
        if (direct.exists() && direct.isFile()) {
            return direct;
        }

        // Try user home, desktop, onedrive desktop, downloads, documents, workspace
        String userHome = System.getProperty("user.home");
        String userDir = System.getProperty("user.dir");
        String parentDir = new File(userDir).getParent();

        String[] candidateDirs = new String[] {
                userHome + File.separator + "Desktop",
                userHome + File.separator + "OneDrive" + File.separator + "Desktop",
                userHome + File.separator + "Downloads",
                userHome + File.separator + "Documents",
                userHome + File.separator + "OneDrive" + File.separator + "Documents",
                userHome + File.separator + "Pictures",
                userHome + File.separator + "OneDrive" + File.separator + "Pictures",
                userHome + File.separator + "Videos",
                userHome + File.separator + "Music",
                userHome,
                userDir,
                parentDir
        };

        for (String dirPath : candidateDirs) {
            if (dirPath != null) {
                File dir = new File(dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    File candidate = new File(dir, path);
                    if (candidate.exists() && candidate.isFile()) {
                        return candidate;
                    }
                    // Recursive search 3 levels deep
                    File deepFound = findFileInDir(dir, path, 3);
                    if (deepFound != null) {
                        return deepFound;
                    }
                }
            }
        }

        return null;
    }

    private File findFileInDir(File dir, String targetName, int depth) {
        if (dir == null || !dir.exists() || !dir.isDirectory() || depth <= 0) return null;
        try {
            File[] files = dir.listFiles();
            if (files == null) return null;
            for (File f : files) {
                if (f.isFile() && f.getName().equalsIgnoreCase(targetName)) {
                    return f;
                }
                if (f.isDirectory() && !f.isHidden() && !f.getName().startsWith(".")) {
                    File sub = findFileInDir(f, targetName, depth - 1);
                    if (sub != null) return sub;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Transactional
    public void unhideFile(Integer id) throws IOException {
        Optional<VaultFile> optional = vaultFileRepository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("No file found in vault with id: " + id);
        }

        VaultFile vaultFile = optional.get();
        String originalPath = vaultFile.getPath();
        String fileName = vaultFile.getFileName() != null ? vaultFile.getFileName() : "restored_file.bin";
        byte[] data = vaultFile.getBinData() != null ? vaultFile.getBinData() : new byte[0];

        File target = null;
        if (originalPath != null && !originalPath.startsWith("VaultStorage://") && (originalPath.contains(File.separator) || originalPath.contains("/") || originalPath.contains(":"))) {
            String targetPath = originalPath.trim();
            if ((targetPath.startsWith("\"") && targetPath.endsWith("\"")) || (targetPath.startsWith("'") && targetPath.endsWith("'"))) {
                targetPath = targetPath.substring(1, targetPath.length() - 1).trim();
            }
            target = new File(targetPath);
        } else {
            // Default fallback restore location: User's Desktop
            String userHome = System.getProperty("user.home");
            File oneDriveDesktop = new File(userHome + File.separator + "OneDrive" + File.separator + "Desktop");
            File standardDesktop = new File(userHome + File.separator + "Desktop");
            File targetDir = oneDriveDesktop.exists() ? oneDriveDesktop : (standardDesktop.exists() ? standardDesktop : new File(userHome + File.separator + "Downloads"));
            target = new File(targetDir, fileName);
        }

        if (target != null) {
            try {
                if (target.getParentFile() != null) {
                    target.getParentFile().mkdirs();
                }
                Files.write(target.toPath(), data);
                log.info("Restored unhidden file to PC at: {}", target.getAbsolutePath());
            } catch (Exception e) {
                log.warn("Could not write restored file to disk ({}): {}", target.getAbsolutePath(), e.getMessage());
            }
        }

        // Delete record from database vault
        vaultFileRepository.deleteById(id);
    }

    public Optional<VaultFile> getFileById(Integer id) {
        return vaultFileRepository.findById(id);
    }
}
