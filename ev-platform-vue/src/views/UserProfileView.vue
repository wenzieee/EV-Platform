<script setup>
import { ref, onMounted } from 'vue'
import { Calendar, Star, ChatDotRound, Edit, Delete, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElTabs, ElTabPane, ElButton, ElSkeleton, ElEmpty } from 'element-plus'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()

// 帖子列表数据
const activeTab = ref('posts')
const posts = ref([])
const likes = ref([])
const comments = ref([])
const collections = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// ====== 缩略图处理工具 ======
// 1. 提取纯文本用于摘要显示
const getSnippet = (html) => {
  if (!html) return ''
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  const paragraphs = Array.from(doc.querySelectorAll('p, div'))
  for (const p of paragraphs) {
    const text = p.textContent.trim()
    if (text.length > 0) return text.length > 100 ? text.slice(0, 100) + '...' : text
  }
  const text = doc.body.textContent.trim().replace(/\s+/g, ' ')
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

// 2. 提取第一张图或视频
const getCoverMedia = (html) => {
  if (!html) return null
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  const img = doc.querySelector('img')
  if (img && img.getAttribute('src')) return { type: 'image', url: img.getAttribute('src') }
  const video = doc.querySelector('video')
  if (video && video.getAttribute('src')) return { type: 'video', url: video.getAttribute('src') }
  return null
}

// 3. 封装一个统一的数据处理函数
const processPosts = (records) => {
  return records.map(post => ({
    ...post,
    coverMedia: getCoverMedia(post.content),
    snippet: getSnippet(post.content)
  }))
}

// ====== 获取数据方法 ======
// 获取用户发布的帖子
const fetchUserPosts = async () => {
  loading.value = true
  try {
    const res = await request.get('/post/my', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        userId: null // 后端会从token中获取当前用户ID
      }
    })
    if (res.code === 200) {
      const processedData = processPosts(res.data.records)
      if (pageNum.value === 1) {
        posts.value = processedData
      } else {
        posts.value = [...posts.value, ...processedData]
      }
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取帖子失败')
  } finally {
    loading.value = false
  }
}

// 获取用户点赞的帖子
const fetchUserLikes = async () => {
  loading.value = true
  try {
    const res = await request.get('/post/like/my', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    if (res.code === 200) {
      const processedData = processPosts(res.data.records)
      if (pageNum.value === 1) {
        likes.value = processedData
      } else {
        likes.value = [...likes.value, ...processedData]
      }
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取点赞帖子失败')
  } finally {
    loading.value = false
  }
}

// 获取用户评论的帖子 (评论本身不需要脱水处理图文)
const fetchUserComments = async () => {
  loading.value = true
  try {
    const res = await request.get('/comment/my', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    console.log('评论数据:', res)
    if (res.code === 200) {
      if (pageNum.value === 1) {
        comments.value = res.data.records
      } else {
        comments.value = [...comments.value, ...res.data.records]
      }
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取评论失败')
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    ElMessage.error('获取评论失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 获取用户收藏的帖子
const fetchUserCollections = async () => {
  loading.value = true
  try {
    const res = await request.get('/post/collection/my', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    if (res.code === 200) {
      const processedData = processPosts(res.data.records)
      if (pageNum.value === 1) {
        collections.value = processedData
      } else {
        collections.value = [...collections.value, ...processedData]
      }
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取收藏帖子失败')
  } finally {
    loading.value = false
  }
}

// ====== 交互操作方法 ======
// 加载更多数据
const loadMore = () => {
  pageNum.value++
  switch (activeTab.value) {
    case 'posts':
      fetchUserPosts()
      break
    case 'likes':
      fetchUserLikes()
      break
    case 'comments':
      fetchUserComments()
      break
    case 'collections':
      fetchUserCollections()
      break
  }
}

// 编辑帖子 (跳转到编辑页)
const editPost = (postId) => {
  router.push(`/community/edit/${postId}`)
}

// 删除帖子
const deletePost = async (postId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.delete(`/post/${postId}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      pageNum.value = 1
      fetchUserPosts()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

// 删除评论
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.delete(`/comment/${commentId}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      pageNum.value = 1
      fetchUserComments()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

// 跳转到帖子详情页
const goToPostDetail = (postId) => {
  router.push(`/post/${postId}`)
}

// 切换标签页
const handleTabChange = (tab) => {
  activeTab.value = tab.props.name
  pageNum.value = 1
  switch (tab.props.name) {
    case 'posts':
      fetchUserPosts()
      break
    case 'likes':
      fetchUserLikes()
      break
    case 'comments':
      fetchUserComments()
      break
    case 'collections':
      fetchUserCollections()
      break
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchUserPosts()
})
</script>

<template>
  <div class="user-profile-container">
    <div class="header">
      <el-button type="text" @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1>创作中心</h1>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="profile-tabs">

      <el-tab-pane label="发布" name="posts">
        <el-skeleton :loading="loading && pageNum === 1" animated :count="3">
          <template #template>
            <div class="post-card skeleton-card">
              <div style="display: flex; align-items: center; margin-bottom: 16px;">
                <el-skeleton-item variant="circle" style="width: 40px; height: 40px; margin-right: 12px;" />
                <el-skeleton-item variant="text" style="width: 120px;" />
              </div>
              <el-skeleton-item variant="h3" style="width: 60%; margin-bottom: 16px;" />
              <el-skeleton-item variant="image" style="width: 100%; height: 300px; margin-bottom: 16px;" />
            </div>
          </template>

          <template #default>
            <el-empty v-if="posts.length === 0" description="暂无发布的帖子" />

            <div v-for="post in posts" :key="post.id" class="post-card">
              <div class="post-header">
                <div class="user-info">
                  <el-avatar :size="40" :src="post.avatar || defaultAvatar" />
                  <span class="username">{{ post.nickname || post.username || '匿名车友' }}</span>
                  <el-tag size="small" type="warning" effect="light" class="elite-tag">精华</el-tag>
                </div>
                <div class="post-actions">
                  <el-button size="small" @click="editPost(post.id)" class="edit-btn" type="primary" plain>
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                  <el-button size="small" type="danger" @click="deletePost(post.id)" class="delete-btn">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </div>
              </div>

              <div class="post-body" @click="goToPostDetail(post.id)">
                <h2 class="post-title">{{ post.title }}</h2>

                <div class="media-container" v-if="post.coverMedia">
                  <el-image
                    v-if="post.coverMedia.type === 'image'"
                    :src="post.coverMedia.url"
                    fit="contain"
                    class="post-cover"
                    lazy
                  />
                  <video
                    v-else-if="post.coverMedia.type === 'video'"
                    :src="post.coverMedia.url"
                    class="post-cover"
                    controls
                    controlslist="nodownload"
                    preload="metadata"
                  ></video>
                </div>

                <p class="post-snippet">{{ post.snippet }}</p>
              </div>

              <div class="post-footer">
                <span class="time">{{ post.createTime }}</span>
                <div class="actions">
                  <span class="action-item" :class="{ 'is-active': post.liked }">
                    <el-icon class="icon">
                      <svg viewBox="0 0 1024 1024" width="16" height="16"><path d="M857.28 344.992h-264.832c12.576-44.256 31.456-83.584 31.456-127.616 0-103.776-71.392-167.776-131.744-167.776-24.448 0-41.536 8.576-55.84 21.056-11.232 9.856-19.168 23.36-23.456 42.144L364.576 345.024H127.36c-17.664 0-32 14.336-32 32v533.312c0 17.664 14.336 32 32 32h729.824c16.544 0 31.136-12.608 31.936-29.152l40-800c0.768-15.392-11.456-28.192-26.848-28.192zM159.36 878.336V408.992h183.168v469.344H159.36z" :fill="post.liked ? '#f56c6c' : 'currentColor'"/></svg>
                    </el-icon>
                    {{ post.likeCount || 0 }}
                  </span>
                  <span class="action-item">
                    <el-icon class="icon"><ChatDotRound /></el-icon>
                    {{ post.commentCount || 0 }}
                  </span>
                  <span class="action-item" :class="{ 'is-active': post.collected }">
                    <el-icon class="icon"><Star /></el-icon>
                    {{ post.collectCount || 0 }}
                  </span>
                </div>
              </div>
            </div>

            <div class="load-more" v-if="posts.length < total">
              <el-button text @click="loadMore">加载更多</el-button>
            </div>
            <div class="load-more no-more" v-else-if="posts.length > 0">
              - 到底啦 -
            </div>
          </template>
        </el-skeleton>
      </el-tab-pane>

      <el-tab-pane label="点赞" name="likes">
        <el-skeleton :loading="loading && pageNum === 1" animated :count="3">
          <template #template>
            <div class="post-card skeleton-card">
              <div style="display: flex; align-items: center; margin-bottom: 16px;">
                <el-skeleton-item variant="circle" style="width: 40px; height: 40px; margin-right: 12px;" />
                <el-skeleton-item variant="text" style="width: 120px;" />
              </div>
              <el-skeleton-item variant="h3" style="width: 60%; margin-bottom: 16px;" />
              <el-skeleton-item variant="image" style="width: 100%; height: 300px; margin-bottom: 16px;" />
            </div>
          </template>

          <template #default>
            <el-empty v-if="likes.length === 0" description="暂无点赞的帖子" />

            <div v-for="post in likes" :key="post.id" class="post-card">
              <div class="post-header">
                <div class="user-info">
                  <el-avatar :size="40" :src="post.avatar || defaultAvatar" />
                  <span class="username">{{ post.nickname || post.username || '匿名车友' }}</span>
                  <el-tag size="small" type="warning" effect="light" class="elite-tag">精华</el-tag>
                </div>
              </div>

              <div class="post-body" @click="goToPostDetail(post.id)">
                <h2 class="post-title">{{ post.title }}</h2>

                <div class="media-container" v-if="post.coverMedia">
                  <el-image
                    v-if="post.coverMedia.type === 'image'"
                    :src="post.coverMedia.url"
                    fit="contain"
                    class="post-cover"
                    lazy
                  />
                  <video
                    v-else-if="post.coverMedia.type === 'video'"
                    :src="post.coverMedia.url"
                    class="post-cover"
                    controls
                    controlslist="nodownload"
                    preload="metadata"
                  ></video>
                </div>

                <p class="post-snippet">{{ post.snippet }}</p>
              </div>

              <div class="post-footer">
                <span class="time">{{ post.createTime }}</span>
                <div class="actions">
                  <span class="action-item" :class="{ 'is-active': post.liked }">
                    <el-icon class="icon">
                      <svg viewBox="0 0 1024 1024" width="16" height="16"><path d="M857.28 344.992h-264.832c12.576-44.256 31.456-83.584 31.456-127.616 0-103.776-71.392-167.776-131.744-167.776-24.448 0-41.536 8.576-55.84 21.056-11.232 9.856-19.168 23.36-23.456 42.144L364.576 345.024H127.36c-17.664 0-32 14.336-32 32v533.312c0 17.664 14.336 32 32 32h729.824c16.544 0 31.136-12.608 31.936-29.152l40-800c0.768-15.392-11.456-28.192-26.848-28.192zM159.36 878.336V408.992h183.168v469.344H159.36z" :fill="post.liked ? '#f56c6c' : 'currentColor'"/></svg>
                    </el-icon>
                    {{ post.likeCount || 0 }}
                  </span>
                  <span class="action-item">
                    <el-icon class="icon"><ChatDotRound /></el-icon>
                    {{ post.commentCount || 0 }}
                  </span>
                  <span class="action-item" :class="{ 'is-active': post.collected }">
                    <el-icon class="icon"><Star /></el-icon>
                    {{ post.collectCount || 0 }}
                  </span>
                </div>
              </div>
            </div>

            <div class="load-more" v-if="likes.length < total">
              <el-button text @click="loadMore">加载更多</el-button>
            </div>
            <div class="load-more no-more" v-else-if="likes.length > 0">
              - 到底啦 -
            </div>
          </template>
        </el-skeleton>
      </el-tab-pane>

      <el-tab-pane label="评论" name="comments">
        <el-skeleton :loading="loading && pageNum === 1" animated :count="3">
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
            <el-empty v-if="comments.length === 0" description="暂无评论" />

            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="32" :src="comment.avatar || defaultAvatar" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-username">{{ comment.nickname || comment.username || '匿名车友' }}</span>
                  <span class="comment-time">{{ comment.createTime }}</span>
                  <el-button size="small" type="danger" @click="deleteComment(comment.id)" class="delete-btn">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </div>
                <div class="comment-text" v-if="comment.parentUsername">
                  <span class="reply-to">@{{ comment.parentUsername }}：</span>
                  {{ comment.content }}
                </div>
                <div class="comment-text" v-else>
                  {{ comment.content }}
                </div>
                <div class="comment-post" @click="goToPostDetail(comment.postId)">
                  查看帖子：{{ comment.postTitle }}
                </div>
              </div>
            </div>

            <div class="load-more" v-if="comments.length < total">
              <el-button text @click="loadMore">加载更多</el-button>
            </div>
            <div class="load-more no-more" v-else-if="comments.length > 0">
              - 到底啦 -
            </div>
          </template>
        </el-skeleton>
      </el-tab-pane>

      <el-tab-pane label="收藏" name="collections">
        <el-skeleton :loading="loading && pageNum === 1" animated :count="3">
          <template #template>
            <div class="post-card skeleton-card">
              <div style="display: flex; align-items: center; margin-bottom: 16px;">
                <el-skeleton-item variant="circle" style="width: 40px; height: 40px; margin-right: 12px;" />
                <el-skeleton-item variant="text" style="width: 120px;" />
              </div>
              <el-skeleton-item variant="h3" style="width: 60%; margin-bottom: 16px;" />
              <el-skeleton-item variant="image" style="width: 100%; height: 300px; margin-bottom: 16px;" />
            </div>
          </template>

          <template #default>
            <el-empty v-if="collections.length === 0" description="暂无收藏的帖子" />

            <div v-for="post in collections" :key="post.id" class="post-card">
              <div class="post-header">
                <div class="user-info">
                  <el-avatar :size="40" :src="post.avatar || defaultAvatar" />
                  <span class="username">{{ post.nickname || post.username || '匿名车友' }}</span>
                  <el-tag size="small" type="warning" effect="light" class="elite-tag">精华</el-tag>
                </div>
              </div>

              <div class="post-body" @click="goToPostDetail(post.id)">
                <h2 class="post-title">{{ post.title }}</h2>

                <div class="media-container" v-if="post.coverMedia">
                  <el-image
                    v-if="post.coverMedia.type === 'image'"
                    :src="post.coverMedia.url"
                    fit="contain"
                    class="post-cover"
                    lazy
                  />
                  <video
                    v-else-if="post.coverMedia.type === 'video'"
                    :src="post.coverMedia.url"
                    class="post-cover"
                    controls
                    controlslist="nodownload"
                    preload="metadata"
                  ></video>
                </div>

                <p class="post-snippet">{{ post.snippet }}</p>
              </div>

              <div class="post-footer">
                <span class="time">{{ post.createTime }}</span>
                <div class="actions">
                  <span class="action-item" :class="{ 'is-active': post.liked }">
                    <el-icon class="icon">
                      <svg viewBox="0 0 1024 1024" width="16" height="16"><path d="M857.28 344.992h-264.832c12.576-44.256 31.456-83.584 31.456-127.616 0-103.776-71.392-167.776-131.744-167.776-24.448 0-41.536 8.576-55.84 21.056-11.232 9.856-19.168 23.36-23.456 42.144L364.576 345.024H127.36c-17.664 0-32 14.336-32 32v533.312c0 17.664 14.336 32 32 32h729.824c16.544 0 31.136-12.608 31.936-29.152l40-800c0.768-15.392-11.456-28.192-26.848-28.192zM159.36 878.336V408.992h183.168v469.344H159.36z" :fill="post.liked ? '#f56c6c' : 'currentColor'"/></svg>
                    </el-icon>
                    {{ post.likeCount || 0 }}
                  </span>
                  <span class="action-item">
                    <el-icon class="icon"><ChatDotRound /></el-icon>
                    {{ post.commentCount || 0 }}
                  </span>
                  <span class="action-item" :class="{ 'is-active': post.collected }">
                    <el-icon class="icon"><Star /></el-icon>
                    {{ post.collectCount || 0 }}
                  </span>
                </div>
              </div>
            </div>

            <div class="load-more" v-if="collections.length < total">
              <el-button text @click="loadMore">加载更多</el-button>
            </div>
            <div class="load-more no-more" v-else-if="collections.length > 0">
              - 到底啦 -
            </div>
          </template>
        </el-skeleton>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
/* 核心容器 */
.user-profile-container {
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

/* 标签页 */
.profile-tabs {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* 帖子卡片样式 */
.post-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 头部：头像、昵称、标签、操作按钮 */
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

.post-actions {
  display: flex;
  gap: 8px;
}

.edit-btn {
  border-radius: 4px;
}

.delete-btn {
  border-radius: 4px;
}

/* 内容区 */
.post-body {
  cursor: pointer;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #111;
  line-height: 1.4;
  margin-bottom: 12px;
}

.media-container {
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

/* 修改图片/视频封面的样式，完美还原画框效果 */
.post-cover {
  width: 100%;
  height: 250px;
  object-fit: contain;
  display: block;
  border-radius: 8px;
  background-color: #f5f7fa;
}

/* 缩略文本摘要样式 */
.post-snippet {
  font-size: 15px;
  color: #555;
  line-height: 1.6;
  margin-top: 12px;
  margin-bottom: 16px;
  /* 限制最多显示两行文字 */
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 底部：时间和操作 */
.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 14px;
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

/* 评论样式 */
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
  align-items: center;
  margin-bottom: 8px;
}

.comment-username {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-right: 10px;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-right: 10px;
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

.comment-post {
  font-size: 12px;
  color: #007aff;
  cursor: pointer;
}

.comment-post:hover {
  text-decoration: underline;
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

/* 限制富文本中的图片和视频宽度 */
.post-content :deep(img),
.post-content :deep(video),
.post-content :deep(iframe) {
  max-width: 100%;
  border-radius: 8px;
  margin: 10px 0;
}
</style>
