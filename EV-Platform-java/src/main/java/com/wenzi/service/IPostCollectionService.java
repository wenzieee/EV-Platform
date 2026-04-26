package com.wenzi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wenzi.entity.PostCollection;
import com.wenzi.vo.PostVO;

public interface IPostCollectionService extends IService<PostCollection> {

    /**
     * 切换帖子收藏状态 (收藏/取消收藏)
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param isCollected true为收藏，false为取消收藏
     * @return 是否成功操作
     */
    boolean togglePostCollection(Long postId, Long userId, Boolean isCollected);

    /**
     * 查询用户是否已收藏某帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return true为已收藏，false为未收藏
     */
    boolean isPostCollectedByUser(Long postId, Long userId);

    /**
     * 分页获取用户收藏的帖子列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param currentUserId 当前登录用户ID (用于判断是否点赞等)
     * @return 收藏帖子列表
     */
    IPage<PostVO> getUserCollectedPosts(Long userId, Integer pageNum, Integer pageSize, Long currentUserId);
}
