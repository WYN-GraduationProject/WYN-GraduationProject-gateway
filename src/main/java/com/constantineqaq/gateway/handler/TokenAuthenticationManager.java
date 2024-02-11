package com.constantineqaq.gateway.handler;

import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
@Primary
public class TokenAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .map(a -> {
                    Collection<? extends GrantedAuthority> authorities = a.getAuthorities();
                    for (GrantedAuthority authority : authorities) {
                        String authorityAuthority = authority.getAuthority();
                        if ("ROLE_USER".equals(authorityAuthority)) {
                            return new UsernamePasswordAuthenticationToken(a.getPrincipal(), a.getCredentials(), a.getAuthorities());
                        }
                    }
                    return new UsernamePasswordAuthenticationToken(a.getPrincipal(), a.getCredentials());
                });
    }
}
