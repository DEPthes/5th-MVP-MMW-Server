package com.wvw.mmw.auth;

import com.wvw.mmw.ExternalCloudClientTestSupport;
import com.wvw.mmw.domain.auth.dto.request.SignupRequest;
import com.wvw.mmw.domain.auth.jwt.JwtProvider;
import com.wvw.mmw.domain.auth.repository.RefreshTokenRepository;
import com.wvw.mmw.domain.auth.service.AuthService;
import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.domain.terms.repository.TermsAgreementRepository;
import com.wvw.mmw.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest extends ExternalCloudClientTestSupport {

    private static final String TEST_JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TermsAgreementRepository termsAgreementRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        termsAgreementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void signupStoresBcryptPasswordAndReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(
                                "Member@Example.com",
                                "member01",
                                "Password1!",
                                "Password1!",
                                true
                        )))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        User savedUser = userRepository
                .findByLoginId("member01")
                .orElseThrow();

        assertThat(savedUser.getEmail()).isEqualTo("member@example.com");
        assertThat(savedUser.getPassword()).isNotEqualTo("Password1!");
        assertThat(savedUser.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(
                "Password1!",
                savedUser.getPassword()
        )).isTrue();
    }

    @Test
    void signupRejectsInvalidLoginIdAndPasswordFormat() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(
                                "member@example.com",
                                "short",
                                "onlyletters",
                                "onlyletters",
                                true
                )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.errors[*].field")
                        .value(hasItems("loginId", "password")));
    }

    @Test
    void signupRejectsPasswordMismatch() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(
                                "member@example.com",
                                "member01",
                                "Password1!",
                                "Password2!",
                                true
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_MISMATCH"));
    }

    @Test
    void signupRejectsMissingPrivacyConsent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(
                                "member@example.com",
                                "member01",
                                "Password1!",
                                "Password1!",
                                false
                )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.errors[0].field")
                        .value("privacyAgreed"));
    }

    @Test
    void signupRejectsDuplicateEmailAndLoginIdWithConflict() throws Exception {
        signup("member@example.com", "member01");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(
                                "MEMBER@example.com",
                                "member02",
                                "Password1!",
                                "Password1!",
                                true
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(
                                "another@example.com",
                                "member01",
                                "Password1!",
                                "Password1!",
                                true
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LOGIN_ID_ALREADY_EXISTS"));
    }

    @Test
    void loginReturnsAccessAndRefreshTokens() throws Exception {
        signup("member@example.com", "member01");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("member01", "Password1!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        User user = userRepository.findByLoginId("member01").orElseThrow();
        assertThat(refreshTokenRepository.findByUser(user)).isPresent();
    }

    @Test
    void loginReturnsSameFailureForUnknownIdAndWrongPassword() throws Exception {
        signup("member@example.com", "member01");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("unknown01", "Password1!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("member01", "Wrongpass1!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void myProfileWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void myProfileWithValidAccessTokenReturnsProfile() throws Exception {
        User user = signup("member@example.com", "member01");
        String accessToken = jwtProvider.issueTokens(user.getId()).accessToken();

        mockMvc.perform(get("/api/v1/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.loginId").value("member01"))
                .andExpect(jsonPath("$.data.nickname").doesNotExist())
                .andExpect(jsonPath("$.data.desiredPosition").doesNotExist());
    }

    @Test
    void myProfileWithTamperedTokenReturnsUnauthorized() throws Exception {
        User user = signup("member@example.com", "member01");
        String accessToken = jwtProvider.issueTokens(user.getId()).accessToken();
        String tamperedToken = tamper(accessToken);

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tamperedToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void myProfileWithExpiredAccessTokenReturnsUnauthorized() throws Exception {
        User user = signup("member@example.com", "member01");
        JwtProvider expiredTokenProvider = new JwtProvider(
                "mmw-test",
                TEST_JWT_SECRET,
                -1,
                1209600000
        );
        String expiredToken = expiredTokenProvider
                .issueTokens(user.getId())
                .accessToken();

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(expiredToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void myProfileWithRefreshTokenReturnsUnauthorized() throws Exception {
        User user = signup("member@example.com", "member01");
        String refreshToken = jwtProvider.issueTokens(user.getId()).refreshToken();

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void myProfileWithUnknownUserTokenReturnsUnauthorized() throws Exception {
        String accessToken = jwtProvider
                .issueTokens(Long.MAX_VALUE)
                .accessToken();

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void unknownUrlReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private User signup(String email, String loginId) {
        authService.signup(new SignupRequest(
                loginId,
                email,
                "Password1!",
                "Password1!",
                "홍길동",
                true
        ));

        return userRepository.findByLoginId(loginId).orElseThrow();
    }

    private String signupJson(
            String email,
            String loginId,
            String password,
            String passwordConfirm,
            boolean privacyAgreed
    ) {
        return """
                {
                  "email": "%s",
                  "loginId": "%s",
                  "password": "%s",
                  "passwordConfirm": "%s",
                  "name": "홍길동",
                  "privacyAgreed": %s
                }
                """.formatted(
                        email,
                        loginId,
                        password,
                        passwordConfirm,
                        privacyAgreed
                );
    }

    private String loginJson(String loginId, String password) {
        return """
                {
                  "loginId": "%s",
                  "password": "%s"
                }
                """.formatted(loginId, password);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String tamper(String token) {
        int index = token.length() - 2;
        char replacement = token.charAt(index) == 'A' ? 'B' : 'A';

        return token.substring(0, index)
                + replacement
                + token.substring(index + 1);
    }
}
