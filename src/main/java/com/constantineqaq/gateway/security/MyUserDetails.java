package com.constantineqaq.gateway.security;

import com.constantineqaq.gateway.entity.constant.AuthRole;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MyUserDetails implements UserDetails {
    private Long id;
    private String phone;
    private String username;
    private String password;
    /**
     * 状态 (1:正常；0:禁用)
     */
    private Integer accountStatus;
    /**
     * 角色集合
     */
    private List<AuthRole> roleList;

    /**
     * 修改为 Security 可识别 的GrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleList.stream()
                .map(authRole -> new SimpleGrantedAuthority(authRole.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * 账户是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 是否未被锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 密码是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否可用
     */
    @Override
    public boolean isEnabled() {
        return accountStatus.equals(1);
    }

}
