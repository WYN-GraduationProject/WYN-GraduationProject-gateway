package com.constantineqaq.gateway.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import utils.JwtUtil;

import java.util.List;

@Slf4j
public class GatewayGlobalFilterConfiguration implements GlobalFilter, Ordered {

    @Resource
    private WhiteListConfig whiteListConfig;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 哪个请求进入了网关过滤器
        log.info("{}请求进入了网关过滤器",exchange.getRequest().getURI().getPath());
        //获得请求和响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //对白名单中的地址放行
        List<String> whiteList = whiteListConfig.getWhiteList();
        for(String str : whiteList){
            if(request.getURI().getPath().contains(str)){
                log.info("白名单，放行{}",request.getURI().getPath());
                return chain.filter(exchange);
            }
        }
        //获得请求头中Authorization token信息
        String token = request.getHeaders().getFirst("Authorization");

        try{
            //解析token
            String username = jwtUtil.resolveJwt(token).getClaim("name").asString();
            log.info("{}解析成功，放行{}",username,request.getURI().getPath());
            return chain.filter(exchange);
        } catch (Exception ex){
            log.error("token解析失败",ex);
            //返回验证失败的响应信息
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 解决中文乱码问题
            response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
            // 以Json格式返回前端
            String result = "{\"code\":401,\"message\":\""+ex.getMessage()+"\"}";
            DataBuffer buffer = response.bufferFactory().wrap(result.getBytes());
            return response.writeWith(Mono.just(buffer));
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
