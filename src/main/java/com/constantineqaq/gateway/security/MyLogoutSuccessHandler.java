package com.constantineqaq.gateway.security;

import com.constantineqaq.gateway.entity.constant.AuthConstant;
import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.service.AccountService;
import com.constantineqaq.gateway.utils.UserJwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import utils.RedisUtil;

import java.util.Objects;

@Component
@Slf4j
public class MyLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    @Resource
    private UserJwtUtil userJwtUtil;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AccountService accountService;

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        RestBean<String> result;
        ServerHttpResponse response = exchange.getExchange().getResponse();
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (Objects.equals(authentication.getPrincipal().toString(), "anonymous")) {
            result = RestBean.failure(401, "未认证");
        } else {
            // 转换为自定义security令牌
            MyAuthenticationToken myAuthenticationToken = (MyAuthenticationToken) authentication;
            MyUserDetails myUserDetails = (MyUserDetails) myAuthenticationToken.getPrincipal();
            // 删除 token
            redisUtil.hdel(AuthConstant.TOKEN_REDIS_KEY, myUserDetails.getUsername());

//        redisTemplate.opsForHash().delete(AuthConstant.TOKEN_REDIS_KEY, userDetails.getId());
            log.info("登出成功：{}", myAuthenticationToken.toString());
            result = RestBean.success("登出成功");
        }
        byte[] bytes;
        try {
            bytes = new ObjectMapper().writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
