package com.wenzi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenzi.common.Result; // 修复包路径
import com.wenzi.dto.LikeToggleDTO;
import com.wenzi.service.IPostLikeService;
import com.wenzi.utils.UserContext; // 引入统一上下文工具类
import com.wenzi.vo.PostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/post/like")
public class PostLikeController {

    @Autowired
    private IPostLikeService postLikeService;

    /**
     * 切换帖子点赞状态 (点赞/取消点赞)
     */
    @PostMapping("/toggle")
    public Result<Boolean> togglePostLike(@Valid @RequestBody LikeToggleDTO dto) {
        Long userId = UserContext.getUserId(); // 从 ThreadLocal 获取用户 ID

        // 修复漏传参数的问题，补上 dto.getIsLiked()
        boolean success = postLikeService.togglePostLike(dto.getPostId(), userId, dto.getIsLiked());
        return Result.success(success);
    }

    /**
     * 查询当前用户是否点赞了某个帖子
     */
    @GetMapping("/status/{postId}")
    public Result<Boolean> isPostLikedByUser(@PathVariable Long postId) {
        Long userId = UserContext.getUserId();


        // 👇 必须加上这个判断：游客没登录，必定是未点赞状态 (false)
        if (userId == null) {
            return Result.success(false);
        }

        boolean isLiked = postLikeService.isPostLikedByUser(postId, userId);
        return Result.success(isLiked);
    }

    /**
     * 获取当前用户点赞的帖子列表
     */
    @GetMapping("/my")
    public Result<IPage<PostVO>> getUserLikedPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }

        IPage<PostVO> likedPosts = postLikeService.getUserLikedPosts(userId, pageNum, pageSize, userId);
        return Result.success(likedPosts);
    }
}