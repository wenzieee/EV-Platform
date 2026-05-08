<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Location } from '@element-plus/icons-vue'
import request from '../../utils/request'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

const fetchDealers = async () => {
  loading.value = true
  try {
    const res = await request.get('/dealer/page', {
      params: {
        pageNum: queryParams.current,
        pageSize: queryParams.size,
        keyword: queryParams.keyword
      }
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取门店列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  fetchDealers()
}

const handleCurrentChange = (val) => {
  queryParams.current = val
  fetchDealers()
}

const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  brand: '',
  province: '',
  city: '',
  address: '',
  phone: '',
  longitude: '',
  latitude: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const formRef = ref(null)

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', brand: '', province: '', city: '', address: '', phone: '', longitude: '', latitude: '', status: 1 })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '此操作将永久删除该门店, 是否继续?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await request.delete(`/dealer/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功!')
      fetchDealers()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.info('已取消删除')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    const postData = {
      ...form,
      longitude: form.longitude ? parseFloat(form.longitude) : null,
      latitude: form.latitude ? parseFloat(form.latitude) : null
    }
    
    if (isEdit.value) {
      const res = await request.put('/dealer', postData)
      if (res.code === 200) {
        ElMessage.success('更新成功!')
      } else {
        ElMessage.error(res.msg || '更新失败')
        return
      }
    } else {
      const res = await request.post('/dealer', postData)
      if (res.code === 200) {
        ElMessage.success('新增成功!')
      } else {
        ElMessage.error(res.msg || '新增失败')
        return
      }
    }
    
    dialogVisible.value = false
    fetchDealers()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

onMounted(() => {
  fetchDealers()
})
</script>

<template>
  <div class="admin-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">4S店管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog">新增门店</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索门店名称"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix-icon>
          <Search />
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table 
        :data="tableData" 
        :loading="loading" 
        border 
        stripe 
        style="width: 100%; margin-top: 20px;" 
      > 
        <el-table-column prop="id" label="ID" width="60" align="center" /> 
        <el-table-column prop="name" label="门店名称" min-width="120" show-overflow-tooltip /> 
        <el-table-column prop="brand" label="品牌" width="90" align="center" /> 
        <el-table-column prop="province" label="省份" width="70" align="center" /> 
        <el-table-column prop="city" label="城市" width="70" align="center" /> 
        <el-table-column prop="address" label="地址" min-width="140" show-overflow-tooltip /> 
        <el-table-column prop="phone" label="电话" width="140" align="center" /> 
        
        <el-table-column prop="status" label="状态" width="80" align="center"> 
          <template #default="scope"> 
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small"> 
              {{ scope.row.status === 1 ? '营业中' : '关闭' }} 
            </el-tag> 
          </template> 
        </el-table-column> 
        
        <el-table-column label="操作" width="140" align="center" fixed="right"> 
          <template #default="scope"> 
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">编辑</el-button> 
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button> 
          </template> 
        </el-table-column> 
      </el-table> 

      <div class="pagination-container"> 
       <el-pagination 
         background 
         layout="total, prev, pager, next, jumper" 
         :total="total" 
         v-model:current-page="queryParams.current" 
         :page-size="queryParams.size" 
         @current-change="handleCurrentChange" 
       /> 
     </div>

    <el-dialog title="门店信息" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="form.brand" placeholder="请输入品牌名称" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model="form.longitude" placeholder="请输入经度" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model="form.latitude" placeholder="请输入纬度" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="营业中" :value="1" />
            <el-option label="关闭" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '更新' : '新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-container {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
