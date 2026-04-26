<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { Search, User, Lock, SwitchButton, ArrowDown, Bell } from '@element-plus/icons-vue'


import { ElMessage, ElBadge } from 'element-plus'
import request from './utils/request'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isCside = computed(() => !route.path.startsWith('/admin'))

// --- 搜索功能 ---
const searchKeyword = ref('')
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { keyword: searchKeyword.value.trim() }
    })
  }
}

// --- 登录状态管理 ---
const isLoggedIn = ref(false)
const currentUsername = ref('')
const currentNickname = ref('')
const unreadMessageCount = ref(0)

const getUnreadCount = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await request.get('/message/unread/count')
    if (res.code === 200) {
      unreadMessageCount.value = res.data || 0
    }
  } catch (e) {
    console.warn('获取未读消息数失败', e)
  }
}

onMounted(() => {
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    const nickname = localStorage.getItem('nickname')
    if (token && username) {
      isLoggedIn.value = true
      currentUsername.value = username
      currentNickname.value = nickname || username
      getUnreadCount()
    }
    updateBodyClass()
  })

watch(isCside, () => {
  updateBodyClass()
})

const updateBodyClass = () => {
  if (isCside.value) {
    document.body.classList.remove('admin-page')
    document.body.classList.add('c-side-page')
  } else {
    document.body.classList.remove('c-side-page')
    document.body.classList.add('admin-page')
  }
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('nickname')
  isLoggedIn.value = false
  currentUsername.value = ''
  currentNickname.value = ''
  unreadMessageCount.value = 0
  ElMessage.success('已安全退出')
}

// 预约记录功能：检查登录状态并跳转
const navigateToAppointmentRecords = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录才能查看预约记录！')
    loginDialogVisible.value = true
  } else {
    router.push('/my-appointments')
  }
}

// 消息通知功能：检查登录状态并跳转
const navigateToMessages = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录才能查看消息！')
    loginDialogVisible.value = true
  } else {
    router.push('/personal?tab=message')
  }
}

// 创作中心功能：检查登录状态并跳转
const navigateToProfile = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录才能进入创作中心！')
    loginDialogVisible.value = true
  } else {
    router.push('/profile')
  }
}

// 个人中心功能：检查登录状态并跳转
const navigateToPersonal = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录才能进入个人中心！')
    loginDialogVisible.value = true
  } else {
    router.push('/personal')
  }
}

// --- 登录/注册弹窗控制 ---
const loginDialogVisible = ref(false)
const activeTab = ref('login')

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', nickname: '', confirmPassword: '' })

// 🚀 登录请求
const handleLoginSubmit = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  try {
    const res = await request.post('/user/login', loginForm)

    // ✅ 核心修复：正确解析后端返回的 Result 结构 (判断 res.code 并从 res.data 中取值)
    if (res && res.code === 200 && res.data) {
      ElMessage.success('登录成功！')

      const loginData = res.data // 拿到真正的 Map 数据
      const token = loginData.token
      const nickname = loginData.nickname
      const userRole = loginData.role !== undefined && loginData.role !== null ? loginData.role : 1

      // 存储到 localStorage
      localStorage.setItem('token', token)
      localStorage.setItem('username', loginData.username)
      // 如果没有昵称，则默认使用账号名兜底
      localStorage.setItem('nickname', nickname || loginData.username)
      localStorage.setItem('role', userRole)

      // 保存用户信息到本地存储
      const userInfo = {
        id: null,
        username: loginData.username,
        nickname: nickname || loginData.username, // 顺便把昵称也存进去
        role: userRole
      }
      localStorage.setItem('userInfo', JSON.stringify(userInfo))

      isLoggedIn.value = true
      currentUsername.value = loginData.username
      currentNickname.value = nickname || loginData.username
      loginDialogVisible.value = false

      getUnreadCount()

      setTimeout(() => {
        if (userRole == 0 || userRole == 2) {
          router.push('/admin')
        } else {
          window.location.reload()
        }
      }, 50)

    } else {
      ElMessage.error(res.msg || '登录失败，请检查账号密码')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络异常，请稍后再试')
  }
}

// 🚀 注册请求
const handleRegisterSubmit = async () => {
  if (!registerForm.username || !registerForm.password || !registerForm.nickname || !registerForm.confirmPassword) {
    ElMessage.warning('请完整填写注册信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  try {
    const res = await request.post('/user/register', {
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname
    })

    // ✅ 核心修复：通过 code === 200 来判断是否成功，而不是死磕 msg 字符串
    if (res && res.code === 200) {
      ElMessage.success('注册成功，请登录！')

      activeTab.value = 'login'
      loginForm.username = registerForm.username
      loginForm.password = registerForm.password

      // 清空注册表单
      registerForm.username = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
      registerForm.nickname = ''
    } else {
      // 如果后端返回了错误信息（比如用户名已存在），就展示后端的报错
      ElMessage.error(res.msg || '注册失败，请稍后重试')
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

        <div class="nav-left">
          <router-link to="/">
            <img src="/logo.png" alt="好电选Logo" class="logo" />
          </router-link>
          <div class="nav-left-items">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/library" class="nav-item">选车</router-link>
            <router-link to="/community" class="nav-item">社区</router-link>
          </div>
        </div>

        <div class="nav-center">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索新能源汽车"
            :prefix-icon="Search"
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="nav-right-items">
          <a href="javascript:void(0);" class="nav-item" @click="navigateToProfile">创作中心</a>
          <a href="javascript:void(0);" class="nav-item" @click="navigateToPersonal">个人中心</a>
        </div>

        <div class="nav-right">
          <template v-if="!isLoggedIn">
            <el-button type="primary" class="login-btn" @click="loginDialogVisible = true">登录</el-button>
          </template>

          <template v-else>
            <el-badge :value="unreadMessageCount" :hidden="unreadMessageCount === 0" :max="99">
              <el-icon :size="22" style="cursor: pointer; color: #666;" @click="navigateToMessages"><Bell /></el-icon>
            </el-badge>
            <span class="user-info" style="color: #666; font-size: 14px; margin-left: 15px; margin-right: 8px;">
              欢迎，{{ currentNickname }}
            </span>
            <a href="javascript:void(0);" style="color: #ff4d4f; font-size: 14px; cursor: pointer; text-decoration: none;" @click="handleLogout">退出</a>
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
              <el-input v-model="loginForm.username" placeholder="请输入账号名" :prefix-icon="User" />
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
              <el-input v-model="registerForm.username" placeholder="请设置账号名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.nickname" placeholder="请设置昵称" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.password" type="password" placeholder="请设置密码" show-password :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password :prefix-icon="Lock" />
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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  background-color: #f5f7fa;
}

body.admin-page #dify-chatbot-bubble-button,
body.admin-page #dify-chatbot-bubble-window {
  display: none !important;
}

.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  width: 100%;
  height: 64px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 24px;
}
.logo {
  height: 42px;
  width: auto;
  transition: transform 0.3s ease;
}
.logo:hover {
  transform: scale(1.02);
}

.nav-left-items {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-center {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: center;
  margin-right: 24px;
  margin-left: 24px;
}

.search-input {
  width: 240px;
}

.nav-right-items {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-right: 24px;
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

.nav-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.login-btn {
  width: 80px;
}

.main-content {
  flex: 1;
  padding-top: 20px;
}
</style>
