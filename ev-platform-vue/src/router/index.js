import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import VehicleDetailView from '../views/VehicleDetailView.vue'
import VehicleLibraryView from '../views/VehicleLibraryView.vue'
import VehicleIntentView from '../views/VehicleIntentView.vue'
import VehicleSearchView from '../views/VehicleSearchView.vue'
import MyAppointmentsView from '../views/MyAppointmentsView.vue' // 引入我的预约记录页面
import CommunityView from '../views/CommunityView.vue'


// 引入后台管理的组件
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminVehicle from '../views/admin/AdminVehicle.vue'
import AdminIntent from '../views/admin/AdminIntent.vue'

// 🚀 核心改动 1：引入两个物理隔离的用户管理页面
import AdminUserSuper from '../views/admin/AdminUserSuper.vue'
import AdminUserNormal from '../views/admin/AdminUserNormal.vue'
import AdminVehicleStats from '../views/admin/AdminVehicleStats.vue' // 引入车辆数据统计页面
import AdminIntentStats from '../views/admin/AdminIntentStats.vue' // 引入用户线索(订单)统计页面
import AdminUserStats from '../views/admin/AdminUserStats.vue' // 引入用户统计页面

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/vehicle/:id', name: 'vehicleDetail', component: VehicleDetailView },
    { path: '/library', name: 'library', component: VehicleLibraryView },
    { path: '/intent/:id', name: 'vehicleIntent', component: VehicleIntentView },
    { path: '/search', name: 'vehicleSearch', component: VehicleSearchView },
    { path: '/my-appointments', name: 'myAppointments', component: MyAppointmentsView }, // 我的预约记录
    { path: '/community', name: 'community', component: CommunityView },
    { path: '/community/create', name: 'postCreate', component: () => import('../views/PostCreateView.vue') },
    { path: '/community/edit/:id', name: 'postEdit', component: () => import('../views/PostCreateView.vue') },
    { path: '/post/:id', name: 'postDetail', component: () => import('../views/PostDetailView.vue') },
    { path: '/profile', name: 'userProfile', component: () => import('../views/UserProfileView.vue') },
    { path: '/personal', name: 'personalCenter', component: () => import('../views/PersonalCenterView.vue') },
    { path: '/community/edit/:id', name: 'postEdit', component: () => import('../views/PostCreateView.vue') },

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
          path: 'vehicle-stats',
          name: 'adminVehicleStats',
          component: AdminVehicleStats
        },
        {
          path: 'intent',
          name: 'adminIntent',
          component: AdminIntent
        },
        {
          path: 'intent-stats',
          name: 'adminIntentStats',
          component: AdminIntentStats
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
            if (currentRole === 0) {
              next('/admin/user-super') // 超级管理员去专属页面
            } else if (currentRole === 2) {
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
        },
        {
          path: 'user-stats',
          name: 'adminUserStats',
          component: AdminUserStats
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userInfoStr = localStorage.getItem('userInfo');
  let role = null;

  if (userInfoStr) {
    try {
      const parsedInfo = JSON.parse(userInfoStr);
      if (typeof parsedInfo.role !== 'undefined' && parsedInfo.role !== null) {
        role = Number(parsedInfo.role);
      }
    } catch (e) {
      console.error("解析用户信息失败", e);
    }
  }

  const isAdminRole = (role === 0 || role === 2);
  const isCsidePath = !to.path.startsWith('/admin');

  // Case 1: Non-admin trying to access admin paths
  // Includes unauthenticated users (role is null) or normal users (role is 1)
  if (!isAdminRole && !isCsidePath) {
    // If they are trying to go to an admin path without admin privileges
    return next('/'); // Redirect to C-side home
  }

  // Case 2: Admin users (role 0 or 2) landing on the C-side home page ('/')
  // This is primarily for post-login redirects or if they manually navigate to '/'.
  // If an admin user is trying to access the C-side home, redirect them to admin dashboard.
  if (isAdminRole && to.path === '/') {
    return next('/admin/vehicle'); // Redirect admin roles to admin dashboard
  }

  // All other scenarios: allow navigation
  next();
});

export default router
