package com.wvw.mmw.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "아이디는 필수입니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(max = 72, message = "비밀번호는 72자 이하로 입력해 주세요.")
        String password
) {
}
