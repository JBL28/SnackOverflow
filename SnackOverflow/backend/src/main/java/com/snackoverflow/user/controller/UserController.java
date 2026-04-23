package com.snackoverflow.user.controller;

import com.snackoverflow.common.ApiResponse;
import com.snackoverflow.user.dto.ChangePasswordRequest;
import com.snackoverflow.user.dto.UpdateNicknameRequest;
import com.snackoverflow.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyProfile(extractUserId(auth))));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<?>> updateNickname(Authentication auth,
                                                          @RequestBody @Valid UpdateNicknameRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateNickname(extractUserId(auth), request)));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication auth,
                                                             @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(extractUserId(auth), request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(Authentication auth,
                                                       HttpServletResponse response) {
        userService.withdraw(extractUserId(auth));
        clearRefreshCookie(response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private UUID extractUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("snack_refresh", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
