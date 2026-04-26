package com.wenzi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wenzi.entity.UserFollow;

public interface IUserFollowService extends IService<UserFollow> {

    /**
     * 切换关注状态（关注/取消关注）
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @param isFollowed 当前是否已关注
     * @return 操作是否成功
     */
    boolean toggleFollow(Long followerId, Long followeeId, boolean isFollowed);

    /**
     * 查询关注状态
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowed(Long followerId, Long followeeId);

    /**
     * 获取用户的关注数
     * @param userId 用户ID
     * @return 关注数
     */
    Long getFollowCount(Long userId);

    /**
     * 获取用户的粉丝数
     * @param userId 用户ID
     * @return 粉丝数
     */
    Long getFanCount(Long userId);
}
