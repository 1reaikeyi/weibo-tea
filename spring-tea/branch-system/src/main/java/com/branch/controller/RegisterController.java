package com.branch.controller;

import com.branch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import com.branch.domain.dto.RegisterDTO;
import common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/user")
    public Result register(@RequestBody RegisterDTO registerDTO){
        RegisterDTO dto = userService.registerUser(registerDTO);
        return Result.success(dto);
    }
    /**
     * 商家注册
     */
    @PostMapping("/merchant")
    public Result registerMerchant(@RequestBody RegisterDTO registerDTO){
        RegisterDTO dto = userService.registerMerchant(registerDTO);
        return Result.success(dto);
    }
    /**
     * admin注册
     */
    @PostMapping("/admin")
    public Result registerAdmin(@RequestBody RegisterDTO registerDTO){
        RegisterDTO dto = userService.registerAdmin(registerDTO);
        return Result.success(dto);
    }
}
