<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '../../utils/request'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const queryParams = reactive({ current: 1, size: 10, keyword: '' })

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await request.post('/user/page', queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.current = 1; fetchUsers() }
const handleCurrentChange = (val) => { queryParams.current = val; fetchUsers() }

// 普通管理员只能封禁普通用户，只传 status 即可
const handleStatusChange = async (row) => {
  try {
    const res = await request.post('/user/update', { id: row.id, status: row.status })
    if (res.code === 200) {
      ElMessage.success('用户状态已更新')
    } else {
      ElMessage.error(res.msg)
      fetchUsers()
    }
  } catch (e) { fetchUsers() }
}

onMounted(() => fetchUsers())
</script>

<template>
  <div class="admin-user-container">
    <div class="toolbar">
      <div class="search-area">
        <el-input v-model="queryParams.keyword" placeholder="搜索普通用户" clearable :prefix-icon="Search" style="width: 260px; margin-right: 15px;" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 20px;" height="calc(100vh - 240px)">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="150" />
      
      <el-table-column label="身份标识" width="160" align="center">
        <template #default="scope">
          <el-tag type="danger" effect="dark" v-if="scope.row.role === 0">超级管理员</el-tag>
          <el-tag type="warning" v-else-if="scope.row.role === 2">普通管理员</el-tag>
          <el-tag type="info" v-else>普通用户</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
      
      <el-table-column label="账号状态 (仅限封禁普通用户)" min-width="150" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" inline-prompt active-text="正常" inactive-text="封禁" :disabled="scope.row.role !== 1" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination background layout="total, prev, pager, next, jumper" :total="total" v-model:current-page="queryParams.current" :page-size="queryParams.size" @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<style scoped>
.admin-user-container { background: #fff; padding: 20px; border-radius: 8px; height: 100%; }
.toolbar { display: flex; justify-content: flex-start; align-items: center; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>