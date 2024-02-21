package com.constantineqaq.gateway.entity.dto;

import lombok.Data;

@Data
public class LoginData {
    /** 登陆方式 */
    private String loginType;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 普通登陆验证码 */
    private String commonLoginVerifyCode;
    /** 手机号 */
    private String phone;
    /** 手机验证码 */
    private String phoneVerifyCode;
    /** 第三方平台类型 */
    private String thirdPlatformType;
    /** 第三方平台id */
    private String thirdPlatformId;
}