package org.example.contest.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.example.contest.domain.UserAccount;
import org.example.contest.domain.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 当前登录用户。权限统一采用 ROLE_ADMIN/ROLE_STUDENT/ROLE_TEACHER。
 */
public record UserPrincipal(UserAccount user) implements UserDetails {
    public UUID id() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == AccountStatus.NORMAL;
    }
}
