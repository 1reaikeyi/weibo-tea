package com.branch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.branch.domain.entity.Permission;
import com.branch.mapper.PermissionMapper;
import com.branch.service.PermissionService;

import org.springframework.stereotype.Service;

/**
 * 权限服务实现类 - 实现权限相关业务逻辑
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
