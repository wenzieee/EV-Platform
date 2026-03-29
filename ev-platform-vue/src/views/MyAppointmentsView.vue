<template>
  <div class="my-appointments-container">
    <h2>我的预约记录</h2>
    <el-table :data="appointments" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="意向车型" width="180">
        <template #default="scope">
          {{ scope.row.brand }} {{ scope.row.model }}
        </template>
      </el-table-column>
      <el-table-column prop="contactPhone" label="联系电话" width="120"></el-table-column>
      <el-table-column prop="message" label="留言信息"></el-table-column>
      <el-table-column prop="status" label="跟进状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
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

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const appointments = ref([]);
const total = ref(0);
const loading = ref(false);
const queryParams = reactive({ current: 1, size: 10 });

const fetchMyAppointments = async () => {
  loading.value = true;
  try {
    // 假设后端提供 /intent/my-records 接口来获取当前用户的预约记录
    const res = await request.post('/intent/my-records', queryParams);
    if (res.code === 200) {
      appointments.value = res.data.records;
      total.value = res.data.total;
    } else {
      ElMessage.error(res.msg || '获取预约记录失败');
    }
  } catch (error) {
    console.error('获取预约记录失败:', error);
    ElMessage.error('网络异常，请稍后再试');
  } finally {
    loading.value = false;
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待跟进';
    case 1: return '跟进中';
    case 2: return '已成交';
    case 3: return '已取消';
    default: return '未知';
  }
};

const getStatusTagType = (status) => {
  switch (status) {
    case 0: return 'warning'; // 待跟进
    case 1: return '';        // 跟进中 (默认)
    case 2: return 'success'; // 已成交
    case 3: return 'danger';  // 已取消
    default: return 'info';
  }
};

const handleCurrentChange = (val) => {
  queryParams.current = val;
  fetchMyAppointments();
};

onMounted(() => {
  fetchMyAppointments();
});
</script>

<style scoped>
.my-appointments-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>