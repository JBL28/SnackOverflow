package com.snackoverflow.auth.controller;

import com.snackoverflow.auth.dto.LoginRequest;
import com.snackoverflow.auth.dto.SignupRequest;
import com.snackoverflow.auth.service.AuthService;
import com.snackoverflow.common.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "snack_refresh";
    private static final int REFRESH_COOKIE_MAX_AGE = 60 * 60 * 24 * 14;

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@RequestBody @Valid SignupRequest request) {
        var user = authService.signup(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid LoginRequest request,
                                                HttpServletResponse response) {
        var result = authService.login(request);
        setRefreshCookie(response, result.rawRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok(result.loginResponse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(HttpServletRequest request,
                                                  HttpServletResponse response) {
        String rawRefresh = extractRefreshCookie(request);
        if (rawRefresh == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("리프레시 토큰이 없습니다."));
        }
        var result = authService.refresh(rawRefresh);
        setRefreshCookie(response, result.rawRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok(
                new TokenRefreshBody(result.accessToken(), result.accessExpiresIn())
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                                     HttpServletResponse response) {
        String rawRefresh = extractRefreshCookie(request);
        if (rawRefresh != null) {
            authService.logout(rawRefresh);
        }
        clearRefreshCookie(response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private void setRefreshCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private record TokenRefreshBody(String accessToken, long accessExpiresIn) {}
}
