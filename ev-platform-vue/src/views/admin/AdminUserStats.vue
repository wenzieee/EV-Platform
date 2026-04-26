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
// ⚠️ 修复 1：引入封装好的 request，而不是原生 axios
import request from '../../utils/request';

const roleChart = ref(null);
const statusChart = ref(null);
let myRoleChart = null;
let myStatusChart = null;

// 控制图表或“暂无数据”消息的显示
const hasRoleData = ref(false);
const hasStatusData = ref(false);

const fetchUserStats = async () => {
  try {
    // ⚠️ 修复 1：使用 request 发送请求，它会自动带上 Token
    const res = await request.get('/user/stats');

    // request 工具通常已经解开了外层的 axios data，直接判断 res.code 即可
    if (res.code === 200) {
      const stats = res.data;

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
      hasStatusData.value = false;
      console.error('Failed to fetch user stats:', res.msg);
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
  // ⚠️ 修复 2：将短路表达式改为正规的 if 语句，解决 ESLint 报错
  if (myRoleChart) {
    myRoleChart.resize();
  }
  if (myStatusChart) {
    myStatusChart.resize();
  }
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
