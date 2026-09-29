package com.branch.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.branch.domain.bo.LoginUserDetails;
import com.branch.domain.dto.LoginDTO;
import common.constant.JwtConstant;
import common.constant.RoleConstant;
import common.constant.StatusConstant;
import com.branch.domain.dto.RegisterDTO;
import com.branch.domain.entity.User;
import com.branch.domain.entity.UserRole;
import com.branch.mapper.UserMapper;
import com.branch.service.UserRoleService;
import com.branch.service.UserService;

import common.constant.RedisPrefixConstant;
import com.branch.properties.JwtProperties;
import com.branch.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类 - 实现用户相关业务逻辑
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public RegisterDTO registerUser(RegisterDTO registerDTO) {
        User user = BeanUtil.toBean(registerDTO, User.class);
        user.setStatus(StatusConstant.ENABLE);
        this.save(user);
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(RoleConstant.USER);
        userRoleService.save(userRole);
        RegisterDTO dto =BeanUtil.toBean(registerDTO, RegisterDTO.class);
        return dto;
    }

    @Override
    public RegisterDTO registerMerchant(RegisterDTO registerDTO) {
        User user = BeanUtil.toBean(registerDTO, User.class);
        user.setStatus(StatusConstant.ENABLE);
        this.save(user);
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(RoleConstant.MERCHANT);
        userRoleService.save(userRole);
        RegisterDTO dto =BeanUtil.toBean(registerDTO, RegisterDTO.class);
        return dto;
    }

    @Override
    public RegisterDTO registerAdmin(RegisterDTO registerDTO) {
        User user = BeanUtil.toBean(registerDTO, User.class);
        user.setStatus(StatusConstant.ENABLE);
        this.save(user);
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(RoleConstant.ADMIN);
        userRoleService.save(userRole);
        RegisterDTO dto =BeanUtil.toBean(registerDTO, RegisterDTO.class);
        return dto;
    }

    @Override
    public void sendCode(String email) {
        String secret = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(secret.length());
            codeBuilder.append(secret.charAt(index));
        }
        String code = codeBuilder.toString();
        stringRedisTemplate.opsForValue().set( RedisPrefixConstant.LOGIN_CODE + email, code, 10, TimeUnit.MINUTES);
        // XADD 命令：发送消息
        Map<String, String> message = new HashMap<>();
        message.put("code", code);
        message.put("email", email);
        stringRedisTemplate.opsForStream().add(RedisPrefixConstant.LOGIN_CODE_STREAM, message);
    }

    @Override
    public String loginByEmail(String email, String code) {
        return "";
    }

    @Override
    public String login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        LoginUserDetails loginUser = (LoginUserDetails) authentication.getPrincipal();
        User dbUser = loginUser.getUser();
        Set<String> roles = loginUser.getRoles();

        Map<String, Object> claims = new HashMap<>();
        Long userId = dbUser.getId();
        claims.put(JwtConstant.TYPE, roles);
        claims.put(JwtConstant.USER_ID, userId.toString());
        claims.put(JwtConstant.USER_NAME, dbUser.getUsername());
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtlMillis(), claims);

        stringRedisTemplate.opsForValue().set(RedisPrefixConstant.WEIBO_AUTHHEADER + userId, token,
                jwtProperties.getTtlMillis(), TimeUnit.SECONDS);
        User update = new User();
        update.setId(userId);
        update.setLastLoginTime(LocalDateTime.now());
        this.updateById(update);

        log.info("用户 {} 登录成功，userId={}", username, userId);
        return token;
    }
}