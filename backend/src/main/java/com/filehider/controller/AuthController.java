package com.filehider.controller;

import com.filehider.dto.ApiResponse;
import com.filehider.dto.AuthRequest;
import com.filehider.dto.SendOtpRequest;
import com.filehider.entity.User;
import com.filehider.security.JwtService;
import com.filehider.service.OtpService;
import com.filehider.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Authentication", description = "Endpoints for OTP delivery, login, and signup with JWT token issuance")
public class AuthController {

    private final OtpService otpService;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(OtpService otpService, UserService userService, JwtService jwtService) {
        this.otpService = otpService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP code", description = "Generates cryptographically secure OTP and dispatches asynchronously via email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String email = request.getEmail().trim();
        String mode = request.getMode() != null ? request.getMode().trim().toLowerCase() : "login";

        // Rate limiting cooldown check
        if (otpService.isRateLimited(email)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Rate limit exceeded. Please wait 60 seconds before requesting another OTP code."));
        }

        // In login mode, verify user exists first
        if ("login".equals(mode) && !userService.isUserExists(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User account not found. Please register first."));
        }

        String otp = otpService.generateOtp(email);

        // Asynchronously dispatch email using bounded ThreadPoolTaskExecutor
        otpService.sendOtpEmailAsync(email, otp);

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        ApiResponse<Map<String, Object>> response = ApiResponse.success("OTP dispatched successfully to your email: " + email, data)
                .withOtp(otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Validates OTP code and returns authenticated JWT bearer token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody AuthRequest request) {
        String email = request.getEmail().trim();
        String otp = request.getOtp().trim();

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Incorrect, expired, or throttled OTP code."));
        }

        Optional<User> userOptional = userService.getUserByEmail(email);
        String name = userOptional.map(User::getName).orElse(email.split("@")[0]);

        // Generate Stateless JWT Token
        String token = jwtService.generateToken(email, name);

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("name", name);
        data.put("token", token);

        return ResponseEntity.ok(ApiResponse.success("Authentication successful", data));
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Validates OTP code, registers user, and returns authenticated JWT bearer token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@Valid @RequestBody AuthRequest request) {
        String email = request.getEmail().trim();
        String otp = request.getOtp().trim();
        String name = request.getName() != null && !request.getName().trim().isEmpty()
                ? request.getName().trim()
                : email.split("@")[0];

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Incorrect, expired, or throttled OTP code."));
        }

        try {
            User user = userService.registerUser(name, email);
            String token = jwtService.generateToken(user.getEmail(), user.getName());

            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            data.put("name", user.getName());
            data.put("token", token);

            return ResponseEntity.ok(ApiResponse.success("User successfully registered", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
