package com.wvw.mmw.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Size(max = 72, message = "현재 비밀번호는 72자 이하로 입력해 주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,72}$",
                message = "새 비밀번호는 영문, 숫자, 특수문자를 모두 포함하여 8~72자로 입력해 주세요."
        )
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        @Size(max = 72, message = "새 비밀번호 확인은 72자 이하로 입력해 주세요.")
        String newPasswordConfirm
) {
}
