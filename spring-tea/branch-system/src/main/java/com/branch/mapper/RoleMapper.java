package com.branch.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.branch.domain.entity.Role;
import org.springframework.stereotype.Repository;

/**
 * 角色Mapper接口 - 提供角色数据访问操作
 */
@Repository
public interface RoleMapper extends BaseMapper<Role> {
}
