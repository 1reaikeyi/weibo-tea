package com.branch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.branch.domain.entity.RolePermission;
import com.branch.mapper.RolePermissionMapper;
import com.branch.service.RolePermissionService;

import org.springframework.stereotype.Service;

/**
 * 角色权限关联服务实现类 - 实现角色权限关联相关业务逻辑
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
