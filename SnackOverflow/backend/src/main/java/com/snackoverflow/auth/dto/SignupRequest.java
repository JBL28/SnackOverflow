package com.snackoverflow.auth.dto;

import com.snackoverflow.common.validation.SafePassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank
        @Size(min = 3, max = 20, message = "아이디는 3~20자여야 합니다.")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "아이디는 영문, 숫자, 언더스코어만 사용 가능합니다.")
        String username,

        @NotBlank
        @Size(min = 2, max = 30, message = "닉네임은 2~30자여야 합니다.")
        String nickname,

        @NotBlank
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        String email,

        @NotBlank
        @SafePassword
        String password
) {}
