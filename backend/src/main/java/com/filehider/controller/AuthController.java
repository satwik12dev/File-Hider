package com.filehider.controller;

import com.filehider.dto.ApiResponse;
import com.filehider.dto.AuthRequest;
import com.filehider.dto.SendOtpRequest;
import com.filehider.entity.User;
import com.filehider.service.OtpService;
import com.filehider.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final OtpService otpService;
    private final UserService userService;

    @Autowired
    public AuthController(OtpService otpService, UserService userService) {
        this.otpService = otpService;
        this.userService = userService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@RequestBody SendOtpRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
        }

        String email = request.getEmail().trim();
        String mode = request.getMode() != null ? request.getMode().trim().toLowerCase() : "login";

        // In login mode, verify user exists first
        if ("login".equals(mode) && !userService.isUserExists(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User account not found. Please register first."));
        }

        String otp = otpService.generateOtp(email);

        // Asynchronously dispatch email
        new Thread(() -> otpService.sendOtpEmail(email, otp)).start();

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        ApiResponse<Map<String, Object>> response = ApiResponse.success("OTP dispatched successfully to your email: " + email, data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email and OTP are required"));
        }

        String email = request.getEmail().trim();
        String otp = request.getOtp().trim();

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Incorrect or expired OTP code"));
        }

        Optional<User> userOptional = userService.getUserByEmail(email);
        String name = userOptional.map(User::getName).orElse(email.split("@")[0]);

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("name", name);

        return ResponseEntity.ok(ApiResponse.success("Authentication successful", data));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email and OTP are required"));
        }

        String email = request.getEmail().trim();
        String otp = request.getOtp().trim();
        String name = request.getName() != null && !request.getName().trim().isEmpty()
                ? request.getName().trim()
                : email.split("@")[0];

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Incorrect or expired OTP code"));
        }

        try {
            User user = userService.registerUser(name, email);
            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            data.put("name", user.getName());
            return ResponseEntity.ok(ApiResponse.success("User successfully registered", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
