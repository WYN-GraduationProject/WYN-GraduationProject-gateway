package com.constantineqaq.gateway.handler;

import com.alibaba.fastjson2.JSONObject;
import com.constantineqaq.gateway.entity.SecurityUserDetails;
import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.entity.vo.response.AuthorizeVO;
import com.constantineqaq.gateway.service.AccountService;
import com.constantineqaq.gateway.utils.UserJwtUtil;
import entity.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DefaultAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Resource
    private UserJwtUtil userJwtUtil;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private AccountService service;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return Mono.defer(() -> Mono.just(webFilterExchange.getExchange().getResponse()).flatMap(response -> {
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer dataBuffer = null;
            // 生成JWT token
            User user = (User) authentication.getPrincipal();
            Account account = service.findAccountByNameOrEmail(user.getUsername());
            String token = userJwtUtil.createJwt(user,account.getUsername(), account.getId());
            log.info("用户{}登陆成功，生成token:{}", account.getUsername(), token);
            if(token == null) {
                dataBuffer = dataBufferFactory.wrap(RestBean.forbidden("登陆验证频繁，请稍后再试").asJsonString().getBytes());
            } else {
                AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, o -> o.setToken(token));
                vo.setExpire(jwtUtil.expireTime());
                dataBuffer = dataBufferFactory.wrap(RestBean.success(vo).asJsonString().getBytes());
            }
            return response.writeWith(Mono.just(dataBuffer));
        }));
    }
}
