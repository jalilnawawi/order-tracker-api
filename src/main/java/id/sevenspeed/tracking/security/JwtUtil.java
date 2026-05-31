package id.sevenspeed.tracking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiry-ms}") long accessTokenTtlMs,
            @Value("${app.jwt.refresh-token-expiry-ms}") long refreshTokenTtlMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMs = accessTokenTtlMs;
        this.refreshTokenTtlMs = refreshTokenTtlMs;
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenTtlMs);

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getUserId()))
                .claim("username", userDetails.getUsername())
                .claim("roleCode", userDetails.getRoleCode())
                .claim("divisionId", userDetails.getDivisionId())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Instant generateRefreshTokenExpiry() {
        return Instant.now().plusMillis(refreshTokenTtlMs);
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty or null: {}", e.getMessage());
            return false;
        }
    }

    public CustomUserDetails extractUserDetails(String token) {
        Claims claims = extractAllClaims(token);
        Long divisionId = claims.get("divisionId", Long.class);

        return new CustomUserDetails(
                Long.parseLong(claims.getSubject()),
                claims.get("username", String.class),
                null,   // password tidak di-embed di token
                claims.get("roleCode", String.class),
                divisionId,
                true
        );
    }
}