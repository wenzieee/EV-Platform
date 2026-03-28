import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import VehicleDetailView from '../views/VehicleDetailView.vue'
import VehicleLibraryView from '../views/VehicleLibraryView.vue'
import VehicleIntentView from '../views/VehicleIntentView.vue'
import VehicleSearchView from '../views/VehicleSearchView.vue' 

// 引入后台管理的组件
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminVehicle from '../views/admin/AdminVehicle.vue'
import AdminIntent from '../views/admin/AdminIntent.vue'

// 🚀 核心改动 1：引入两个物理隔离的用户管理页面
import AdminUserSuper from '../views/admin/AdminUserSuper.vue'
import AdminUserNormal from '../views/admin/AdminUserNormal.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/vehicle/:id', name: 'vehicleDetail', component: VehicleDetailView },
    { path: '/library', name: 'library', component: VehicleLibraryView },
    { path: '/intent/:id', name: 'vehicleIntent', component: VehicleIntentView },
    { path: '/search', name: 'vehicleSearch', component: VehicleSearchView },

    // 后台管理路由
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/vehicle', // 默认重定向到车辆管理
      children: [
        {
          path: 'vehicle',
          name: 'adminVehicle',
          component: AdminVehicle
        },
        {
          path: 'intent',
          name: 'adminIntent',
          component: AdminIntent
        },
        // 🚀 核心改动 2：路由分流枢纽
        {
          path: 'user',
          name: 'adminUser',
          // 当侧边栏点击 /admin/user 时，这个独享守卫会根据角色自动弹射到对应的物理页面
          beforeEnter: (to, from, next) => {
            const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
            // 🚀 核心修复：强制转为数字
            const currentRole = Number(userInfo.role)
            if (userInfo.role === 0) {
              next('/admin/user-super') // 超级管理员去专属页面
            } else if (userInfo.role === 2) {
              next('/admin/user-normal') // 普通管理员去限制页面
            } else {
              next('/') // 其他情况踢回首页
            }
          }
        },
        // 注册两个真实的物理路由
        {
          path: 'user-super',
          name: 'adminUserSuper',
          component: AdminUserSuper
        },
        {
          path: 'user-normal',
          name: 'adminUserNormal',
          component: AdminUserNormal
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  if (!to.path.startsWith('/admin')) {
    return next()
  }

  const userInfoStr = localStorage.getItem('userInfo')
  let role = null
  
  if (userInfoStr) {
    try {
      // 🚀 核心修复：强制转为数字
      const parsedRole = JSON.parse(userInfoStr).role
      if (parsedRole !== undefined && parsedRole !== null) {
        role = Number(parsedRole)
      }
    } catch (e) {
      console.error("解析用户信息失败")
    }
  }

  // 拦截逻辑
  if (role === null || role === 1) {
    return next('/')
  }

  next()
})

export default router