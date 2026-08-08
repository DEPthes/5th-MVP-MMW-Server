package com.wvw.mmw.domain.profile.repository;

import com.wvw.mmw.domain.profile.entity.ApplicationProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationProfileRepository extends JpaRepository<ApplicationProfile, Long> {

    // 본인 소유인지 함께 검증하며 조회한다.
    Optional<ApplicationProfile> findByIdAndUserId(Long id, Long userId);

    List<ApplicationProfile> findByUserIdOrderByCreatedAtDesc(Long userId);
}