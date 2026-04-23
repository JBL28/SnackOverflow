package com.snackoverflow.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(UUID userId, String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getAccessTtlSeconds())))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(UUID userId, UUID familyId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .subject(userId.toString())
                .claim("familyId", familyId.toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getRefreshTtlSeconds())))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public TokenValidationResult validate(String token) {
        try {
            Claims claims = parseClaims(token);
            return TokenValidationResult.ofValid(claims);
        } catch (ExpiredJwtException e) {
            return TokenValidationResult.ofExpired();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return TokenValidationResult.ofInvalid();
        }
    }

    public record TokenValidationResult(boolean valid, boolean expired, Claims claims) {
        public static TokenValidationResult ofValid(Claims claims) {
            return new TokenValidationResult(true, false, claims);
        }
        public static TokenValidationResult ofExpired() {
            return new TokenValidationResult(false, true, null);
        }
        public static TokenValidationResult ofInvalid() {
            return new TokenValidationResult(false, false, null);
        }
    }
}
