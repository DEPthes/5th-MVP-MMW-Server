package com.wvw.mmw.domain.profile.controller;

import com.wvw.mmw.domain.profile.dto.ApplicationProfileResponse;
import com.wvw.mmw.domain.profile.dto.CreateApplicationProfileRequest;
import com.wvw.mmw.domain.profile.service.ApplicationProfileService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * X-User-Id 헤더는 임시 방편.
 */
@RestController
@RequestMapping("/application-profiles")
@RequiredArgsConstructor
public class ApplicationProfileController {

    private final ApplicationProfileService applicationProfileService;

    @GetMapping
    public ResponseEntity<List<ApplicationProfileResponse>> list(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(applicationProfileService.findAll(userId));
    }

    @PostMapping
    public ResponseEntity<ApplicationProfileResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateApplicationProfileRequest request) {

        ApplicationProfileResponse response = applicationProfileService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ApplicationProfileResponse> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long profileId,
            @Valid @RequestBody CreateApplicationProfileRequest request) {

        return ResponseEntity.ok(applicationProfileService.update(userId, profileId, request));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long profileId) {

        applicationProfileService.delete(userId, profileId);
        return ResponseEntity.noContent().build();
    }
}
