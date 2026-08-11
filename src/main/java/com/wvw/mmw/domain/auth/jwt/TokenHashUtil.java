package com.wvw.mmw.domain.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class TokenHashUtil {

    private TokenHashUtil() {
    }

    public static String sha256(String token) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] digest = messageDigest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    exception
            );
        }
    }
}