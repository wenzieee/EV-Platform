<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { StarFilled, Location, Bicycle, Loading, ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const goToIntent = (type, trimId = null) => {
  if (!vehicle.value.id) return
  const query = { type: type }
  if (trimId) {
    query.trimId = trimId
    query.trimName = selectedTrim.value?.trimName || ''
  }
  router.push({ path: `/intent/${vehicle.value.id}`, query })
}

const vehicle = ref({})
const trims = ref([])
const selectedTrim = ref(null)
const isFavorited = ref(false)
const dealers = ref([])
const locationLoading = ref(false)

const isExpanded = ref(false)

const displayedDealers = computed(() => {
  if (isExpanded.value) {
    return dealers.value
  }
  return dealers.value.slice(0, 4)
})

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

const fetchVehicleDetail = async () => {
  const vehicleId = route.params.id
  if (!vehicleId) return
  try {
    const res = await request.get(`/vehicle/${vehicleId}`)
    if (res.code === 200) {
      vehicle.value = res.data.vehicle
      trims.value = res.data.trims || []
      if (trims.value.length > 0) {
        selectedTrim.value = trims.value[0]
      }
      if (vehicle.value.brand) {
        getUserLocation()
      }
    }
  } catch (error) {
    console.error('获取车辆详情失败:', error)
    ElMessage.error('获取车辆详情失败')
  }
}

const selectTrim = (trim) => {
  selectedTrim.value = trim
}

const navigateToDealer = (dealer) => {
  if (dealer.longitude && dealer.latitude) {
    const url = `https://uri.amap.com/navigation?from=&to=${dealer.longitude},${dealer.latitude},${dealer.name}&mode=drive&callnative=0`;
    window.open(url, '_blank');
  } else {
    ElMessage.warning('该4S店缺少地理位置信息，无法导航。');
  }
};

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

const fetchNearestDealers = async (brand, lng, lat) => {
  try {
    const res = await request.get(`/dealer/nearest`, { params: { brand, lng, lat } })
    if (res.code === 200) {
      dealers.value = res.data
    } else {
      ElMessage.error(res.msg || '获取附近门店失败')
    }
  } catch (error) {
    console.error('获取附近经销商失败:', error)
    ElMessage.error('获取附近门店失败')
  }
}

const checkFavoriteStatus = async () => {
  const vehicleId = route.params.id
  if (!vehicleId) return
  try {
    const res = await request.get(`/favorite/check/${vehicleId}`)
    if (res.code === 200) {
      isFavorited.value = res.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

const toggleFavorite = async () => {
  const vehicleId = route.params.id
  if (!vehicleId) return

  try {
    const res = await request.post(`/favorite/toggle/${vehicleId}`)
    if (res.code === 200) {
      isFavorited.value = !isFavorited.value
      ElMessage.success(isFavorited.value ? '车辆收藏成功！' : '已取消收藏')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('切换收藏状态失败:', error)
    ElMessage.error('操作失败，请先登录！')
  }
}

onMounted(() => {
  fetchVehicleDetail()
  checkFavoriteStatus()
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
            <div class="car-title-wrapper">
              <h1 class="car-title">{{ vehicle.brand }} {{ vehicle.model }}</h1>
              <el-icon
                class="favorite-icon"
                :class="{ 'is-favorited': isFavorited }"
                @click="toggleFavorite"
              >
                <StarFilled />
              </el-icon>
            </div>

            <div class="car-price">
              <span class="label">指导价：</span>
              <template v-if="vehicle.minPrice && vehicle.maxPrice">
                <span class="price-num">{{ vehicle.minPrice }}</span>
                <span class="price-range">-</span>
                <span class="price-num">{{ vehicle.maxPrice }}</span>
              </template>
              <template v-else-if="vehicle.minPrice">
                <span class="price-num">{{ vehicle.minPrice }}</span>
              </template>
              <template v-else-if="vehicle.price">
                <span class="price-num">{{ vehicle.price }}</span>
              </template>
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

          </div>
        </el-col>
      </el-row>
    </div>

    <div v-if="trims.length > 0" class="trim-selection">
      <h3>选择配置版本</h3>
      <div class="trim-list">
        <el-card
          v-for="trim in trims"
          :key="trim.id"
          class="trim-card"
          :class="{ 'is-selected': selectedTrim?.id === trim.id }"
          shadow="hover"
          @click="selectTrim(trim)"
        >
          <div class="trim-header">
            <span class="trim-name">{{ trim.trimName }}</span>
            <span class="trim-price">{{ trim.price }} 万元</span>
          </div>
          <div class="trim-specs">
            <span v-if="trim.rangeKm">{{ trim.rangeKm }}km 续航</span>
            <span v-if="trim.batteryCapacity">| {{ trim.batteryCapacity }}kWh 电池</span>
            <span v-if="trim.motorPower">| {{ trim.motorPower }}kW 功率</span>
          </div>
        </el-card>
      </div>
    </div>

    <div v-if="selectedTrim" class="selected-trim-detail">
      <div class="trim-header">
        <h3>配置详情</h3>
        <div class="trim-action-bar">
          <el-button type="primary" size="small" class="action-btn" @click="goToIntent('price', selectedTrim?.id)">询问底价</el-button>
          <el-button type="primary" size="small" class="action-btn" @click="goToIntent('testdrive', selectedTrim?.id)">预约试驾</el-button>
        </div>
      </div>
      <el-descriptions :column="2" border class="trim-detail">
        <el-descriptions-item label="配置名称">{{ selectedTrim.trimName }}</el-descriptions-item>
        <el-descriptions-item label="指导价格">{{ selectedTrim.price }} 万元</el-descriptions-item>
        <el-descriptions-item label="续航里程">{{ selectedTrim.rangeKm }} km</el-descriptions-item>
        <el-descriptions-item label="电池容量">{{ selectedTrim.batteryCapacity }} kWh</el-descriptions-item>
        <el-descriptions-item label="电机功率">{{ selectedTrim.motorPower }} kW</el-descriptions-item>
        <el-descriptions-item label="最高时速">{{ selectedTrim.maxSpeed }} km/h</el-descriptions-item>
        <el-descriptions-item label="百公里加速">{{ selectedTrim.accelerationTime }} 秒</el-descriptions-item>
        <el-descriptions-item label="快充时间">{{ selectedTrim.chargeTime }} 分钟(0-80%)</el-descriptions-item>
        <el-descriptions-item label="车身颜色">{{ selectedTrim.colors || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驱动方式">
          <el-tag type="success">
            {{ selectedTrim.driveType || '-' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="dealer-recommendation">
      <h3>附近4S门店</h3>
      <div v-if="locationLoading" class="loading-box">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在获取您的位置并加载附近门店...</span>
      </div>

      <div v-else-if="dealers.length > 0">
        <div class="dealer-list">
          <el-card
            v-for="dealer in displayedDealers"
            :key="dealer.id"
            class="dealer-card"
            shadow="hover"
          >
            <div class="dealer-info">
              <div class="dealer-name">{{ dealer.name }}</div>
              <div class="dealer-address"><el-icon><Location /></el-icon>{{ dealer.address }}</div>
              <div class="dealer-distance">
                <el-icon><Bicycle /></el-icon>
                距离: {{ dealer.distance }} km
              </div>
              <el-button type="primary" :icon="Location" @click="navigateToDealer(dealer)" class="navigate-btn">导航 / 到这去</el-button>
            </div>
          </el-card>
        </div>

        <div v-if="dealers.length > 4" class="expand-btn-wrapper">
          <el-button type="primary" link @click="toggleExpand" class="expand-btn">
            {{ isExpanded ? '收起门店列表' : `查看全部 ${dealers.length} 家门店` }}
            <el-icon class="el-icon--right">
              <ArrowUp v-if="isExpanded" />
              <ArrowDown v-else />
            </el-icon>
          </el-button>
        </div>
      </div>

      <el-empty v-else description="暂无附近门店推荐" :image-size="50"></el-empty>
    </div>

  </div>
</template>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 30px auto 60px;
}

.detail-card {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.image-showcase {
  background: #f8f9fa;
  aspect-ratio: 4 / 3;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.car-detail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.car-detail-img:hover {
  transform: scale(1.02);
}

.info-showcase {
  padding-left: 10px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.car-title-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.car-title {
  font-size: 32px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.favorite-icon {
  font-size: 28px;
  cursor: pointer;
  color: #dcdfe6;
  transition: all 0.3s ease;
}

.favorite-icon:hover {
  color: gold;
  transform: scale(1.1);
}

.favorite-icon.is-favorited {
  color: gold;
  fill: gold;
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
.car-price .price-range { font-size: 24px; margin: 0 5px; }
.car-price .unit { font-size: 16px; margin-left: 5px; }

.param-list {
  flex: 1;
  margin-bottom: 40px;
}

.action-bar {
  display: flex;
  gap: 20px;
  margin-top: auto;
}
.action-btn {
  flex: 1;
  font-size: 18px;
  font-weight: bold;
  height: 50px;
  border-radius: 6px;
}

.trim-selection {
  margin-top: 30px;
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.trim-selection h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
}

.trim-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.trim-card {
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.trim-card:hover {
  border-color: #409eff;
}

.trim-card.is-selected {
  border-color: #409eff;
  background: #f0f5ff;
}

.trim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.trim-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.trim-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.trim-specs {
  font-size: 14px;
  color: #666;
}

.selected-trim-detail {
  margin-top: 30px;
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.trim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.trim-header h3 {
  font-size: 20px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.trim-action-bar {
  display: flex;
  gap: 10px;
}

.trim-action-bar .action-btn {
  padding: 5px 14px;
  font-size: 13px;
  height: auto;
  line-height: 1.6;
}

.trim-detail {
  margin-top: 0;
}

.dealer-recommendation {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.dealer-recommendation h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
}

.loading-box {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
}

.loading-box .el-icon {
  margin-right: 10px;
  font-size: 20px;
}

.dealer-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.dealer-card {
  border-radius: 8px;
}

.dealer-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dealer-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.dealer-address, .dealer-distance {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 5px;
}

.navigate-btn {
  margin-top: 15px;
  width: 100%;
}

.expand-btn-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.expand-btn {
  font-size: 15px;
}
</style>