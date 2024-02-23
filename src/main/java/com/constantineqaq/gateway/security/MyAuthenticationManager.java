package com.constantineqaq.gateway.security;

import com.constantineqaq.gateway.entity.constant.LoginType;
import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.entity.dto.LoginData;
import com.constantineqaq.gateway.service.AccountService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Primary
@Slf4j
public class MyAuthenticationManager implements ReactiveAuthenticationManager {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AccountService accountService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        // 已经通过验证，直接返回
        if (authentication.isAuthenticated()) {
            return Mono.just(authentication);
        }

        // 转换为自定义security令牌
        MyAuthenticationToken myAuthenticationToken = (MyAuthenticationToken) authentication;
        log.error("{}", myAuthenticationToken);

        // 获取登录参数
        LoginData loginData = myAuthenticationToken.getLoginData();
        if (loginData == null) {
            log.error("未获取到登陆参数");
            throw new AuthenticationServiceException("未获取到登陆参数");
        }
        String loginType = loginData.getLoginType();
        if (StringUtils.isBlank(loginType)) {
            log.error("登陆方式不可为空");
            throw new AuthenticationServiceException("登陆方式不可为空");
        }
        UserDetails userDetails = null;

        // 获取用户实体。此处为登录方式的逻辑实现。
        if (LoginType.USERNAME_CODE.equals(loginType)) {
            userDetails = accountService.loadUserByUsername(loginData.getUsername());

            if (userDetails == null) {
                log.error("用户不存在");
                return Mono.error(new UsernameNotFoundException("用户不存在"));
            }
            if (!passwordEncoder.matches(loginData.getPassword(), userDetails.getPassword())) {
                log.error("密码错误");
                return Mono.error(new BadCredentialsException("密码错误"));
            }
        } else if (LoginType.PHONE_CODE.equals(loginType)) {

//            this.checkPhoneVerifyCode(loginData.getPhone(), loginData.getPhoneVerifyCode());
//            userDetails = userDetailsService.loadUserByPhone(loginData.getPhone());

        } else {
            log.error("不支持的登陆方式");
            throw new AuthenticationServiceException("不支持的登陆方式");
        }

        MyAuthenticationToken authenticationToken = new MyAuthenticationToken(userDetails, myAuthenticationToken);
        log.info("自定义请求体：{}",authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        return Mono.just(authenticationToken);
    }
}
