package com.filehider.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptionServiceTest {

    private AesEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new AesEncryptionService("TestMasterSecretKeyForUnitTestingAES256GCM!");
    }

    @Test
    @DisplayName("Should encrypt and successfully decrypt back to exact plaintext")
    void testEncryptDecrypt() {
        String originalText = "TopSecretMilitaryGradePayload_123456789";
        byte[] originalBytes = originalText.getBytes(StandardCharsets.UTF_8);

        AesEncryptionService.EncryptedResult result = encryptionService.encrypt(originalBytes);

        assertNotNull(result);
        assertNotNull(result.cipherData());
        assertNotNull(result.iv());
        assertEquals(AesEncryptionService.GCM_IV_LENGTH_BYTES, result.iv().length);
        assertFalse(Arrays.equals(originalBytes, result.cipherData()), "Ciphertext must not match plaintext");

        byte[] decryptedBytes = encryptionService.decrypt(result.cipherData(), result.iv());
        String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

        assertEquals(originalText, decryptedText);
    }

    @Test
    @DisplayName("Should generate unique IVs for identical plaintext inputs")
    void testIvUniqueness() {
        byte[] input = "ConstantStringData".getBytes(StandardCharsets.UTF_8);

        AesEncryptionService.EncryptedResult result1 = encryptionService.encrypt(input);
        AesEncryptionService.EncryptedResult result2 = encryptionService.encrypt(input);

        assertFalse(Arrays.equals(result1.iv(), result2.iv()), "Consecutive IVs must be cryptographically unique");
        assertFalse(Arrays.equals(result1.cipherData(), result2.cipherData()), "Ciphertexts must differ due to unique IVs");
    }

    @Test
    @DisplayName("Should reject tampered ciphertext with SecurityException due to GCM authentication tag")
    void testTamperDetection() {
        byte[] input = "SensitiveBankLedger".getBytes(StandardCharsets.UTF_8);
        AesEncryptionService.EncryptedResult result = encryptionService.encrypt(input);

        byte[] tamperedCipher = Arrays.copyOf(result.cipherData(), result.cipherData().length);
        tamperedCipher[0] ^= 0x01; // Flip a bit in the ciphertext

        assertThrows(SecurityException.class, () -> {
            encryptionService.decrypt(tamperedCipher, result.iv());
        });
    }

    @Test
    @DisplayName("Should compute correct SHA-256 hash string")
    void testSha256Checksum() {
        byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
        String checksum = encryptionService.computeSha256(input);

        // Standard known SHA-256 of "hello world"
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", checksum);
    }
}
