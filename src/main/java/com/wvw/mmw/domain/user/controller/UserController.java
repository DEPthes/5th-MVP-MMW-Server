package com.wvw.mmw.domain.user.controller;

import com.wvw.mmw.global.response.ApiResponse;
import com.wvw.mmw.domain.user.dto.request.UpdateProfileRequest;
import com.wvw.mmw.domain.user.dto.response.UserProfileResponse;
import com.wvw.mmw.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId
    ) {
        UserProfileResponse response = userService.getMyProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "내 정보를 조회했습니다.",
                        response
                )
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResponse response = userService.updateMyProfile(
                userId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "프로필을 수정했습니다.",
                        response
                )
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal Long userId
    ) {
        userService.deleteMyAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
