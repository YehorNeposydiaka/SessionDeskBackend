package org.example.Security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example.Security.UserPrincipal;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
        this.accessTokenExpirationMs = jwtProperties.accessTokenExpirationMs();
        this.refreshTokenExpirationMs = jwtProperties.refreshTokenExpirationMs();
    }

    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(principal.getId().toString())
                .claim("email", principal.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // Refresh token теж підписуємо JWT-ом (а не просто random string) —
    // так простіше валідувати expiry без запиту в БД, а сам факт revoke
    // перевіряємо окремо через RefreshTokenRepository (по хешу).
    public String generateRefreshToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(principal.getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // токен протух — нормальна ситуація, не логуємо як помилку
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // підпис невалідний, токен пошкоджений/підроблений
            return false;
        }
    }

    public UUID getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }
}