package com.zk.projectboot.security;

import com.zk.projectboot.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class BlogUserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String username;
    private final String password;
    private final User.UserStatus status;
    private final String roleName;

    public BlogUserPrincipal(Integer id, String username, String password,
                             User.UserStatus status, String roleName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.roleName = roleName;
    }

    public static BlogUserPrincipal from(User user) {
        return new BlogUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                user.getRole().getRoleName()
        );
    }

    public Integer getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != User.UserStatus.banned;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == User.UserStatus.active;
    }
}
