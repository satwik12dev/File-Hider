package com.filehider.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService();
    }

    @Test
    @DisplayName("Should generate a 4-digit numeric OTP and validate correctly")
    void testGenerateAndValidateOtp() {
        String email = "agent@cyphervault.io";
        String otp = otpService.generateOtp(email);

        assertNotNull(otp);
        assertEquals(4, otp.length());
        assertTrue(otp.matches("^\\d{4}$"), "OTP must be numeric 4 digits");

        // Validate correct OTP
        assertTrue(otpService.validateOtp(email, otp));

        // Subsequent validation should fail (single-use)
        assertFalse(otpService.validateOtp(email, otp), "OTP must be single-use only");
    }

    @Test
    @DisplayName("Should invalidate OTP after 5 failed brute-force attempts")
    void testAttemptThrottling() {
        String email = "target@cyphervault.io";
        String actualOtp = otpService.generateOtp(email);

        // Attempt 1 to 5 with wrong OTP
        for (int i = 0; i < 5; i++) {
            assertFalse(otpService.validateOtp(email, "0000".equals(actualOtp) ? "1111" : "0000"));
        }

        // Now even the real OTP should be rejected because attempts exceeded max
        assertFalse(otpService.validateOtp(email, actualOtp), "OTP must be invalidated after 5 failed attempts");
    }

    @Test
    @DisplayName("Should enforce 60s rate limit cooldown on consecutive OTP dispatches")
    void testRateLimiting() {
        String email = "speedy@cyphervault.io";

        // First generation sets the timestamp
        otpService.generateOtp(email);

        // Immediate check must indicate rate limited
        assertTrue(otpService.isRateLimited(email), "Should be rate limited immediately after dispatch");
    }
}
