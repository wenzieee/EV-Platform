package com.wenzi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenzi.dto.CommentCreateDTO;
import com.wenzi.dto.CommentQueryDTO;
import com.wenzi.dto.CommentUpdateDTO;
import com.wenzi.entity.Comment;
import com.wenzi.entity.Post;
import com.wenzi.entity.User;
import com.wenzi.mapper.CommentMapper;
import com.wenzi.mapper.PostMapper;
import com.wenzi.mapper.UserMapper;
import com.wenzi.service.ICommentService;
import com.wenzi.service.IPostService;
import com.wenzi.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 评论业务实现类
 *
 * @author wenzi
 * @date 2024-xx-xx
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IPostService postService;

    /**
     * 创建评论
     *
     * @param commentCreateDTO 评论创建DTO
     * @param userId           当前用户ID
     * @return 评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 明确异常回滚策略
    public Long createComment(CommentCreateDTO commentCreateDTO, Long userId) {
        // 1. 参数校验
        if (commentCreateDTO == null || userId == null || !StringUtils.hasText(commentCreateDTO.getContent())
                || commentCreateDTO.getPostId() == null) {
            throw new IllegalArgumentException("评论参数不合法");
        }

        // 2. 校验帖子是否存在且有效
        Post post = postMapper.selectById(commentCreateDTO.getPostId());
        if (post == null || post.getStatus() == 0) {
            throw new RuntimeException("帖子不存在或已被删除");
        }

        // 3. 构建评论实体
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentCreateDTO, comment);
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setStatus(1); // 1-正常展示 0-删除/隐藏
        comment.setLikeCount(0); // 初始化点赞数
        // 父评论ID默认0（顶级评论）
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        // 4. 插入评论
        commentMapper.insert(comment);

        // 5. 更新帖子评论数（加1）
        postService.updateCommentCount(commentCreateDTO.getPostId(), true);

        return comment.getId();
    }

    /**
     * 更新评论
     *
     * @param commentUpdateDTO 评论更新DTO
     * @param userId           当前用户ID
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateComment(CommentUpdateDTO commentUpdateDTO, Long userId) {
        // 1. 参数校验
        if (commentUpdateDTO == null || commentUpdateDTO.getId() == null || userId == null) {
            throw new IllegalArgumentException("更新参数不合法");
        }

        // 2. 查询评论是否存在
        Comment comment = commentMapper.selectById(commentUpdateDTO.getId());
        if (comment == null || comment.getStatus() == 0) {
            return false; // 评论不存在或已删除
        }

        // 3. 权限校验：仅评论作者或管理员可修改
        if (!comment.getUserId().equals(userId) && !isAdmin(userId)) {
            return false;
        }

        // 4. 更新评论内容
        if (StringUtils.hasText(commentUpdateDTO.getContent())) {
            comment.setContent(commentUpdateDTO.getContent());
        }
        // 仅管理员可修改状态
        if (commentUpdateDTO.getStatus() != null && isAdmin(userId)) {
            comment.setStatus(commentUpdateDTO.getStatus());
        }
        //comment.setUpdateTime(LocalDateTime.now()); // 补充更新时间

        // 5. 执行更新
        return commentMapper.updateById(comment) > 0;
    }

    /**
     * 删除评论（逻辑删除）
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, Long userId) {
        // 1. 参数校验
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("删除参数不合法");
        }

        // 2. 查询评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            return false;
        }

        // 3. 权限校验：评论作者、帖子作者、管理员可删除
        Post post = postMapper.selectById(comment.getPostId());
        boolean isPostAuthor = post != null && post.getUserId().equals(userId);
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isAdmin = isAdmin(userId);
        if (!isCommentAuthor && !isPostAuthor && !isAdmin) {
            return false; // 无权限
        }

        // 4. 逻辑删除（修改状态）
        comment.setStatus(0);
        //comment.setUpdateTime(LocalDateTime.now());
        boolean success = commentMapper.updateById(comment) > 0;

        // 5. 更新帖子评论数（减1）
        if (success) {
            postService.updateCommentCount(comment.getPostId(), false);
        }

        return success;
    }

    /**
     * 获取评论详情
     *
     * @param commentId     评论ID
     * @param currentUserId 当前用户ID
     * @return 评论VO
     */
    @Override
    public CommentVO getCommentDetail(Long commentId, Long currentUserId) {
        // 1. 参数校验
        if (commentId == null) {
            return null;
        }

        // 2. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            return null;
        }

        // 3. 转换为VO
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 4. 补充评论者信息
        User commentUser = userMapper.selectById(comment.getUserId());
        if (commentUser != null) {
            commentVO.setUsername(commentUser.getUsername());
            commentVO.setNickname(commentUser.getNickname());
            commentVO.setAvatar(commentUser.getAvatar());
        }

        // 5. 补充父评论者信息（如果有）
        if (comment.getParentId() != null && comment.getParentId() != 0) {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null && parentComment.getStatus() == 1) {
                User parentUser = userMapper.selectById(parentComment.getUserId());
                if (parentUser != null) {
                    commentVO.setParentUsername(parentUser.getNickname() != null ? parentUser.getNickname() : parentUser.getUsername());
                }
            }
        }

        // 6. 暂未实现点赞功能，默认false
        commentVO.setLiked(false);

        return commentVO;
    }

    /**
     * 分页查询评论
     *
     * @param queryDTO       查询DTO
     * @param currentUserId 当前用户ID
     * @return 评论VO分页列表
     */
    @Override
    public IPage<CommentVO> pageQueryComments(CommentQueryDTO queryDTO, Long currentUserId) {
        // 1. 参数校验
        if (queryDTO == null || queryDTO.getPostId() == null) {
            throw new IllegalArgumentException("查询参数不合法");
        }
        // 分页参数默认值
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
        Page<Comment> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, queryDTO.getPostId());
        // 按用户ID筛选
        if (queryDTO.getUserId() != null) {
            wrapper.eq(Comment::getUserId, queryDTO.getUserId());
        }
        // 按父评论ID筛选 (仅当明确传了 parentId 时才过滤，否则查询该帖子下的所有评论和回复)
        if (queryDTO.getParentId() != null) {
            wrapper.eq(Comment::getParentId, queryDTO.getParentId());
        }
        // 按状态筛选，默认只查正常评论
        wrapper.eq(Comment::getStatus, queryDTO.getStatus() == null ? 1 : queryDTO.getStatus());
        // 按创建时间倒序（最新评论在前）
        wrapper.orderByDesc(Comment::getCreateTime);

        // 3. 分页查询
        IPage<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        // 4. 转换为VO
        return commentPage.convert(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);

            // 补充评论者信息
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }

            // 补充父评论者信息
            if (comment.getParentId() != null && comment.getParentId() != 0) {
                Comment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null && parentComment.getStatus() == 1) {
                    User parentUser = userMapper.selectById(parentComment.getUserId());
                    if (parentUser != null) {
                        // ✅ 优先使用昵称作为回复展示
                        vo.setParentUsername(parentUser.getNickname() != null ? parentUser.getNickname() : parentUser.getUsername());
                    }
                }
            }

            // 点赞状态暂未实现
            vo.setLiked(false);
            return vo;
        });
    }

    /**
     * 更新评论点赞数
     *
     * @param commentId 评论ID
     * @param increment 是否增加（true-加1，false-减1）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLikeCount(Long commentId, boolean increment) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            return;
        }

        // 更新点赞数（防止负数）
        int newLikeCount = increment ? comment.getLikeCount() + 1 : comment.getLikeCount() - 1;
        comment.setLikeCount(Math.max(newLikeCount, 0));
        //comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
    }
    
    /**
     * 切换评论点赞状态
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @param isLiked 是否点赞
     * @return 操作是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCommentLike(Long commentId, Long userId, boolean isLiked) {
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }

        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            return false;
        }

        // 这里简化处理，直接更新点赞数
        // 实际项目中应该有评论点赞表来记录用户的点赞状态
        updateLikeCount(commentId, isLiked);
        return true;
    }

    /**
     * 判断是否为管理员
     *
     * @param userId 用户ID
     * @return 是否是管理员
     */
    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        // 防御性判空，避免空指针
        return user != null && 2 == user.getRole(); // 假设2为管理员角色
    }

    @Override
    public IPage<CommentVO> getUserComments(Long userId, Integer pageNum, Integer pageSize, Long currentUserId) {
        // 1. 创建分页对象
        Page<Comment> page = new Page<>(pageNum, pageSize);
        
        // 2. 查询用户的评论记录
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId);
        wrapper.eq(Comment::getStatus, 1); // 只查询未删除的评论
        wrapper.orderByDesc(Comment::getCreateTime);
        
        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);
        
        // 3. 转换为 CommentVO 列表
        return commentPage.convert(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            
            // 补充评论者信息
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            
            // 补充父评论者信息
            if (comment.getParentId() != null && comment.getParentId() != 0) {
                Comment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null && parentComment.getStatus() == 1) {
                    User parentUser = userMapper.selectById(parentComment.getUserId());
                    if (parentUser != null) {
                        // ✅ 让个人中心的 @ 回复也优先显示昵称
                        vo.setParentUsername(parentUser.getNickname() != null ? parentUser.getNickname() : parentUser.getUsername());
                    }
                }
            }
            
            // 补充帖子标题
            Post post = postMapper.selectById(comment.getPostId());
            if (post != null) {
                vo.setPostTitle(post.getTitle());
            }
            
            // 点赞状态暂未实现
            vo.setLiked(false);
            
            return vo;
        });
    }
}