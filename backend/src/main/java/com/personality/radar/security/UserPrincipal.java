package com.personality.radar.security;

import com.personality.radar.domain.Role;
import com.personality.radar.domain.UserAccount;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String phone;
    private final String passwordHash;
    private final Role role;

    public UserPrincipal(UserAccount user) {
        this.id = user.getId();
        this.phone = user.getPhone();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
    }

    public Long id() {
        return id;
    }

    public Role role() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return phone;
    }
}

