package com.wvw.mmw.domain.auth.service;

import com.wvw.mmw.domain.auth.dto.request.ChangePasswordRequest;
import com.wvw.mmw.domain.auth.dto.request.LoginRequest;
import com.wvw.mmw.domain.auth.dto.request.SignupRequest;
import com.wvw.mmw.domain.auth.dto.request.TokenReissueRequest;
import com.wvw.mmw.domain.auth.dto.response.TokenResponse;
import com.wvw.mmw.domain.auth.entity.RefreshToken;
import com.wvw.mmw.domain.auth.error.AuthErrorCode;
import com.wvw.mmw.domain.auth.error.AuthException;
import com.wvw.mmw.domain.auth.jwt.JwtProvider;
import com.wvw.mmw.domain.auth.jwt.TokenHashUtil;
import com.wvw.mmw.domain.auth.repository.RefreshTokenRepository;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import com.wvw.mmw.domain.terms.entity.TermsAgreement;
import com.wvw.mmw.domain.terms.entity.TermsType;
import com.wvw.mmw.domain.terms.repository.TermsAgreementRepository;
import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    public AuthService(
            UserRepository userRepository,
            TermsAgreementRepository termsAgreementRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.termsAgreementRepository = termsAgreementRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
    }

    /**
     * 회원가입
     */
    @Transactional
    public void signup(SignupRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        validateBcryptLength(request.password());
        validateSignup(request, normalizedEmail);

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = User.create(
                request.loginId(),
                normalizedEmail,
                encodedPassword,
                request.name()
        );

        User savedUser = userRepository.save(user);

        TermsAgreement privacyAgreement = TermsAgreement.create(
                savedUser,
                TermsType.PRIVACY,
                LocalDateTime.now()
        );

        termsAgreementRepository.save(privacyAgreement);
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        validateBcryptLength(request.password());

        User user = userRepository
                .findByLoginId(request.loginId())
                .orElseThrow(() ->
                        new AuthException(
                                AuthErrorCode.INVALID_CREDENTIALS
                        )
                );

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new AuthException(
                    AuthErrorCode.INVALID_CREDENTIALS
            );
        }

        JwtProvider.IssuedTokens issuedTokens =
                jwtProvider.issueTokens(user.getId());

        String refreshTokenHash = TokenHashUtil.sha256(
                issuedTokens.refreshToken()
        );

        saveOrUpdateRefreshToken(
                user,
                refreshTokenHash,
                issuedTokens.refreshTokenExpiresAt()
        );

        return TokenResponse.of(
                issuedTokens.accessToken(),
                issuedTokens.refreshToken()
        );
    }

    /**
     * Refresh Token을 검증하고 Access/Refresh Token을 모두 교체한다.
     */
    @Transactional(noRollbackFor = AuthException.class)
    public TokenResponse reissue(TokenReissueRequest request) {
        String rawRefreshToken = request.refreshToken();
        String tokenHash = TokenHashUtil.sha256(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(this::invalidRefreshToken);

        if (storedToken.isExpired(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw invalidRefreshToken();
        }

        if (!jwtProvider.validateRefreshToken(rawRefreshToken)) {
            throw invalidRefreshToken();
        }

        Long tokenUserId;

        try {
            tokenUserId = jwtProvider.getUserId(rawRefreshToken);
        } catch (IllegalArgumentException exception) {
            throw invalidRefreshToken();
        }

        User user = storedToken.getUser();

        if (!Objects.equals(tokenUserId, user.getId())) {
            throw invalidRefreshToken();
        }

        JwtProvider.IssuedTokens issuedTokens =
                jwtProvider.issueTokens(user.getId());

        storedToken.update(
                TokenHashUtil.sha256(issuedTokens.refreshToken()),
                issuedTokens.refreshTokenExpiresAt()
        );

        return TokenResponse.of(
                issuedTokens.accessToken(),
                issuedTokens.refreshToken()
        );
    }

    /**
     * 저장된 Refresh Token을 삭제한다.
     */
    @Transactional
    public void logout(Long userId) {
        User user = getAuthenticatedUser(userId);
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * 현재 비밀번호를 검증한 뒤 새 비밀번호로 교체하고 Refresh Token을 폐기한다.
     */
    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {
        User user = getAuthenticatedUser(userId);

        validateBcryptLength(request.currentPassword());
        validateBcryptLength(request.newPassword());

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {
            throw new AuthException(
                    AuthErrorCode.INVALID_CURRENT_PASSWORD
            );
        }

        if (!Objects.equals(
                request.newPassword(),
                request.newPasswordConfirm()
        )) {
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {
            throw new AuthException(
                    AuthErrorCode.SAME_AS_CURRENT_PASSWORD
            );
        }

        user.changePassword(
                passwordEncoder.encode(request.newPassword())
        );
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * 회원가입 입력값과 중복 여부 검증
     */
    private void validateSignup(
            SignupRequest request,
            String normalizedEmail
    ) {
        if (!Boolean.TRUE.equals(request.privacyAgreed())) {
            throw new AuthException(
                    AuthErrorCode.PRIVACY_CONSENT_REQUIRED
            );
        }

        if (!Objects.equals(
                request.password(),
                request.passwordConfirm()
        )) {
            throw new AuthException(
                    AuthErrorCode.PASSWORD_MISMATCH
            );
        }

        if (userRepository.existsByLoginId(request.loginId())) {
            throw new AuthException(
                    AuthErrorCode.LOGIN_ID_ALREADY_EXISTS
            );
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new AuthException(
                    AuthErrorCode.EMAIL_ALREADY_EXISTS
            );
        }
    }

    /**
     * 이메일 앞뒤 공백 제거 및 소문자 변환
     */
    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    /**
     * 사용자에게 기존 Refresh Token이 있으면 갱신하고,
     * 없으면 새로 생성
     */
    private void saveOrUpdateRefreshToken(
            User user,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByUser(user)
                .map(existingToken -> {
                    existingToken.update(
                            tokenHash,
                            expiresAt
                    );

                    return existingToken;
                })
                .orElseGet(() ->
                        RefreshToken.create(
                                user,
                                tokenHash,
                                expiresAt
                        )
                );

        refreshTokenRepository.save(refreshToken);
    }

    private User getAuthenticatedUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new AuthException(AuthErrorCode.INVALID_TOKEN)
                );
    }

    private AuthException invalidRefreshToken() {
        return new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    private void validateBcryptLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
