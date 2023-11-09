package com.example.Applemango_BE_Follow.auth.repository;

import com.example.Applemango_BE_Follow.auth.domain.RefreshToken;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserEmail (String email);
    void deleteRefreshTokenByUserEmail (@NotNull String userEmail);
}
