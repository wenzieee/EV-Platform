<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Plus as PlusIcon, Minus, Van } from '@element-plus/icons-vue'
import request from '../../utils/request'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  isAdmin: true
})

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

const handleSearch = () => {
  queryParams.current = 1
  fetchVehicles()
}

const handleCurrentChange = (val) => {
  queryParams.current = val
  fetchVehicles()
}

const dialogVisible = ref(false)
const trimDialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  brand: '',
  model: '',
  minPrice: undefined,
  maxPrice: undefined,
  rangeKm: undefined,
  driveType: '',
  imageUrl: '',
  hotScore: 0,
  status: 1
})

const trims = ref([
  { id: null, trimName: '', price: undefined, rangeKm: undefined, batteryCapacity: undefined, motorPower: undefined, maxSpeed: undefined, accelerationTime: undefined, chargeTime: undefined, colors: '', driveType: '', isHot: 0, status: 1 }
])

const rules = {
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入车型', trigger: 'blur' }],
  rangeKm: [{ required: true, message: '请输入续航', trigger: 'blur' }]
}

const formRef = ref(null)

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { id: null, brand: '', model: '', minPrice: undefined, maxPrice: undefined, rangeKm: undefined, driveType: '', imageUrl: '', hotScore: 0, status: 1 })
  trims.value = [{ id: null, trimName: '', price: undefined, rangeKm: undefined, batteryCapacity: undefined, motorPower: undefined, maxSpeed: undefined, accelerationTime: undefined, chargeTime: undefined, colors: '', driveType: '', isHot: 0, status: 1 }]
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const openEditDialog = async (row) => {
  isEdit.value = true
  Object.assign(form, row)
  
  try {
    const res = await request.get(`/vehicle/${row.id}`)
    if (res.code === 200) {
      trims.value = res.data.trims || [{ id: null, trimName: '', price: undefined, rangeKm: undefined, batteryCapacity: undefined, motorPower: undefined, maxSpeed: undefined, accelerationTime: undefined, chargeTime: undefined, colors: '', driveType: '', isHot: 0, status: 1 }]
    }
  } catch (error) {
    console.error('获取车辆配置失败:', error)
    trims.value = [{ id: null, trimName: '', price: undefined, rangeKm: undefined, batteryCapacity: undefined, motorPower: undefined, maxSpeed: undefined, accelerationTime: undefined, chargeTime: undefined, colors: '', driveType: '', isHot: 0, status: 1 }]
  }
  
  dialogVisible.value = true
}

const addTrimRow = () => {
  trims.value.push({ id: null, trimName: '', price: undefined, rangeKm: undefined, batteryCapacity: undefined, motorPower: undefined, maxSpeed: undefined, accelerationTime: undefined, chargeTime: undefined, colors: '', driveType: '', isHot: 0, status: 1 })
}

const removeTrimRow = (index) => {
  if (trims.value.length > 1) {
    trims.value.splice(index, 1)
  } else {
    ElMessage.warning('至少需要保留一个配置')
  }
}

const handleSave = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const validTrims = trims.value.filter(t => t.trimName && t.price)
        if (validTrims.length === 0) {
          ElMessage.warning('请至少添加一个配置版本')
          return
        }
        
        const data = {
          vehicle: { ...form },
          trims: validTrims
        }
        
        const url = isEdit.value ? '/vehicle/update' : '/vehicle/save'
        const res = await request.post(url, data)
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
          dialogVisible.value = false
          fetchVehicles()
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

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要永久删除该车辆数据吗？', '高危操作', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
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
  })
}

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

      <el-table-column label="指导价(万)" width="160" align="center">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: bold;">
            <template v-if="scope.row.minPrice && scope.row.maxPrice">
              {{ scope.row.minPrice }} - {{ scope.row.maxPrice }}
            </template>
            <template v-else-if="scope.row.price">
              {{ scope.row.price }}
            </template>
            <template v-else>
              -
            </template>
          </span>
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
      width="800px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="padding-right: 30px;">
        <el-form-item label="车辆品牌" prop="brand">
          <el-input v-model="form.brand" placeholder="例：比亚迪、特斯拉" />
        </el-form-item>
        
        <el-form-item label="车型名称" prop="model">
          <el-input v-model="form.model" placeholder="例：汉EV 创世版" />
        </el-form-item>
        
        <el-form-item label="价格区间" prop="price">
          <el-row :gutter="10">
            <el-col :span="11">
              <el-input-number v-model="form.minPrice" :precision="2" :step="0.1" :min="0" style="width: 100%;" placeholder="最低价格" />
            </el-col>
            <el-col :span="2" style="text-align: center; line-height: 40px;">-</el-col>
            <el-col :span="11">
              <el-input-number v-model="form.maxPrice" :precision="2" :step="0.1" :min="0" style="width: 100%;" placeholder="最高价格" />
            </el-col>
          </el-row>
          <div class="form-tip">单位：万元（可通过下方配置自动计算）</div>
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

        <el-form-item label="车型配置（SKU）">
          <div class="trim-section">
            <div class="trim-header">
              <span style="font-weight: bold; display: flex; align-items: center;">
                <el-icon><Van /></el-icon>
                配置版本列表
              </span>
              <el-button type="primary" size="small" :icon="PlusIcon" @click="addTrimRow">添加配置</el-button>
            </div>
            
            <div v-for="(trim, index) in trims" :key="index" class="trim-row">
              <div class="trim-index">配置 {{ index + 1 }}</div>
              <el-row :gutter="15" class="trim-fields">
                <el-col :span="7">
                  <el-input v-model="trim.trimName" placeholder="配置名称（例：标准版）" style="width: 100%;" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="trim.price" :precision="2" :step="0.1" :min="0" placeholder="价格(万)" controls-position="right" style="width: 100%;" />
                </el-col>
                <el-col :span="5">
                  <el-input-number v-model="trim.rangeKm" :min="0" :step="10" placeholder="续航(km)" controls-position="right" style="width: 100%;" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="trim.batteryCapacity" :precision="1" :step="1" :min="0" placeholder="电池(kWh)" controls-position="right" style="width: 100%;" />
                </el-col>

                <el-col :span="6">
                  <el-input-number v-model="trim.motorPower" :min="0" :step="10" placeholder="功率(kW)" controls-position="right" style="width: 100%;" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="trim.maxSpeed" :min="0" placeholder="最高速" controls-position="right" style="width: 100%;" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="trim.accelerationTime" :precision="1" :step="0.1" placeholder="加速(s)" controls-position="right" style="width: 100%;" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="trim.chargeTime" :min="0" placeholder="快充(分)" controls-position="right" style="width: 100%;" />
                </el-col>

                <el-col :span="8">
                  <el-input v-model="trim.colors" placeholder="颜色（逗号分隔）" style="width: 100%;" />
                </el-col>
                <el-col :span="6">
                  <el-select v-model="trim.driveType" placeholder="驱动方式" style="width: 100%;">
                    <el-option label="后驱" value="后驱" />
                    <el-option label="四驱" value="四驱" />
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-select v-model="trim.isHot" placeholder="是否主推" style="width: 100%;">
                    <el-option :label="'普通'" :value="0" />
                    <el-option :label="'主推'" :value="1" />
                  </el-select>
                </el-col>
                <el-col :span="4" style="display: flex; align-items: center; justify-content: flex-end;">
                  <el-button v-if="trims.length > 1" type="danger" size="small" :icon="Minus" @click="removeTrimRow(index)">删除</el-button>
                </el-col>
              </el-row>
            </div>
          </div>
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

.trim-section {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
}

.trim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}

.trim-row {
  margin-bottom: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
}

.trim-row:last-child {
  margin-bottom: 0;
}

.trim-index {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 15px;
}

.trim-fields {
  align-items: center;
}

.trim-fields .el-col {
  margin-bottom: 10px;
}

.trim-fields .el-row {
  margin-bottom: 5px;
}
</style>