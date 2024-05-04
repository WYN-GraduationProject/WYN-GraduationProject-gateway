package com.constantineqaq.gateway.filter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import utils.SnowflakeIdGeneratorUtil;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class RequestLogFilter implements WebFilter {

    @Resource
    SnowflakeIdGeneratorUtil generator;

    private final Set<String> ignores = Set.of("/swagger-ui", "/v3/api-docs");

    /**
     * 判定当前请求url是否不需要日志打印
     *
     * @param url 路径
     * @return 是否忽略
     */
    private boolean isIgnoreUrl(String url) {
        for (String ignore : ignores) {
            if (url.startsWith(ignore)) return true;
        }
        return false;
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    private ExchangeFilterFunction logResponse(ServerHttpResponse response, long startTime) {
        long time = System.currentTimeMillis() - startTime;
        int status = Objects.requireNonNull(response.getStatusCode()).value();
        log.info("请求处理耗时: {}ms | 响应结果: {}", time, status);
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response: {}", clientResponse.headers().asHttpHeaders().get("property-header"));
            return Mono.just(clientResponse);
        });
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if (this.isIgnoreUrl(request.getPath().value())) {
            return chain.filter(exchange);
        } else {
            long startTime = System.currentTimeMillis();
            this.logRequest();
            return chain.filter(exchange).doOnSuccess(aVoid -> this.logResponse(response, startTime));
        }
    }
}