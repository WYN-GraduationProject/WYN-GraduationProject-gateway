package com.constantineqaq.gateway.security;


import com.alibaba.fastjson.JSONObject;
import com.constantineqaq.gateway.entity.constant.AuthConstant;
import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.entity.vo.response.AuthorizeVO;
import com.constantineqaq.gateway.service.AccountService;
import com.constantineqaq.gateway.utils.UserJwtUtil;
import entity.RestBean;
import jakarta.annotation.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import utils.RedisUtil;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Resource
    private AccountService accountService;
    @Resource
    private UserJwtUtil userJwtUtil;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return Mono.defer(() -> Mono
                .just(webFilterExchange.getExchange().getResponse())
                .flatMap(response -> {
                    DataBufferFactory dataBufferFactory = response.bufferFactory();

                    // 生成JWT token
                    Map<String, Object> map = new HashMap<>();
                    MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
                    Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
                    String token = userJwtUtil.createJwt(userDetails,account.getUsername(),account.getId());

                    // 组装返回参数
                    AuthorizeVO result = account.asViewObject(AuthorizeVO.class, o -> o.setToken(token));

                    // 存到redis
                    redisUtil.hset(AuthConstant.TOKEN_REDIS_KEY,account.getId().toString(),token);
                    DataBuffer dataBuffer =dataBufferFactory.wrap(JSONObject.toJSONString(RestBean.success(result)).getBytes());
                    response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

                    return response.writeWith(Mono.just(dataBuffer));
                }));
    }
}
