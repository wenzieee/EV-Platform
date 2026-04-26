<script setup>
import { reactive, shallowRef, onBeforeUnmount, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useRouter, useRoute } from 'vue-router'

// 引入 wangEditor 相关样式和组件
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

// 判断当前是不是编辑模式
const postId = route.params.id
const isEditMode = ref(!!postId)

// 表单数据绑定
const postForm = reactive({
  title: '',
  content: '',
  mediaType: 1
})

// === 富文本编辑器核心配置 ===
const editorRef = shallowRef()
const mode = 'default'

// 工具栏配置：解除 video 的封印，只排除全屏功能
const toolbarConfig = {
  excludeKeys: ['fullScreen']
}

// 编辑器配置：接管图片和视频的上传逻辑
const editorConfig = {
  placeholder: '分享你的用车生活，支持直接粘贴图片或点击图标插入视频哦...',
  MENU_CONF: {
    // 1. 图片上传拦截逻辑
    uploadImage: {
      async customUpload(file, insertFn) {
        const formData = new FormData()
        formData.append('file', file)
        try {
          const res = await request.post('/upload/image', formData)
          if (res.code === 200) {
            // 上传成功后，将后端返回的真实图片 URL 插入到光标位置
            insertFn(res.data)
          } else {
            ElMessage.error(res.msg || '图片上传失败')
          }
        } catch (error) {
          console.error(error)
          ElMessage.error('网络异常，上传失败')
        }
      }
    },

    // 2. 视频上传拦截逻辑
    uploadVideo: {
      async customUpload(file, insertFn) {
        // 限制上传视频的大小（例如不能超过 50MB）
        if (file.size > 50 * 1024 * 1024) {
          return ElMessage.warning('视频大小不能超过 50MB')
        }

        const formData = new FormData()
        formData.append('file', file)
        try {
          const res = await request.post('/upload/video', formData)
          if (res.code === 200) {
            // 上传成功后，将后端返回的真实视频 URL 插入到光标位置
            // 第二个参数是视频的海报封面图(可选)，留空即可
            insertFn(res.data, '')
          } else {
            ElMessage.error(res.msg || '视频上传失败')
          }
        } catch (error) {
          console.error(error)
          ElMessage.error('网络异常，上传失败')
        }
      }
    }
  }
}

// 组件销毁时，一定要销毁编辑器防止内存泄漏
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

// 记录编辑器实例
const handleCreated = (editor) => {
  editorRef.value = editor
}
// === 编辑器配置结束 ===
// 如果是编辑模式，先获取原帖子数据
onMounted(async () => {
  if (isEditMode.value) {
    loading.value = true
    try {
      const res = await request.get(`/post/${postId}`)
      if (res.code === 200) {
        postForm.title = res.data.title
        // 赋值给 content 后，wangEditor 会自动渲染出图文
        postForm.content = res.data.content
      }
    } catch (err) {
      ElMessage.error('获取帖子详情失败')
    } finally {
      loading.value = false
    }
  }
})

// 提交帖子到后端 (智能区分新增或更新)
const handleSubmit = async () => {
  if (postForm.content === '<p><br></p>' || !postForm.content.trim()) {
    return ElMessage.warning('说点什么吧，正文不能为空哦')
  }

  loading.value = true
  try {
    let res;
    if (isEditMode.value) {
      // 编辑模式，调用 PUT 接口，并带上 ID
      res = await request.put('/post', { id: postId, ...postForm })
    } else {
      // 新增模式，调用 POST 接口
      res = await request.post('/post', postForm)
    }

    if (res.code === 200) {
      ElMessage.success(isEditMode.value ? '修改成功！' : '发布成功！')
      router.back() // 成功后返回上一页
    } else if (res.code === 401) {
      ElMessage.warning('登录状态已失效，请重新登录')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (err) {
    console.error('发布异常', err)
    ElMessage.error('网络错误，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="create-post-container">
    <div class="create-card">
      <div class="header">
        <el-button :icon="Back" circle @click="router.back()" />
        <span class="page-title">发布帖子</span>
        <el-button type="primary" :loading="loading" @click="handleSubmit" round>
          发布
        </el-button>
      </div>

      <div class="editor-area">
        <el-input
          v-model="postForm.title"
          placeholder="起个吸睛的标题吧 (选填)"
          maxlength="100"
          class="title-input"
        />

        <el-divider />

        <div class="editor-wrapper">
          <Toolbar
            class="toolbar"
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            :mode="mode"
          />
          <Editor
            class="editor-content"
            v-model="postForm.content"
            :defaultConfig="editorConfig"
            :mode="mode"
            @onCreated="handleCreated"
          />
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* 核心容器 */
.create-post-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

/* 白色卡片背景 */
.create-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}

/* 头部样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
}

/* 标题输入框无边框化处理 */
.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  font-size: 20px;
  font-weight: bold;
  padding: 0;
}

/* 编辑器外层边框包裹 */
.editor-wrapper {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  z-index: 100;
  transition: border-color 0.2s;
}
.editor-wrapper:hover {
  border-color: #c0c4cc;
}
.editor-wrapper:focus-within {
  border-color: #409eff;
}

/* 工具栏样式 */
.toolbar {
  border-bottom: 1px solid #e4e7ed;
  background-color: #fafafa;
  position: sticky;
  top: 64px;
  z-index: 101;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

/* 编辑器内部高度控制 */
.editor-content {
  min-height: 450px;
}
</style>
