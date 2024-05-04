package com.constantineqaq.gateway.entity.vo.response;

import com.constantineqaq.gateway.entity.constant.AuthRole;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 登录验证成功的用户信息响应
 */
@Data
public class AuthorizeVO {
    String username;
    int code;
    List<AuthRole> roleList;
    String token;
    Date expire;
}
