package framework.security;


import common.constant.StatusConstant;
import framework.bo.UserBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户信息载体（参照若依 LoginUser 设计）
 *
 * 作为 Spring Security 的 UserDetails 实现，同时承载：
 * 1. 用户基本信息（user）
 * 2. 权限标识集合（permissions，如 system:user:list）
 * 3. 角色编码集合（roles，如 admin、user）
 * 4. 登录时间与过期时间（用于 Token 滑动续期判断）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDetails implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户信息 */
    private UserBO userBO;

    /** 权限集合：权限标识 permission_code */
    private Set<String> permissions;

    /** 角色集合：角色编码 role_code */
    private Set<String> roles;

    /** 登录时间（毫秒时间戳），用于 Token 续期判断 */
    private Long loginTime;

    /** 过期时间（毫秒时间戳），用于 Token 续期判断 */
    private Long expireTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 返回数据库中加密后的密码（BCrypt）
     */
    @Override
    public String getPassword() {
        return userBO == null ? null : userBO.getPassword();
    }

    /**
     * 返回登录用户名
     */
    @Override
    public String getUsername() {
        return userBO == null ? null : userBO.getUsername();
    }

    /**
     * 账号是否未过期（true=正常）
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未锁定（true=正常）
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证（密码）是否未过期（true=正常）
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用（true=正常可用）
     */
    @Override
    public boolean isEnabled() {
        return userBO != null && StatusConstant.ENABLE.equals(userBO.getStatus());
    }

}
