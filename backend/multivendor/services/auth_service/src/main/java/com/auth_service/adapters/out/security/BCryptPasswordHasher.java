package com.auth_service.adapters.out.security;

import com.auth_service.application.port.out.PasswordHasher;
import com.auth_service.domain.model.vo.PasswordHash;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final PasswordEncoder passwordEncoder;
    public BCryptPasswordHasher(){this.passwordEncoder = new BCryptPasswordEncoder();}
    @Override
    public PasswordHash hash(String password){
        return new PasswordHash(passwordEncoder.encode(password));
    }
    @Override
    public boolean matches(String password, PasswordHash passwordHash) {
        return passwordEncoder.matches(
                password,
                passwordHash.value()
        );
    }
}