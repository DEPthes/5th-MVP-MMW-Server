package com.wvw.mmw.domain.auth.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "아이디는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,12}$",
                message = "아이디는 영문과 숫자를 모두 포함하여 8~12자로 입력해 주세요."
        )
        String loginId,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식으로 입력해 주세요.")
        @Size(max = 255, message = "이메일은 255자 이하로 입력해 주세요.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,72}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함하여 8~72자로 입력해 주세요."
        )
        String password,

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        String passwordConfirm,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
        String name,

        @NotNull(message = "개인정보 수집 동의 여부는 필수입니다.")
        @AssertTrue(message = "개인정보 수집에 동의해야 회원가입할 수 있습니다.")
        Boolean privacyAgreed
) {
}
