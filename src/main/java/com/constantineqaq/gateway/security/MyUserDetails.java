package com.constantineqaq.gateway.security;

import com.constantineqaq.gateway.entity.constant.AuthRole;
import entity.BaseData;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class MyUserDetails implements UserDetails, BaseData {
    private Long id;
    private String email;
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

    public static MyUserDetailsBuilder builder() {
        return new MyUserDetailsBuilder();
    }

    public static MyUserDetailsBuilder withUsername(String username) {
        return builder().username(username);
    }

    public static final class MyUserDetailsBuilder {
        private Long id;
        private String email;
        private String phone;
        private String username;
        private String password;
        private Integer accountStatus;
        private List<AuthRole> roleList;

        private Function<String, String> passwordEncoder = (password) -> {
            return password;
        };

        private MyUserDetailsBuilder() {
        }

        public MyUserDetailsBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MyUserDetailsBuilder email(String email) {
            this.email = email;
            return this;
        }

        public MyUserDetailsBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public MyUserDetailsBuilder username(String username) {
            this.username = username;
            return this;
        }

        public MyUserDetailsBuilder password(String password) {
            this.password = password;
            return this;
        }

        public MyUserDetailsBuilder accountStatus(Integer accountStatus) {
            this.accountStatus = accountStatus;
            return this;
        }

        public MyUserDetailsBuilder roleList(List<AuthRole> roleList) {
            this.roleList = roleList;
            return this;
        }

        public MyUserDetailsBuilder passwordEncoder(Function<String, String> encoder) {
            Assert.notNull(encoder, "encoder cannot be null");
            this.passwordEncoder = encoder;
            return this;
        }

        public MyUserDetails build() {
            String encodedPassword = (String)this.passwordEncoder.apply(this.password);
            return new MyUserDetails(this.id, this.email, this.phone, this.username, encodedPassword, this.accountStatus, this.roleList);
        }
    }



}
