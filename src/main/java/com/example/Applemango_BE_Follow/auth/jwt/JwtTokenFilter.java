package com.example.Applemango_BE_Follow.auth.jwt;

import com.example.Applemango_BE_Follow.auth.dto.GlobalResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtTokenUtil.getHeaderToken(request, "Access");
        String refreshToken = jwtTokenUtil.getHeaderToken(request, "Refresh");

        if (accessToken != null) {
            if (!jwtTokenUtil.tokenValidataion(accessToken)) {
                jwtExceptionHandler(response, "AccessToken Expired", HttpStatus.BAD_REQUEST);
                return;
            }
            setAuthentication(jwtTokenUtil.getEmailFromToken(accessToken));
        } else if (refreshToken != null) {
            if (!jwtTokenUtil.refreshTokenValidation(refreshToken)) {
                jwtExceptionHandler(response, "RefreshToken Expired", HttpStatus.BAD_REQUEST);
                return;
            }
            setAuthentication(jwtTokenUtil.getEmailFromToken(refreshToken));
        }

        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String email) {
        Authentication authentication = jwtTokenUtil.createAuthentication(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void jwtExceptionHandler (HttpServletResponse response, String msg, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        try {
            String json = new ObjectMapper().writeValueAsString(new GlobalResDto(msg, status.value()));
            response.getWriter().write(json);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
