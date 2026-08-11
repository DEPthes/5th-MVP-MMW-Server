package com.wvw.mmw.auth;

import com.wvw.mmw.domain.auth.jwt.JwtProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private static final String TEST_JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void validatesIssuerAndTokenType() {
        JwtProvider provider = new JwtProvider(
                "mmw-test",
                TEST_JWT_SECRET,
                3600000,
                1209600000
        );
        JwtProvider.IssuedTokens tokens = provider.issueTokens(1L);

        assertThat(provider.validateAccessToken(tokens.accessToken())).isTrue();
        assertThat(provider.validateRefreshToken(tokens.accessToken())).isFalse();
        assertThat(provider.validateRefreshToken(tokens.refreshToken())).isTrue();
        assertThat(provider.validateAccessToken(tokens.refreshToken())).isFalse();
    }

    @Test
    void rejectsTokenIssuedByDifferentIssuer() {
        JwtProvider issuer = new JwtProvider(
                "another-service",
                TEST_JWT_SECRET,
                3600000,
                1209600000
        );
        JwtProvider verifier = new JwtProvider(
                "mmw-test",
                TEST_JWT_SECRET,
                3600000,
                1209600000
        );

        String accessToken = issuer.issueTokens(1L).accessToken();

        assertThat(verifier.validateAccessToken(accessToken)).isFalse();
    }
}
