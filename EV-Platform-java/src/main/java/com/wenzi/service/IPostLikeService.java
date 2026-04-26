package com.wenzi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wenzi.entity.PostLike;
import com.wenzi.vo.PostVO;

public interface IPostLikeService extends IService<PostLike> {

    /**
     * 切换帖子点赞状态 (点赞/取消点赞)
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param isLiked true为点赞，false为取消点赞
     * @return 是否成功操作
     */
    boolean togglePostLike(Long postId, Long userId, Boolean isLiked);

    /**
     * 查询用户是否已点赞某帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return true为已点赞，false为未点赞
     */
    boolean isPostLikedByUser(Long postId, Long userId);

    /**
     * 分页获取用户点赞的帖子列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param currentUserId 当前登录用户ID (用于判断是否点赞等)
     * @return 点赞帖子列表
     */
    IPage<PostVO> getUserLikedPosts(Long userId, Integer pageNum, Integer pageSize, Long currentUserId);
}
