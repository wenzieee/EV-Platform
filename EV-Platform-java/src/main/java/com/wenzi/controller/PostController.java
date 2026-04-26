package com.wenzi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenzi.common.Result;
import com.wenzi.dto.PostCreateDTO;
import com.wenzi.dto.PostUpdateDTO;
import com.wenzi.dto.PostQueryDTO;
import com.wenzi.service.IPostService;
import com.wenzi.vo.PostVO;
import com.wenzi.utils.UserContext;
import com.wenzi.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private IPostService postService;

    /**
     * 创建帖子 (必须登录，已被拦截器保护)
     */
    @PostMapping
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO postCreateDTO) {
        Long userId = UserContext.getUserId();
        Long postId = postService.createPost(postCreateDTO, userId);
        return Result.success(postId);
    }

    /**
     * 更新帖子 (必须登录)
     */
    @PutMapping
    public Result<Boolean> updatePost(@Valid @RequestBody PostUpdateDTO postUpdateDTO) {
        Long userId = UserContext.getUserId();
        boolean success = postService.updatePost(postUpdateDTO, userId);
        return success ? Result.success(true) : Result.error("更新失败或无权限");
    }

    /**
     * 删除帖子 (必须登录)
     */
    @DeleteMapping("/{postId}")
    public Result<Boolean> deletePost(@PathVariable Long postId) {
        Long userId = UserContext.getUserId();
        boolean success = postService.deletePost(postId, userId);
        return success ? Result.success(true) : Result.error("删除失败或无权限");
    }

    // =========================================================
    // 帖子查询接口：物理隔离社区大厅与个人中心
    // =========================================================

    /**
     * 1. 获取帖子详情 (游客/用户皆可)
     */
    @GetMapping("/{postId}")
    public Result<PostVO> getPostDetail(@PathVariable Long postId, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long currentUserId = (token != null && JwtUtils.verifyToken(token)) ? JwtUtils.getUserId(token) : null;

        PostVO postVO = postService.getPostDetail(postId, currentUserId);
        return postVO != null ? Result.success(postVO) : Result.error("帖子不存在或已删除");
    }

    /**
     * 2. 社区大厅：分页查询全站帖子 (游客/用户皆可)
     * 🚀 核心改动：不再强制过滤 userId
     */
    @GetMapping("/page")
    public Result<IPage<PostVO>> pageQueryPosts(PostQueryDTO queryDTO, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long currentUserId = (token != null && JwtUtils.verifyToken(token)) ? JwtUtils.getUserId(token) : null;

        // 强制清空 queryDTO 里的 userId，确保能查到全站所有人发的帖子
        queryDTO.setUserId(null);

        IPage<PostVO> page = postService.pageQueryPosts(queryDTO, currentUserId);
        return Result.success(page);
    }

    /**
     * 3. 个人中心：查询我发布的帖子 (必须登录)
     * 🚀 核心改动：新增专属接口，强制过滤当前用户的帖子
     */
    @GetMapping("/my")
    public Result<IPage<PostVO>> getMyPosts(PostQueryDTO queryDTO, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long currentUserId = (token != null && JwtUtils.verifyToken(token)) ? JwtUtils.getUserId(token) : null;

        if (currentUserId == null) {
            return Result.error("未登录或登录已过期");
        }

        // 强制把查询条件锁死为当前登录用户
        queryDTO.setUserId(currentUserId);

        IPage<PostVO> page = postService.pageQueryPosts(queryDTO, currentUserId);
        return Result.success(page);
    }
}