package com.branch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.branch.domain.dto.LoginDTO;
import com.branch.domain.dto.RegisterDTO;
import com.branch.domain.entity.User;


/**
 * 用户服务接口 - 定义用户相关业务操作
 */
public interface UserService extends IService<User> {

    RegisterDTO registerUser(RegisterDTO registerDTO);

    RegisterDTO registerMerchant(RegisterDTO registerDTO);

    RegisterDTO registerAdmin(RegisterDTO registerDTO);

    void sendCode(String email);

    String loginByEmail(String email, String code);

    String login(LoginDTO loginDTO);
}