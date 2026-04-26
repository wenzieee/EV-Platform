package com.wenzi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenzi.common.Result; // 修复包路径
import com.wenzi.dto.CollectionToggleDTO;
import com.wenzi.service.IPostCollectionService;
import com.wenzi.utils.UserContext; // 引入统一上下文
import com.wenzi.vo.PostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/post/collection")
public class PostCollectionController {

    @Autowired
    private IPostCollectionService postCollectionService;

    /**
     * 切换帖子收藏状态 (收藏/取消收藏)
     */
    @PostMapping("/toggle")
    public Result<Boolean> togglePostCollection(@Valid @RequestBody CollectionToggleDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(postCollectionService.togglePostCollection(dto.getPostId(), userId, dto.getIsCollected()));
    }

    /**
     * 查询当前用户是否收藏了某个帖子
     */
    @GetMapping("/status/{postId}")
    public Result<Boolean> isPostCollectedByUser(@PathVariable Long postId) {
        Long userId = UserContext.getUserId();
        return Result.success(postCollectionService.isPostCollectedByUser(postId, userId));
    }

    /**
     * 分页获取用户收藏的帖子列表
     */
    @GetMapping("/my")
    public Result<IPage<PostVO>> getUserCollectedPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return Result.success(postCollectionService.getUserCollectedPosts(userId, pageNum, pageSize, userId));
    }
}