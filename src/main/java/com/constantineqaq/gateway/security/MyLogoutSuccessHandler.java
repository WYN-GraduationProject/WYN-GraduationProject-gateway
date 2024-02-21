package com.constantineqaq.gateway.security;

import com.alibaba.fastjson.JSONObject;
import com.constantineqaq.gateway.entity.constant.AuthConstant;
import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.service.AccountService;
import entity.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import utils.RedisUtil;

@Component
@Slf4j
public class MyLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AccountService accountService;

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        ServerHttpResponse response = exchange.getExchange().getResponse();
        // 定义返回值
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSONObject.toJSONString(RestBean.success()).getBytes());
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // 转换为自定义security令牌
        MyAuthenticationToken myAuthenticationToken = (MyAuthenticationToken) authentication;
        MyUserDetails userDetails = (MyUserDetails) myAuthenticationToken.getPrincipal();

        // 找到真实用户
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        // 删除 token
        redisUtil.hdel(AuthConstant.TOKEN_REDIS_KEY, account.getId());
//        redisTemplate.opsForHash().delete(AuthConstant.TOKEN_REDIS_KEY, userDetails.getId());
        log.info("登出成功：{}", myAuthenticationToken.toString());

        return response.writeWith(Mono.just(dataBuffer));
    }
}
