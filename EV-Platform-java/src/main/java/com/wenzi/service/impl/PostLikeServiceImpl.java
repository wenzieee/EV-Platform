package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.entity.Post;
import com.wenzi.entity.PostLike;
import com.wenzi.mapper.PostLikeMapper;
import com.wenzi.service.IPostLikeService;
import com.wenzi.service.IPostService;
import com.wenzi.vo.PostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements IPostLikeService {

    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private IPostService postService; // 注入帖子服务，用于更新帖子点赞数

    @Override
    @Transactional
    public boolean togglePostLike(Long postId, Long userId, Boolean isLiked) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId);
        wrapper.eq(PostLike::getUserId, userId);

        if (isLiked) { // 执行点赞操作
            if (!isPostLikedByUser(postId, userId)) { // 如果未点赞，则添加点赞记录
                PostLike postLike = new PostLike();
                postLike.setPostId(postId);
                postLike.setUserId(userId);
                postLike.setCreateTime(LocalDateTime.now());
                postLikeMapper.insert(postLike);
                postService.updateLikeCount(postId, true); // 增加帖子点赞数
                return true;
            }
        } else { // 执行取消点赞操作
            if (isPostLikedByUser(postId, userId)) { // 如果已点赞，则删除点赞记录
                postLikeMapper.delete(wrapper);
                postService.updateLikeCount(postId, false); // 减少帖子点赞数
                return true;
            }
        }
        return false; // 状态未改变
    }

    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId);
        wrapper.eq(PostLike::getUserId, userId);
        return postLikeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public IPage<PostVO> getUserLikedPosts(Long userId, Integer pageNum, Integer pageSize, Long currentUserId) {
        // 1. 创建分页对象
        Page<PostLike> page = new Page<>(pageNum, pageSize);
        
        // 2. 查询用户点赞的帖子记录
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId);
        wrapper.orderByDesc(PostLike::getCreateTime);
        
        Page<PostLike> likePage = postLikeMapper.selectPage(page, wrapper);
        
        // 3. 转换为帖子VO列表
        return likePage.convert(like -> {
            // 根据帖子ID获取帖子详情
            return postService.getPostDetail(like.getPostId(), currentUserId);
        });
    }
}
