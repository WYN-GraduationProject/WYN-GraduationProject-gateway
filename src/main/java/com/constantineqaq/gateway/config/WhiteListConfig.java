package com.constantineqaq.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "user")
public class WhiteListConfig {

    //放行白名单
    private List<String> whiteList;
}

