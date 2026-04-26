package com.wenzi.controller;

import com.wenzi.common.Result; // 修复包路径
import com.wenzi.service.IUserFollowService;
import com.wenzi.utils.UserContext; // 引入统一上下文
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/follow")
public class UserFollowController {

    @Autowired
    private IUserFollowService userFollowService;

    /**
     * 切换关注状态
     */
    @PostMapping("/toggle/{followeeId}")
    public Result<Boolean> toggleFollow(@PathVariable Long followeeId, @RequestParam boolean isFollowed) {
        Long followerId = UserContext.getUserId();
        // 修复：替换为 401 错误码
        if (followerId == null) {
            return Result.error(401, "用户未登录");
        }
        return Result.success(userFollowService.toggleFollow(followerId, followeeId, isFollowed));
    }

    /**
     * 查询关注状态
     */
    @GetMapping("/status/{followeeId}")
    public Result<Boolean> getFollowStatus(@PathVariable Long followeeId) {
        Long followerId = UserContext.getUserId();
        // 游客（未登录）访问时，直接返回 false，无需报错
        if (followerId == null) {
            return Result.success(false);
        }
        return Result.success(userFollowService.isFollowed(followerId, followeeId));
    }

    /**
     * 获取用户的关注数
     */
    @GetMapping("/count/follow/{userId}")
    public Result<Long> getFollowCount(@PathVariable Long userId) {
        return Result.success(userFollowService.getFollowCount(userId));
    }

    /**
     * 获取用户的粉丝数
     */
    @GetMapping("/count/fan/{userId}")
    public Result<Long> getFanCount(@PathVariable Long userId) {
        return Result.success(userFollowService.getFanCount(userId));
    }
}