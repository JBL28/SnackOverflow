package com.snackoverflow.user;

import com.snackoverflow.AbstractIntegrationTest;
import com.snackoverflow.auth.repository.RefreshTokenRepository;
import com.snackoverflow.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User API 통합 테스트")
class UserControllerTest extends AbstractIntegrationTest {

    private static final String USERNAME = "user_ctrl1";
    private static final String EMAIL = "user1@test.com";
    private static final String PASSWORD = "Test123!";
    private static final String NICKNAME = "유저닉네임";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        performSignup(USERNAME, EMAIL, PASSWORD, NICKNAME);
        accessToken = loginAndGetToken(USERNAME, PASSWORD);
    }

    @AfterEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("내 프로필 조회 - 200, 사용자 정보 반환")
    void getMyProfile_authenticated_returnsProfile() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(USERNAME))
                .andExpect(jsonPath("$.data.nickname").value(NICKNAME));
    }

    @Test
    @DisplayName("내 프로필 조회 - 인증 없으면 401")
    void getMyProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("닉네임 변경 성공 - 200, 변경된 닉네임 반환")
    void updateNickname_success() throws Exception {
        Map<String, String> body = Map.of("nickname", "새닉네임");

        mockMvc.perform(patch("/api/users/me/nickname")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("닉네임 변경 실패 - 빈 값 입력 시 400")
    void updateNickname_blank_returns400() throws Exception {
        Map<String, String> body = Map.of("nickname", "");

        mockMvc.perform(patch("/api/users/me/nickname")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 변경 성공 - 200 반환")
    void changePassword_success() throws Exception {
        Map<String, String> body = Map.of(
                "currentPassword", PASSWORD,
                "newPassword", "NewPass456!"
        );

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_wrongCurrent_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "currentPassword", "WrongPass1!",
                "newPassword", "NewPass456!"
        );

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 현재와 동일")
    void changePassword_sameAsCurrentPassword_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "currentPassword", PASSWORD,
                "newPassword", PASSWORD
        );

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 200 반환, refresh 쿠키 만료")
    void withdraw_success() throws Exception {
        Cookie refreshCookie = loginAndGetRefreshCookie(USERNAME, PASSWORD);

        var result = mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", bearer(accessToken))
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        Cookie clearedCookie = result.getResponse().getCookie("snack_refresh");
        assertThat(clearedCookie).isNotNull();
        assertThat(clearedCookie.getMaxAge()).isZero();
    }
}
