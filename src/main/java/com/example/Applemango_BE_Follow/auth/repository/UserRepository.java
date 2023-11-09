package com.example.Applemango_BE_Follow.auth.repository;

import com.example.Applemango_BE_Follow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); //회원가입할때 적은 이메일이 존재하는지 확인
    boolean existsByNickName(String nickName); //회원가입할때 적은 닉네임이 존재하는지 확인
    Optional<User> findByEmail(String email);
}
