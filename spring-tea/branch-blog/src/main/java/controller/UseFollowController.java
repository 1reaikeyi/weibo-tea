package controller;

import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.UserFollowService;

import java.util.List;

@RestController
@RequestMapping("/follow")
public class UseFollowController {
    @Autowired
    private UserFollowService userFollowService;

    /**
     * 关注
     */
    @PostMapping("/{id}/{ifFollow}")
    public Result useFollow(@PathVariable("id") Long followUserId, @PathVariable Boolean ifFollow) {
        return Result.success(userFollowService.follow(followUserId, ifFollow));
    }

    /**
     * 获取结果
     */
    @GetMapping("/{id}")
    public Result getUserFollow(@PathVariable("id") Long followUserId) {
        return Result.success(userFollowService.followStatus(followUserId));
    }

    /**
     * 共同关注
     */
    @GetMapping("/common/{id}")
    public Result getUserFollowCommon(@PathVariable("id") Long followId) {
        List<?> common = userFollowService.commonFollow(followId);
        if (common == null) {
            return Result.success(null);
        }
        return Result.success(common);
    }
}
