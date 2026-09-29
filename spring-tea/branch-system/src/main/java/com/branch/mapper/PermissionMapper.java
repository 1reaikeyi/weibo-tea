package com.branch.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.branch.domain.entity.Permission;
import org.springframework.stereotype.Repository;

/**
 * 权限Mapper接口 - 提供权限数据访问操作
 */
@Repository
public interface PermissionMapper extends BaseMapper<Permission> {
}
