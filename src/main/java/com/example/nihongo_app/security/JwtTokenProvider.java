package com.example.nihongo_app.security;

import com.example.nihongo_app.entity.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generateToken(User user) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiration = issuedAt + (expirationMs / 1000L);

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                Locale.ROOT,
            "{\"sub\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
                escapeJson(user.getEmail()),
            escapeJson(user.getRole()),
                issuedAt,
                expiration
        );

        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedHeader + "." + encodedPayload);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = splitToken(token);
            if (parts == null) {
                return false;
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long expiration = extractLongClaim(payloadJson, "exp");
            return expiration > Instant.now().getEpochSecond();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        String[] parts = splitToken(token);
        if (parts == null) {
            throw new IllegalArgumentException("Invalid token");
        }

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return extractStringClaim(payloadJson, "sub");
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException ex) {
            throw new IllegalStateException("Invalid JWT signing key", ex);
        }
    }

    private String[] splitToken(String token) {
        if (token == null) {
            return null;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3 ? parts : null;
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractStringClaim(String json, String claimName) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(claimName) + "\\\":\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing claim: " + claimName);
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private long extractLongClaim(String json, String claimName) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(claimName) + "\\\":(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing claim: " + claimName);
        }
        return Long.parseLong(matcher.group(1));
    }
}