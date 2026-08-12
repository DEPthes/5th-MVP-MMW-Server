package com.wvw.mmw.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenReissueRequest(

        @NotBlank(message = "Refresh Token은 필수입니다.")
        @Size(max = 4096, message = "Refresh Token 형식이 올바르지 않습니다.")
        String refreshToken
) {
}
