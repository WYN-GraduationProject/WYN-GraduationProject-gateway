package com.constantineqaq.gateway.handler;

import com.alibaba.fastjson2.JSONObject;
import entity.RestBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@Component
public class DefaultAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap(response -> {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            String result = JSONObject.toJSONString(RestBean.failure(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
            DataBuffer buffer = dataBufferFactory.wrap(result.getBytes(
                    Charset.defaultCharset()));
            return response.writeWith(Mono.just(buffer));
        });
    }
}