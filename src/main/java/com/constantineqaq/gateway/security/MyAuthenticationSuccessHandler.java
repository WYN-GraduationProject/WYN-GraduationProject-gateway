package com.constantineqaq.gateway.security;


import com.constantineqaq.gateway.entity.constant.AuthConstant;
import com.constantineqaq.gateway.entity.vo.response.AuthorizeVO;
import com.constantineqaq.gateway.service.AccountService;
import com.constantineqaq.gateway.utils.UserJwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import utils.RedisUtil;

@Component
@Slf4j
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
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    // 生成JWT token
                    MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
                    log.info("返回的用户数据: {}", myUserDetails);
                    String token = userJwtUtil.createJwt(myUserDetails, myUserDetails.getUsername(), myUserDetails.getId());

                    // 组装返回参数
                    AuthorizeVO result = myUserDetails.asViewObject(AuthorizeVO.class, o ->
                            o.setToken(token));
                    log.info("{}", result);
                    // 存到redis
                    // TODO 增加缓存过期时间
                    redisUtil.hset(AuthConstant.TOKEN_REDIS_KEY, myUserDetails.getId().toString(), token);
                    // 返回数据
                    log.info("用户{}登录成功", myUserDetails.getUsername());
                    RestBean<AuthorizeVO> restBean = RestBean.success(result);
                    byte[] bytes;
                    try {
                        bytes = new ObjectMapper().writeValueAsBytes(restBean);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                    DataBuffer buffer = response.bufferFactory().wrap(bytes);
                    return response.writeWith(Mono.just(buffer));
                }));
    }
}
