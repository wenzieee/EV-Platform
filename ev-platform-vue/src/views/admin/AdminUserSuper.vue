<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete } from '@element-plus/icons-vue'
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

const handleUpdate = async (row) => {
  try {
    const res = await request.post('/user/update', { id: row.id, status: row.status, role: row.role })
    if (res.code === 200) {
      ElMessage.success('更新成功')
    } else {
      ElMessage.error(res.msg)
      fetchUsers()
    }
  } catch (e) { fetchUsers() }
}

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '', role: 1, status: 1 })

const openAddDialog = () => {
  Object.assign(form, { username: '', password: '', role: 1, status: 1 })
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

const handleAdd = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      const res = await request.post('/user/add', form)
      if (res.code === 200) {
        ElMessage.success(res.msg)
        dialogVisible.value = false
        fetchUsers()
      } else { ElMessage.error(res.msg) }
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定永久删除该账号吗？', '高危操作', { type: 'warning' }).then(async () => {
    const res = await request.delete(`/user/delete/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchUsers()
    } else { ElMessage.error(res.msg) }
  }).catch(() => {})
}

onMounted(() => fetchUsers())
</script>

<template>
  <div class="admin-user-container">
    <div class="toolbar">
      <div class="search-area">
        <el-input v-model="queryParams.keyword" placeholder="搜索用户名" clearable :prefix-icon="Search" style="width: 260px; margin-right: 15px;" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-button type="success" :icon="Plus" @click="openAddDialog">新增账号</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 20px;" height="calc(100vh - 240px)">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="150" />
      
      <el-table-column label="角色分配" width="160" align="center">
        <template #default="scope">
          <el-tag type="danger" effect="dark" v-if="scope.row.role === 0">超级管理员</el-tag>
          <el-select v-else v-model="scope.row.role" size="small" @change="handleUpdate(scope.row)">
            <el-option label="普通用户" :value="1" />
            <el-option label="普通管理员" :value="2" />
          </el-select>
        </template>
      </el-table-column>
      
      <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
      
      <el-table-column label="账号状态" width="120" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" inline-prompt active-text="正常" inactive-text="封禁" :disabled="scope.row.role === 0" @change="handleUpdate(scope.row)" />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.role !== 0" size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination background layout="total, prev, pager, next, jumper" :total="total" v-model:current-page="queryParams.current" :page-size="queryParams.size" @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="dialogVisible" title="新增账号" width="400px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '必填' }]">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" placeholder="默认123456" /></el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role"><el-radio :label="1">普通用户</el-radio><el-radio :label="2">普通管理员</el-radio></el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleAdd">确认</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-user-container { background: #fff; padding: 20px; border-radius: 8px; height: 100%; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>