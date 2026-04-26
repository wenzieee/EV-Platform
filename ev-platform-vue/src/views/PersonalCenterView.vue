<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Calendar, Star, Van, MapLocation, Phone, Message, ArrowLeft, Bell, Camera, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElTabs, ElTabPane, ElButton, ElSkeleton, ElEmpty, ElTable, ElTableColumn, ElTag, ElPagination, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeTab = ref('appointments')
const appointments = ref([])
const favoriteVehicles = ref([])
const messages = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryParams = ref({ current: 1, size: 10 })

// ====== 账号设置相关状态 ======
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const currentAvatar = ref(localStorage.getItem('avatar') || defaultAvatar)
const saving = ref(false)

const settingsForm = reactive({
  nickname: localStorage.getItem('nickname') || '',
  currentPassword: '',
  password: '',
  confirmPassword: ''
})

// ====== 现有数据获取方法 ======
const fetchMyAppointments = async () => {
  loading.value = true
  try {
    const res = await request.post('/intent/my-records', queryParams.value)
    if (res.code === 200) {
      appointments.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取预约记录失败')
    }
  } catch (error) {
    console.error('获取预约记录失败:', error)
    ElMessage.error('网络异常，请稍后再试')
  } finally {
    loading.value = false
  }
}

const fetchFavoriteVehicles = async () => {
  loading.value = true
  try {
    const res = await request.get('/favorite/list')
    if (res.code === 200) {
      favoriteVehicles.value = res.data
    } else {
      ElMessage.error(res.msg || '获取收藏车辆失败')
    }
  } catch (error) {
    console.error('获取收藏车辆失败:', error)
    ElMessage.error('网络异常，请稍后再试')
  } finally {
    loading.value = false
  }
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const res = await request.get('/message/my')
    if (res.code === 200) {
      messages.value = res.data
    } else {
      ElMessage.error(res.msg || '获取消息失败')
    }
  } catch (error) {
    console.error('获取消息失败:', error)
    ElMessage.error('网络异常，请稍后再试')
  } finally {
    loading.value = false
  }
}

// ====== 账号设置方法 ======
// 1. 头像上传
const handleAvatarUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const uploadRes = await request.post('/upload/image', formData)
    if (uploadRes.code === 200) {
      const avatarUrl = uploadRes.data
      const updateRes = await request.post('/user/updateAvatar', { avatar: avatarUrl })
      if (updateRes.code === 200) {
        currentAvatar.value = avatarUrl
        localStorage.setItem('avatar', avatarUrl)
        ElMessage.success('头像更新成功！')
        setTimeout(() => { window.location.reload() }, 1000)
      } else {
        ElMessage.error(updateRes.msg || '头像保存失败')
      }
    } else {
      ElMessage.error(uploadRes.msg || '图片上传失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('上传异常，请检查网络')
  }
}

// 2. 提交修改信息
const submitSettings = async () => {
  if (settingsForm.password && settingsForm.password !== settingsForm.confirmPassword) {
    return ElMessage.warning('两次输入的新密码不一致！')
  }

  if (settingsForm.password && !settingsForm.currentPassword) {
    return ElMessage.warning('修改密码时请输入当前密码！')
  }

  saving.value = true
  try {
    const res = await request.post('/user/updateProfile', {
      nickname: settingsForm.nickname,
      currentPassword: settingsForm.currentPassword,
      password: settingsForm.password
    })

    if (res.code === 200) {
      ElMessage.success('个人信息修改成功！')
      localStorage.setItem('nickname', settingsForm.nickname)

      if (settingsForm.password) {
        ElMessage.success('密码已修改，请重新登录')
        localStorage.removeItem('token')
        setTimeout(() => {
          window.location.href = '/'
        }, 1500)
      } else {
        setTimeout(() => { window.location.reload() }, 500)
      }
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络异常，请稍后再试')
  } finally {
    saving.value = false
  }
}

// ====== 辅助方法 ======
const handleMessageClick = async (message) => {
  if (message.isRead === 0) {
    try {
      const res = await request.put(`/message/read/${message.id}`)
      if (res.code === 200) {
        message.isRead = 1
      }
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

const handleDeleteMessage = async (message, event) => {
  event.stopPropagation()
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.delete(`/message/${message.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功！')
      fetchMessages()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败，请稍后再试')
    }
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待跟进'
    case 1: return '跟进中'
    case 2: return '已成交'
    case 3: return '已取消'
    default: return '未知'
  }
}

const getStatusTagType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return ''
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

const handleTabChange = (tab) => {
  activeTab.value = tab.props.name
  pageNum.value = 1
  queryParams.value.current = 1
  switch (tab.props.name) {
    case 'appointments':
      fetchMyAppointments()
      break
    case 'favorites':
      fetchFavoriteVehicles()
      break
    case 'message':
      fetchMessages()
      break
  }
}

const handleCurrentChange = (val) => {
  queryParams.value.current = val
  fetchMyAppointments()
}

const goToVehicleDetail = (vehicleId) => {
  router.push(`/vehicle/${vehicleId}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定要取消对 ${row.brand} ${row.model} 的预约吗？`,
    '取消预约确认',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '暂不取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await request.post(`/intent/cancel/${row.id}`)
      if (res.code === 200) {
        ElMessage.success('预约已成功取消！')
        fetchMyAppointments()
      } else {
        ElMessage.error(res.msg || '取消预约失败')
      }
    } catch (error) {
      console.error('取消预约失败:', error)
      ElMessage.error('网络异常，请稍后再试')
    }
  }).catch(() => {})
}

onMounted(() => {
  if (route.query.tab === 'message') {
    activeTab.value = 'message'
    fetchMessages()
  } else {
    fetchMyAppointments()
  }
})
</script>

<template>
  <div class="user-profile-container">
    <div class="header">
      <el-button type="text" @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1>个人中心</h1>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="profile-tabs">

      <el-tab-pane label="预约记录" name="appointments">
        <el-skeleton :loading="loading" animated :count="3">
          <template #default>
            <el-empty v-if="appointments.length === 0" description="暂无预约记录" />
            <div v-else class="appointments-container">
              <el-table :data="appointments" border stripe style="width: 100%">
                <el-table-column prop="id" label="ID" width="80"></el-table-column>
                <el-table-column label="意向车型" min-width="140">
                  <template #default="scope">
                    {{ scope.row.brand }} {{ scope.row.model }}
                  </template>
                </el-table-column>
                <el-table-column label="预约门店" min-width="160">
                  <template #default="scope">
                    <span v-if="scope.row.dealerName">
                      <div v-for="(name, index) in scope.row.dealerName.split(',')" :key="index">{{ name.trim() }}</div>
                    </span>
                    <span v-else>未选择门店</span>
                  </template>
                </el-table-column>
                <el-table-column label="门店地址" min-width="200">
                  <template #default="scope">
                    <span v-if="scope.row.dealerAddress">
                      <div v-for="(address, index) in scope.row.dealerAddress.split(',')" :key="index">{{ address.trim() }}</div>
                    </span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column prop="contactPhone" label="联系电话" width="120"></el-table-column>
                <el-table-column prop="message" label="留言信息" min-width="150"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                  <template #default="scope">
                    <el-button
                      v-if="scope.row.status !== 3 && scope.row.status !== 2"
                      type="danger"
                      size="small"
                      @click="handleCancel(scope.row)"
                    >
                      取消预约
                    </el-button>
                    <span v-else style="color: #999; font-size: 12px;">-</span>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination-wrapper" v-if="total > 0">
                <el-pagination
                  background
                  layout="total, prev, pager, next, jumper"
                  :total="total"
                  v-model:current-page="queryParams.current"
                  :page-size="queryParams.size"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-tab-pane>

      <el-tab-pane label="收藏车辆" name="favorites">
        <el-skeleton :loading="loading" animated :count="3">
          <template #default>
            <el-empty v-if="favoriteVehicles.length === 0" description="暂无收藏车辆" />
            <div v-else class="favorites-container">
              <div class="car-grid">
                <div class="car-card" v-for="favorite in favoriteVehicles" :key="favorite.vehicleId" @click="goToVehicleDetail(favorite.vehicleId)">
                  <div class="card-img-wrapper">
                    <img :src="favorite.vehicleImage || `/cars/${favorite.vehicleId}.jpg`" class="card-img" onerror="this.src='/logo.jpg'" />
                  </div>
                  <div class="card-info">
                    <h4 class="card-title">{{ favorite.vehicleBrand }} {{ favorite.vehicleModel }}</h4>
                    <p class="card-price">{{ favorite.vehiclePrice }}万</p>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-tab-pane>

      <el-tab-pane label="消息通知" name="message">
        <el-skeleton :loading="loading" animated :count="3">
          <template #default>
            <el-empty v-if="messages.length === 0" description="暂无消息通知" />
            <div v-else class="messages-container">
              <div
                v-for="message in messages"
                :key="message.id"
                class="message-item"
                :class="{ 'message-unread': message.isRead === 0, 'message-read': message.isRead === 1 }"
                @click="handleMessageClick(message)"
              >
                <div class="message-icon">
                  <el-icon :size="24"><Bell /></el-icon>
                </div>
                <div class="message-content">
                  <div class="message-text">
                    <span v-if="message.intentId" class="message-intent-id">预约ID: {{ message.intentId }}</span>
                    {{ message.content }}
                  </div>
                  <div class="message-time">{{ formatTime(message.createTime) }}</div>
                </div>
                <div class="message-actions">
                  <el-button type="danger" size="small" :icon="Delete" circle @click="handleDeleteMessage(message, $event)" />
                </div>
                <div class="message-status" v-if="message.isRead === 0">
                  <el-tag type="danger" size="small">未读</el-tag>
                </div>
                <div class="message-status" v-else>
                  <el-tag type="info" size="small">已读</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-tab-pane>

      <el-tab-pane label="账号设置" name="settings">
        <div class="settings-container">

          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <div class="avatar-wrapper">
                <el-avatar :size="100" :src="currentAvatar" />
                <div class="edit-mask">
                  <el-icon :size="24" color="#fff"><Camera /></el-icon>
                  <span>修改头像</span>
                </div>
              </div>
            </el-upload>
            <div class="avatar-tip">点击图片更换头像</div>
          </div>

          <div class="form-section">
            <el-form :model="settingsForm" label-width="100px" style="max-width: 400px; margin: 0 auto;">
              <el-form-item label="用户昵称：">
                <el-input v-model="settingsForm.nickname" placeholder="给自己起个好听的名字吧" />
              </el-form-item>

              <el-form-item label="当前密码：">
                <el-input v-model="settingsForm.currentPassword" type="password" show-password placeholder="修改密码时请输入当前密码" />
              </el-form-item>

              <el-form-item label="修改密码：">
                <el-input v-model="settingsForm.password" type="password" show-password placeholder="不修改请留空" />
              </el-form-item>

              <el-form-item label="确认密码：">
                <el-input v-model="settingsForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="submitSettings" :loading="saving" style="width: 100%;">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>

        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<style scoped>
.user-profile-container { max-width: 1200px; margin: 0 auto; padding: 20px 0 60px 0; }
.header { display: flex; align-items: center; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #eee; }
.back-btn { margin-right: 20px; }
.header h1 { font-size: 20px; font-weight: 600; margin: 0; }
.profile-tabs { background-color: #ffffff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); overflow: hidden; min-height: 500px;}
.appointments-container, .favorites-container, .messages-container { padding: 20px; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.car-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px; }
.car-card { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; cursor: pointer; transition: all 0.3s; position: relative; }
.car-card:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); transform: translateY(-4px); }
.card-img-wrapper { width: 100%; aspect-ratio: 4 / 3; background: #f8f9fa; overflow: hidden; }
.card-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.car-card:hover .card-img { transform: scale(1.05); }
.card-info { padding: 15px; text-align: center; }
.card-title { font-size: 18px; color: #333; margin-bottom: 8px; }
.card-price { font-size: 16px; color: #ff6666; font-weight: bold; }
.message-item { display: flex; align-items: center; padding: 16px; border-radius: 8px; margin-bottom: 12px; cursor: pointer; transition: all 0.3s; }
.message-unread { background-color: #fff5f5; border-left: 3px solid #ff4d4f; }
.message-unread:hover { background-color: #ffeaea; }
.message-read { background-color: #fafafa; border-left: 3px solid #dcdfe6; }
.message-read:hover { background-color: #f0f0f0; }
.message-icon { width: 48px; height: 48px; border-radius: 50%; background-color: #fff; display: flex; align-items: center; justify-content: center; margin-right: 16px; color: #409EFF; flex-shrink: 0; }
.message-unread .message-icon { color: #ff4d4f; }
.message-content { flex: 1; min-width: 0; }
.message-text { font-size: 15px; color: #333; margin-bottom: 4px; line-height: 1.5; }
.message-intent-id { display: inline-block; background-color: #e6f7ff; color: #1890ff; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-right: 8px; }
.message-unread .message-text { font-weight: 600; color: #333; }
.message-read .message-text { font-weight: 400; color: #999; }
.message-time { font-size: 12px; color: #999; }
.message-status { margin-left: 16px; flex-shrink: 0; }
.message-actions { margin-left: 8px; flex-shrink: 0; }

.settings-container { padding: 40px 20px; }
.avatar-section { text-align: center; margin-bottom: 40px; }
.avatar-wrapper { position: relative; display: inline-block; cursor: pointer; border-radius: 50%; overflow: hidden; border: 2px solid #f0f0f0; }
.edit-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; flex-direction: column; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s; color: #fff; font-size: 12px; gap: 4px; }
.avatar-wrapper:hover .edit-mask { opacity: 1; }
.avatar-tip { margin-top: 12px; font-size: 13px; color: #999; }
.form-section { background-color: #fafafa; padding: 30px; border-radius: 8px; max-width: 600px; margin: 0 auto; }
</style>