package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.entity.Post;
import com.wenzi.entity.PostCollection;
import com.wenzi.mapper.PostCollectionMapper;
import com.wenzi.service.IPostCollectionService;
import com.wenzi.service.IPostService;
import com.wenzi.service.IPostLikeService;
import com.wenzi.mapper.UserMapper;
import com.wenzi.vo.PostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostCollectionServiceImpl extends ServiceImpl<PostCollectionMapper, PostCollection> implements IPostCollectionService {

    @Autowired
    private PostCollectionMapper postCollectionMapper;
    @Autowired
    private IPostService postService;
    @Autowired
    private IPostLikeService postLikeService; // 用于判断收藏帖子的点赞状态
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean togglePostCollection(Long postId, Long userId, Boolean isCollected) {
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostCollection::getPostId, postId);
        wrapper.eq(PostCollection::getUserId, userId);

        if (isCollected) {
            // 收藏：如果不存在则添加
            if (postCollectionMapper.selectCount(wrapper) == 0) {
                PostCollection collection = new PostCollection();
                collection.setPostId(postId);
                collection.setUserId(userId);
                collection.setCreateTime(LocalDateTime.now());
                postCollectionMapper.insert(collection);
                postService.updateCollectCount(postId, true); // 增加帖子收藏数
                return true;
            }
        } else {
            // 取消收藏：如果存在则删除
            if (postCollectionMapper.selectCount(wrapper) > 0) {
                postCollectionMapper.delete(wrapper);
                postService.updateCollectCount(postId, false); // 减少帖子收藏数
                return true;
            }
        }
        return false; // 状态未改变
    }

    @Override
    public boolean isPostCollectedByUser(Long postId, Long userId) {
        if (userId == null) {
            return false; // 未登录用户不可能收藏
        }
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostCollection::getPostId, postId);
        wrapper.eq(PostCollection::getUserId, userId);
        return postCollectionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public IPage<PostVO> getUserCollectedPosts(Long userId, Integer pageNum, Integer pageSize, Long currentUserId) {
        Page<PostCollection> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PostCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostCollection::getUserId, userId);
        wrapper.orderByDesc(PostCollection::getCreateTime);

        // 先查询收藏记录
        IPage<PostCollection> collectedPage = postCollectionMapper.selectPage(page, wrapper);

        // 根据收藏记录中的postId查询帖子详情并转换为PostVO
        IPage<PostVO> postVOPage = collectedPage.convert(postCollection -> {
            Post post = postService.getById(postCollection.getPostId());
            if (post == null || post.getStatus() == 0) {
                return null; // 帖子不存在或已删除
            }
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);

            // ⚠️ 修复 1：传统的 null 判断，而不是用 ifPresent
            com.wenzi.entity.User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                postVO.setUsername(user.getUsername());
                postVO.setNickname(user.getNickname());
                postVO.setAvatar(user.getAvatar());
            }

            // ⚠️ 修复 2：把大写的 Post.getId() 改成小写的 post.getId()
            if (currentUserId != null) {
                postVO.setLiked(postLikeService.isPostLikedByUser(post.getId(), currentUserId));
            } else {
                postVO.setLiked(false);
            }

            postVO.setCollected(true);
            return postVO;
        });

        // 过滤掉null值 (即已删除或不存在的帖子)
        List<PostVO> filteredRecords = postVOPage.getRecords().stream()
                .filter(postVO -> postVO != null)
                .collect(Collectors.toList());
        postVOPage.setRecords(filteredRecords);
        postVOPage.setTotal(filteredRecords.size()); // 重新设置总数

        return postVOPage;
    }
}
