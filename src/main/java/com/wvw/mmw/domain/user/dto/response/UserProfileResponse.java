package com.wvw.mmw.domain.user.dto.response;

import com.wvw.mmw.domain.user.entity.User;

public record UserProfileResponse(
        Long id,
        String loginId,
        String nickname,
        String desiredPosition
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getLoginId(),
                user.getNickname(),
                user.getDesiredPosition()
        );
    }
}
