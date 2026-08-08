package com.wvw.mmw.domain.profile.dto;

import com.wvw.mmw.domain.profile.entity.ApplicationProfile;
import com.wvw.mmw.domain.profile.entity.CareerLevel;

public record ApplicationProfileResponse(
        Long id,
        String companyName,
        String jobPosition,
        CareerLevel careerLevel
) {

    public static ApplicationProfileResponse from(ApplicationProfile profile) {
        return new ApplicationProfileResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getJobPosition(),
                profile.getCareerLevel()
        );
    }
}
