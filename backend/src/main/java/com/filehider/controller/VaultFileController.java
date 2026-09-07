package com.filehider.controller;

import com.filehider.dto.ApiResponse;
import com.filehider.dto.FileResponseDto;
import com.filehider.dto.HidePathRequest;
import com.filehider.dto.UnhideRequest;
import com.filehider.entity.VaultFile;
import com.filehider.service.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class VaultFileController {

    private final VaultService vaultService;

    @Autowired
    public VaultFileController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFiles(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Email query parameter is required");
            return ResponseEntity.badRequest().body(err);
        }

        List<FileResponseDto> files = vaultService.getFilesByEmail(email.trim());
        Map<String, Object> response = new HashMap<>();
        response.put("files", files);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/hide", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponseDto>> hideUploadedFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email,
            @RequestParam(value = "path", required = false) String customPath) {

        if (file.isEmpty() || email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File and email are required"));
        }

        try {
            VaultFile saved = vaultService.hideUploadedFile(file, email.trim(), customPath);
            FileResponseDto dto = new FileResponseDto(
                    saved.getId(),
                    saved.getFileName(),
                    saved.getPath(),
                    saved.getEmail(),
                    saved.getBinData() != null ? saved.getBinData().length : 0
            );
            String msg = (saved.getPath() != null && !saved.getPath().startsWith("VaultStorage://"))
                    ? "File securely encapsulated and original wiped from PC (" + saved.getPath() + ")"
                    : "File securely encapsulated into vault";
            return ResponseEntity.ok(ApiResponse.success(msg, dto));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to process file upload: " + e.getMessage()));
        }
    }

    @PostMapping("/hide-path")
    public ResponseEntity<ApiResponse<FileResponseDto>> hideByPath(@RequestBody HidePathRequest request) {
        if (request.getPath() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Absolute path and email are required"));
        }

        try {
            VaultFile saved = vaultService.hideLocalFile(request.getPath().trim(), request.getEmail().trim());
            FileResponseDto dto = new FileResponseDto(
                    saved.getId(),
                    saved.getFileName(),
                    saved.getPath(),
                    saved.getEmail(),
                    saved.getBinData() != null ? saved.getBinData().length : 0
            );
            return ResponseEntity.ok(ApiResponse.success("File hidden and wiped from local path", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error reading file: " + e.getMessage()));
        }
    }

    @PostMapping("/unhide")
    public ResponseEntity<ApiResponse<Void>> unhideFile(@RequestBody UnhideRequest request) {
        if (request.getId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File ID is required"));
        }

        try {
            vaultService.unhideFile(request.getId());
            return ResponseEntity.ok(ApiResponse.success("File unhidden and removed from vault"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Error restoring file: " + e.getMessage()));
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("id") Integer id,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<VaultFile> optional = vaultService.getFileById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        VaultFile file = optional.get();
        byte[] data = file.getBinData() != null ? file.getBinData() : new byte[0];
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
    public ResponseEntity<Resource> viewFile(@RequestParam("id") Integer id) {
        return downloadFile(id, true);
    }
}
