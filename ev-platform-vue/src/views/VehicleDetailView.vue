<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'

import { useRouter } from 'vue-router' // 记得顶部引入 useRouter
const router = useRouter()

// 跳转到留资页，type 取值为 'price' (底价) 或 'testdrive' (试驾)
const goToIntent = (type) => {
  if (!vehicle.value.id) return
  // 使用 query 传参： /intent/1?type=price
  router.push({ path: `/intent/${vehicle.value.id}`, query: { type: type } })
}

const route = useRoute()   // 用于获取 URL 上的参数 (如 id)
const vehicle = ref({})    // 存放当前车辆的数据

// 根据 URL 里的 id 向后端请求车辆详情
const fetchVehicleDetail = async () => {
  const vehicleId = route.params.id
  try {
    const res = await request.get(`/vehicle/${vehicleId}`)
    if (res.code === 200) {
      vehicle.value = res.data
    }
  } catch (error) {
    console.error('获取车辆详情失败:', error)
  }
}

onMounted(() => {
  fetchVehicleDetail()
})
</script>

<template>
  <div class="detail-container">
    
    <div class="detail-card">
      <el-row :gutter="40">
        
        <el-col :span="12">
          <div class="image-showcase">
            <img 
              v-if="vehicle.id"
              :src="vehicle.imageUrl || `/cars/${vehicle.id}.jpg`"
              class="car-detail-img" 
              onerror="this.src='/logo.jpg'" 
              alt="车辆实拍图"
            />
          </div>
        </el-col>

        <el-col :span="12">
          <div class="info-showcase">
            <h1 class="car-title">{{ vehicle.brand }} {{ vehicle.model }}</h1>
            
            <div class="car-price">
              <span class="label">指导价：</span>
              <span class="price-num">{{ vehicle.price }}</span>
              <span class="unit">万元</span>
            </div>

            <el-descriptions title="核心参数" :column="1" border class="param-list">
              <el-descriptions-item label="所属品牌">{{ vehicle.brand }}</el-descriptions-item>
              <el-descriptions-item label="纯电续航">{{ vehicle.rangeKm }} km</el-descriptions-item>
              <el-descriptions-item label="驱动类型">{{ vehicle.driveType }}</el-descriptions-item>
              <el-descriptions-item label="车辆状态">
                <el-tag type="success" v-if="vehicle.status === 1">在售</el-tag>
                <el-tag type="info" v-else>停售</el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <div class="action-bar">
              <el-button type="primary" size="large" class="action-btn" @click="goToIntent('price')">询问底价</el-button>
              <el-button type="primary" size="large" class="action-btn" @click="goToIntent('testdrive')">预约试驾</el-button>
            </div>
            
          </div>
        </el-col>
      </el-row>
    </div>
    
  </div>
</template>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 30px auto 60px; /* 顶部留出一点呼吸空间 */
}

.detail-card {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

/* 左侧图片展示区样式更新 */
.image-showcase {
  background: #f8f9fa;
  aspect-ratio: 4 / 3; /* 保证图片区域比例协调 */
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ebeef5;
  overflow: hidden; /* 防止图片溢出圆角 */
}

.car-detail-img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保证图片铺满不拉伸变形 */
  transition: transform 0.3s;
}
.car-detail-img:hover {
  transform: scale(1.02); /* 鼠标放上去轻微放大，增加交互感 */
}

/* 右侧信息区样式 */
.info-showcase { 
  padding-left: 10px; 
  display: flex;
  flex-direction: column;
  height: 100%; /* 撑满高度 */
}

.car-title { 
  font-size: 32px; 
  color: #333; 
  margin-bottom: 20px; 
  font-weight: 600; 
}

.car-price { 
  margin-bottom: 30px; 
  background: #fff5f5; 
  padding: 15px 20px; 
  border-radius: 8px; 
  color: #ff4d4f; 
}
.car-price .label { font-size: 16px; color: #666; }
.car-price .price-num { font-size: 36px; font-weight: bold; font-style: italic; }
.car-price .unit { font-size: 16px; margin-left: 5px; }

.param-list { 
  flex: 1; /* 让参数列表占据中间所有空间，把按钮挤到底部 */
  margin-bottom: 40px; 
}

/* 按钮区域：均分宽度 */
.action-bar { 
  display: flex; 
  gap: 20px; 
  margin-top: auto; /* 自动贴底 */
}
.action-btn { 
  flex: 1; /* 核心：两个按钮各占一半宽度 */
  font-size: 18px; 
  font-weight: bold; 
  height: 50px; /* 稍微加高一点，显得更大气 */
  border-radius: 6px;
}
</style>