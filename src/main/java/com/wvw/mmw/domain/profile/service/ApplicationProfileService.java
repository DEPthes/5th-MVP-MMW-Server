package com.wvw.mmw.domain.profile.service;

import com.wvw.mmw.domain.profile.dto.ApplicationProfileResponse;
import com.wvw.mmw.domain.profile.dto.CreateApplicationProfileRequest;
import com.wvw.mmw.domain.profile.entity.ApplicationProfile;
import com.wvw.mmw.domain.profile.repository.ApplicationProfileRepository;
import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationProfileService {

    private final ApplicationProfileRepository profileRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ApplicationProfileResponse> findAll(Long userId) {
        return profileRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ApplicationProfileResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationProfileResponse create(Long userId, CreateApplicationProfileRequest request) {
        ApplicationProfile profile = ApplicationProfile.builder()
                .user(entityManager.getReference(User.class, userId))
                .companyName(request.companyName())
                .jobPosition(request.jobPosition())
                .careerLevel(request.careerLevel())
                .build();

        return ApplicationProfileResponse.from(profileRepository.save(profile));
    }

    @Transactional
    public ApplicationProfileResponse update(Long userId, Long profileId,
                                             CreateApplicationProfileRequest request) {
        ApplicationProfile profile = findOwned(userId, profileId);
        profile.update(request.companyName(), request.jobPosition(), request.careerLevel());

        return ApplicationProfileResponse.from(profile);
    }

    @Transactional
    public void delete(Long userId, Long profileId) {
        profileRepository.delete(findOwned(userId, profileId));
    }

    private ApplicationProfile findOwned(Long userId, Long profileId) {
        return profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
