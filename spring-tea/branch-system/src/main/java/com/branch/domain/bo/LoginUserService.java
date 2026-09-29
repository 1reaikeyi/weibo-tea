package com.branch.domain.bo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.branch.domain.entity.Permission;
import com.branch.domain.entity.Role;
import com.branch.domain.entity.RolePermission;
import com.branch.domain.entity.User;
import com.branch.domain.entity.UserRole;
import com.branch.mapper.PermissionMapper;
import com.branch.mapper.RoleMapper;
import com.branch.mapper.RolePermissionMapper;
import com.branch.mapper.UserMapper;
import com.branch.mapper.UserRoleMapper;
import common.constant.ErrorConstant;
import common.constant.StatusConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户认证服务（参照若依 UserDetailsServiceImpl 设计）
 *
 * 职责：
 * 1. 根据用户名加载用户信息
 * 2. 校验用户状态（停用账号拒绝登录）
 * 3. 加载用户角色编码集合（roles）
 * 4. 加载用户权限标识集合（permissions）
 *    - 超级管理员（userId == 1）授予通配权限 *:*:*，拥有全部权限
 *    - 普通用户通过 user_role → role_permission → permission 链路加载
 */
@Slf4j
@Service
public class LoginUserService implements UserDetailsService {

    /** 表示拥有全部权限 */
    private static final String ALL_PERMISSION = "*:*:*";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户
        User dbUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (dbUser == null) {
            log.info("登录用户：{} 不存在", username);
            throw new UsernameNotFoundException(ErrorConstant.USER_NOT_EXIST);
        }

        // 2. 校验用户状态
        if (!StatusConstant.ENABLE.equals(dbUser.getStatus())) {
            log.info("登录用户：{} 已被停用", username);
            throw new UsernameNotFoundException(ErrorConstant.USER_DISABLED);
        }

        // 3. 加载用户角色编码集合
        Set<String> roles = loadRoles(dbUser.getId());

        // 4. 加载用户权限标识集合
        Set<String> permissions = loadPermissions(dbUser.getId(), roles);

        // 5. 封装 LoginUserDetails 返回
        LoginUserDetails loginUser = new LoginUserDetails();
        loginUser.setUser(dbUser);
        loginUser.setRoles(roles);
        loginUser.setPermissions(permissions);
        return loginUser;
    }

    /**
     * 加载用户的角色编码集合
     * 链路：user_role → role，提取 role_code
     */
    private Set<String> loadRoles(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());
    }

    /**
     * 加载用户的权限标识集合
     * 链路：超级管理员直接授予 *:*:*；普通用户走 role_permission → permission，提取 permission_code
     */
    private Set<String> loadPermissions(Long userId, Set<String> roles) {
        // 超管：userId == 1 直接拥有全部权限（参照若依约定，与 LoginUserDetails.isAdmin 保持一致）
        if (userId != null && userId == 1L) {
            Set<String> all = new HashSet<>();
            all.add(ALL_PERMISSION);
            return all;
        }

        // 普通用户：通过角色关联查询权限
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds));
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());
    }
}
