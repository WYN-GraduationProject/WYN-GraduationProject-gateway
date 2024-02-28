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
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


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
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/goods/test1").authenticated()
                        .anyExchange().access(myAuthorizationManager)
                )
                .securityContextRepository(mySecurityContextRepository)
                .exceptionHandling(conf -> conf
                        .accessDeniedHandler(myAccessDeniedHandler)
                        .authenticationEntryPoint(myAuthenticationEntryPoint)
                )
                .addFilterAt(authenticationWebFilter(),SecurityWebFiltersOrder.AUTHENTICATION)
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(myLogoutSuccessHandler))
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
//                .authorizeExchange(exchange -> {
//                    List<String> urlList = whiteListConfig.getWhiteList();
//                    String[] pattern = urlList.toArray(new String[0]);
//                    log.error("securityWebFilterChain ignoreUrls:" + Arrays.toString(pattern));
//                    // 过滤不需要拦截的url
//                    exchange.pathMatchers("/login").permitAll()
//                            // 拦截认证
//                            .pathMatchers(HttpMethod.OPTIONS).permitAll()
//                            .anyExchange().access(myAuthorizationManager);
//                })
                .build();
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