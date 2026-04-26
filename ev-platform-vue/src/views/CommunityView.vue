<script setup>
import { ref, onMounted } from 'vue'
import { ChatDotRound, MagicStick, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()

const posts = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await request.get('/post/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    if (res.code === 200) {
      const processedRecords = res.data.records.map(post => {
        return {
          ...post,
          coverMedia: getCoverMedia(post.content),
          snippet: getSnippet(post.content)
        }
      })

      if (pageNum.value === 1) {
        posts.value = processedRecords
      } else {
        posts.value = [...posts.value, ...processedRecords]
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

const toggleFollow = async (post) => {
  try {
    const isFollowed = !post.isFollowed
    const res = await request.post(`/user/follow/toggle/${post.userId}?isFollowed=${isFollowed}`)
    if (res.code === 200) {
      ElMessage.success(isFollowed ? '关注成功' : '取消关注成功')
      post.isFollowed = isFollowed
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

const toggleLike = async (post) => {
  console.log('toggleLike called', post.id)
  try {
    const isLiking = !post.liked
    const res = await request.post('/post/like/toggle', {
      postId: post.id,
      isLiked: isLiking
    })
    if (res.code === 200) {
      post.liked = isLiking
      post.likeCount += isLiking ? 1 : -1
    } else if (res.code === 401) {
      ElMessage.warning('请先登录')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const toggleCollect = async (post) => {
  try {
    const isCollecting = !post.collected
    const res = await request.post('/post/collection/toggle', {
      postId: post.id,
      isCollected: isCollecting
    })
    if (res.code === 200) {
      ElMessage.success(isCollecting ? '收藏成功' : '取消收藏成功')
      post.collected = isCollecting
      post.collectCount += isCollecting ? 1 : -1
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

const goToPostDetail = (postId) => {
  console.log('goToPostDetail called', postId)
  router.push(`/post/${postId}`)
}

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

const getCoverMedia = (html) => {
  if (!html) return null
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')

  const img = doc.querySelector('img')
  if (img && img.getAttribute('src')) {
    return { type: 'image', url: img.getAttribute('src') }
  }

  const video = doc.querySelector('video')
  if (video) {
    const source = video.querySelector('source')
    const videoUrl = video.getAttribute('src') || (source ? source.getAttribute('src') : null)
    if (videoUrl) {
      return { type: 'video', url: videoUrl }
    }
  }

  return null
}

onMounted(() => {
  fetchPosts()
})
</script>

<template>
  <div class="community-container">
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
        <el-empty v-if="posts.length === 0" description="暂无社区帖子，快来发布第一篇吧！" />

        <div v-for="post in posts" :key="post.id" class="post-card">
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
              @click="toggleFollow(post)"
            >
              {{ post.isFollowed ? '已关注' : '+ 关注' }}
            </el-button>
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
              <span class="action-item" :class="{ 'is-active': post.liked }" @click="toggleLike(post)">
                <el-icon class="icon">
                  <svg viewBox="0 0 1024 1024" width="16" height="16"><path d="M857.28 344.992h-264.832c12.576-44.256 31.456-83.584 31.456-127.616 0-103.776-71.392-167.776-131.744-167.776-24.448 0-41.536 8.576-55.84 21.056-11.232 9.856-19.168 23.36-23.456 42.144L364.576 345.024H127.36c-17.664 0-32 14.336-32 32v533.312c0 17.664 14.336 32 32 32h729.824c16.544 0 31.136-12.608 31.936-29.152l40-800c0.768-15.392-11.456-28.192-26.848-28.192zM159.36 878.336V408.992h183.168v469.344H159.36z" :fill="post.liked ? '#f56c6c' : 'currentColor'"/></svg>
                </el-icon>
                {{ post.likeCount || 0 }}
              </span>
              <span class="action-item" @click.stop="goToPostDetail(post.id)">
                <el-icon class="icon"><ChatDotRound /></el-icon>
                {{ post.commentCount || 0 }}
              </span>
              <span class="action-item" :class="{ 'is-active': post.collected }" @click.stop="toggleCollect(post)">
                <el-icon class="icon"><Star /></el-icon>
                {{ post.collectCount || 0 }}
              </span>
            </div>
          </div>
        </div>

        <div class="load-more" v-if="posts.length < total">
          <el-button text @click="pageNum++; fetchPosts()">加载更多</el-button>
        </div>
        <div class="load-more no-more" v-else-if="posts.length > 0">
          - 到底啦 -
        </div>
      </template>
    </el-skeleton>

    <el-tooltip content="发布帖子" placement="left">
      <div class="fab-btn" @click="router.push('/community/create')">
        <el-icon :size="24" color="#fff"><MagicStick /></el-icon>
      </div>
    </el-tooltip>
  </div>
</template>

<style scoped>
.community-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0 60px 0;
}

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
  display: block;
}
.post-cover {
  width: 100%;
  max-height: 500px;
  object-fit: cover;
  display: block;
  border-radius: 8px;
  background-color: #fff;
}

video.post-cover {
  object-fit: cover;
  background-color: #fff;
}

.post-snippet {
  font-size: 15px;
  color: #555;
  line-height: 1.6;
  margin-top: 12px;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

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

.load-more {
  text-align: center;
  padding: 20px 0;
}
.no-more {
  color: #999;
  font-size: 14px;
}

/* 右上角悬浮按钮 */
.fab-btn {
  position: fixed;
  right: 40px;
  top: 80px;
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background-color: #007aff;
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s;
  z-index: 99;
}
.fab-btn:hover {
  transform: translateY(-3px);
}

.post-content :deep(img),
.post-content :deep(video),
.post-content :deep(iframe) {
  max-width: 100%;
  border-radius: 8px;
  margin: 10px 0;
}

</style>