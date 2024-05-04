package com.constantineqaq.gateway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.mapper.AccountMapper;
import com.constantineqaq.gateway.security.MyUserDetails;
import com.constantineqaq.gateway.service.AccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 账户信息处理相关服务
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    /**
     * 从数据库中通过用户名或邮箱查找用户详细信息
     *
     * @param username 用户名
     * @return 用户详细信息
     * @throws UsernameNotFoundException 如果用户未找到则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        return MyUserDetails.withUsername(username).password(account.getPassword())
                .roleList(account.toRoleList())
                .id(Long.valueOf(account.getId()))
                .email(account.getEmail())
                .build();
    }


    /**
     * 通过用户名或邮件地址查找用户
     *
     * @param text 用户名或邮件
     * @return 账户实体
     */
    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username", text)
                .or()
                .eq("email", text).one();
    }
}
