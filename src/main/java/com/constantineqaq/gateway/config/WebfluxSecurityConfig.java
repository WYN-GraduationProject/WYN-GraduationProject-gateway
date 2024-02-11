package com.constantineqaq.gateway.config;

import com.constantineqaq.gateway.entity.DefaultSecurityContextRepository;
import com.constantineqaq.gateway.filter.RequestLogFilter;
import com.constantineqaq.gateway.handler.*;
import com.constantineqaq.gateway.service.AccountService;
import constant.Const;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.LinkedList;


@EnableWebFluxSecurity
@Configuration
public class WebfluxSecurityConfig {

    @Resource
    private RequestLogFilter requestLogFilter;
    @Resource
    private WhiteListConfig whiteListConfig;

    @Resource
    private AccountService accountService;

    @Resource
    private DefaultAuthorizationManager defaultAuthorizationManager;

    @Resource
    private DefaultAuthenticationSuccessHandler defaultAuthenticationSuccessHandler;

    @Resource
    private DefaultAuthenticationFailureHandler defaultAuthenticationFailureHandler;

    @Resource
    private TokenAuthenticationManager tokenAuthenticationManager;

    @Resource
    private DefaultSecurityContextRepository defaultSecurityContextRepository;

    @Resource
    private DefaultAuthenticationEntryPoint defaultAuthenticationEntryPoint;

    @Resource
    private DefaultAccessDeniedHandler defaultAccessDeniedHandler;


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .authenticationManager(reactiveAuthenticationManager())
                .securityContextRepository(defaultSecurityContextRepository)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/**", "/error").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyExchange().hasRole(Const.ROLE_DEFAULT)
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/api/auth/login")
                        .authenticationFailureHandler(defaultAuthenticationFailureHandler)
                        .authenticationSuccessHandler(defaultAuthenticationSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(defaultAccessDeniedHandler)
                        .authenticationEntryPoint(defaultAuthenticationEntryPoint)
                )
                .addFilterBefore(requestLogFilter, SecurityWebFiltersOrder.HTTP_BASIC)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    /**
     * 退出登录处理，将对应的Jwt令牌列入黑名单不再使用
     * @param request 请求
     * @param response 响应
     * @param authentication 验证实体
     * @throws IOException 可能的异常
     */
//    private void onLogoutSuccess(ServerWebExchange request, ServerWebExchange response,
//                                 Authentication authentication) throws IOException {
//        response.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
//        PrintWriter writer = response.getResponse().writeWith(Mono.empty()).block().bufferFactory().wrap(response.getResponse().bufferFactory().allocateBuffer());
//        if (authentication != null) {
//            Account account = (Account) authentication.getPrincipal();
//            List<AuthorizeVO> authorizeVOS = accountService.getAuthorizeVOS(account.getId());
//            for (AuthorizeVO authorizeVO : authorizeVOS) {
//                accountService.removeToken(authorizeVO.getToken());
//            }
//            writer.write(RestBean.success("退出登录成功").asJsonString());
//            return;
//        }
//        writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
//    }

    /**
     * 注册用户信息验证管理器，可按需求添加多个按顺序执行
     */
    @Bean
    ReactiveAuthenticationManager reactiveAuthenticationManager() {
        LinkedList<ReactiveAuthenticationManager> managers = new LinkedList<>();
        managers.add(authentication -> {
            // 其他登陆方式 (比如手机号验证码登陆) 可在此设置不得抛出异常或者 Mono.error
            return Mono.empty();
        });
        // 必须放最后不然会优先使用用户名密码校验但是用户名密码不对时此 AuthenticationManager 会调用 Mono.error 造成后面的 AuthenticationManager 不生效
        managers.add(new UserDetailsRepositoryReactiveAuthenticationManager(accountService));
        managers.add(tokenAuthenticationManager);
        return new DelegatingReactiveAuthenticationManager(managers);
    }
}