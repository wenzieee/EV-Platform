package com.wenzi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenzi.entity.Comment;
import com.wenzi.dto.CommentCreateDTO;
import com.wenzi.dto.CommentQueryDTO;
import com.wenzi.dto.CommentUpdateDTO;
import com.wenzi.vo.CommentVO;

/**
 * 评论服务接口
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 创建评论
     */
    Long createComment(CommentCreateDTO commentCreateDTO, Long userId);

    /**
     * 更新评论
     */
    boolean updateComment(CommentUpdateDTO commentUpdateDTO, Long userId);

    /**
     * 删除评论（逻辑删除）
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 获取评论详情
     */
    CommentVO getCommentDetail(Long commentId, Long currentUserId);

    /**
     * 分页查询评论
     */
    IPage<CommentVO> pageQueryComments(CommentQueryDTO queryDTO, Long currentUserId);

    /**
     * 更新评论点赞数
     */
    void updateLikeCount(Long commentId, boolean increment);
    
    /**
     * 切换评论点赞状态
     */
    boolean toggleCommentLike(Long commentId, Long userId, boolean isLiked);

    /**
     * 分页获取用户评论的列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param currentUserId 当前登录用户ID
     * @return 评论列表
     */
    IPage<CommentVO> getUserComments(Long userId, Integer pageNum, Integer pageSize, Long currentUserId);
}