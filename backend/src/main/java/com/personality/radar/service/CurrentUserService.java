package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.repository.UserRepository;
import com.personality.radar.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository users;

    public CurrentUserService(UserRepository users) {
        this.users = users;
    }

    public UserAccount requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(401, "请先登录");
        }
        UserAccount user = users.findById(principal.id()).orElseThrow(() -> new BusinessException(401, "登录状态已失效"));
        if (!user.isActive()) {
            throw new BusinessException(403, "账号已停用");
        }
        return user;
    }
}
