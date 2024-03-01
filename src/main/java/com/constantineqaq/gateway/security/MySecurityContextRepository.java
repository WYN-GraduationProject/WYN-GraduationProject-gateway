package com.constantineqaq.gateway.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.constantineqaq.gateway.entity.constant.AuthConstant;
import com.constantineqaq.gateway.entity.constant.AuthRole;
import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.service.AccountService;
import com.constantineqaq.gateway.utils.UserJwtUtil;
import constant.Const;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import utils.JwtUtil;
import utils.RedisUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 从 token 中提取用户凭证
 */
@Component
@Slf4j
public class MySecurityContextRepository implements ServerSecurityContextRepository {

    @Resource
    private MyAuthenticationManager myAuthenticationManager;

    @Resource
    private AccountService accountService;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    JwtUtil jwtUtil;

    @Resource
    UserJwtUtil utils;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        // 获取 token
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
        String authorization = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authorization)) {
            log.info("请求头中没有找到 token");
            return Mono.empty();
        }

        // 解析 token
        DecodedJWT jwt = jwtUtil.resolveJwt(authorization);
        if (jwt == null) {
            return Mono.empty();
        }
        UserDetails user = utils.toUser(jwt);
        log.info("解析出的用户信息：{}", user);
        Integer userId = utils.toId(jwt);
        Account account = accountService.findAccountByNameOrEmail(user.getUsername());
        if (redisUtil.hget(AuthConstant.TOKEN_REDIS_KEY, account.getId().toString()) == null) {
            return Mono.empty();
        }
        // 构建用户令牌
        MyUserDetails myUserDetails = new MyUserDetails();
        myUserDetails.setId(Long.valueOf(userId));
        myUserDetails.setUsername(user.getUsername());

//        Claims claims = JwtUtil.getClaims(token);
//        String username = claims.getSubject();
//        String userId = claims.get(AuthConstant.USER_ID_KEY, String.class);
//        String rolesStr = claims.get(AuthConstant.ROLES_STRING_KEY, String.class);
//        List<AuthRole> list = Arrays.stream(rolesStr.split(","))
//                .map(roleName -> new AuthRole().setName(roleName))
//                .collect(Collectors.toList());


        // 确认 token 有效性
        if (jwtUtil.isInvalidToken(jwt.getId())) {
            // TODO
        }

        // 构建 Security 的认证凭据
        MyAuthenticationToken authToken = new MyAuthenticationToken(myUserDetails, null, user.getAuthorities());
        log.info("从 token 中解析出的用户信息：{}", myUserDetails);

        // 从请求头中删除token，并添加解析出来的信息
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(AuthConstant.USER_ID_KEY, String.valueOf(userId))
                .header(AuthConstant.USERNAME_KEY, user.getUsername())
                .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                .build();
        exchange.mutate().request(request).build();

        return myAuthenticationManager
                .authenticate(authToken)
                .map(SecurityContextImpl::new);
    }
}
