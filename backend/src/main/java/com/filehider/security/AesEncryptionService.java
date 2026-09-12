package com.filehider.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Production-grade AES-256-GCM Authenticated Encryption Service.
 * Provides confidentiality, integrity, and authenticity guarantees with unique 96-bit IVs.
 */
@Service
public class AesEncryptionService {

    private static final Logger log = LoggerFactory.getLogger(AesEncryptionService.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    public static final int GCM_IV_LENGTH_BYTES = 12;

    private final SecretKey masterSecretKey;
    private final SecureRandom secureRandom;

    public record EncryptedResult(byte[] cipherData, byte[] iv, String checksumSha256) {}

    public AesEncryptionService(
            @Value("${vault.security.master-key:CypherVaultMasterSecretKey2026SecureProdModeAES256}") String masterKeySeed) {
        this.secureRandom = new SecureRandom();
        this.masterSecretKey = derive256BitKey(masterKeySeed);
        log.info("AesEncryptionService initialized with AES-256-GCM cipher engine");
    }

    /**
     * Derives a deterministic 256-bit AES key from the configured master key string using SHA-256.
     */
    private SecretKey derive256BitKey(String seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to derive AES-256 key", e);
        }
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM with a freshly generated 12-byte IV.
     * Computes the SHA-256 checksum of original plaintext for post-decryption integrity verification.
     */
    public EncryptedResult encrypt(byte[] plaintext) {
        if (plaintext == null) {
            return new EncryptedResult(new byte[0], new byte[GCM_IV_LENGTH_BYTES], "");
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterSecretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext);
            String checksum = computeSha256(plaintext);

            return new EncryptedResult(cipherText, iv, checksum);
        } catch (Exception e) {
            log.error("Failed to encrypt data with AES-256-GCM: {}", e.getMessage());
            throw new SecurityException("Cryptographic encryption failure", e);
        }
    }

    /**
     * Decrypts ciphertext bytes using AES-256-GCM with the corresponding IV.
     */
    public byte[] decrypt(byte[] ciphertext, byte[] iv) {
        if (ciphertext == null || ciphertext.length == 0) {
            return new byte[0];
        }
        if (iv == null || iv.length != GCM_IV_LENGTH_BYTES) {
            throw new IllegalArgumentException("Invalid GCM IV length. Expected " + GCM_IV_LENGTH_BYTES + " bytes");
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, masterSecretKey, parameterSpec);

            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            log.error("Failed to decrypt data with AES-256-GCM: {}", e.getMessage());
            throw new SecurityException("Cryptographic decryption failure: data corrupted or key mismatch", e);
        }
    }

    /**
     * Computes SHA-256 hex string for verifying payload integrity.
     */
    public String computeSha256(byte[] data) {
        if (data == null || data.length == 0) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
