package com.constantineqaq.gateway.security;


import com.alibaba.fastjson.JSONObject;
import entity.RestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
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
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            RestBean resultVO = null;
            // 账号不存在
            if (exception instanceof UsernameNotFoundException) {
                resultVO = RestBean.failure(401, "账号不存在");
                // 用户名或密码错误
            } else if (exception instanceof BadCredentialsException) {
                resultVO = RestBean.failure(401, "用户名或密码错误");
                // 账号已过期
            } else if (exception instanceof AccountExpiredException) {
                resultVO = RestBean.failure(401, "账号已过期");
                // 账号已被锁定
            } else if (exception instanceof LockedException) {
                resultVO = RestBean.failure(401, "账号已被锁定");
                // 用户凭证已失效
            } else if (exception instanceof CredentialsExpiredException) {
                resultVO = RestBean.failure(401, "用户凭证已失效");
                // 账号已被禁用
            } else if (exception instanceof DisabledException) {
                resultVO = RestBean.failure(401, "账号已被禁用");
            }
            DataBuffer dataBuffer = dataBufferFactory.wrap(JSONObject.toJSONString(resultVO).getBytes());
            return response.writeWith(Mono.just(dataBuffer));
        }));
    }
}
