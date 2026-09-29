package com.branch.controller;

import com.branch.domain.dto.LoginDTO;
import com.branch.service.UserService;
import common.annotion.Logging;
import common.constant.ResponseConstant;
import common.result.Result;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @Logging(desc = "发送验证码")
    @PostMapping("/code")
    public Result sendCode(@Email String email) {
        userService.sendCode(email);
        return Result.success(ResponseConstant.LOGIN_CODE);
    }

    @Logging(desc = "邮箱登录")
    @PostMapping("/byEmail")
    public Result loginByEmail(String email, String code){
        String token = userService.loginByEmail(email,code);
        return Result.success(token);
    }
    @Logging(desc = "登录")
    @PostMapping("/user")
    public Result login(LoginDTO loginDTO){
        String token = userService.login(loginDTO);
        return Result.success(token);
    }

}
