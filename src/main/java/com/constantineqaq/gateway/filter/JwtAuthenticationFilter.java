package com.constantineqaq.gateway.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.constantineqaq.gateway.utils.UserJwtUtil;
import constant.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import utils.JwtUtil;


@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    @Resource
    JwtUtil jwtUtil;

    @Resource
    UserJwtUtil utils;

/**
 * 此方法用于过滤每个HTTP请求并执行JWT认证。
 * 它从Authorization头部提取JWT令牌，解码它，并在SecurityContext中设置认证。
 * 它还将用户ID作为请求的一个属性设置。
 *
 * @param exchange       包含客户端对servlet的请求的ServerWebExchange对象。
 * @param chain          提供对过滤资源请求的调用链视图的WebFilterChain对象。
 * @return               一个表示过滤链的Mono对象。
 */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("load security context");
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        DecodedJWT jwt = jwtUtil.resolveJwt(authorization);
        if(jwt != null) {
            UserDetails user = utils.toUser(jwt);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            exchange.getAttributes().put(Const.ATTR_USER_ID, utils.toId(jwt));
        }
        return chain.filter(exchange);
    }
}
