package com.constantineqaq.gateway.security;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.constantineqaq.gateway.entity.constant.AuthConstant;
import entity.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import utils.RedisUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 授权逻辑处理中心
 */
@Component
@Slf4j
public class MyAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext authorizationContext) {

        ServerWebExchange exchange = authorizationContext.getExchange();
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        log.info("进入权限验证，当前路径：{}", path);

        // 假设这是你的角色列表
        List<String> roles = Arrays.asList("admin", "user", "guest");
        // 将角色列表转换为JSON数组格式的字符串
        String rolesJson = JSON.toJSONString(roles);
        // 存储到Redis
        redisUtil.hset(AuthConstant.ROLES_REDIS_KEY, path, rolesJson);
        // 从redis中获取当前路径可访问的角色列表
        Object obj = redisUtil.hget(AuthConstant.ROLES_REDIS_KEY, path);
        if (obj == null) {
            log.info("当前路径未配置角色限制，允许访问");
            // 需要继续Authentication
            return Mono.just(new AuthorizationDecision(true));
        }
        List<String> needAuthorityList = JSONArray.parseArray(obj.toString(), String.class);
//        needAuthorityList = needAuthorityList.stream().map(role -> AuthConstant.ROLE_PRE + role).toList();
        log.info("当前路径需要的角色列表：{}", needAuthorityList);

        //认证通过且角色匹配的用户可访问当前路径
        return authentication
                .filter(Authentication::isAuthenticated)
                .flatMapIterable(auth -> {
                    log.info("当前用户的角色列表：{}", auth.getAuthorities());
                    return auth.getAuthorities();
                })
                .map(GrantedAuthority::getAuthority)
                .any(needAuthorityList::contains)
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    @Override
    public Mono<Void> verify(Mono<Authentication> authentication, AuthorizationContext object) {
        return check(authentication, object)
                .filter(AuthorizationDecision::isGranted)
                .switchIfEmpty(Mono.defer(() -> {
                    String body = JSONObject.toJSONString(RestBean.failure(401, "权限不足"));
                    return Mono.error(new AccessDeniedException(body));
                }))
                .flatMap(d -> Mono.empty());
    }
}