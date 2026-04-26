package com.wenzi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenzi.common.Result;
import com.wenzi.dto.CommentCreateDTO;
import com.wenzi.dto.CommentUpdateDTO;
import com.wenzi.dto.CommentQueryDTO;
import com.wenzi.dto.CommentLikeDTO;
import com.wenzi.service.ICommentService;
import com.wenzi.vo.CommentVO;
import com.wenzi.utils.UserContext;
import com.wenzi.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    /**
     * 创建评论 (必须登录)
     */
    @PostMapping
    public Result<Long> createComment(@Valid @RequestBody CommentCreateDTO commentCreateDTO) {
        Long userId = UserContext.getUserId();
        Long commentId = commentService.createComment(commentCreateDTO, userId);
        return Result.success(commentId);
    }

    /**
     * 更新评论 (必须登录)
     */
    @PutMapping
    public Result<Boolean> updateComment(@Valid @RequestBody CommentUpdateDTO commentUpdateDTO) {
        Long userId = UserContext.getUserId();
        boolean success = commentService.updateComment(commentUpdateDTO, userId);
        return success ? Result.success(true) : Result.error("更新失败或无权限");
    }

    /**
     * 删除评论 (逻辑删除, 必须登录)
     */
    @DeleteMapping("/{commentId}")
    public Result<Boolean> deleteComment(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        boolean success = commentService.deleteComment(commentId, userId);
        return success ? Result.success(true) : Result.error("删除失败或无权限");
    }

    /**
     * 获取评论详情 (游客/用户皆可)
     */
    @GetMapping("/{commentId}")
    public Result<CommentVO> getCommentDetail(@PathVariable Long commentId, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long currentUserId = (token != null && JwtUtils.verifyToken(token)) ? JwtUtils.getUserId(token) : null;

        CommentVO commentVO = commentService.getCommentDetail(commentId, currentUserId);
        return commentVO != null ? Result.success(commentVO) : Result.error("评论不存在或已删除");
    }

    /**
     * 分页查询评论列表 (游客/用户皆可)
     */
    @GetMapping("/page")
    public Result<IPage<CommentVO>> pageQueryComments(CommentQueryDTO queryDTO, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long currentUserId = (token != null && JwtUtils.verifyToken(token)) ? JwtUtils.getUserId(token) : null;

        IPage<CommentVO> page = commentService.pageQueryComments(queryDTO, currentUserId);
        return Result.success(page);
    }
    
    /**
     * 切换评论点赞状态 (必须登录)
     */
    @PostMapping("/like/toggle")
    public Result<Boolean> toggleCommentLike(@RequestBody CommentLikeDTO commentLikeDTO) {
        Long userId = UserContext.getUserId();
        boolean success = commentService.toggleCommentLike(commentLikeDTO.getCommentId(), userId, commentLikeDTO.getIsLiked());
        return success ? Result.success(true) : Result.error("操作失败");
    }

    /**
     * 获取当前用户的评论列表
     */
    @GetMapping("/my")
    public Result<IPage<CommentVO>> getUserComments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = (token != null && JwtUtils.verifyToken(token)) ? JwtUtils.getUserId(token) : null;
        if (userId == null) {
            return Result.error("请先登录");
        }

        IPage<CommentVO> comments = commentService.getUserComments(userId, pageNum, pageSize, userId);
        return Result.success(comments);
    }
}