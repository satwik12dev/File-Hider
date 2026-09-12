package com.filehider.service;

import com.filehider.entity.VaultFile;
import com.filehider.repository.VaultFileRepository;
import com.filehider.security.AesEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultServiceTest {

    @Mock
    private VaultFileRepository vaultFileRepository;

    private AesEncryptionService aesEncryptionService;
    private VaultService vaultService;

    @BeforeEach
    void setUp() {
        aesEncryptionService = new AesEncryptionService("TestMasterKey32ByteStringRequired!");
        vaultService = new VaultService(vaultFileRepository, aesEncryptionService);
    }

    @Test
    @DisplayName("Should encrypt file upload and persist into repository")
    void testHideUploadedFile() throws IOException {
        String plainContent = "TopSecretDocumentContent123";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "secret_notes.txt",
                "text/plain",
                plainContent.getBytes(StandardCharsets.UTF_8)
        );

        when(vaultFileRepository.save(any(VaultFile.class))).thenAnswer(invocation -> {
            VaultFile vf = invocation.getArgument(0);
            vf.setId(42);
            return vf;
        });

        VaultFile result = vaultService.hideUploadedFile(file, "satwik@cyphervault.io", null);

        assertNotNull(result);
        assertEquals(42, result.getId());
        assertEquals("secret_notes.txt", result.getFileName());
        assertEquals("satwik@cyphervault.io", result.getEmail());
        assertNotNull(result.getIv());
        assertNotNull(result.getChecksum());
        assertEquals(plainContent.getBytes(StandardCharsets.UTF_8).length, result.getFileSize());

        // Verify data was encrypted
        assertFalse(new String(result.getBinData()).contains(plainContent));

        // Verify decryption matches original
        byte[] decrypted = vaultService.decryptPayload(result);
        assertEquals(plainContent, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should decrypt payload when unhiding file")
    void testUnhideFile() throws IOException {
        byte[] original = "UnhideTestData".getBytes(StandardCharsets.UTF_8);
        AesEncryptionService.EncryptedResult enc = aesEncryptionService.encrypt(original);

        VaultFile vf = new VaultFile("test.txt", "VaultStorage://test.txt", "user@test.com", enc.cipherData(), enc.iv(), enc.checksumSha256(), (long) original.length, "text/plain");
        vf.setId(10);

        when(vaultFileRepository.findByIdAndEmail(10, "user@test.com")).thenReturn(Optional.of(vf));

        vaultService.unhideFile(10, "user@test.com");

        verify(vaultFileRepository, times(1)).delete(vf);
    }

    @Test
    @DisplayName("Should detect and block dangerous system paths from deletion")
    void testSystemPathProtection() {
        File windowsSystem = new File("C:\\Windows\\System32\\calc.exe");
        assertFalse(vaultService.isPathSafeForDeletion(windowsSystem), "Windows system folder must be protected");

        File etcPasswd = new File("/etc/passwd");
        assertFalse(vaultService.isPathSafeForDeletion(etcPasswd), "Linux /etc system folder must be protected");
    }
}
