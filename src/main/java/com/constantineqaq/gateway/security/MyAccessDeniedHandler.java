package com.constantineqaq.gateway.security;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.RestBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 鉴权错误处理器
 */
@Component
@Slf4j
public class MyAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        log.error("权限不足", denied);
        return Mono
                .defer(() -> Mono.just(exchange.getResponse()))
                .flatMap(response -> {
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    byte[] bytes = new byte[0];
                    try {
                        bytes = new ObjectMapper().writeValueAsBytes(RestBean.failure(403, "权限不足"));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    DataBuffer buffer = response.bufferFactory().wrap(bytes);
                    return response.writeWith(Mono.just(buffer));
                });

    }
}

