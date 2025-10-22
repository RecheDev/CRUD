package com.portfolio.usermanagement.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private static final int MINIMUM_SECRET_LENGTH = 64;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Validates JWT secret on application startup.
     * Ensures the secret is long enough to be secure (64+ characters).
     */
    @PostConstruct
    public void validateSecret() {
        if (jwtSecret == null || jwtSecret.length() < MINIMUM_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT secret must be at least " + MINIMUM_SECRET_LENGTH + " characters long"
            );
        }
        logger.info("JWT configuration validated successfully");
    }

    /**
     * Generates a JWT token with enhanced security claims:
     * - jti: JWT ID for token blacklisting support
     * - type: Token type (access token)
     * - issuer: Application identifier
     * - audience: Intended recipients
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("jti", UUID.randomUUID().toString()) // Unique token ID for blacklisting
                .claim("type", "access")
                .issuer("user-management-system")
                .audience().add("api").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the JWT ID (jti) from a token.
     * Used for blacklist checking during logout.
     */
    public String getJtiFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("jti", String.class);
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Gets the expiration time in milliseconds from a JWT token.
     * Used for token blacklisting to set the correct TTL.
     *
     * @param token the JWT token
     * @return milliseconds until token expires
     */
    public long getExpirationMs(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        long expirationMs = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, expirationMs); // Return 0 if already expired
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
