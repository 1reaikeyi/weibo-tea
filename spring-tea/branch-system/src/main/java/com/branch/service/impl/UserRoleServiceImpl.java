package com.branch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.branch.domain.entity.UserRole;
import com.branch.mapper.UserRoleMapper;
import com.branch.service.UserRoleService;

import org.springframework.stereotype.Service;

/**
 * 用户角色关联服务实现类 - 实现用户角色关联相关业务逻辑
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
