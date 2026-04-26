<script setup>
import { ref, onMounted, computed } from 'vue'
import { ChatDotRound, Star, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElInput } from 'element-plus'
import request from '../utils/request'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 帖子详情数据
const post = ref(null)
const loading = ref(false)

// 评论相关数据
const comments = ref([])
const commentLoading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 评论输入框
const commentContent = ref('')
const replyTo = ref(null)
const replyContent = ref('')

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取帖子详情
const fetchPostDetail = async () => {
  const postId = route.params.id
  if (!postId) return

  loading.value = true
  try {
    const res = await request.get(`/post/${postId}`)
    if (res.code === 200) {
      post.value = res.data
    } else {
      ElMessage.error('获取帖子详情失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取帖子详情失败')
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  const postId = route.params.id
  if (!postId) return

  commentLoading.value = true
  try {
    const res = await request.get('/comment/page', {
      params: {
        postId,
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    if (res.code === 200) {
      if (pageNum.value === 1) {
        comments.value = res.data.records
      } else {
        comments.value = [...comments.value, ...res.data.records]
      }
      total.value = res.data.total
    } else {
      ElMessage.error('获取评论失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取评论失败')
  } finally {
    commentLoading.value = false
  }
}

// 发布评论
const publishComment = async () => {
  const postId = route.params.id
  if (!postId || !commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const res = await request.post('/comment', {
      postId,
      parentId: 0,
      content: commentContent.value.trim()
    })
    if (res.code === 200) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      pageNum.value = 1
      fetchComments()
      fetchPostDetail() // 更新帖子评论数
    } else if (res.code === 401) {
      ElMessage.warning('请先登录')
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('评论失败')
  }
}

// 回复评论
const publishReply = async (parentComment) => {
  const postId = route.params.id
  if (!postId || !replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    const res = await request.post('/comment', {
      postId,
      parentId: parentComment.id,
      content: replyContent.value.trim()
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyContent.value = ''
      replyTo.value = null
      pageNum.value = 1
      fetchComments()
      fetchPostDetail() // 更新帖子评论数
    } else if (res.code === 401) {
      ElMessage.warning('请先登录')
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('回复失败')
  }
}

// 关注用户
const toggleFollow = async () => {
  if (!post.value) return

  try {
    const isFollowed = !post.value.isFollowed
    const res = await request.post(`/user/follow/toggle/${post.value.userId}?isFollowed=${isFollowed}`)
    if (res.code === 200) {
      ElMessage.success(isFollowed ? '关注成功' : '取消关注成功')
      post.value.isFollowed = isFollowed
    } else if (res.code === 401) {
      ElMessage.warning('请先登录')
    } else {
      ElMessage.error(res.msg)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

// 帖子点赞
const toggleLike = async () => {
  if (!post.value) return

  try {
    const isLiking = !post.value.liked
    const res = await request.post('/post/like/toggle', {
      postId: post.value.id,
      isLiked: isLiking
    })
    if (res.code === 200) {
      post.value.liked = isLiking
      post.value.likeCount += isLiking ? 1 : -1
    } else if (res.code === 401) {
      ElMessage.warning('请先登录')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

// 帖子收藏
const toggleCollect = async () => {
  if (!post.value) return

  try {
    const isCollecting = !post.value.collected
    const res = await request.post('/post/collection/toggle', {
      postId: post.value.id,
      isCollected: isCollecting
    })
    if (res.code === 200) {
      ElMessage.success(isCollecting ? '收藏成功' : '取消收藏成功')
      post.value.collected = isCollecting
      post.value.collectCount += isCollecting ? 1 : -1
    } else if (res.code === 401) {
      ElMessage.warning('请先登录')
    } else {
      ElMessage.error(res.msg)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

// 评论点赞
const toggleCommentLike = async (comment) => {
  try {
    const isLiking = !comment.liked
    const res = await request.post('/comment/like/toggle', {
      commentId: comment.id,
      isLiked: isLiking
    })
    if (res.code === 200) {
      comment.liked = isLiking
      comment.likeCount += isLiking ? 1 : -1
    } else if (res.code === 401) {
      ElMessage.warning('请先登录')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

// 加载更多评论
const loadMoreComments = () => {
  if (comments.value.length < total.value) {
    pageNum.value++
    fetchComments()
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchPostDetail()
  fetchComments()
})
</script>

<template>
  <div class="post-detail-container">
    <div class="header">
      <el-button type="text" @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1>帖子详情</h1>
    </div>

    <el-skeleton :loading="loading" animated :count="1">
      <template #template>
        <div class="post-card skeleton-card">
          <div style="display: flex; align-items: center; margin-bottom: 16px;">
            <el-skeleton-item variant="circle" style="width: 40px; height: 40px; margin-right: 12px;" />
            <el-skeleton-item variant="text" style="width: 120px;" />
          </div>
          <el-skeleton-item variant="h3" style="width: 60%; margin-bottom: 16px;" />
          <el-skeleton-item variant="image" style="width: 100%; height: 300px; margin-bottom: 16px;" />
          <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px;" />
          <el-skeleton-item variant="text" style="width: 80%;" />
        </div>
      </template>

      <template #default>
        <div v-if="post" class="post-card">
          <div class="post-header">
            <div class="user-info">
              <el-avatar :size="40" :src="post.avatar || defaultAvatar" />
              <span class="username">{{ post.nickname || post.username || '匿名车友' }}</span>
              <el-tag size="small" type="warning" effect="light" class="elite-tag">精华</el-tag>
            </div>
            <el-button
              :type="post.isFollowed ? 'default' : 'primary'"
              size="small"
              class="follow-btn"
              @click="toggleFollow"
            >
              {{ post.isFollowed ? '已关注' : '+ 关注' }}
            </el-button>
          </div>

          <div class="post-body">
            <h2 class="post-title">{{ post.title }}</h2>

            <div class="post-content" v-html="post.content"></div>

          </div>

          <div class="post-footer">
            <span class="time">{{ post.createTime }}</span>
            <div class="actions">
              <span class="action-item" :class="{ 'is-active': post.liked }" @click="toggleLike">
                <el-icon class="icon">
                  <svg viewBox="0 0 1024 1024" width="16" height="16"><path d="M857.28 344.992h-264.832c12.576-44.256 31.456-83.584 31.456-127.616 0-103.776-71.392-167.776-131.744-167.776-24.448 0-41.536 8.576-55.84 21.056-11.232 9.856-19.168 23.36-23.456 42.144L364.576 345.024H127.36c-17.664 0-32 14.336-32 32v533.312c0 17.664 14.336 32 32 32h729.824c16.544 0 31.136-12.608 31.936-29.152l40-800c0.768-15.392-11.456-28.192-26.848-28.192zM159.36 878.336V408.992h183.168v469.344H159.36z" :fill="post.liked ? '#f56c6c' : 'currentColor'"/></svg>
                </el-icon>
                {{ post.likeCount || 0 }}
              </span>
              <span class="action-item">
                <el-icon class="icon"><ChatDotRound /></el-icon>
                {{ post.commentCount || 0 }}
              </span>
              <span class="action-item" :class="{ 'is-active': post.collected }" @click="toggleCollect">
                <el-icon class="icon"><Star /></el-icon>
                {{ post.collectCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>

    <!-- 评论区 -->
    <div class="comment-section">
      <h3 class="comment-title">
        <el-icon class="icon"><ChatDotRound /></el-icon>
        评论 ({{ total }})
      </h3>

      <!-- 评论输入框 -->
      <div class="comment-input-container">
        <el-avatar :size="40" :src="defaultAvatar" class="input-avatar" />
        <div class="input-wrapper">
          <el-input
            v-model="commentContent"
            type="textarea"
            placeholder="写下你的评论..."
            :rows="3"
            resize="none"
            class="comment-input"
          />
          <div class="input-footer">
            <el-button type="primary" @click="publishComment" :loading="loading">发布评论</el-button>
          </div>
        </div>
      </div>

      <!-- 评论列表 -->
      <el-skeleton :loading="commentLoading && pageNum === 1" animated :count="3">
        <template #template>
          <div class="comment-item">
            <el-skeleton-item variant="circle" style="width: 32px; height: 32px; margin-right: 12px;" />
            <div class="comment-content">
              <el-skeleton-item variant="text" style="width: 100px; margin-bottom: 8px;" />
              <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px;" />
              <el-skeleton-item variant="text" style="width: 60%;" />
            </div>
          </div>
        </template>

        <template #default>
          <div v-if="comments.length === 0" class="no-comments">
            <el-empty description="暂无评论，快来抢沙发吧！" />
          </div>

          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="32" :src="comment.avatar || defaultAvatar" />
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-username">{{ comment.nickname || comment.username || '匿名车友' }}</span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-text" v-if="comment.parentUsername">
                <span class="reply-to">@{{ comment.parentUsername }}：</span>
                {{ comment.content }}
              </div>
              <div class="comment-text" v-else>
                {{ comment.content }}
              </div>
              <div class="comment-actions">
                <span class="action-item" :class="{ 'is-active': comment.liked }" @click="toggleCommentLike(comment)">
                  <el-icon class="icon">
                    <svg viewBox="0 0 1024 1024" width="14" height="14"><path d="M857.28 344.992h-264.832c12.576-44.256 31.456-83.584 31.456-127.616 0-103.776-71.392-167.776-131.744-167.776-24.448 0-41.536 8.576-55.84 21.056-11.232 9.856-19.168 23.36-23.456 42.144L364.576 345.024H127.36c-17.664 0-32 14.336-32 32v533.312c0 17.664 14.336 32 32 32h729.824c16.544 0 31.136-12.608 31.936-29.152l40-800c0.768-15.392-11.456-28.192-26.848-28.192zM159.36 878.336V408.992h183.168v469.344H159.36z" :fill="comment.liked ? '#f56c6c' : 'currentColor'"/></svg>
                  </el-icon>
                  {{ comment.likeCount || 0 }}
                </span>
                <span class="action-item" @click="replyTo = comment">
                  回复
                </span>
              </div>

              <!-- 回复输入框 -->
              <div v-if="replyTo && replyTo.id === comment.id" class="reply-input-container">
                <el-avatar :size="24" :src="defaultAvatar" class="reply-avatar" />
                <div class="reply-input-wrapper">
                  <el-input
                    v-model="replyContent"
                    type="textarea"
                    placeholder="写下你的回复..."
                    :rows="2"
                    resize="none"
                    class="reply-input"
                  />
                  <div class="reply-input-footer">
                    <el-button type="text" @click="replyTo = null">取消</el-button>
                    <el-button type="primary" @click="publishReply(comment)">发布回复</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="load-more" v-if="comments.length < total">
            <el-button text @click="loadMoreComments()">加载更多</el-button>
          </div>
          <div class="load-more no-more" v-else-if="comments.length > 0">
            - 到底啦 -
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>

<style scoped>
/* 核心容器 */
.post-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0 60px 0;
}

/* 头部 */
.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.back-btn {
  margin-right: 20px;
}

.header h1 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

/* 帖子卡片样式 */
.post-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* 头部：头像、昵称、标签、关注按钮 */
.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  font-size: 16px;
  font-weight: bold;
  color: #222;
  margin: 0 10px;
}

.elite-tag {
  font-weight: bold;
  border-radius: 4px;
}

.follow-btn {
  background-color: #007aff;
  border-color: #007aff;
  border-radius: 4px;
  font-weight: bold;
}

/* 内容区 */
.post-body {
  margin-bottom: 20px;
}

.post-title {
  font-size: 20px;
  font-weight: 600;
  color: #111;
  line-height: 1.4;
  margin-bottom: 16px;
}

.media-container {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.post-image {
  width: 100%;
  max-height: 400px;
  display: block;
  margin-bottom: 10px;
}

.post-content {
  font-size: 16px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 0;
}

/* 底部：时间和操作 */
.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 14px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.actions {
  display: flex;
  gap: 24px;
}

.action-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: color 0.2s;
}

.action-item:hover {
  color: #007aff;
}

.action-item.is-active {
  color: #f56c6c;
}

.icon {
  margin-right: 4px;
  font-size: 18px;
}

/* 评论区 */
.comment-section {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.comment-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.comment-title .icon {
  margin-right: 8px;
  font-size: 18px;
}

/* 评论输入框 */
.comment-input-container {
  display: flex;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.input-avatar {
  margin-right: 12px;
}

.input-wrapper {
  flex: 1;
}

.comment-input {
  margin-bottom: 10px;
  border-radius: 8px;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
}

/* 评论列表 */
.comment-item {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.comment-item .el-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-username {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 10px;
}

.reply-to {
  color: #007aff;
}

.comment-actions {
  display: flex;
  gap: 20px;
}

.comment-actions .action-item {
  font-size: 12px;
  color: #999;
}

/* 回复输入框 */
.reply-input-container {
  display: flex;
  margin-top: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.reply-avatar {
  margin-right: 10px;
  flex-shrink: 0;
}

.reply-input-wrapper {
  flex: 1;
}

.reply-input {
  margin-bottom: 10px;
  border-radius: 6px;
  font-size: 14px;
}

.reply-input-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 无评论状态 */
.no-comments {
  padding: 40px 0;
  text-align: center;
}

/* 底部加载更多状态 */
.load-more {
  text-align: center;
  padding: 20px 0;
}

.no-more {
  color: #999;
  font-size: 14px;
}

/* 骨架屏样式 */
.skeleton-card {
  padding: 20px;
  border-radius: 8px;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* 限制富文本中的图片和视频宽度，防止撑爆卡片 */
.post-content :deep(img),
.post-content :deep(video),
.post-content :deep(iframe) {
  max-width: 100%;
  border-radius: 8px;
  margin: 10px 0;
}

</style>
