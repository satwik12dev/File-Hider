package com.filehider.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Production-ready Stateless JWT Token Service.
 * Manages generation, parsing, and cryptographic verification of JWT bearer tokens.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long tokenValidityMs;

    public JwtService(
            @Value("${vault.jwt.secret:CypherVaultProductionJwtSecretKeyMustBeAtLeast256BitsLongForHMACSHA256Security}") String secret,
            @Value("${vault.jwt.expiration-ms:86400000}") long tokenValidityMs) { // default 24h
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenValidityMs = tokenValidityMs;
        log.info("JwtService configured with expiration {}ms", tokenValidityMs);
    }

    /**
     * Generates a signed JWT for the authenticated user.
     */
    public String generateToken(String email, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", name);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenValidityMs);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates the given JWT string.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the user email (subject) from the token.
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Extracts the user's display name from token claims.
     */
    public String extractName(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("name", String.class) : null;
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
