package com.example.Applemango_BE_Follow.auth.service;

import com.example.Applemango_BE_Follow.auth.domain.RefreshToken;
import com.example.Applemango_BE_Follow.auth.domain.User;
import com.example.Applemango_BE_Follow.auth.dto.GlobalResDto;
import com.example.Applemango_BE_Follow.auth.dto.JoinRequest;
import com.example.Applemango_BE_Follow.auth.dto.LoginRequest;
import com.example.Applemango_BE_Follow.auth.dto.TokenDto;
import com.example.Applemango_BE_Follow.auth.jwt.JwtTokenUtil;
import com.example.Applemango_BE_Follow.auth.repository.RefreshTokenRepository;
import com.example.Applemango_BE_Follow.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder; //spring security를 사용해 로그인 구현 시 사용
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;


    //회원가입시, 아이디 (이메일) 중복 확인
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    //회원가입시, 닉네임 중복 확인
    public boolean checknickNameDuplicate(String nickName) {
        return userRepository.existsByNickName(nickName);
    }

    //JoinRequest를 입력받아 User로 변환 후 저장
    //이 과정에서 비밀번호는 암호화 되어 저장
    //이메일, 닉네임 중복시 예외처리 / 비밀번호랑 비밀번호체크랑 다르면 예외처리
    public GlobalResDto join(JoinRequest joinRequest) {
        if (checkEmailDuplicate(joinRequest.getEmail())) {
            throw new RuntimeException("Email is duplicated");
        }

        if (checknickNameDuplicate(joinRequest.getNickName())) {
            throw new RuntimeException("Nickname is duplicated");
        }

        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            throw new RuntimeException("Not matches password and passwordcheck");
        }

        //저장
        userRepository.save(joinRequest.toEntity(encoder.encode(joinRequest.getPassword())));
        return new GlobalResDto("Success join", HttpStatus.OK.value());
    }

    @Transactional
    public GlobalResDto login(LoginRequest request, HttpServletResponse response) {

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                new RuntimeException("Not found user"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Not matches Password");
        }


        else {
            TokenDto tokenDto = jwtTokenUtil.createAllToken(request.getEmail());

            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserEmail(request.getEmail());

            if(refreshToken.isPresent()) {
                refreshTokenRepository.save(refreshToken.get().updateToken(tokenDto.getRefreshToken()));
            }
            else {
                RefreshToken newToken = new RefreshToken(tokenDto.getRefreshToken(), request.getEmail());
                refreshTokenRepository.save(newToken);
            }

            setHeader(response, tokenDto);

            return new GlobalResDto("Success Login", HttpStatus.OK.value());
        }
    }

    @Transactional
    public GlobalResDto logout(String userEmail) {
        if (refreshTokenRepository.findByUserEmail(userEmail) == null)
            throw new RuntimeException("Not found login user");
        refreshTokenRepository.deleteRefreshTokenByUserEmail(userEmail);

        return new GlobalResDto("Success Logout", HttpStatus.OK.value());
    }

    private void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader(JwtTokenUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtTokenUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());
    }
}