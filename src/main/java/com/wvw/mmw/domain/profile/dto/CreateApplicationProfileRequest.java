package com.wvw.mmw.domain.profile.dto;

import com.wvw.mmw.domain.profile.entity.CareerLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateApplicationProfileRequest(

        @NotBlank
        @Size(max = 100)
        String companyName,

        @NotBlank
        @Size(max = 100)
        String jobPosition,

        @NotNull
        CareerLevel careerLevel
) {
}
