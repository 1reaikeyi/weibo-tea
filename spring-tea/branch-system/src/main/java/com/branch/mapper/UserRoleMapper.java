package com.branch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.branch.domain.entity.UserRole;
import org.springframework.stereotype.Repository;


/**
 * 用户角色关联Mapper接口 - 提供用户角色关联数据访问操作
 */
@Repository
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
