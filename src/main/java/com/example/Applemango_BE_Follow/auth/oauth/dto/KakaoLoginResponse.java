package com.example.Applemango_BE_Follow.auth.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KakaoLoginResponse {
    private boolean loginSuccess;
    private long userId;
    private String email;
}
