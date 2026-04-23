package com.snackoverflow.auth;

import com.snackoverflow.AbstractIntegrationTest;
import com.snackoverflow.auth.repository.RefreshTokenRepository;
import com.snackoverflow.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth API 통합 테스트")
class AuthControllerTest extends AbstractIntegrationTest {

    private static final String USERNAME = "auth_user1";
    private static final String EMAIL = "auth1@test.com";
    private static final String PASSWORD = "Test123!";
    private static final String NICKNAME = "테스트유저";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @AfterEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 - 201 반환, 사용자 정보 포함")
    void signup_success() throws Exception {
        Map<String, String> body = Map.of(
                "username", USERNAME,
                "nickname", NICKNAME,
                "email", EMAIL,
                "password", PASSWORD
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(USERNAME));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 아이디")
    void signup_duplicateUsername_returns400() throws Exception {
        performSignup(USERNAME, EMAIL, PASSWORD, NICKNAME);

        Map<String, String> body = Map.of(
                "username", USERNAME,
                "nickname", "다른닉네임",
                "email", "other@test.com",
                "password", PASSWORD
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signup_duplicateEmail_returns400() throws Exception {
        performSignup(USERNAME, EMAIL, PASSWORD, NICKNAME);

        Map<String, String> body = Map.of(
                "username", "other_user",
                "nickname", "다른닉네임",
                "email", EMAIL,
                "password", PASSWORD
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효하지 않은 비밀번호 (5자)")
    void signup_invalidPassword_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "username", USERNAME,
                "nickname", NICKNAME,
                "email", EMAIL,
                "password", "12345"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 - accessToken 반환, refresh 쿠키 설정")
    void login_success() throws Exception {
        performSignup(USERNAME, EMAIL, PASSWORD, NICKNAME);

        Map<String, String> body = Map.of("username", USERNAME, "password", PASSWORD);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("snack_refresh");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.isHttpOnly()).isTrue();
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_wrongPassword_returns401() throws Exception {
        performSignup(USERNAME, EMAIL, PASSWORD, NICKNAME);

        Map<String, String> body = Map.of("username", USERNAME, "password", "WrongPass1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 새 accessToken 반환")
    void refresh_success() throws Exception {
        performSignup(USERNAME, EMAIL, PASSWORD, NICKNAME);
        Cookie refreshCookie = loginAndGetRefreshCookie(USERNAME, PASSWORD);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 쿠키 없음")
    void refresh_noCookie_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 성공 - refresh 쿠키 만료 처리")
    void logout_success() throws Exception {
        performSignup(USERNAME, EMAIL, PASSWORD, NICKNAME);
        String token = loginAndGetToken(USERNAME, PASSWORD);
        Cookie refreshCookie = loginAndGetRefreshCookie(USERNAME, PASSWORD);

        var result = mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearer(token))
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andReturn();

        Cookie clearedCookie = result.getResponse().getCookie("snack_refresh");
        assertThat(clearedCookie).isNotNull();
        assertThat(clearedCookie.getMaxAge()).isZero();
    }
}
