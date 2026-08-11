package com.wvw.mmw.domain.auth;

import com.jayway.jsonpath.JsonPath;
import com.wvw.mmw.ExternalCloudClientTestSupport;
import com.wvw.mmw.domain.auth.dto.request.LoginRequest;
import com.wvw.mmw.domain.auth.dto.request.SignupRequest;
import com.wvw.mmw.domain.auth.dto.response.TokenResponse;
import com.wvw.mmw.domain.auth.entity.RefreshToken;
import com.wvw.mmw.domain.auth.jwt.TokenHashUtil;
import com.wvw.mmw.domain.auth.repository.RefreshTokenRepository;
import com.wvw.mmw.domain.auth.service.AuthService;
import com.wvw.mmw.domain.terms.repository.TermsAgreementRepository;
import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountApiIntegrationTest extends ExternalCloudClientTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TermsAgreementRepository termsAgreementRepository;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        termsAgreementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void reissueRotatesBothTokensAndRejectsPreviousRefreshToken()
            throws Exception {
        User user = signup();
        TokenResponse loginTokens = login();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reissueJson(loginTokens.refreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String newRefreshToken = JsonPath.read(
                body,
                "$.data.refreshToken"
        );

        assertThat(newRefreshToken)
                .isNotEqualTo(loginTokens.refreshToken());
        assertThat(refreshTokenRepository
                .findByTokenHash(TokenHashUtil.sha256(newRefreshToken))
                .map(token -> token.getUser().getId()))
                .contains(user.getId());

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reissueJson(loginTokens.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void reissueRejectsAccessTokenAndDeletesExpiredStoredToken()
            throws Exception {
        User user = signup();
        TokenResponse tokens = login();

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reissueJson(tokens.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REFRESH_TOKEN"));

        RefreshToken storedToken = refreshTokenRepository
                .findByUser(user)
                .orElseThrow();
        storedToken.update(
                storedToken.getTokenHash(),
                LocalDateTime.now().minusSeconds(1)
        );
        refreshTokenRepository.saveAndFlush(storedToken);

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reissueJson(tokens.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REFRESH_TOKEN"));

        assertThat(refreshTokenRepository.findByUser(user)).isEmpty();
    }

    @Test
    void sessionReturnsAuthenticatedUser() throws Exception {
        User user = signup();
        TokenResponse tokens = login();

        mockMvc.perform(get("/api/v1/auth/session")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(jsonPath("$.data.userId").value(user.getId()));

        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void logoutDeletesStoredRefreshToken() throws Exception {
        User user = signup();
        TokenResponse tokens = login();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertThat(refreshTokenRepository.findByUser(user)).isEmpty();

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reissueJson(tokens.refreshToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordChangeValidatesRequestAndRevokesRefreshToken()
            throws Exception {
        User user = signup();
        TokenResponse tokens = login();

        mockMvc.perform(patch("/api/v1/auth/password")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(
                                "WrongPassword1!",
                                "NewPassword2!",
                                "NewPassword2!"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_CURRENT_PASSWORD"));

        mockMvc.perform(patch("/api/v1/auth/password")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(
                                "Password1!",
                                "NewPassword2!",
                                "Different3!"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_MISMATCH"));

        mockMvc.perform(patch("/api/v1/auth/password")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordJson(
                                "Password1!",
                                "NewPassword2!",
                                "NewPassword2!"
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertThat(refreshTokenRepository.findByUser(user)).isEmpty();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("Password1!")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("NewPassword2!")))
                .andExpect(status().isOk());
    }

    @Test
    void profilePatchUpdatesOnlyProvidedFields() throws Exception {
        signup();
        TokenResponse tokens = login();

        mockMvc.perform(patch("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "  면접왕  ",
                                  "desiredPosition": "백엔드 개발자"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("면접왕"))
                .andExpect(jsonPath("$.data.desiredPosition")
                        .value("백엔드 개발자"));

        mockMvc.perform(patch("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"새닉네임\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.data.desiredPosition")
                        .value("백엔드 개발자"));

        mockMvc.perform(patch("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_INPUT_VALUE"));
    }

    @Test
    void accountDeletionHardDeletesUserAndAuthenticationData()
            throws Exception {
        User user = signup();
        TokenResponse tokens = login();

        mockMvc.perform(delete("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        ))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(refreshTokenRepository.count()).isZero();
        assertThat(termsAgreementRepository.count()).isZero();

        mockMvc.perform(get("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(tokens.accessToken())
                        ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unsupportedMethodReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/api/v1/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    private User signup() {
        authService.signup(new SignupRequest(
                "member01",
                "member@example.com",
                "Password1!",
                "Password1!",
                "홍길동",
                true
        ));

        return userRepository.findByLoginId("member01").orElseThrow();
    }

    private TokenResponse login() {
        return authService.login(
                new LoginRequest("member01", "Password1!")
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String reissueJson(String refreshToken) {
        return "{\"refreshToken\":\"%s\"}"
                .formatted(refreshToken);
    }

    private String loginJson(String password) {
        return """
                {
                  "loginId": "member01",
                  "password": "%s"
                }
                """.formatted(password);
    }

    private String passwordJson(
            String currentPassword,
            String newPassword,
            String newPasswordConfirm
    ) {
        return """
                {
                  "currentPassword": "%s",
                  "newPassword": "%s",
                  "newPasswordConfirm": "%s"
                }
                """.formatted(
                        currentPassword,
                        newPassword,
                        newPasswordConfirm
                );
    }
}
