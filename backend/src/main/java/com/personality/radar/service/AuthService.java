package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.Role;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.UserRepository;
import com.personality.radar.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public ApiDtos.AuthResponse register(ApiDtos.RegisterRequest request) {
        if (users.existsByPhone(request.phone())) {
            throw new BusinessException(409, "手机号已注册");
        }
        UserAccount user = new UserAccount();
        user.setPhone(request.phone());
        user.setDisplayName(request.displayName());
        user.setPasswordHash(encoder.encode(request.password()));
        user.setRole(Role.USER);
        users.save(user);
        return authResponse(user);
    }

    public ApiDtos.AuthResponse login(ApiDtos.LoginRequest request) {
        UserAccount user = users.findByPhone(request.phone())
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        return authResponse(user);
    }

    private ApiDtos.AuthResponse authResponse(UserAccount user) {
        return new ApiDtos.AuthResponse(jwtService.createToken(user.getPhone()), DtoMapper.user(user));
    }
}

