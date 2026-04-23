package com.snackoverflow.snack;

import com.snackoverflow.AbstractIntegrationTest;
import com.snackoverflow.auth.repository.RefreshTokenRepository;
import com.snackoverflow.snack.repository.SnackPurchaseRepository;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.entity.UserRole;
import com.snackoverflow.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("SnackPurchase API 통합 테스트")
class SnackPurchaseControllerTest extends AbstractIntegrationTest {

    private static final String ADMIN_USERNAME = "snack_admin";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String USER_USERNAME = "snack_user1";
    private static final String USER_EMAIL = "snackuser@test.com";
    private static final String PASSWORD = "Test123!";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SnackPurchaseRepository snackPurchaseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        User admin = User.builder()
                .username(ADMIN_USERNAME)
                .nickname("관리자")
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);
        adminToken = loginAndGetToken(ADMIN_USERNAME, PASSWORD);

        performSignup(USER_USERNAME, USER_EMAIL, PASSWORD, "일반유저");
        userToken = loginAndGetToken(USER_USERNAME, PASSWORD);
    }

    @AfterEach
    void cleanup() {
        snackPurchaseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("과자 목록 조회 - 인증 사용자는 200 반환")
    void getAll_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/snack-purchases")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("과자 목록 조회 - 미인증 시에도 200 반환 (공개 엔드포인트)")
    void getAll_unauthenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/snack-purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("상태별 과자 목록 조회 - IN_STOCK 필터 적용")
    void getAll_filteredByStatus_returns200() throws Exception {
        createSnack(adminToken, "허니버터칩");

        mockMvc.perform(get("/api/snack-purchases?status=IN_STOCK")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("IN_STOCK"));
    }

    @Test
    @DisplayName("과자 단건 조회 - 존재하지 않는 ID는 400")
    void getOne_notFound_returns400() throws Exception {
        mockMvc.perform(get("/api/snack-purchases/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isBadRequest());
    }

    // ── 생성 ──────────────────────────────────────────

    @Test
    @DisplayName("과자 생성 - 관리자는 201 반환")
    void create_admin_returns201() throws Exception {
        Map<String, String> body = Map.of("name", "새우깡");

        mockMvc.perform(post("/api/snack-purchases")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("새우깡"))
                .andExpect(jsonPath("$.data.status").value("IN_STOCK"));
    }

    @Test
    @DisplayName("과자 생성 - 일반 유저는 403 반환")
    void create_user_returns403() throws Exception {
        Map<String, String> body = Map.of("name", "새우깡");

        mockMvc.perform(post("/api/snack-purchases")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("과자 생성 실패 - 이름 누락 시 400")
    void create_missingName_returns400() throws Exception {
        Map<String, String> body = Map.of("name", "");

        mockMvc.perform(post("/api/snack-purchases")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isBadRequest());
    }

    // ── 이름 수정 ──────────────────────────────────────

    @Test
    @DisplayName("과자 이름 수정 - 관리자는 200 반환")
    void updateName_admin_returns200() throws Exception {
        String snackId = createSnack(adminToken, "오리온 초코파이");

        Map<String, String> body = Map.of("name", "롯데 초코파이");

        mockMvc.perform(patch("/api/snack-purchases/" + snackId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("롯데 초코파이"));
    }

    @Test
    @DisplayName("과자 이름 수정 - 일반 유저는 403 반환")
    void updateName_user_returns403() throws Exception {
        String snackId = createSnack(adminToken, "오리온 초코파이");

        Map<String, String> body = Map.of("name", "롯데 초코파이");

        mockMvc.perform(patch("/api/snack-purchases/" + snackId)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isForbidden());
    }

    // ── 상태 변경 ──────────────────────────────────────

    @Test
    @DisplayName("과자 상태 변경 - 일반 유저도 200 반환")
    void updateStatus_user_returns200() throws Exception {
        String snackId = createSnack(adminToken, "포카칩");

        Map<String, String> body = Map.of("status", "OUT_OF_STOCK");

        mockMvc.perform(patch("/api/snack-purchases/" + snackId + "/status")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OUT_OF_STOCK"));
    }

    @Test
    @DisplayName("과자 상태 변경 - 관리자도 200 반환")
    void updateStatus_admin_returns200() throws Exception {
        String snackId = createSnack(adminToken, "포카칩");

        Map<String, String> body = Map.of("status", "DELIVERING");

        mockMvc.perform(patch("/api/snack-purchases/" + snackId + "/status")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERING"));
    }

    // ── 삭제 ──────────────────────────────────────────

    @Test
    @DisplayName("과자 삭제 - 관리자는 200 반환")
    void delete_admin_returns200() throws Exception {
        String snackId = createSnack(adminToken, "삭제될 과자");

        mockMvc.perform(delete("/api/snack-purchases/" + snackId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("과자 삭제 - 일반 유저는 403 반환")
    void delete_user_returns403() throws Exception {
        String snackId = createSnack(adminToken, "삭제될 과자");

        mockMvc.perform(delete("/api/snack-purchases/" + snackId)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    // ── 헬퍼 ──────────────────────────────────────────

    private String createSnack(String token, String name) throws Exception {
        Map<String, String> body = Map.of("name", name);
        MvcResult result = mockMvc.perform(post("/api/snack-purchases")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();
    }
}
