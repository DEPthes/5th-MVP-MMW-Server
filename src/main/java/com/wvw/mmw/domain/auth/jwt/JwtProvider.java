package com.wvw.mmw.domain.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final String issuer;
    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtProvider(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}")
            long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}")
            long refreshTokenExpirationMs
    ) {
        this.issuer = issuer;
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public IssuedTokens issueTokens(Long userId) {
        Instant issuedAt = Instant.now();

        Instant accessTokenExpiresAt =
                issuedAt.plusMillis(accessTokenExpirationMs);

        Instant refreshTokenExpiresAt =
                issuedAt.plusMillis(refreshTokenExpirationMs);

        String accessToken = createToken(
                userId,
                ACCESS_TOKEN_TYPE,
                issuedAt,
                accessTokenExpiresAt
        );

        String refreshToken = createToken(
                userId,
                REFRESH_TOKEN_TYPE,
                issuedAt,
                refreshTokenExpiresAt
        );

        LocalDateTime refreshExpiresAt =
                LocalDateTime.ofInstant(
                        refreshTokenExpiresAt,
                        ZoneId.systemDefault()
                );

        return new IssuedTokens(
                accessToken,
                refreshToken,
                refreshExpiresAt
        );
    }

    public boolean validateAccessToken(String token) {
        return validateTokenType(token, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, REFRESH_TOKEN_TYPE);
    }

    private boolean validateTokenType(
            String token,
            String expectedTokenType
    ) {
        try {
            Claims claims = parseClaims(token);

            return expectedTokenType.equals(
                    claims.get(TOKEN_TYPE_CLAIM, String.class)
            );
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public Long getUserId(String token) {
        String subject = parseClaims(token).getSubject();
        return Long.valueOf(subject);
    }

    public String getTokenType(String token) {
        return parseClaims(token)
                .get(TOKEN_TYPE_CLAIM, String.class);
    }

    private String createToken(
            Long userId,
            String tokenType,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(userId.toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record IssuedTokens(
            String accessToken,
            String refreshToken,
            LocalDateTime refreshTokenExpiresAt
    ) {
    }
}
