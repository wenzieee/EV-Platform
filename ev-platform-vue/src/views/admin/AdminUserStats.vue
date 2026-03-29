<template>
  <div class="admin-user-stats-container">
    <h2>用户统计</h2>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户角色分布</span>
            </div>
          </template>
          <div v-if="hasRoleData" ref="roleChart" style="width: 100%; height: 400px;"></div>
          <div v-else class="no-data-message">暂无用户角色数据</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户状态分布</span>
            </div>
          </template>
          <div v-if="hasStatusData" ref="statusChart" style="width: 100%; height: 400px;"></div>
          <div v-else class="no-data-message">暂无用户状态数据</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import axios from 'axios';

const roleChart = ref(null);
const statusChart = ref(null);
let myRoleChart = null;
let myStatusChart = null;

// 控制图表或“暂无数据”消息的显示
const hasRoleData = ref(false);
const hasStatusData = ref(false);

const fetchUserStats = async () => {
  try {
    const response = await axios.get('/api/user/stats');
    console.log('API Response:', response); // 添加日志
    if (response.data.code === 200) {
      const stats = response.data.data;
      console.log('User Stats Data:', stats); // 添加日志

      if (stats.roleStats && stats.roleStats.length > 0) {
        hasRoleData.value = true;
        nextTick(() => {
          renderRoleChart(stats.roleStats);
        });
      } else {
        hasRoleData.value = false;
      }

      if (stats.statusStats && stats.statusStats.length > 0) {
        hasStatusData.value = true;
        nextTick(() => {
          renderStatusChart(stats.statusStats);
        });
      } else {
        hasStatusData.value = false;
      }

    } else {
      hasRoleData.value = false;
      hasStatusData.value = false;      console.error('Failed to fetch user stats:', response.data.msg);
    }
  } catch (error) {
    console.error('Error fetching user stats:', error);
  }
};

const renderRoleChart = (data) => {
  if (myRoleChart) {
    myRoleChart.dispose();
  }
  myRoleChart = echarts.init(roleChart.value);
  const option = {
    title: {
      text: '用户角色分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '角色',
        type: 'pie',
        radius: '50%',
        data: data.map(item => ({ value: item.count, name: item.roleText })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  myRoleChart.setOption(option);
};

const renderStatusChart = (data) => {
  if (myStatusChart) {
    myStatusChart.dispose();
  }
  myStatusChart = echarts.init(statusChart.value);
  const option = {
    title: {
      text: '用户状态分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '状态',
        type: 'pie',
        radius: '50%',
        data: data.map(item => ({ value: item.count, name: item.statusText })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  myStatusChart.setOption(option);
};

const handleResize = () => {
  myRoleChart && myRoleChart.resize();
  myStatusChart && myStatusChart.resize();
};

onMounted(() => {
  fetchUserStats();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  if (myRoleChart) {
    myRoleChart.dispose();
  }
  if (myStatusChart) {
    myStatusChart.dispose();
  }
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.admin-user-stats-container {
  padding: 20px;
}
.chart-card {
  margin-bottom: 20px;
}
.card-header {
  font-weight: bold;
}

.no-data-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px; /* 与图表高度一致 */
  color: #909399;
  font-size: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
