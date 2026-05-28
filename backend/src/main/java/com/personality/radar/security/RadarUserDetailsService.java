package com.personality.radar.security;

import com.personality.radar.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RadarUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public RadarUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return users.findByPhone(phone)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("账号不存在"));
    }
}

