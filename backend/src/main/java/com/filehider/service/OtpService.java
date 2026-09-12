package com.filehider.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Long> otpTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastDispatchTimes = new ConcurrentHashMap<>();

    private static final long OTP_VALIDITY_DURATION_MS = 10 * 60 * 1000; // 10 minutes
    private static final long RATE_LIMIT_COOLDOWN_MS = 60 * 1000; // 60 seconds cooldown
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:satwiksaxena41@gmail.com}")
    private String mailFrom;

    /**
     * Checks if the email is in the rate-limit cooldown window.
     */
    public boolean isRateLimited(String email) {
        Long lastTime = lastDispatchTimes.get(email);
        return lastTime != null && (System.currentTimeMillis() - lastTime) < RATE_LIMIT_COOLDOWN_MS;
    }

    /**
     * Generates a cryptographically secure 4-digit OTP using SecureRandom.
     */
    public String generateOtp(String email) {
        int code = secureRandom.nextInt(10000);
        String otp = String.format("%04d", code);

        otpStore.put(email, otp);
        otpTimestamps.put(email, System.currentTimeMillis());
        otpAttempts.put(email, 0);
        lastDispatchTimes.put(email, System.currentTimeMillis());

        log.info("Generated cryptographically secure OTP for {} -> [{}]", email, otp);
        return otp;
    }

    /**
     * Asynchronously sends the OTP email via the configured thread pool.
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Boolean> sendOtpEmailAsync(String email, String otp) {
        boolean sent = sendOtpEmail(email, otp);
        return CompletableFuture.completedFuture(sent);
    }

    public boolean sendOtpEmail(String email, String otp) {
        log.info("Dispatching OTP to email: {}", email);
        if (mailSender == null) {
            log.warn("JavaMailSender bean is not configured. OTP for {}: {}", email, otp);
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom, "CypherVault Security");
            helper.setTo(email);
            helper.setSubject(otp + " is your CypherVault verification code");

            String generatedTime = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC));

            String htmlContent = "<!DOCTYPE html>"
                    + "<html lang=\"en\">"
                    + "<head>"
                    + "    <meta charset=\"UTF-8\">"
                    + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                    + "    <title>CypherVault Security OTP</title>"
                    + "</head>"
                    + "<body style=\"margin:0;padding:0;background-color:#090D16;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;-webkit-font-smoothing:antialiased;color:#E2E8F0;\">"
                    + "    <!-- Hidden Preheader -->"
                    + "    <div style=\"display:none;max-height:0px;overflow:hidden;opacity:0;\">"
                    + "        Your CypherVault verification code is " + otp + ". Valid for 10 minutes."
                    + "    </div>"
                    + "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#090D16;padding:40px 16px;\">"
                    + "        <tr>"
                    + "            <td align=\"center\">"
                    + "                <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width:560px;background-color:#111827;border:1px solid #1F2937;border-radius:16px;overflow:hidden;box-shadow:0 20px 40px rgba(0,0,0,0.6);\">"
                    + "                    <!-- Top Accent Gradient Line -->"
                    + "                    <tr>"
                    + "                        <td style=\"height:4px;background:linear-gradient(90deg,#10B981,#06B6D4,#3B82F6);font-size:0;line-height:0;\">&nbsp;</td>"
                    + "                    </tr>"
                    + "                    <!-- Header / Logo -->"
                    + "                    <tr>"
                    + "                        <td style=\"padding:32px 36px 24px 36px;\">"
                    + "                            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
                    + "                                <tr>"
                    + "                                    <td>"
                    + "                                        <div style=\"font-size:22px;font-weight:800;letter-spacing:2px;color:#FFFFFF;\">"
                    + "                                            CYPHER<span style=\"color:#10B981;\">VAULT</span>"
                    + "                                        </div>"
                    + "                                        <div style=\"font-size:12px;color:#94A3B8;margin-top:4px;letter-spacing:0.5px;\">"
                    + "                                            MILITARY-GRADE VAULT SYSTEM"
                    + "                                        </div>"
                    + "                                    </td>"
                    + "                                    <td align=\"right\" valign=\"top\">"
                    + "                                        <span style=\"display:inline-block;font-size:11px;font-weight:700;color:#10B981;background-color:rgba(16,185,129,0.12);border:1px solid rgba(16,185,129,0.3);padding:5px 12px;border-radius:20px;letter-spacing:0.8px;\">"
                    + "                                            VERIFICATION"
                    + "                                        </span>"
                    + "                                    </td>"
                    + "                                </tr>"
                    + "                            </table>"
                    + "                        </td>"
                    + "                    </tr>"
                    + "                    <!-- Separator Line -->"
                    + "                    <tr>"
                    + "                        <td style=\"padding:0 36px;\">"
                    + "                            <div style=\"height:1px;background-color:#1F2937;\"></div>"
                    + "                        </td>"
                    + "                    </tr>"
                    + "                    <!-- Body Content -->"
                    + "                    <tr>"
                    + "                        <td style=\"padding:32px 36px;\">"
                    + "                            <h1 style=\"margin:0 0 12px 0;font-size:20px;font-weight:700;color:#FFFFFF;\">"
                    + "                                One-Time Security Passcode"
                    + "                            </h1>"
                    + "                            <p style=\"margin:0 0 28px 0;font-size:14px;line-height:1.6;color:#94A3B8;\">"
                    + "                                We received an access verification request for your CypherVault enclave. Use the 4-digit security passcode below to complete your authentication."
                    + "                            </p>"
                    + "                            <!-- High-Tech OTP Code Container -->"
                    + "                            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#0B0F19;border:1px solid rgba(16,185,129,0.5);border-radius:12px;margin-bottom:24px;\">"
                    + "                                <tr>"
                    + "                                    <td align=\"center\" style=\"padding:28px 20px;\">"
                    + "                                        <div style=\"font-size:11px;font-weight:700;color:#64748B;letter-spacing:2px;text-transform:uppercase;margin-bottom:12px;\">"
                    + "                                            ONE-TIME PASSCODE"
                    + "                                        </div>"
                    + "                                        <div style=\"font-family:'SFMono-Regular',Consolas,'Liberation Mono',Menlo,Courier,monospace;font-size:42px;font-weight:800;letter-spacing:16px;color:#10B981;padding-left:16px;margin-bottom:12px;\">"
                    + otp
                    + "                                        </div>"
                    + "                                        <div style=\"font-size:12px;color:#64748B;\">"
                    + "                                            Expires in <strong style=\"color:#CBD5E1;\">10 minutes</strong> &bull; Single-use only"
                    + "                                        </div>"
                    + "                                    </td>"
                    + "                                </tr>"
                    + "                            </table>"
                    + "                            <!-- Request Details Card -->"
                    + "                            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#1A2234;border:1px solid #283548;border-radius:10px;margin-bottom:24px;font-size:13px;\">"
                    + "                                <tr>"
                    + "                                    <td style=\"padding:12px 16px;border-bottom:1px solid #283548;color:#94A3B8;width:140px;\">"
                    + "                                        Account Email"
                    + "                                    </td>"
                    + "                                    <td style=\"padding:12px 16px;border-bottom:1px solid #283548;color:#FFFFFF;font-weight:600;\">"
                    + email
                    + "                                    </td>"
                    + "                                </tr>"
                    + "                                <tr>"
                    + "                                    <td style=\"padding:12px 16px;border-bottom:1px solid #283548;color:#94A3B8;\">"
                    + "                                        Timestamp (UTC)"
                    + "                                    </td>"
                    + "                                    <td style=\"padding:12px 16px;border-bottom:1px solid #283548;color:#CBD5E1;font-family:monospace;font-size:12px;\">"
                    + generatedTime
                    + "                                    </td>"
                    + "                                </tr>"
                    + "                                <tr>"
                    + "                                    <td style=\"padding:12px 16px;color:#94A3B8;\">"
                    + "                                        Enclave Shield"
                    + "                                    </td>"
                    + "                                    <td style=\"padding:12px 16px;color:#10B981;font-weight:600;\">"
                    + "                                        AES-256 &bull; Zero Knowledge"
                    + "                                    </td>"
                    + "                                </tr>"
                    + "                            </table>"
                    + "                            <!-- Warning Banner -->"
                    + "                            <div style=\"background-color:rgba(234,179,8,0.08);border-left:3px solid #EAB308;border-radius:6px;padding:12px 14px;margin-bottom:24px;\">"
                    + "                                <p style=\"margin:0;font-size:12px;line-height:1.5;color:#FDE047;\">"
                    + "                                    <strong>Security Notice:</strong> Never share this passcode with anyone. CypherVault representatives will never ask for your verification code."
                    + "                                </p>"
                    + "                            </div>"
                    + "                            <p style=\"margin:0;font-size:13px;color:#64748B;line-height:1.5;\">"
                    + "                                Regards,<br>"
                    + "                                <strong style=\"color:#94A3B8;\">CypherVault Security Team</strong>"
                    + "                            </p>"
                    + "                        </td>"
                    + "                    </tr>"
                    + "                    <!-- Footer -->"
                    + "                    <tr>"
                    + "                        <td style=\"background-color:#0D131F;padding:20px 36px;border-top:1px solid #1F2937;text-align:center;\">"
                    + "                            <p style=\"margin:0 0 6px 0;font-size:11px;color:#64748B;\">"
                    + "                                This is an automated security transmission. Please do not reply directly to this email."
                    + "                            </p>"
                    + "                            <p style=\"margin:0;font-size:11px;color:#475569;\">"
                    + "                                &copy; " + java.time.Year.now().getValue() + " CypherVault. All rights reserved."
                    + "                            </p>"
                    + "                        </td>"
                    + "                    </tr>"
                    + "                </table>"
                    + "            </td>"
                    + "        </tr>"
                    + "    </table>"
                    + "</body>"
                    + "</html>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully sent OTP email to {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
            log.warn("DEVELOPMENT FALLBACK: Generated OTP for {} is: [{}]", email, otp);
            return false;
        }
    }

    public boolean validateOtp(String email, String otp) {
        if (email == null || otp == null)
            return false;

        String expectedOtp = otpStore.get(email);
        Long timestamp = otpTimestamps.get(email);

        if (expectedOtp == null || timestamp == null) {
            return false;
        }

        if (System.currentTimeMillis() - timestamp > OTP_VALIDITY_DURATION_MS) {
            otpStore.remove(email);
            otpTimestamps.remove(email);
            otpAttempts.remove(email);
            return false;
        }

        // Track and throttle attempts to prevent brute force
        int attempts = otpAttempts.getOrDefault(email, 0) + 1;
        otpAttempts.put(email, attempts);

        if (attempts > MAX_FAILED_ATTEMPTS) {
            log.warn("Exceeded maximum OTP validation attempts for {}. Invalidating code.", email);
            otpStore.remove(email);
            otpTimestamps.remove(email);
            otpAttempts.remove(email);
            return false;
        }

        if (expectedOtp.equals(otp.trim())) {
            otpStore.remove(email);
            otpTimestamps.remove(email);
            otpAttempts.remove(email);
            return true;
        }

        return false;
    }
}
