package com.constantineqaq.gateway.security;

import com.constantineqaq.gateway.entity.dto.LoginData;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;

public class MyAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    @Getter
    @Setter
    private LoginData loginData;

    public MyAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
    }

    public MyAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    public MyAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials, LoginData loginData) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.loginData = loginData;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
