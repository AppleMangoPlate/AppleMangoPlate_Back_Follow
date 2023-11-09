package com.example.Applemango_BE_Follow.auth.controller;

import com.example.Applemango_BE_Follow.auth.dto.GlobalResDto;
import com.example.Applemango_BE_Follow.auth.dto.JoinRequest;
import com.example.Applemango_BE_Follow.auth.dto.LoginRequest;
import com.example.Applemango_BE_Follow.auth.jwt.JwtTokenUtil;
import com.example.Applemango_BE_Follow.auth.jwt.PrincipalDetails;
import com.example.Applemango_BE_Follow.auth.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jwt-login")
public class JwtLoginController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/join")
    public GlobalResDto join(@RequestBody @Valid JoinRequest joinRequest) {
        return userService.join(joinRequest);
    }

    @PostMapping("/login")
    public GlobalResDto login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        return userService.login(loginRequest, response);
    }

    @DeleteMapping("/logout")
    public GlobalResDto logout(@RequestBody @Valid String userEmail) {
        return userService.logout(userEmail);
    }

    @GetMapping("/issue/token")
    public GlobalResDto issuedToken(@AuthenticationPrincipal PrincipalDetails userDetails, HttpServletResponse response) {
        response.addHeader(JwtTokenUtil.ACCESS_TOKEN, jwtTokenUtil.createToken(userDetails.getUsername(), "Access"));
        return new GlobalResDto("Success IssuedToken", HttpStatus.OK.value());
    }

}
