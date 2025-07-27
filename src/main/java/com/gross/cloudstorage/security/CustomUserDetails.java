package com.gross.cloudstorage.security;

import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@ToString
public class CustomUserDetails implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public CustomUserDetails(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof CustomUserDetails that)) return false;

        return Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(username);
        result = 31 * result + Objects.hashCode(password);
        return result;
    }
}
