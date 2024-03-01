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
        return exchange
                .getRequest()
                .getBody()
                .next()
                .flatMap(buffer -> {
                    // 读取请求体
                    LoginData loginData = null;
                    try {
                        loginData = JSONObject.parseObject(buffer.asInputStream(),LoginData.class, Feature.OrderedField);
                        log.info("请求参数： {}", loginData);
                    } catch (IOException e) {
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                    // 封装 security 的自定义令牌
                    String principal = null;
                    String password = loginData.getPassword();

                    // 尝试以用户名、电子邮件或手机号作为登录标识
                    String username = loginData.getUsername();
                    String email = loginData.getEmail();
                    String phone = loginData.getPhone();

                    // 清理输入
                    username = username != null ? username.trim() : "";
                    email = email != null ? email.trim() : "";
                    phone = phone != null ? phone.trim() : "";
                    password = password != null ? password : "";

                    // 确定主体标识
                    if (!username.isEmpty()) {
                        principal = username;
                    } else if (!email.isEmpty()) {
                        principal = email;
                    } else if (!phone.isEmpty()) {
                        principal = phone;
                    }

                    // 使用确定的主体标识创建认证令牌
                    MyAuthenticationToken myAuthToken = new MyAuthenticationToken(principal, password);

                    myAuthToken.setLoginData(loginData);
                    return Mono.just(myAuthToken);
                });
    }
}
