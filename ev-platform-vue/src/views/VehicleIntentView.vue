<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()

// 当前激活的 Tab ('price' 询问底价, 'testdrive' 预约试驾)
const activeTab = ref('price')
const vehicle = ref({}) // 车辆信息

// 表单数据
const form = reactive({
  city: '郑州',
  name: '',
  phone: '',

})

const cityOptions = ['北京', '上海', '广州', '深圳', '郑州', '杭州', '成都', '武汉']

// 获取车辆信息
const fetchVehicle = async () => {
  try {
    const res = await request.get(`/vehicle/${route.params.id}`)
    if (res.code === 200) {
      vehicle.value = res.data
    }
  } catch (error) {
    console.error('获取车辆信息失败:', error)
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
      phone: form.phone
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
  </div>
</template>

<style scoped>
.intent-container {
  max-width: 1200px;
  margin: 0 auto 60px;
  padding-top: 40px;
  display: flex;
  justify-content: center; /* 让表单在页面中居中 */
}

.form-card {
  width: 850px; /* 限制表单区域总宽度，贴合截图的紧凑感 */
  background: white;
  padding: 50px 80px;
  border-radius: 8px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.05);
}

/* 顶部文字 Tab 切换 */
.tab-header {
  display: flex;
  gap: 30px;
  margin-bottom: 40px;
  align-items: flex-end; /* 底部对齐，让大小不同的字体底端平齐 */
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
  border-bottom: 1px dashed #ebeef5; /* 虚线分割线 */
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