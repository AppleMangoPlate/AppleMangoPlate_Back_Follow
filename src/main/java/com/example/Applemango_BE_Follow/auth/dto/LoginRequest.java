package com.example.Applemango_BE_Follow.auth.dto;

import com.example.Applemango_BE_Follow.auth.domain.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
        @NotBlank(message = "로그인 아이디가 비어있습니다.")
        private String email;
        @NotBlank(message = "비밀번호가 비어있습니다.")
        private String password;
}
