package com.constantineqaq.gateway.security;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.RestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MyAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        return Mono.defer(() -> Mono.just(webFilterExchange.getExchange()
                .getResponse()).flatMap(response -> {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            RestBean resultVO = null;
            DataBuffer buffer = null;
            // 账号不存在
            if (exception instanceof UsernameNotFoundException) {
                log.info("账号不存在");
                resultVO = RestBean.failure(401, "账号不存在");
                // 用户名或密码错误
            } else if (exception instanceof BadCredentialsException) {
                log.info("用户名或密码错误");
                resultVO = RestBean.failure(401, "用户名或密码错误");
                // 账号已过期
            } else if (exception instanceof AccountExpiredException) {
                log.info("账号已过期");
                resultVO = RestBean.failure(401, "账号已过期");
                // 账号已被锁定
            } else if (exception instanceof LockedException) {
                log.info("账号已被锁定");
                resultVO = RestBean.failure(401, "账号已被锁定");
                // 用户凭证已失效
            } else if (exception instanceof CredentialsExpiredException) {
                log.info("用户凭证已失效");
                resultVO = RestBean.failure(401, "用户凭证已失效");
                // 账号已被禁用
            } else if (exception instanceof DisabledException) {
                log.info("账号已被禁用");
                resultVO = RestBean.failure(401, "账号已被禁用");
            }else if (exception instanceof AuthenticationServiceException) {
                log.info("登录方式为空");
                resultVO = RestBean.failure(402, exception.getMessage());
            }
            try {
                byte[] bytes = new ObjectMapper().writeValueAsBytes(resultVO);
                buffer = response.bufferFactory().wrap(bytes);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
            return response.writeWith(Mono.just(buffer));
        }));
    }
}
