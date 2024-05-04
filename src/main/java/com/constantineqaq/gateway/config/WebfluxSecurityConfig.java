package com.constantineqaq.gateway.config;

import com.constantineqaq.gateway.security.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.LinkedList;


@EnableWebFluxSecurity
@Configuration
@Slf4j
public class WebfluxSecurityConfig {

    @Resource
    private MyAuthorizationManager myAuthorizationManager;

    @Resource
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;

    @Resource
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;

    @Resource
    private MyAuthenticationManager myAuthenticationManager;

    @Resource
    private MySecurityContextRepository mySecurityContextRepository;

    @Resource
    private MyAuthenticationEntryPoint myAuthenticationEntryPoint;

    @Resource
    private MyAccessDeniedHandler myAccessDeniedHandler;

    @Resource
    private MyAuthenticationConverter myAuthenticationConverter;

    @Resource
    private MyLogoutSuccessHandler myLogoutSuccessHandler;

    @Resource
    private WhiteListConfig whiteListConfig;


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {

        return httpSecurity
                .authorizeExchange(conf -> conf
                        .pathMatchers("/api/user/**").permitAll()
                        .pathMatchers("/api/video-pre/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/api/auth/login").permitAll()  // 允许OPTIONS请求到特定端点
                        .anyExchange().access(myAuthorizationManager)
                )
                .securityContextRepository(mySecurityContextRepository)
                .exceptionHandling(conf -> conf
                        .accessDeniedHandler(myAccessDeniedHandler)
                        .authenticationEntryPoint(myAuthenticationEntryPoint)
                )
                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(myLogoutSuccessHandler))
                .csrf().disable()
                .cors().and()
                .httpBasic().disable()
                .formLogin().disable()
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(reactiveAuthenticationManager());
        filter.setSecurityContextRepository(mySecurityContextRepository);
        filter.setServerAuthenticationConverter(myAuthenticationConverter);
        filter.setAuthenticationSuccessHandler(myAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(myAuthenticationFailureHandler);
        filter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/api/auth/login")
        );
        return filter;
    }

    /**
     * 用户信息验证管理器，可按需求添加多个按顺序执行
     */
    @Bean
    ReactiveAuthenticationManager reactiveAuthenticationManager() {
        LinkedList<ReactiveAuthenticationManager> managers = new LinkedList<>();
        managers.add(myAuthenticationManager);
        return new DelegatingReactiveAuthenticationManager(managers);
    }
}