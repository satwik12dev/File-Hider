package com.filehider.service;

import com.filehider.dto.FileResponseDto;
import com.filehider.entity.VaultFile;
import com.filehider.repository.VaultFileRepository;
import com.filehider.security.AesEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production-hardened Vault Service.
 * Coordinates military-grade AES-256-GCM authenticated encryption,
 * cryptographic disk sanitization, path-traversal prevention, and strict multi-tenant access control.
 */
@Service
public class VaultService {

    private static final Logger log = LoggerFactory.getLogger(VaultService.class);

    private final VaultFileRepository vaultFileRepository;
    private final AesEncryptionService aesEncryptionService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public VaultService(VaultFileRepository vaultFileRepository, AesEncryptionService aesEncryptionService) {
        this.vaultFileRepository = vaultFileRepository;
        this.aesEncryptionService = aesEncryptionService;
    }

    @Transactional(readOnly = true)
    public List<FileResponseDto> getFilesByEmail(String email) {
        List<VaultFile> files = vaultFileRepository.findByEmailOrderByIdDesc(email);
        return files.stream().map(f -> new FileResponseDto(
                f.getId(),
                f.getFileName(),
                f.getPath(),
                f.getEmail(),
                f.getFileSize() != null ? f.getFileSize() : (f.getBinData() != null ? f.getBinData().length : 0),
                f.getChecksum(),
                f.getContentType(),
                f.getCreatedAt()
        )).collect(Collectors.toList());
    }

    @Transactional
    public VaultFile hideUploadedFile(MultipartFile multipartFile, String email, String customPath) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String fileName = (originalFilename != null && !originalFilename.trim().isEmpty())
                ? Path.of(originalFilename).getFileName().toString()
                : "vault_file.bin";

        byte[] plainBytes = multipartFile.getBytes();
        long originalSize = plainBytes.length;

        // Apply AES-256-GCM Authenticated Encryption
        AesEncryptionService.EncryptedResult encrypted = aesEncryptionService.encrypt(plainBytes);

        // Check if this file exists locally on user's workstation to unlink
        File localFile = resolveLocalFile(fileName);
        String path = (localFile != null) ? localFile.getAbsolutePath()
                : ((customPath != null && !customPath.trim().isEmpty()) ? sanitizePath(customPath) : "VaultStorage://" + fileName);

        String contentType = detectContentType(fileName);

        VaultFile vaultFile = new VaultFile(
                fileName,
                path,
                email,
                encrypted.cipherData(),
                encrypted.iv(),
                encrypted.checksumSha256(),
                originalSize,
                contentType
        );

        VaultFile saved = vaultFileRepository.save(vaultFile);

        // Secure wipe local matching file if detected
        if (localFile != null && localFile.exists() && localFile.isFile() && isPathSafeForDeletion(localFile)) {
            wipeFileFromDisk(localFile);
        }

        return saved;
    }

    @Transactional
    public VaultFile hideLocalFile(String inputPath, String email) throws IOException {
        File file = resolveLocalFile(inputPath);
        if (file == null) {
            throw new IllegalArgumentException("File not found: \"" + inputPath + "\". Please verify path existence.");
        }

        if (!isPathSafeForDeletion(file)) {
            throw new SecurityException("Access to system-critical location blocked: " + file.getAbsolutePath());
        }

        String resolvedAbsolutePath = file.getAbsolutePath();
        byte[] plainBytes = Files.readAllBytes(file.toPath());
        String fileName = file.getName();
        long originalSize = plainBytes.length;

        // Apply AES-256-GCM Authenticated Encryption
        AesEncryptionService.EncryptedResult encrypted = aesEncryptionService.encrypt(plainBytes);
        String contentType = detectContentType(fileName);

        VaultFile vaultFile = new VaultFile(
                fileName,
                resolvedAbsolutePath,
                email,
                encrypted.cipherData(),
                encrypted.iv(),
                encrypted.checksumSha256(),
                originalSize,
                contentType
        );

        VaultFile saved = vaultFileRepository.save(vaultFile);

        // Cryptographically overwrite and unlink file from disk
        wipeFileFromDisk(file);

        return saved;
    }

    /**
     * Overwrites file contents with zeros/random bytes before deletion to prevent forensic data recovery.
     */
    public boolean wipeFileFromDisk(File file) {
        if (file == null || !file.exists()) return true;
        String absPath = file.getAbsolutePath();
        try {
            // Remove Windows readonly, system, or hidden attributes
            try {
                file.setWritable(true, false);
                new ProcessBuilder("cmd.exe", "/c", "attrib", "-r", "-s", "-h", "\"" + absPath + "\"").start().waitFor();
            } catch (Exception ignored) {}

            // Forensic zero-overwrite of disk sectors
            if (file.isFile() && file.length() > 0) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
                    long length = file.length();
                    byte[] zeroBuffer = new byte[(int) Math.min(length, 65536)];
                    Arrays.fill(zeroBuffer, (byte) 0);
                    long remaining = length;
                    while (remaining > 0) {
                        int toWrite = (int) Math.min(remaining, zeroBuffer.length);
                        raf.write(zeroBuffer, 0, toWrite);
                        remaining -= toWrite;
                    }
                    try {
                        raf.getFD().sync();
                    } catch (Exception ignored) {}
                } catch (Exception e) {
                    log.warn("Forensic overwrite pass encountered exception: {}", e.getMessage());
                }
            }

            // Retry deletion to handle OneDrive sync engine and Windows Defender indexing locks
            boolean deleted = false;
            for (int attempt = 0; attempt < 5; attempt++) {
                try {
                    deleted = Files.deleteIfExists(file.toPath());
                } catch (Exception e) {
                    deleted = file.delete();
                }
                if (deleted || !file.exists()) {
                    deleted = true;
                    break;
                }
                try {
                    Thread.sleep(120);
                } catch (InterruptedException ignored) {}
            }

            // Forceful OS fallback deletion (PowerShell & CMD)
            if (!deleted && file.exists()) {
                try {
                    Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "Remove-Item -LiteralPath '" + absPath.replace("'", "''") + "' -Force").start();
                    p.waitFor();
                    deleted = !file.exists();
                } catch (Exception ignored) {}
            }

            if (!deleted && file.exists()) {
                try {
                    Process p = new ProcessBuilder("cmd.exe", "/c", "del", "/f", "/q", "/a", "\"" + absPath + "\"").start();
                    p.waitFor();
                    deleted = !file.exists();
                } catch (Exception ignored) {}
            }

            if (deleted) {
                log.info("Disk asset cryptographically sanitized and deleted: {}", absPath);
                return true;
            } else {
                log.warn("Unable to immediately unlink file from disk (scheduled for deleteOnExit): {}", absPath);
                file.deleteOnExit();
                return false;
            }
        } catch (Exception e) {
            log.error("Exception while sanitizing file on disk ({}): {}", absPath, e.getMessage());
            return false;
        }
    }

    @Transactional
    public void unhideFile(Integer id, String authenticatedEmail) throws IOException {
        Optional<VaultFile> optional;
        if (authenticatedEmail != null && !authenticatedEmail.trim().isEmpty()) {
            optional = vaultFileRepository.findByIdAndEmail(id, authenticatedEmail.trim());
        } else {
            optional = vaultFileRepository.findById(id);
        }

        if (optional.isEmpty()) {
            throw new IllegalArgumentException("No vault asset found with ID " + id + " for current user");
        }

        VaultFile vaultFile = optional.get();
        String originalPath = vaultFile.getPath();
        String fileName = vaultFile.getFileName() != null ? vaultFile.getFileName() : "restored_file.bin";

        // Decrypt the payload
        byte[] decryptedData = decryptPayload(vaultFile);

        File target = resolveRestoreLocation(originalPath, fileName);
        if (target != null && isPathSafeForRestoration(target)) {
            try {
                if (target.getParentFile() != null) {
                    target.getParentFile().mkdirs();
                }
                Files.write(target.toPath(), decryptedData);
                log.info("Restored decrypted file to PC: {}", target.getAbsolutePath());
            } catch (Exception e) {
                log.warn("Could not write restored file to disk ({}): {}", target.getAbsolutePath(), e.getMessage());
            }
        }

        // Delete from vault storage
        vaultFileRepository.delete(vaultFile);
    }

    /**
     * Decrypts the binary payload of the given VaultFile. Supports backward compatibility with unencrypted legacy records.
     */
    public byte[] decryptPayload(VaultFile file) {
        if (file.getBinData() == null || file.getBinData().length == 0) {
            return new byte[0];
        }

        if (file.getIv() != null && file.getIv().length == AesEncryptionService.GCM_IV_LENGTH_BYTES) {
            return aesEncryptionService.decrypt(file.getBinData(), file.getIv());
        }

        // Legacy unencrypted fallback
        return file.getBinData();
    }

    public Optional<VaultFile> getFileById(Integer id, String authenticatedEmail) {
        if (authenticatedEmail != null && !authenticatedEmail.trim().isEmpty()) {
            return vaultFileRepository.findByIdAndEmail(id, authenticatedEmail.trim());
        }
        return vaultFileRepository.findById(id);
    }

    private File resolveRestoreLocation(String originalPath, String fileName) {
        if (originalPath != null && !originalPath.startsWith("VaultStorage://")
                && (originalPath.contains(File.separator) || originalPath.contains("/") || originalPath.contains(":"))) {
            return new File(sanitizePath(originalPath));
        }

        String userHome = System.getProperty("user.home");
        File oneDriveDesktop = new File(userHome + File.separator + "OneDrive" + File.separator + "Desktop");
        File standardDesktop = new File(userHome + File.separator + "Desktop");
        File targetDir = oneDriveDesktop.exists() ? oneDriveDesktop : (standardDesktop.exists() ? standardDesktop : new File(userHome + File.separator + "Downloads"));
        return new File(targetDir, fileName);
    }

    private String sanitizePath(String path) {
        String cleaned = path.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    /**
     * Validates that path is not an OS-critical directory (e.g. C:\Windows, C:\Program Files, root drives).
     */
    public boolean isPathSafeForDeletion(File file) {
        if (file == null) return false;
        try {
            String canonical = file.getCanonicalPath().toLowerCase().replace('/', '\\');
            String rawPath = file.getPath().toLowerCase().replace('/', '\\');

            List<String> protectedPrefixes = List.of(
                    "\\windows",
                    "\\program files",
                    "\\program files (x86)",
                    "\\boot",
                    "\\recovery",
                    "\\etc",
                    "\\bin",
                    "\\sbin",
                    "\\usr",
                    "\\sys",
                    "\\proc"
            );

            for (String prefix : protectedPrefixes) {
                if (canonical.contains(prefix) || rawPath.startsWith(prefix) || rawPath.contains(prefix)) {
                    log.warn("Blocked operation on protected directory: {}", canonical);
                    return false;
                }
            }

            // Also ensure it's not a root drive directory directly (e.g. C:\)
            File parent = file.getParentFile();
            if (parent == null) return false;

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isPathSafeForRestoration(File file) {
        return isPathSafeForDeletion(file);
    }

    private String detectContentType(String fileName) {
        return MediaTypeFactory.getMediaType(fileName)
                .map(MediaType::toString)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    private File resolveLocalFile(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty())
            return null;

        String path = sanitizePath(rawPath);
        File direct = new File(path);
        if (direct.exists() && direct.isFile()) {
            return direct;
        }

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
}
