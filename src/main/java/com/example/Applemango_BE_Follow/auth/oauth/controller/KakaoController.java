package com.example.Applemango_BE_Follow.auth.oauth.controller;

import com.example.Applemango_BE_Follow.auth.oauth.dto.KakaoLoginResponse;
import com.example.Applemango_BE_Follow.auth.oauth.service.KakaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class KakaoController {
    private final KakaoService kakaoService;
    private final Logger logger = LoggerFactory.getLogger(KakaoController.class);

    @PostMapping("/login/oauth2/callback/kakao")
    public KakaoLoginResponse kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) {
        String kakaoAccessToken = kakaoService.getKakaoAccessToken(code).getAccess_token();

        return kakaoService.kakaoLogin(kakaoAccessToken, response);
    }
}
