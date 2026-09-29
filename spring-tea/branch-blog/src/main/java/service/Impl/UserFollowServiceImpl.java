package service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import framework.security.SecurityContextParam;
import mapper.UserFollowMapper;
import model.entity.User;
import model.entity.UserFollow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import service.UserFollowService;
import service.UserService;

import java.util.List;
import java.util.Set;

@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    private static final String FOLLOW_PREFIX = "follow:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;

    @Override
    public String follow(Long followUserId, Boolean ifFollow) {
        Long userId = SecurityContextParam.getCurrentUserId();
        if (ifFollow) {
            UserFollow userFollow = UserFollow.builder()
                    .userId(userId)
                    .followUserId(followUserId)
                    .build();
            stringRedisTemplate.opsForSet().add(FOLLOW_PREFIX + userId, followUserId.toString());
            super.save(userFollow);
        }
        if (!ifFollow) {
            super.remove(new LambdaQueryWrapper<UserFollow>()
                    .eq(UserFollow::getFollowUserId, userId)
                    .eq(UserFollow::getFollowUserId, followUserId));
            stringRedisTemplate.opsForSet().remove(FOLLOW_PREFIX + userId, followUserId.toString());
        }
        return followUserId + "::" + (BooleanUtil.isTrue(ifFollow) ? "关注" : "取关");
    }

    @Override
    public String followStatus(Long followUserId) {
        Long userId = SecurityContextParam.getCurrentUserId();
        UserFollow userFollow = super.getOne(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowUserId, followUserId));
        return userFollow != null ? "关注" : "取关";
    }

    @Override
    public List<?> commonFollow(Long followId) {
        Long userId = SecurityContextParam.getCurrentUserId();
        Set<String> commonSet = stringRedisTemplate.opsForSet().intersect(FOLLOW_PREFIX + followId, FOLLOW_PREFIX + userId);
        if (CollectionUtil.isEmpty(commonSet)) {
            return null;
        }
        List<Long> ids = commonSet.stream().map(s -> Long.parseLong(s)).toList();
        List<User> userList = userService.listByIds(ids);
        return userList;
    }
}
