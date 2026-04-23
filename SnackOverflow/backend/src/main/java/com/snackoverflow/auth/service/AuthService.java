package com.snackoverflow.auth.service;

import com.snackoverflow.auth.dto.LoginRequest;
import com.snackoverflow.auth.dto.LoginResponse;
import com.snackoverflow.auth.dto.SignupRequest;
import com.snackoverflow.auth.entity.RefreshToken;
import com.snackoverflow.auth.repository.RefreshTokenRepository;
import com.snackoverflow.security.JwtProperties;
import com.snackoverflow.security.JwtTokenProvider;
import com.snackoverflow.user.dto.UserSummaryResponse;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserSummaryResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .username(request.username())
                .nickname(request.nickname())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        return UserSummaryResponse.from(userRepository.save(user));
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UUID userId = UUID.fromString(auth.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        UUID familyId = UUID.randomUUID();
        String rawRefresh = jwtTokenProvider.createRefreshToken(userId, familyId);
        String accessToken = jwtTokenProvider.createAccessToken(userId, user.getUsername(), user.getRole().name());

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash(rawRefresh))
                .userId(userId)
                .familyId(familyId)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(token);

        return new LoginResult(
                new LoginResponse(accessToken, jwtProperties.getAccessTtlSeconds(),
                        userId, user.getUsername(), user.getNickname(), user.getRole()),
                rawRefresh
        );
    }

    @Transactional
    public TokenRefreshResult refresh(String rawRefreshToken) {
        String tokenHash = hash(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (stored.isRevoked()) {
            // 재사용 감지 → family 전체 무효화
            refreshTokenRepository.revokeAllByFamilyId(stored.getFamilyId());
            throw new IllegalArgumentException("토큰이 재사용되었습니다. 보안을 위해 재로그인이 필요합니다.");
        }

        if (stored.isExpired()) {
            throw new IllegalArgumentException("만료된 토큰입니다. 다시 로그인해주세요.");
        }

        stored.revoke();

        UUID userId = stored.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        String newRawRefresh = jwtTokenProvider.createRefreshToken(userId, stored.getFamilyId());
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getUsername(), user.getRole().name());

        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(hash(newRawRefresh))
                .userId(userId)
                .familyId(stored.getFamilyId())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(newToken);

        return new TokenRefreshResult(newAccessToken, jwtProperties.getAccessTtlSeconds(), newRawRefresh);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(t -> {
                    t.revoke();
                    refreshTokenRepository.save(t);
                });
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record LoginResult(LoginResponse loginResponse, String rawRefreshToken) {}
    public record TokenRefreshResult(String accessToken, long accessExpiresIn, String rawRefreshToken) {}
}
