package com.filehider.controller;

import com.filehider.dto.ApiResponse;
import com.filehider.dto.FileResponseDto;
import com.filehider.dto.HidePathRequest;
import com.filehider.dto.UnhideRequest;
import com.filehider.entity.VaultFile;
import com.filehider.service.VaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
@Tag(name = "Vault Files", description = "Endpoints for concealing, viewing, downloading, and decrypting vault assets")
@SecurityRequirement(name = "bearerAuth")
public class VaultFileController {

    private final VaultService vaultService;

    @Autowired
    public VaultFileController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    private String resolveAuthenticatedEmail(String requestedEmail, Principal principal) {
        if (principal != null && principal.getName() != null) {
            String principalEmail = principal.getName().trim().toLowerCase();
            if (requestedEmail != null && !requestedEmail.trim().equalsIgnoreCase(principalEmail)) {
                throw new AccessDeniedException("Forbidden: You cannot access assets belonging to another user account");
            }
            return principalEmail;
        }
        return requestedEmail != null ? requestedEmail.trim().toLowerCase() : null;
    }

    @GetMapping
    @Operation(summary = "Get vault files", description = "Retrieves all encrypted vault files for the user")
    public ResponseEntity<Map<String, Object>> getFiles(
            @RequestParam(value = "email", required = false) String email,
            Principal principal) {

        String effectiveEmail = resolveAuthenticatedEmail(email, principal);
        if (effectiveEmail == null || effectiveEmail.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "User email is required");
            return ResponseEntity.badRequest().body(err);
        }

        List<FileResponseDto> files = vaultService.getFilesByEmail(effectiveEmail);
        Map<String, Object> response = new HashMap<>();
        response.put("files", files);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/hide", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Hide uploaded file", description = "Encrypts uploaded file with AES-256-GCM and persists into secure vault")
    public ResponseEntity<ApiResponse<FileResponseDto>> hideUploadedFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email,
            @RequestParam(value = "path", required = false) String customPath,
            Principal principal) {

        String effectiveEmail = resolveAuthenticatedEmail(email, principal);
        if (file.isEmpty() || effectiveEmail == null || effectiveEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File and email are required"));
        }

        try {
            VaultFile saved = vaultService.hideUploadedFile(file, effectiveEmail, customPath);
            FileResponseDto dto = new FileResponseDto(
                    saved.getId(),
                    saved.getFileName(),
                    saved.getPath(),
                    saved.getEmail(),
                    saved.getFileSize() != null ? saved.getFileSize() : 0,
                    saved.getChecksum(),
                    saved.getContentType(),
                    saved.getCreatedAt()
            );
            String msg = (saved.getPath() != null && !saved.getPath().startsWith("VaultStorage://"))
                    ? "File securely encrypted with AES-256-GCM and original wiped from PC (" + saved.getPath() + ")"
                    : "File securely encrypted with AES-256-GCM into vault enclave";
            return ResponseEntity.ok(ApiResponse.success(msg, dto));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to process file upload: " + e.getMessage()));
        }
    }

    @PostMapping("/hide-path")
    @Operation(summary = "Hide file by path", description = "Reads local file, encrypts with AES-256-GCM, saves into vault, and unlinks original from disk")
    public ResponseEntity<ApiResponse<FileResponseDto>> hideByPath(
            @Valid @RequestBody HidePathRequest request,
            Principal principal) {

        String effectiveEmail = resolveAuthenticatedEmail(request.getEmail(), principal);

        try {
            VaultFile saved = vaultService.hideLocalFile(request.getPath().trim(), effectiveEmail);
            FileResponseDto dto = new FileResponseDto(
                    saved.getId(),
                    saved.getFileName(),
                    saved.getPath(),
                    saved.getEmail(),
                    saved.getFileSize() != null ? saved.getFileSize() : 0,
                    saved.getChecksum(),
                    saved.getContentType(),
                    saved.getCreatedAt()
            );
            return ResponseEntity.ok(ApiResponse.success("File encrypted with AES-256-GCM and wiped from local path", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error reading file: " + e.getMessage()));
        }
    }

    @PostMapping("/unhide")
    @Operation(summary = "Unhide file", description = "Decrypts vault asset and restores it to original local disk path")
    public ResponseEntity<ApiResponse<Void>> unhideFile(
            @Valid @RequestBody UnhideRequest request,
            Principal principal) {

        String authenticatedEmail = (principal != null) ? principal.getName() : null;

        try {
            vaultService.unhideFile(request.getId(), authenticatedEmail);
            return ResponseEntity.ok(ApiResponse.success("File decrypted, restored to disk, and removed from vault"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error restoring file: " + e.getMessage()));
        }
    }

    @GetMapping("/download")
    @Operation(summary = "Download file", description = "Streams decrypted file content directly to caller")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("id") Integer id,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline,
            Principal principal) {

        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        String authenticatedEmail = (principal != null) ? principal.getName() : null;
        Optional<VaultFile> optional = vaultService.getFileById(id, authenticatedEmail);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VaultFile file = optional.get();
        // Secure AES-256-GCM Decryption on the fly
        byte[] data = vaultService.decryptPayload(file);
        ByteArrayResource resource = new ByteArrayResource(data);

        MediaType mediaType = MediaTypeFactory.getMediaType(file.getFileName())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        String disposition = inline ? "inline" : "attachment";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + file.getFileName() + "\"")
                .contentType(mediaType)
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/view")
    @Operation(summary = "Preview file", description = "Inline preview of decrypted file")
    public ResponseEntity<Resource> viewFile(
            @RequestParam("id") Integer id,
            Principal principal) {
        return downloadFile(id, true, principal);
    }
}
