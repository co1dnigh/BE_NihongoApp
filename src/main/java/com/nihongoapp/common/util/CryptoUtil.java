package com.nihongoapp.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class CryptoUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-ttl-minutes}")
    private long accessTtlMinutes;

    private SecretKey signingKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlMinutes * 60)))
                .signWith(signingKey())
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(7 * 24 * 3600)))
                .signWith(signingKey())
                .compact();
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return expectedSubject.equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            return subject != null ? Long.parseLong(subject) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String hashPassword(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }

    public boolean matchesPassword(String raw, String hash) {
        return BCrypt.checkpw(raw, hash);
    }
}
