<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from './utils/request' // 引入我们封装的网络请求工具
import { useRouter, useRoute } from 'vue-router'

const router = useRouter() // 2. 实例化路由
const route = useRoute() // 实例化 route，用于在 template 中判断当前路径


// --- 搜索功能 ---
const searchKeyword = ref('')
const handleSearch = () => {
  // 如果输入框不为空，则跳转到搜索页，并通过 URL 的 query 携带关键词
  if (searchKeyword.value.trim()) {
    router.push({ 
      path: '/search', 
      query: { keyword: searchKeyword.value.trim() } 
    })
  }
}

// --- 登录状态管理 ---
const isLoggedIn = ref(false) // 是否已登录
const currentUsername = ref('') // 当前登录的用户名

// 页面刚加载时，检查本地有没有存 token
onMounted(() => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  if (token && username) {
    isLoggedIn.value = true
    currentUsername.value = username
  }
})

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  isLoggedIn.value = false
  currentUsername.value = ''
  ElMessage.success('已安全退出')
}

// --- 登录/注册弹窗控制 ---
const loginDialogVisible = ref(false)
const activeTab = ref('login')

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '' })

// 🚀 终极版：使用纯 Vue Router 解决时序问题
const handleLoginSubmit = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  try {
    const res = await request.post('/user/login', loginForm)
    if (res.code === 200) {
      ElMessage.success('登录成功！')
      
      const token = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('username', loginForm.username)
      
      let userRole = 1 // 默认为普通用户
      try {
        const payload = JSON.parse(window.atob(token.split('.')[1]))
        // 确保数据先写入本地存储
        localStorage.setItem('userInfo', JSON.stringify(payload))
        
        if (payload.role !== undefined && payload.role !== null) {
          userRole = Number(payload.role) 
        }
      } catch (e) {
        console.warn('Token 解析失败')
      }
      
      // 更新前台的响应式状态，先让弹窗关闭
      isLoggedIn.value = true
      currentUsername.value = loginForm.username
      loginDialogVisible.value = false
      
      // 🚀 核心修复：使用 Vue Router 的 push，并利用 setTimeout 确保 localStorage 已落盘
      setTimeout(() => {
        if (userRole === 0 || userRole === 2) {
          // 管理员：通过 router 跳转到后台
          router.push('/admin') 
        } else {
          // 普通用户：如果当前在不需要登录的页面，就不刷新，体验更好
          // 如果强制刷新，使用 reload
          window.location.reload() 
        }
      }, 50) // 延迟 50 毫秒，给 localStorage 和响应式状态一点时间
      
    } else {
      ElMessage.error(res.msg || '登录失败，请检查账号密码')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络异常，请稍后再试')
  }
}

// 🚀 真实的注册请求
const handleRegisterSubmit = async () => {
  if (!registerForm.username || !registerForm.password) {
    ElMessage.warning('请完整填写注册信息')
    return
  }
  try {
    // 发送 POST 请求到后端的 /user/register
    const res = await request.post('/user/register', registerForm)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录！')
      
      // 注册成功后，自动跳转到“登录”Tab，并帮用户填好刚注册的账号密码
      activeTab.value = 'login'
      loginForm.username = registerForm.username
      loginForm.password = registerForm.password
      
      // 清空注册表单
      registerForm.username = ''
      registerForm.password = ''
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络异常，请稍后再试')
  }
}
</script>



<template>
  <div class="app-wrapper">
    <header class="navbar" v-if="!route.path.startsWith('/admin')">
      <div class="nav-container">
        
        <router-link to="/" class="nav-left">
          <img src="/logo.png" alt="好电选Logo" class="logo" />
        </router-link>

        <div class="nav-center">
          <router-link to="/" class="nav-item">内容</router-link>
          <a href="#" class="nav-item">视频</a>
          
          <router-link to="/library" class="nav-item">选车</router-link>
          
          <a href="#" class="nav-item">补贴查询</a>
          <a href="#" class="nav-item">社区</a>
        </div>

        <div class="nav-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索新能源汽车"
            :prefix-icon="Search"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          
          <template v-if="!isLoggedIn">
            <el-button type="primary" class="login-btn" @click="loginDialogVisible = true">登录</el-button>
          </template>
          
          <template v-else>
            <span style="color: #666; font-size: 14px;">欢迎，{{ currentUsername }}</span>
            <el-button type="danger" plain size="small" :icon="SwitchButton" @click="handleLogout">退出</el-button>
          </template>
        </div>

      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <el-dialog
      v-model="loginDialogVisible"
      title="欢迎来到好电选"
      width="400px"
      center
      destroy-on-close
    >
      <el-tabs v-model="activeTab" class="auth-tabs" stretch>
        
        <el-tab-pane label="账号登录" name="login">
          <el-form :model="loginForm" size="large">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 100%;" @click="handleLoginSubmit">登 录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="新用户注册" name="register">
          <el-form :model="registerForm" size="large">
            <el-form-item>
              <el-input v-model="registerForm.username" placeholder="请设置用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.password" type="password" placeholder="请设置密码" show-password :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" style="width: 100%;" @click="handleRegisterSubmit">注 册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
      </el-tabs>
    </el-dialog>
  </div>
</template>

<style>
/* ====== 全局基础样式 ====== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  /* 给整个网页加一点极浅的灰色背景，让白色的导航栏和表格更突出 */
  background-color: #f5f7fa; 
}

.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ====== 顶部导航栏样式 ====== */
.navbar {
  width: 100%;
  height: 64px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); /* 底部阴影依然拉满全屏宽度 */
  position: sticky; 
  top: 0;
  z-index: 100;
}

/* 核心限制容器：宽度 1200px 且居中，与下方内容完美对齐 */
.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 左侧 Logo 区 */
.nav-left {
  display: flex;
  align-items: center;
  text-decoration: none;
  cursor: pointer;
}
.logo {
  height: 42px; /* 高度调大一点，突出品牌 */
  width: auto;
  transition: transform 0.3s ease;
}
.logo:hover {
  transform: scale(1.02); /* 鼠标悬浮Logo轻微放大 */
}

/* 中间导航链接 */
.nav-center {
  display: flex;
  gap: 32px;
}
.nav-item {
  text-decoration: none;
  color: #333;
  font-size: 16px;
  font-weight: 500;
  transition: color 0.3s; 
}
.nav-item:hover {
  color: #409EFF; 
}

/* 右侧操作区 */
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.search-input {
  width: 240px;
}
.login-btn {
  width: 80px;
}

/* ====== 主体内容区 ====== */
.main-content {
  flex: 1;
  padding-top: 20px;
}
</style>