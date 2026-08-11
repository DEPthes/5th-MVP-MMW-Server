package com.wvw.mmw.domain.auth.dto.response;

public record SessionResponse(
        boolean authenticated,
        Long userId
) {

    public static SessionResponse authenticated(Long userId) {
        return new SessionResponse(true, userId);
    }
}
