package com.branch.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.branch.domain.entity.User;
import org.springframework.stereotype.Repository;


/**
 * 用户Mapper接口 - 提供用户数据访问操作
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

}