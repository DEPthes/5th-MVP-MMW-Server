package com.wvw.mmw.domain.user.service;

import com.wvw.mmw.domain.auth.error.AuthErrorCode;
import com.wvw.mmw.domain.auth.error.AuthException;
import com.wvw.mmw.domain.user.dto.request.UpdateProfileRequest;
import com.wvw.mmw.domain.user.dto.response.UserProfileResponse;
import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import com.wvw.mmw.domain.user.repository.UserRepository;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getMyProfile(Long userId) {
        return UserProfileResponse.from(getAuthenticatedUser(userId));
    }

    @Transactional
    public UserProfileResponse updateMyProfile(
            Long userId,
            UpdateProfileRequest request
    ) {
        validateProfileUpdate(request);

        User user = getAuthenticatedUser(userId);
        user.updateProfile(
                trimIfPresent(request.nickname()),
                trimIfPresent(request.desiredPosition())
        );

        return UserProfileResponse.from(user);
    }

    @Transactional
    public void deleteMyAccount(Long userId) {
        userRepository.delete(getAuthenticatedUser(userId));
    }

    private User getAuthenticatedUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new AuthException(AuthErrorCode.INVALID_TOKEN)
                );
    }

    private void validateProfileUpdate(UpdateProfileRequest request) {
        if (request.nickname() == null
                && request.desiredPosition() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if ((request.nickname() != null
                && !StringUtils.hasText(request.nickname()))
                || (request.desiredPosition() != null
                && !StringUtils.hasText(request.desiredPosition()))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String trimIfPresent(String value) {
        return value == null ? null : value.trim();
    }
}
