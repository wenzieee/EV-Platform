<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Check, Delete } from '@element-plus/icons-vue'
import request from '../../utils/request'

// --- 表格与分页数据 ---
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '' // 用于搜索姓名或手机号
})

// --- 获取线索列表数据 ---
const fetchIntents = async () => {
  loading.value = true
  try {
    // 假设后端线索分页接口为 /intent/page
    const res = await request.post('/intent/page', queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取线索列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索触发
const handleSearch = () => {
  queryParams.current = 1
  fetchIntents()
}

// 分页触发
const handleCurrentChange = (val) => {
  queryParams.current = val
  fetchIntents()
}

// --- 标记为已处理 ---
const handleProcess = (row) => {
  ElMessageBox.confirm(`确定将尾号为 ${row.contactPhone.slice(-4)} 的客户线索标记为“已处理”吗？`, '处理确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success',
  }).then(async () => {
    try {
      // 发送状态更新请求
      const res = await request.post('/intent/update', { id: row.id, status: 1 })
      if (res.code === 200) {
        ElMessage.success('已成功标记为处理完成')
        fetchIntents() // 刷新表格
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('网络异常')
    }
  }).catch(() => {})
}

// --- 删除线索 ---
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要永久删除这条用户留资线索吗？', '高危操作', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await request.delete(`/intent/delete/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchIntents()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchIntents()
})
</script>

<template>
  <div class="admin-intent-container">
    
    <div class="toolbar">
      <div class="search-area">
        <el-input 
          v-model="queryParams.keyword" 
          placeholder="搜索客户姓名或手机号" 
          clearable 
          :prefix-icon="Search"
          style="width: 260px; margin-right: 15px;"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索线索</el-button>
      </div>
    </div>

    <el-table 
      :data="tableData" 
      v-loading="loading" 
      border 
      stripe 
      style="width: 100%; margin-top: 20px;"
      height="calc(100vh - 240px)"
    >
      <el-table-column prop="id" label="线索ID" width="80" align="center" />
      <el-table-column prop="vehicleId" label="意向车型ID" width="100" align="center" />

      <el-table-column prop="brand" label="意向品牌" width="100" align="center">
        <template #default="scope">
          <el-tag type="info" size="small" v-if="scope.row.brand">{{ scope.row.brand }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="model" label="意向车型" min-width="150" align="center">
        <template #default="scope">
          <strong>{{ scope.row.model }}</strong>
        </template>
      </el-table-column>
      
      <el-table-column prop="contactPhone" label="联系电话" width="140" align="center">
        <template #default="scope">
          <span style="color: #409EFF; font-weight: bold;">{{ scope.row.contactPhone }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="message" label="客户留言 (含姓名/城市等)" min-width="250" />
      
      <el-table-column prop="createTime" label="提交时间" width="170" align="center" />

      <el-table-column prop="status" label="处理状态" width="120" align="center" fixed="right">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'" effect="dark">
            {{ scope.row.status === 1 ? '已跟进' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button 
            v-if="scope.row.status === 0" 
            size="small" 
            type="success" 
            :icon="Check" 
            @click="handleProcess(scope.row)"
          >标记处理</el-button>
          
          <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
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

<style scoped>
.admin-intent-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  height: 100%;
}

.toolbar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>