<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Van, List, User } from '@element-plus/icons-vue' // 🚀 追加引入 User 图标

const router = useRouter()
const route = useRoute()
const adminName = ref('管理员')

// 解析本地存储的用户信息
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

// 动态计算展示的名字：如果是 role=0，显示“超级管理员”，否则显示用户名
const displayName = ref(userInfo.role === 0 ? '超级管理员' : userInfo.username)

// 退出登录
const handleLogout = () => {
  // 1. 清理全部本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('username') // 补上遗漏的 username

  // 2. 🚀 核心修改：不用 router.push，改用原生跳转！
  // 强制刷新页面并回到首页，这样 App.vue 的状态会彻底重置，右上角绝对显示“登录”按钮
  window.location.href = '/'
}

onMounted(() => {
  // 从本地存储获取用户信息
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const user = JSON.parse(userInfoStr)
    adminName.value = user.nickname || user.username
    // 安全校验：如果不是管理员(role !== 0)，强制踢回前台
    if (user.role !== 0) {
      router.push('/')
    }
  } else {
    router.push('/')
  }
})
</script>

<template>
  <el-container class="admin-container">
    <el-aside width="220px" class="admin-aside">
      <div class="logo-area">
        <img src="/logo.png" alt="logo" class="logo-img" onerror="this.style.display='none'" />
        <span>管理后台</span>
      </div>
      
      <el-menu
        :default-active="route.path"
        router
        class="admin-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/vehicle">
          <el-icon><Van /></el-icon>
          <span>车辆数据管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/intent">
          <el-icon><List /></el-icon>
          <span>用户线索(订单)</span>
        </el-menu-item>

        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户权限管理</span>
        </el-menu-item>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-breadcrumb">
          <h3>后台控制台</h3>
        </div>
        <div class="header-right">
        <span class="welcome-text">欢迎您，{{ displayName }}</span>
        <el-button type="danger" link @click="handleLogout">退出登录</el-button>
      </div>
      </el-header>
      
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-container {
  height: 100vh; /* 撑满整个屏幕高度 */
  width: 100vw;
}

.admin-aside {
  background-color: #304156;
  display: flex;
  flex-direction: column;
}
.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3643;
}
.logo-img {
  height: 30px;
  margin-right: 10px;
  border-radius: 4px;
}
.admin-menu {
  border-right: none;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>