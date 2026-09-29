package service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.UserInfoMapper;
import model.entity.UserInfo;
import org.springframework.stereotype.Service;
import service.UserInfoService;

/**
 * 博主信息服务实现类 - 实现博主信息相关业务逻辑
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
}
