<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()

const vehicleList = ref([])
const total = ref(0)
const currentKeyword = ref('') // 当前展示的搜索词

// 发给后端的查询参数
const queryParams = reactive({
  current: 1,
  size: 8, // 一行4个，展示两行
  keyword: ''
})

// 向后端请求数据
const fetchVehicles = async () => {
  try {
    const res = await request.post('/vehicle/page', queryParams)
    if (res.code === 200) {
      vehicleList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('搜索失败:', error)
  }
}

// 初始化搜索逻辑
const initSearch = () => {
  // 从 URL 取出 keyword，如果没有则为空字符串
  currentKeyword.value = route.query.keyword || ''
  queryParams.keyword = currentKeyword.value
  queryParams.current = 1 // 每次新搜索都回到第一页
  fetchVehicles()
}

// 翻页事件
const handleCurrentChange = (val) => {
  queryParams.current = val
  fetchVehicles()
}

// 跳转详情页
const goToDetail = (id) => {
  router.push(`/vehicle/${id}`)
}

onMounted(() => {
  initSearch()
})

// 🚀 核心：监听路由参数变化。解决“在搜索页再次搜索时不刷新”的问题
watch(() => route.query.keyword, () => {
  initSearch()
})
</script>

<template>
  <div class="search-container">
    <div class="result-section">
      
      <div class="result-header">
        <h3>搜索 <span class="highlight-keyword">"{{ currentKeyword }}"</span> 的结果</h3>
        <span class="total-text">共有 <span class="highlight-total">{{ total }}</span> 个车系符合条件</span>
      </div>

      <div class="car-grid">
        <div class="car-card" v-for="item in vehicleList" :key="item.id" @click="goToDetail(item.id)">
          <div class="card-img-wrapper">
            <img :src="item.imageUrl ||`/cars/${item.id}.jpg`" class="card-img" onerror="this.src='/logo.jpg'" />
          </div>
          <div class="card-info">
            <h4 class="card-title">{{ item.brand }} {{ item.model }}</h4>
            <p class="card-price">{{ item.price }}万</p>
          </div>
        </div>
      </div>

      <el-empty 
        v-if="vehicleList.length === 0" 
        :description="`未找到与 \u0022${currentKeyword}\u0022 相关的车型，换个词试试吧`" 
      />

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
  </div>
</template>

<style scoped>
.search-container {
  max-width: 1200px;
  margin: 30px auto 60px;
}

.result-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.result-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.result-header h3 { margin: 0; font-size: 24px; color: #333; }
.highlight-keyword { color: #409EFF; }
.total-text { font-size: 14px; color: #666; }
.highlight-total { color: #ff6666; font-weight: bold; font-size: 16px; margin: 0 4px; }

/* 车辆网格 */
.car-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}
.car-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}
.car-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}
.card-img-wrapper {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #f8f9fa;
  overflow: hidden;
}
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}
.car-card:hover .card-img {
  transform: scale(1.05);
}
.card-info {
  padding: 15px;
  text-align: center;
}
.card-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}
.card-price {
  font-size: 16px;
  color: #ff6666;
  font-weight: bold;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>