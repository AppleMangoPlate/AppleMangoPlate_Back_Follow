package com.example.Applemango_BE_Follow.auth.jwt;

import com.example.Applemango_BE_Follow.auth.domain.User;
import com.example.Applemango_BE_Follow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    public final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Not found User")
        );

        PrincipalDetails userDetails = new PrincipalDetails();
        userDetails.setUser(user);

        return userDetails;
    }
}
