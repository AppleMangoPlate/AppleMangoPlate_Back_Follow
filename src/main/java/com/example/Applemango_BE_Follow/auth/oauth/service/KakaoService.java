package com.example.Applemango_BE_Follow.auth.oauth.service;

import com.example.Applemango_BE_Follow.auth.domain.RefreshToken;
import com.example.Applemango_BE_Follow.auth.domain.User;
import com.example.Applemango_BE_Follow.auth.dto.TokenDto;
import com.example.Applemango_BE_Follow.auth.jwt.JwtTokenUtil;
import com.example.Applemango_BE_Follow.auth.oauth.dto.KakaoLoginResponse;
import com.example.Applemango_BE_Follow.auth.oauth.dto.KakaoTokenDto;
import com.example.Applemango_BE_Follow.auth.repository.RefreshTokenRepository;
import com.example.Applemango_BE_Follow.auth.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoService {
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Logger logger = LoggerFactory.getLogger(KakaoService.class);

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String client_id;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String client_secret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirect_uri;

    public KakaoTokenDto getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", client_id);
        params.add("redirect_uri", redirect_uri);
        params.add("code", code);
        params.add("client_secret", client_secret);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenReq = new HttpEntity<>(params, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> accessTokenReq = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenReq,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        KakaoTokenDto kakaoTokenDto = null;
        try {
            kakaoTokenDto = objectMapper.readValue(accessTokenReq.getBody(), KakaoTokenDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return kakaoTokenDto;
    }

    public KakaoLoginResponse kakaoLogin(String kakaoAccessToken, HttpServletResponse response) {
        User user = getKakaoInfo(kakaoAccessToken);

        User existOwner = userRepository.findByEmail(user.getEmail()).orElse(null);
        if(existOwner == null) {
            logger.info("첫 로그인 회원");
            userRepository.save(user);
        }

        TokenDto tokenDto = jwtTokenUtil.createAllToken(user.getEmail());

        RefreshToken refreshToken = refreshTokenRepository.findByUserEmail(user.getEmail()).orElse(null);
        if(refreshToken != null) {
            refreshTokenRepository.save(refreshToken.updateToken(tokenDto.getRefreshToken()));
        } else {
            RefreshToken newToken = new RefreshToken(tokenDto.getRefreshToken(), user.getEmail());
            refreshTokenRepository.save(newToken);
        }
        response.addHeader(JwtTokenUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtTokenUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());

        return new KakaoLoginResponse(true, existOwner==null ? user.getId() : existOwner.getId(), user.getEmail());
    }

    public User getKakaoInfo(String kakaoAccessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + kakaoAccessToken);
        httpHeaders.add("COntent-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<String> requestEntiry = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                requestEntiry,
                String.class
        );

        String userInfo = responseEntity.getBody();

        Gson gsonObj = new Gson();
        Map<?, ?> data = gsonObj.fromJson(userInfo, Map.class);

        boolean emailAgreement = (boolean) ((Map<?, ?>) (data.get("kakao_account"))).get("email_needs_agreement");
        String email;
        if(emailAgreement) {
            email = "";
        } else {
            email = (String) ((Map<?, ?>) (data.get("kakao_acount"))).get("email");
        }

        boolean nicknameAgreement = (boolean) ((Map<?, ?>) (data.get("kakao_account"))).get("profile_nickname_needs_agreement");
        String nickName;
        if(nicknameAgreement) {
            nickName = "";
        } else {
            nickName = (String) ((Map<?, ?>) (data.get("properties"))).get("nickname");
        }

        User user= new User().builder().
                email(email).
                nickName(nickName).
                build();

        return user;
    }
}
