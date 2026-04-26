package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.entity.User;
import com.wenzi.entity.UserFollow;
import com.wenzi.mapper.UserFollowMapper;
import com.wenzi.mapper.UserMapper; // 引入 UserMapper
import com.wenzi.service.IUserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements IUserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    // 👇 注入 UserMapper 用于更新总数
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 增加回滚配置
    public boolean toggleFollow(Long followerId, Long followeeId, boolean isFollowed) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("不能关注自己");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId);
        wrapper.eq(UserFollow::getFolloweeId, followeeId);

        if (isFollowed) { // 执行关注操作
            if (userFollowMapper.selectCount(wrapper) == 0) {
                UserFollow userFollow = new UserFollow();
                userFollow.setFollowerId(followerId);
                userFollow.setFolloweeId(followeeId);
                userFollow.setCreateTime(LocalDateTime.now());
                userFollowMapper.insert(userFollow);

                // ⚠️ 完成 TODO: 更新用户关注数和粉丝数 (+1)
                updateUserCounts(followerId, followeeId, 1);
                return true;
            }
        } else { // 执行取消关注操作
            if (userFollowMapper.selectCount(wrapper) > 0) {
                userFollowMapper.delete(wrapper);

                // ⚠️ 完成 TODO: 更新用户关注数和粉丝数 (-1)
                updateUserCounts(followerId, followeeId, -1);
                return true;
            }
        }
        return false;
    }

    /**
     * 辅助方法：动态更新双方的粉丝和关注数量
     */
    private void updateUserCounts(Long followerId, Long followeeId, int delta) {
        // 1. 关注者的“关注数”变化
        User follower = userMapper.selectById(followerId);
        if (follower != null) {
            int currentFollowing = follower.getFollowingCount() == null ? 0 : follower.getFollowingCount();
            follower.setFollowingCount(Math.max(0, currentFollowing + delta));
            userMapper.updateById(follower);
        }

        // 2. 被关注者的“粉丝数”变化
        User followee = userMapper.selectById(followeeId);
        if (followee != null) {
            int currentFollowers = followee.getFollowerCount() == null ? 0 : followee.getFollowerCount();
            followee.setFollowerCount(Math.max(0, currentFollowers + delta));
            userMapper.updateById(followee);
        }
    }

    @Override
    public boolean isFollowed(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            return false;
        }
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId);
        wrapper.eq(UserFollow::getFolloweeId, followeeId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Long getFollowCount(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId);
        return userFollowMapper.selectCount(wrapper);
    }

    @Override
    public Long getFanCount(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFolloweeId, userId);
        return userFollowMapper.selectCount(wrapper);
    }
}
