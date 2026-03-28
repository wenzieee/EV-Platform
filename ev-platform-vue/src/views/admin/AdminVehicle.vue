<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import request from '../../utils/request'

// --- 表格与分页数据 ---
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  isAdmin: true
})

// --- 获取表格数据 ---
const fetchVehicles = async () => {
  loading.value = true
  try {
    const res = await request.post('/vehicle/page', queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取车辆列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索触发
const handleSearch = () => {
  queryParams.current = 1 // 搜索时回到第一页
  fetchVehicles()
}

// 分页触发
const handleCurrentChange = (val) => {
  queryParams.current = val
  fetchVehicles()
}

// --- 弹窗与表单数据 ---
const dialogVisible = ref(false)
const isEdit = ref(false) // 区分是“新增”还是“编辑”

const form = reactive({
  id: null,
  brand: '',
  model: '',
  price: undefined,
  rangeKm: undefined,
  driveType: '',
  imageUrl: '', // 🚀 新增图片字段
  hotScore: 0, // 🚀 新增：热度值字段
  status: 1
})

// 表单校验规则
const rules = {
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入车型', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  rangeKm: [{ required: true, message: '请输入续航', trigger: 'blur' }]
}

const formRef = ref(null)

// 打开新增弹窗 (记得把 imageUrl 也清空)
const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { id: null, brand: '', model: '', price: undefined, rangeKm: undefined, driveType: '', imageUrl: '', hotScore: 0, status: 1 })
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  // 数据回显（将当前行的数据拷贝到表单中）
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交表单 (保存或修改)
const handleSave = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 判断调用的接口路径
        const url = isEdit.value ? '/vehicle/update' : '/vehicle/save'
        // 发送 POST 请求
        const res = await request.post(url, form)
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
          dialogVisible.value = false
          fetchVehicles() // 刷新表格
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('网络异常，请稍后再试')
      }
    }
  })
}

// --- 删除操作 ---
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要永久删除该车辆数据吗？', '高危操作', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      // 发送 DELETE 请求
      const res = await request.delete(`/vehicle/delete/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchVehicles()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 页面加载时获取第一页数据
onMounted(() => {
  fetchVehicles()
})
</script>

<template>
  <div class="admin-vehicle-container">
    
    <div class="toolbar">
      <div class="search-area">
        <el-input 
          v-model="queryParams.keyword" 
          placeholder="搜索品牌或车型" 
          clearable 
          :prefix-icon="Search"
          style="width: 260px; margin-right: 15px;"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <div class="action-area">
        <el-button type="success" :icon="Plus" @click="openAddDialog">新增车辆</el-button>
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
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="brand" label="品牌" width="120" />
      <el-table-column prop="model" label="车型" min-width="180" />
      
      <el-table-column label="车辆图片" width="100" align="center">
        <template #default="scope">
          <el-image
            style="width: 60px; height: 40px; border-radius: 4px;"
            :src="scope.row.imageUrl || `/cars/${scope.row.id}.jpg`"
            fit="cover"
            :preview-src-list="[scope.row.imageUrl || `/cars/${scope.row.id}.jpg`]"
            preview-teleported
          >
            <template #error>
              <div style="font-size: 12px; color: #999; line-height: 40px; background: #f5f7fa;">无图</div>
            </template>
          </el-image>
        </template>
      </el-table-column>

      <el-table-column prop="price" label="指导价(万)" width="120" align="center">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: bold;">{{ scope.row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rangeKm" label="续航(km)" width="100" align="center" />
      <el-table-column prop="driveType" label="驱动类型" width="140" />
      
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '上架中' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" :icon="Edit" @click="openEditDialog(scope.row)">编辑</el-button>
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

    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑车辆信息' : '新增车辆'" 
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="padding-right: 30px;">
        <el-form-item label="车辆品牌" prop="brand">
          <el-input v-model="form.brand" placeholder="例：比亚迪、特斯拉" />
        </el-form-item>
        
        <el-form-item label="车型名称" prop="model">
          <el-input v-model="form.model" placeholder="例：汉EV 创世版" />
        </el-form-item>
        
        <el-form-item label="指导价格" prop="price">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" style="width: 100%;" />
          <div class="form-tip">单位：万元</div>
        </el-form-item>
        
        <el-form-item label="续航里程" prop="rangeKm">
          <el-input-number v-model="form.rangeKm" :min="0" :step="10" style="width: 100%;" />
          <div class="form-tip">单位：km</div>
        </el-form-item>
        
        <el-form-item label="驱动类型" prop="driveType">
          <el-select v-model="form.driveType" placeholder="请选择" style="width: 100%;">
            <el-option label="前置前驱" value="前置前驱" />
            <el-option label="后置后驱" value="后置后驱" />
            <el-option label="双电机四驱" value="双电机四驱" />
          </el-select>
        </el-form-item>

        <el-form-item label="主图链接" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入网络图片URL(选填)" clearable />
          <div class="form-tip">若不填，将默认尝试读取本地 /cars/车辆ID.jpg</div>
        </el-form-item>

        <el-form-item label="车辆热度" prop="hotScore">
          <el-input-number v-model="form.hotScore" :min="0" :step="100" style="width: 100%;" />
          <div class="form-tip">热度值越大，在热门推荐中越靠前</div>
        </el-form-item>

        <el-form-item label="上架状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确认保存</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>
.admin-vehicle-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  height: 100%;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
</style>