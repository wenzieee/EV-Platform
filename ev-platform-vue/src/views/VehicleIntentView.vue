<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Bicycle, Loading } from '@element-plus/icons-vue'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()

// 当前激活的 Tab ('price' 询问底价, 'testdrive' 预约试驾)
const activeTab = ref('price')
const vehicle = ref({}) // 车辆信息
const dealers = ref([]) // 新增：存放附近的4S店列表
const locationLoading = ref(false) // 新增：地理位置加载状态

// 表单数据
const form = reactive({
  city: '郑州',
  name: '',
  phone: '',
  selectedDealers: [] // 新增：选中的4S店列表
})

const cityOptions = ['北京', '上海', '广州', '深圳', '郑州', '杭州', '成都', '武汉']

// 获取车辆信息
const fetchVehicle = async () => {
  try {
    const res = await request.get(`/vehicle/${route.params.id}`)
    if (res.code === 200) {
      vehicle.value = res.data
      // 在获取到车辆品牌后，再尝试获取用户位置并加载附近经销商
      if (vehicle.value.brand) {
        getUserLocation()
      }
    }
  } catch (error) {
    console.error('获取车辆信息失败:', error)
    ElMessage.error('获取车辆信息失败')
  }
}

// 获取用户地理位置
const getUserLocation = () => {
  locationLoading.value = true
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        fetchNearestDealers(vehicle.value.brand, longitude, latitude)
        locationLoading.value = false
      },
      (error) => {
        console.error('获取地理位置失败:', error)
        ElMessage.warning('无法获取您的地理位置，无法推荐附近门店。')
        locationLoading.value = false
      },
      { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
    )
  } else {
    ElMessage.warning('您的浏览器不支持地理位置功能，无法推荐附近门店。')
    locationLoading.value = false
  }
}

// 获取附近经销商
const fetchNearestDealers = async (brand, lng, lat) => {
  try {
    const res = await request.get(`/dealer/nearest`, { params: { brand, lng, lat } })
    if (res.code === 200) {
      dealers.value = res.data
      // 默认选中前两个经销商（如果存在）
      if (dealers.value.length > 0) {
        form.selectedDealers = dealers.value.slice(0, 2).map(d => d.id)
      }
    } else {
      ElMessage.error(res.msg || '获取附近门店失败')
    }
  } catch (error) {
    console.error('获取附近经销商失败:', error)
    ElMessage.error('获取附近门店失败')
  }
}

// 切换 Tab
const switchTab = (tab) => {
  activeTab.value = tab
}



// 真实的提交表单请求 (注意这里加了 async)
const handleSubmit = async () => {
  if (!form.name || !form.phone) {
    ElMessage.warning('请将必填信息填写完整')
    return
  }

  try {
    // 组装要发给后端的数据
    const postData = {
      vehicleId: vehicle.value.id,
      intentType: activeTab.value, // 'price' 或 'testdrive'
      city: form.city,
      name: form.name,
      phone: form.phone,
      dealerIds: form.selectedDealers // 新增：选中的4S店ID列表
    }

    // 🚀 核心：真正调用后端的接口！
    const res = await request.post('/intent/submit', postData)

    if (res.code === 200) {
      ElMessage.success(`提交成功！经销商将尽快与您联系确认${activeTab.value === 'price' ? '底价' : '试驾时间'}。`)

      // 提交成功后延迟 2 秒返回上一页
      setTimeout(() => {
        router.back()
      }, 2000)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络异常，请稍后再试')
  }
}

onMounted(() => {
  // 从 URL 参数中获取初始选中的 Tab
  if (route.query.type) {
    activeTab.value = route.query.type
  }
  fetchVehicle()
})
</script>

<template>
  <div class="intent-container">
    <el-row :gutter="20" class="main-content-row">
      <el-col :span="16">
        <div class="form-card">

          <div class="tab-header">
            <span
              :class="['tab-item', { active: activeTab === 'price' }]"
              @click="switchTab('price')"
            >询问底价</span>
            <span
              :class="['tab-item', { active: activeTab === 'testdrive' }]"
              @click="switchTab('testdrive')"
            >预约试驾</span>
          </div>

          <div class="vehicle-snippet">
            <img
              :src="vehicle?.imageUrl || `/cars/${vehicle?.id || 'default'}.jpg`"
              class="snippet-img"
              onerror="this.src='/logo.jpg'"
            />
            <div class="snippet-info">
              <div class="snippet-title">{{ vehicle.brand }} {{ vehicle.model }}</div>
              <div class="snippet-subtitle">2026款 官方指导价({{ vehicle.price }}万)</div>
            </div>
          </div>

          <p class="form-hint" v-if="activeTab === 'price'">
            实际购车价格还有议价空间，填写以下信息，经销商将通过短信等方式为您报出最底价。
          </p>
          <p class="form-hint" v-else>
            留下您的联系方式，经销商将尽快与您联系，安排专属试驾体验。
          </p>

          <div class="form-area">
            <el-form :model="form" label-width="100px" size="large">

              <el-form-item label="购车城市：">
                <el-select v-model="form.city" style="width: 320px;">
                  <el-option v-for="city in cityOptions" :key="city" :label="city" :value="city" />
                </el-select>
              </el-form-item>

              <el-form-item label="姓 名：" required>
                <el-input v-model="form.name" placeholder="请填写称呼（如李先生）" style="width: 320px;" />
              </el-form-item>

              <el-form-item label="手机号码：" required>
                <el-input v-model="form.phone" placeholder="请填写手机号码" style="width: 320px;" />
              </el-form-item>



              <el-form-item>
                <el-button
                  type="primary"
                  class="submit-btn"
                  @click="handleSubmit"
                >
                  {{ activeTab === 'price' ? '获取底价' : '立即预约' }}
                </el-button>
                <div class="agreement-text">
                  点击提交即视为同意 <a href="#">《个人信息披露及保护声明》</a>
                </div>
              </el-form-item>

            </el-form>
          </div>

        </div>
      </el-col>

      <el-col :span="8">
        <div class="dealer-list-sidebar">
        <h3 class="sidebar-title">本地经销商</h3>
        <div v-if="locationLoading" class="loading-box">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在获取您的位置并加载附近门店...</span>
        </div>
        <div v-else-if="dealers.length > 0" class="dealer-checkbox-list">
          <el-card
            v-for="dealer in dealers"
            :key="dealer.id"
            class="dealer-card-item"
            shadow="hover"
          >
            <el-checkbox v-model="form.selectedDealers" :label="dealer.id">
              <div class="dealer-content">
                <div class="dealer-name">{{ dealer.name }}</div>
                <div class="dealer-info">
                  <div class="dealer-address">
                    <el-icon><Location /></el-icon>
                    <span>{{ dealer.address }}</span>
                  </div>
                  <div class="dealer-distance">
                    <el-icon><Bicycle /></el-icon>
                    <span>距离: {{ dealer.distance }} km</span>
                  </div>
                </div>
              </div>
            </el-checkbox>
          </el-card>
        </div>
        <el-empty v-else description="暂无附近门店推荐" :image-size="50"></el-empty>
      </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.intent-container {
  max-width: 1200px;
  margin: 0 auto 60px;
  padding-top: 40px;
  min-height: 80vh; /* 保证容器有最小高度，避免内容过少时底部上移 */
}

.main-content-row {
  align-items: stretch; /* 确保 ElCol 子元素等高 */
}

.form-card {
  background: white;
  padding: 50px 80px;
  border-radius: 8px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.05);
  min-height: 100%; /* 确保卡片撑满高度 */
}

.dealer-list-sidebar {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.05);
  /* min-height: 100%;  Removed to allow natural height */
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  font-weight: bold;
  text-align: center;
}

.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
  min-height: 150px; /* 适当调整最小高度 */
}

.loading-box .el-icon {
  margin-bottom: 10px;
  font-size: 24px;
}

.dealer-checkbox-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  flex-grow: 1; /* 允许列表区域填充剩余空间 */
}

.dealer-card-item {
  border: 1px solid #EBEEF5; /* 默认边框 */
  border-radius: 8px;
  padding: 15px;
  transition: all 0.3s ease;
  /* Removed fixed min-height to allow content to dictate height */
}

/* 选中状态的样式 */
.el-checkbox-group .el-checkbox.is-checked + .el-card .dealer-card-item {
  border-color: #409EFF; /* Element Plus primary color */
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.2);
}

.dealer-card-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* Ensure el-checkbox and its label can expand vertically within the card */
.dealer-card-item .el-checkbox {
  width: 100%; /* Ensure checkbox takes full width of card */
  height: auto; /* Allow height to adjust based on content */
  align-items: flex-start; /* Align checkbox input and label to the top */
}

.dealer-card-item .el-checkbox__label {
  display: flex; /* Use flex to arrange content vertically */
  flex-direction: column;
  flex-grow: 1; /* Allow label content to grow */
  white-space: normal; /* Allow text to wrap */
  word-break: break-word; /* Allow long words to break */
  padding-left: 8px; /* Add some space between checkbox and text */
  line-height: 1.5; /* Adjust line height for better readability */
}

/* Ensure dealer-content fills the space and allows text wrap */
.dealer-card-item .dealer-content {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.dealer-card-item .dealer-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  white-space: normal; /* Important for name wrapping */
  word-break: break-word;
}

.dealer-card-item .dealer-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 13px;
  color: #606266;
  white-space: normal; /* Important for info wrapping */
  word-break: break-word;
}

.dealer-card-item .dealer-address,
.dealer-card-item .dealer-distance {
  display: flex;
  align-items: flex-start;
  gap: 5px;
}

/* 顶部文字 Tab 切换 */
.tab-header {
  display: flex;
  gap: 30px;
  margin-bottom: 40px;
  align-items: flex-end;
}
.tab-item {
  font-size: 20px;
  color: #999;
  cursor: pointer;
  transition: all 0.3s;
}
.tab-item.active {
  font-size: 26px;
  color: #333;
  font-weight: bold;
}

/* 车辆小卡片 */
.vehicle-snippet {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}
.snippet-img {
  width: 160px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  background: #f8f9fa;
}
.snippet-title {
  font-size: 18px;
  color: #333;
  font-weight: bold;
  margin-bottom: 8px;
}
.snippet-subtitle {
  font-size: 14px;
  color: #666;
}

/* 提示语 */
.form-hint {
  font-size: 14px;
  color: #999;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
}

/* 核心表单 */
.form-area {
  padding-left: 20px;
}
.submit-btn {
  width: 320px;
  height: 45px;
  font-size: 16px;
  font-weight: bold;
  margin-top: 10px;
}
.agreement-text {
  width: 320px;
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}
.agreement-text a {
  color: #409EFF;
  text-decoration: none;
}
</style>
