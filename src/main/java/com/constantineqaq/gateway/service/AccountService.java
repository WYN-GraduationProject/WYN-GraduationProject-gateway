package com.constantineqaq.gateway.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.constantineqaq.gateway.entity.dto.Account;
import com.constantineqaq.gateway.entity.vo.request.ConfirmResetVO;
import com.constantineqaq.gateway.entity.vo.request.EmailRegisterVO;
import com.constantineqaq.gateway.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);
    String registerEmailVerifyCode(String type, String email, String address);
    String registerEmailAccount(EmailRegisterVO info);
    String resetEmailAccountPassword(EmailResetVO info);
    String resetConfirm(ConfirmResetVO info);
}
