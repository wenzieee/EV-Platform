package com.wenzi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wenzi.entity.Post;
import com.wenzi.dto.PostCreateDTO;
import com.wenzi.dto.PostUpdateDTO;
import com.wenzi.dto.PostQueryDTO;
import com.wenzi.vo.PostVO;

public interface IPostService extends IService<Post> {

    /**
     * 创建帖子
     * @param postCreateDTO 帖子创建DTO
     * @param userId 发布者ID
     * @return 创建成功后的帖子ID
     */
    Long createPost(PostCreateDTO postCreateDTO, Long userId);

    /**
     * 更新帖子
     * @param postUpdateDTO 帖子更新DTO
     * @param userId 操作者ID
     * @return 是否更新成功
     */
    boolean updatePost(PostUpdateDTO postUpdateDTO, Long userId);

    /**
     * 删除帖子 (逻辑删除，或修改状态)
     * @param postId 帖子ID
     * @param userId 操作者ID
     * @return 是否删除成功
     */
    boolean deletePost(Long postId, Long userId);

    /**
     * 根据ID获取帖子详情
     * @param postId 帖子ID
     * @param currentUserId 当前登录用户ID (用于判断点赞/收藏状态)
     * @return 帖子VO
     */
    PostVO getPostDetail(Long postId, Long currentUserId);

    /**
     * 分页查询帖子列表
     * @param queryDTO 查询条件
     * @param currentUserId 当前登录用户ID (用于判断点赞/收藏状态)
     * @return 分页结果
     */
    IPage<PostVO> pageQueryPosts(PostQueryDTO queryDTO, Long currentUserId);

    /**
     * 增加帖子浏览量
     * @param postId 帖子ID
     */
    void incrementViewCount(Long postId);

    /**
     * 增加或减少帖子点赞数
     * @param postId 帖子ID
     * @param increment 是否增加 (true为增加，false为减少)
     */
    void updateLikeCount(Long postId, boolean increment);

    /**
     * 增加或减少帖子评论数
     * @param postId 帖子ID
     * @param increment 是否增加 (true为增加，false为减少)
     */
    void updateCommentCount(Long postId, boolean increment);

    /**
     * 增加或减少帖子收藏数
     * @param postId 帖子ID
     * @param increment 是否增加 (true为增加，false为减少)
     */
    void updateCollectCount(Long postId, boolean increment);
}
