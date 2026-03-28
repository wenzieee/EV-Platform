<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()

// 车辆数据与分页总数
const vehicleList = ref([])
const total = ref(0)

// 真正的查询参数 (发给后端的)
const queryParams = reactive({
  current: 1,
  size: 8, // 每页显示 8 辆车 (一行 4 个，刚好两行)
  brand: '',
  minPrice: null,
  maxPrice: null
})

// --- 筛选项配置 ---
// 品牌选项
const brandOptions = ['比亚迪', '特斯拉', '理想', '蔚来', '小鹏', '问界', '极氪', '小米']
// 价格选项 (包含展示文本和对应的真实价格区间)
const priceOptions = [
  { label: '15万以下', min: 0, max: 15 },
  { label: '15-25万', min: 15, max: 25 },
  { label: '25-35万', min: 25, max: 35 },
  { label: '35万以上', min: 35, max: 999 }
]

// 记录当前选中的高亮状态 ('不限' 为空字符串)
const activeBrand = ref('')
const activePriceLabel = ref('')

// 获取车辆列表核心方法
const fetchVehicles = async () => {
  try {
    const res = await request.post('/vehicle/page', queryParams)
    if (res.code === 200) {
      vehicleList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// 点击品牌筛选
const selectBrand = (brand) => {
  activeBrand.value = brand
  queryParams.brand = brand
  queryParams.current = 1 // 每次改变条件，都要重置回第一页
  fetchVehicles()
}

// 点击价格筛选
const selectPrice = (priceObj) => {
  if (priceObj === '') {
    // 点击了不限
    activePriceLabel.value = ''
    queryParams.minPrice = null
    queryParams.maxPrice = null
  } else {
    activePriceLabel.value = priceObj.label
    queryParams.minPrice = priceObj.min
    queryParams.maxPrice = priceObj.max
  }
  queryParams.current = 1
  fetchVehicles()
}

// 页码改变时触发
const handleCurrentChange = (val) => {
  queryParams.current = val
  fetchVehicles()
}

// 跳转详情页
const goToDetail = (id) => {
  router.push(`/vehicle/${id}`)
}

// 页面初始化加载数据
onMounted(() => {
  fetchVehicles()
})
</script>

<template>
  <div class="library-container">
    
    <div class="filter-card">
      <h3 class="filter-title">选车条件</h3>
      
      <div class="filter-row">
        <div class="filter-label">价格：</div>
        <div class="filter-options">
          <span :class="['option-item', { active: activePriceLabel === '' }]" @click="selectPrice('')">不限</span>
          <span 
            v-for="(item, index) in priceOptions" 
            :key="index"
            :class="['option-item', { active: activePriceLabel === item.label }]"
            @click="selectPrice(item)"
          >
            {{ item.label }}
          </span>
        </div>
      </div>

      <div class="filter-row">
        <div class="filter-label">品牌：</div>
        <div class="filter-options">
          <span :class="['option-item', { active: activeBrand === '' }]" @click="selectBrand('')">不限</span>
          <span 
            v-for="brand in brandOptions" 
            :key="brand"
            :class="['option-item', { active: activeBrand === brand }]"
            @click="selectBrand(brand)"
          >
            {{ brand }}
          </span>
        </div>
      </div>
    </div>

    <div class="result-section">
      <div class="result-header">
        <span>共有 <span class="highlight-total">{{ total }}</span> 个车系符合条件</span>
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

      <el-empty v-if="vehicleList.length === 0" description="暂无符合条件的车型" />

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
.library-container {
  max-width: 1200px;
  margin: 30px auto 60px;
}

/* --- 筛选区域样式 --- */
.filter-card {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}
.filter-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  font-weight: bold;
}
.filter-row {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}
.filter-label {
  width: 60px;
  color: #666;
  font-size: 14px;
  padding-top: 6px;
}
.filter-options {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}
/* 筛选项标签样式 (贴合截图效果) */
.option-item {
  cursor: pointer;
  padding: 4px 12px;
  font-size: 14px;
  color: #333;
  border-radius: 4px;
  transition: all 0.2s;
}
.option-item:hover {
  color: #409EFF;
}
/* 选中的高亮样式 */
.option-item.active {
  background-color: #409EFF;
  color: #fff;
}

/* --- 结果列表区域样式 --- */
.result-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.result-header {
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}
.highlight-total {
  color: #ff6666;
  font-weight: bold;
  font-size: 16px;
  margin: 0 4px;
}

/* 车辆网格 (改成一行 4 个) */
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
  aspect-ratio: 4 / 3; /* 图片区域比例 */
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

/* 分页器居中 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>