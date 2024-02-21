package com.constantineqaq.gateway.security;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.constantineqaq.gateway.entity.dto.LoginData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerFormLoginAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

@Slf4j
@Component
public class MyAuthenticationConverter extends ServerFormLoginAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {

        HttpMethod method = exchange.getRequest().getMethod();
        MediaType contentType = exchange.getRequest().getHeaders().getContentType();

        return exchange
                .getRequest()
                .getBody()
                .next()
                .flatMap(buffer -> {
                    // 读取请求体
                    LoginData loginData = null;
                    try {
                        loginData = JSONObject.parseObject(buffer.asInputStream(),LoginData.class, Feature.OrderedField);
                    } catch (IOException e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                    log.info(loginData.toString());

                    // 封装 security 的自定义令牌
                    String username = loginData.getUsername();
                    String password = loginData.getPassword();
                    username = username == null ? "" : username;
                    username = username.trim();
                    password = password == null ? "" : password;

                    MyAuthenticationToken myAuthToken = new MyAuthenticationToken(username, password);
                    myAuthToken.setLoginData(loginData);
                    return Mono.just(myAuthToken);
                });
    }
}
