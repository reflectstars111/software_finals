package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.Role;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.UserRepository;
import com.personality.radar.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    @Transactional
    public ApiDtos.AuthResponse login(ApiDtos.LoginRequest request) {
        UserAccount user = users.findByPhone(request.phone())
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));
        if (!user.isActive()) {
            throw new BusinessException(403, "账号已停用");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new BusinessException(429, "登录失败次数过多，请稍后再试");
        }
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(Instant.now().plus(10, ChronoUnit.MINUTES));
            }
            users.save(user);
            throw new BusinessException(401, "账号或密码错误");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        users.save(user);
        return authResponse(user);
    }

    private ApiDtos.AuthResponse authResponse(UserAccount user) {
        return new ApiDtos.AuthResponse(jwtService.createToken(user.getPhone()), DtoMapper.user(user));
    }
}
