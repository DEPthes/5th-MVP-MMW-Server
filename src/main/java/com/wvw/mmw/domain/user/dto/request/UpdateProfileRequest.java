package com.wvw.mmw.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(max = 50, message = "닉네임은 50자 이하로 입력해 주세요.")
        String nickname,

        @Size(max = 100, message = "희망 직무는 100자 이하로 입력해 주세요.")
        String desiredPosition
) {
}
