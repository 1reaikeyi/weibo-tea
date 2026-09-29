package com.branch.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.branch.domain.entity.RolePermission;
import org.springframework.stereotype.Repository;

/**
 * 角色权限关联Mapper接口 - 提供角色权限关联数据访问操作
 */
@Repository
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
