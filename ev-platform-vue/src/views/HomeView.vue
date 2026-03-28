<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()

const goToDetail = (id) => {
  router.push(`/vehicle/${id}`)
}

// 存放最新发布车辆的数据 (给轮播图用)
const vehicleList = ref([])
// 存放热门推荐车辆的数据 (给卡片用)
const recommendVehicles = ref([]) 

// 获取最新发布的车辆 (为了省带宽，既然首页没表格了，直接查前3条即可)
const fetchLatestVehicles = async () => {
  try {
    const res = await request.post('/vehicle/page', { current: 1, size: 3 })
    if (res.code === 200) {
      vehicleList.value = res.data.records
    }
  } catch (error) {
    console.error('获取最新首发失败:', error)
  }
}

// 获取热门推荐车辆 (真正按热度值排序的前6辆)
const fetchHotVehicles = async () => {
  try {
    const res = await request.get('/vehicle/hot')
    if (res.code === 200) {
      recommendVehicles.value = res.data
    }
  } catch (error) {
    console.error('获取热门推荐失败:', error)
  }
}

// 轮播图数据直接使用查回来的前 3 条
const carouselVehicles = computed(() => {
  return vehicleList.value
})

onMounted(() => {
  // 页面加载时，同时发出两个请求
  fetchLatestVehicles()
  fetchHotVehicles()
})
</script>

<template>
  <div class="home-container">
    
    <div class="banner-section">

        <div class="section-header">
        <h3>重磅首发</h3>
      </div>


      <el-carousel height="450px" trigger="click" :interval="4000" type="card">
        <el-carousel-item v-for="item in carouselVehicles" :key="item.id" @click="goToDetail(item.id)">
          <div class="carousel-card">
            
            <img 
              :src="item.imageUrl ||`/cars/${item.id}.jpg`" 
              class="banner-img" 
              alt="car cover"
              onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
            />
            
            <div class="fallback-bg bg-gradient-1" style="display: none;"></div>
            
            <div class="banner-content">
              <h2 class="banner-title">{{ item.brand }} {{ item.model }}</h2>
              <p class="banner-subtitle">
                <el-tag type="success" effect="dark" round>续航 {{ item.rangeKm }} km</el-tag>
                <el-tag type="warning" effect="dark" round style="margin-left: 10px">{{ item.driveType }}</el-tag>
              </p>
              <div class="banner-price">
                <span class="price-symbol">￥</span>
                <span class="price-num">{{ item.price }}</span>
                <span class="price-unit">万</span>
              </div>
              <el-button type="primary" size="large" round class="view-btn">立即探索</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="recommend-section">
      <div class="section-header">
        <h3>热门推荐</h3>
      </div>
      
      <div class="recommend-grid">
        <div class="recommend-card" v-for="item in recommendVehicles" :key="item.id" @click="goToDetail(item.id)">
          
          <div class="card-img-wrapper">
            <img 
              :src="item.imageUrl ||`/cars/${item.id}.jpg`" 
              class="card-img"
              onerror="this.src='/car-logo.png'" 
            />
            </div>
          
          <div class="card-info">
            <h4 class="card-title">{{ item.brand }} {{ item.model }}</h4>
            <p class="card-price">{{ item.price }}万</p>
          </div>
        </div>
      </div>
    </div>

    </div>
</template>

<style scoped>
.home-container {
  width: 100%;
}
.section-header h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 24px;
  padding-left: 12px;
  border-left: 4px solid #409EFF;
}

/* --- 轮播图区域样式 --- */
.banner-section {
  max-width: 1200px;
  margin: 20px auto 40px;
}
.carousel-card {
  width: 100%;
  height: 100%;
  border-radius: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  cursor: pointer;
}

/* 轮播图真实图片样式 */
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保证图片不变形，完美覆盖整个卡片 */
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

/* 兜底渐变背景 */
.fallback-bg {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}
.bg-gradient-1 { background: linear-gradient(135deg, #141e30 0%, #243b55 100%); }

/* 轮播图文字层叠在图片之上 */
.banner-content { 
  position: relative;
  z-index: 10; 
  padding: 40px; 
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  background: rgba(0, 0, 0, 0.4); /* 给图片加上一层半透明黑色遮罩，确保白色文字能看清 */
}
.banner-title { font-size: 42px; font-weight: bold; margin-bottom: 20px; letter-spacing: 2px; text-shadow: 0 2px 4px rgba(0,0,0,0.5); }
.banner-subtitle { margin-bottom: 30px; }
.banner-price { margin-bottom: 30px; text-shadow: 0 2px 4px rgba(0,0,0,0.5); }
.price-symbol { font-size: 24px; }
.price-num { font-size: 48px; font-weight: bold; font-style: italic; }
.price-unit { font-size: 20px; margin-left: 4px; }

/* --- 热门推荐卡片样式 --- */
.recommend-section {
  max-width: 1200px;
  margin: 0 auto 60px;
}
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); 
  gap: 30px; 
}
.recommend-card {
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px; /* 给卡片加一点点圆角更好看 */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 保证内部图片不溢出圆角 */
  aspect-ratio: 1 / 1; 
  transition: all 0.3s ease;
  cursor: pointer;
}
.recommend-card:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08); 
  transform: translateY(-4px); 
}
.card-img-wrapper {
  width: 100%;
  flex: 1; 
  background: #f8f9fa;
  overflow: hidden;
}

/* 卡片里的真实图片样式 */
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保证图片填满容器不变形 */
  transition: transform 0.5s ease;
}
/* 鼠标悬浮时，图片轻微放大，电商常用高级交互 */
.recommend-card:hover .card-img {
  transform: scale(1.05); 
}

.card-info {
  text-align: center;
  padding: 15px 0;
  background: white;
}
.card-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}
.card-price {
  font-size: 18px;
  color: #ff6666; 
}
</style>