package com.wenzi.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.dto.PostCreateDTO;
import com.wenzi.dto.PostQueryDTO;
import com.wenzi.dto.PostUpdateDTO;
import com.wenzi.entity.Post;
import com.wenzi.entity.PostCollection;
import com.wenzi.mapper.PostCollectionMapper;
import com.wenzi.mapper.PostMapper;
import com.wenzi.service.IPostService;
import com.wenzi.vo.PostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.wenzi.mapper.PostLikeMapper;
import com.wenzi.entity.PostLike; // 记得导入
import com.wenzi.entity.User;
import com.wenzi.mapper.UserMapper;
import com.wenzi.service.IUserFollowService;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostCollectionMapper postCollectionMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private IUserFollowService userFollowService;

    @Override
    public Long createPost(PostCreateDTO postCreateDTO, Long userId) {
        Post post = new Post();
        BeanUtils.copyProperties(postCreateDTO, post);

        post.setUserId(userId); // 绑定发布者ID
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());

        // 处理媒体URL JSON序列化
        if (postCreateDTO.getMediaUrls() != null && !postCreateDTO.getMediaUrls().isEmpty()) {
            post.setMediaUrls(JSON.toJSONString(postCreateDTO.getMediaUrls()));
        }

        this.save(post);
        return post.getId(); // MyBatis-Plus save后会自动将自增ID回填到实体对象中
    }

    @Override
    public boolean updatePost(PostUpdateDTO postUpdateDTO, Long userId) {
        // 安全校验：确认帖子存在且属于该用户
        Post existPost = this.getById(postUpdateDTO.getId());
        if (existPost == null || !existPost.getUserId().equals(userId)) {
            return false; // 帖子不存在或无权修改（非作者本人）
        }

        Post post = new Post();
        BeanUtils.copyProperties(postUpdateDTO, post);
        post.setUpdateTime(new Date());

        if (postUpdateDTO.getMediaUrls() != null) {
            post.setMediaUrls(JSON.toJSONString(postUpdateDTO.getMediaUrls()));
        }

        return this.updateById(post);
    }

    @Override
    public boolean deletePost(Long postId, Long userId) {
        Post existPost = this.getById(postId);
        if (existPost == null || !existPost.getUserId().equals(userId)) {
            return false; // 无权删除
        }

        // 建议这里做逻辑删除（更新status=0），而不是物理删除，以免丢失社区历史数据
        Post post = new Post();
        post.setId(postId);
        post.setStatus(0);
        return this.updateById(post);

        // 如果想物理删除： return this.removeById(postId);
    }

    @Override
    public PostVO getPostDetail(Long postId, Long currentUserId) {
        Post post = this.getById(postId);
        if (post == null || post.getStatus() == 0) {
            return null;
        }

        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);

        // 反序列化 mediaUrls
        if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            postVO.setMediaUrlList(JSON.parseArray(post.getMediaUrls(), String.class));
        }

        // 查询用户信息并设置 username 和 avatar
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            postVO.setUsername(user.getUsername());
            postVO.setNickname(user.getNickname());
            postVO.setAvatar(user.getAvatar());
        }

        // ================= 核心修改：查询当前用户的点赞状态 =================
        if (currentUserId != null) {
            // 判断是否点赞
            LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(PostLike::getPostId, post.getId())
                    .eq(PostLike::getUserId, currentUserId);
            // ⚠️ 修复：把 vo 改成 postVO
            postVO.setLiked(postLikeMapper.selectCount(likeWrapper) > 0);

            // 👇 新增：判断是否收藏
            LambdaQueryWrapper<PostCollection> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(PostCollection::getPostId, post.getId())
                    .eq(PostCollection::getUserId, currentUserId);
            // ⚠️ 修复：把 vo 改成 postVO
            postVO.setCollected(postCollectionMapper.selectCount(collectWrapper) > 0);
            
            // 👇 新增：判断是否关注
            postVO.setIsFollowed(userFollowService.isFollowed(currentUserId, post.getUserId()));
        } else {
            // ⚠️ 修复：把 vo 改成 postVO
            postVO.setLiked(false);
            postVO.setCollected(false);
            postVO.setIsFollowed(false);
        }

        return postVO;
    }

    @Override
    public IPage<PostVO> pageQueryPosts(PostQueryDTO queryDTO, Long currentUserId) {
        Page<Post> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getUserId() != null, Post::getUserId, queryDTO.getUserId());
        wrapper.like(queryDTO.getTitle() != null, Post::getTitle, queryDTO.getTitle());
        wrapper.eq(Post::getStatus, queryDTO.getStatus() != null ? queryDTO.getStatus() : 1);
        wrapper.orderByDesc(Post::getCreateTime);

        Page<Post> postPage = this.page(page, wrapper);

        // 转换 Po 到 VO，并顺便查出每个帖子的点赞状态
        return postPage.convert(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);
            if (post.getMediaUrls() != null) {
                vo.setMediaUrlList(JSON.parseArray(post.getMediaUrls(), String.class));
            }

            // 查询用户信息并设置 username 和 avatar
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }

            // ================= 核心修改：分页列表里的点赞状态 =================
            if (currentUserId != null) {
                // 判断是否点赞
                LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.eq(PostLike::getPostId, post.getId())
                        .eq(PostLike::getUserId, currentUserId);
                vo.setLiked(postLikeMapper.selectCount(likeWrapper) > 0);

                // 👇 新增：判断是否收藏
                LambdaQueryWrapper<PostCollection> collectWrapper = new LambdaQueryWrapper<>();
                collectWrapper.eq(PostCollection::getPostId, post.getId())
                        .eq(PostCollection::getUserId, currentUserId);
                vo.setCollected(postCollectionMapper.selectCount(collectWrapper) > 0);
                
                // 👇 新增：判断是否关注
                vo.setIsFollowed(userFollowService.isFollowed(currentUserId, post.getUserId()));
            } else {
                vo.setLiked(false);
                vo.setCollected(false);
                vo.setIsFollowed(false);
            }

            return vo;
        });
    }



    @Override
    public void incrementViewCount(Long postId) {
        // 注意：高并发下最好用自定义SQL: UPDATE biz_post SET view_count = view_count + 1 WHERE id = #{postId}
        Post post = this.getById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            this.updateById(post);
        }
    }

    @Override
    public void updateLikeCount(Long postId, boolean increment) {
        Post post = this.getById(postId);
        if (post != null) {
            int newCount = increment ? post.getLikeCount() + 1 : Math.max(0, post.getLikeCount() - 1);
            post.setLikeCount(newCount);
            this.updateById(post);
        }
    }

    @Override
    public void updateCommentCount(Long postId, boolean increment) {
        Post post = this.getById(postId);
        if (post != null) {
            int newCount = increment ? post.getCommentCount() + 1 : Math.max(0, post.getCommentCount() - 1);
            post.setCommentCount(newCount);
            this.updateById(post);
        }
    }

    @Override
    public void updateCollectCount(Long postId, boolean increment) {
        Post post = this.getById(postId);
        if (post != null) {
            // 修复了原来 collectionCount 拼写错误的问题
            int newCount = increment ? post.getCollectCount() + 1 : Math.max(0, post.getCollectCount() - 1);
            post.setCollectCount(newCount);
            this.updateById(post);
        }
    }
}