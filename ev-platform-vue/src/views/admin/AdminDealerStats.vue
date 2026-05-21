<template>
  <div class="dealer-stats-container">
    <h2>经销商数据统计</h2>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>按状态统计经销商数量</h3>
          <div ref="statusChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>按品牌统计经销商数量</h3>
          <div ref="brandChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '../../utils/request';
import * as echarts from 'echarts';

const statusChart = ref(null);
const brandChart = ref(null);
let statusChartInstance = null;
let brandChartInstance = null;

// 模拟数据（用于测试）
const mockStats = {
  statusStats: [
    { status: 1, count: 80 },
    { status: 0, count: 20 },
  ],
  brandStats: [
    { brand: '比亚迪', count: 25 },
    { brand: '特斯拉', count: 18 },
    { brand: '蔚来', count: 12 },
    { brand: '小鹏', count: 15 },
    { brand: '理想', count: 10 },
    { brand: '华为', count: 8 },
  ],
};

const fetchDealerStats = async () => {
  try {
    const res = await request.get('/dealer/stats');
    console.log('接口返回数据:', res);
    
    // 检查数据结构是否正确
    if (res.code === 200 && res.data && typeof res.data === 'object' && !Array.isArray(res.data)) {
      // 检查是否包含必要的数据字段（状态和品牌）
      const hasRequiredData = res.data.statusStats || res.data.brandStats;
      if (hasRequiredData) {
        renderCharts(res.data);
        return;
      }
    }
    
    // 如果数据不符合预期，使用模拟数据
    console.warn('接口返回数据不符合预期，使用模拟数据');
    renderCharts(mockStats);
    ElMessage.warning('数据加载异常，显示演示数据');
  } catch (error) {
    console.error('获取经销商统计数据失败:', error);
    // 使用模拟数据
    renderCharts(mockStats);
    ElMessage.warning('网络异常，显示演示数据');
  }
};

const renderCharts = (data) => {
  console.log('开始渲染图表，数据:', data);
  const { statusStats, brandStats } = data;

  // 渲染状态饼图
  if (statusChartInstance && statusStats && Array.isArray(statusStats)) {
    const pieData = statusStats.map(item => ({
      name: item.status === 1 ? '营业中' : '已关闭',
      value: typeof item.count === 'string' ? parseInt(item.count) : (item.count || 0),
    }));
    statusChartInstance.setOption({
      series: [
        {
          data: pieData,
        },
      ],
    });
  }

  // 渲染品牌统计柱状图
  if (brandChartInstance && brandStats && Array.isArray(brandStats)) {
    const brandNames = brandStats.map(item => item.brand || item.name || '未知');
    const brandCounts = brandStats.map(item => typeof item.count === 'string' ? parseInt(item.count) : (item.count || 0));
    brandChartInstance.setOption({
      xAxis: {
        data: brandNames,
      },
      series: [
        {
          data: brandCounts,
        },
      ],
    });
  }
};

onMounted(() => {
  // 初始化状态饼图
  statusChartInstance = echarts.init(statusChart.value);
  statusChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '20',
            fontWeight: 'bold'
          }
        },
        labelLine: { show: false },
        data: [],
      },
    ],
  });

  // 初始化品牌统计柱状图
  brandChartInstance = echarts.init(brandChart.value);
  brandChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '经销商数量',
        type: 'bar',
        itemStyle: {
          color: '#91CC75'
        }
      },
    ],
  });

  fetchDealerStats();

  // 监听页面大小变化，重新渲染图表
  window.addEventListener('resize', () => {
    statusChartInstance.resize();
    brandChartInstance.resize();
  });
});

onUnmounted(() => {
  // 销毁图表实例，防止内存泄漏
  if (statusChartInstance) {
    statusChartInstance.dispose();
  }
  if (brandChartInstance) {
    brandChartInstance.dispose();
  }
});
</script>

<style scoped>
.dealer-stats-container {
  padding: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-card h3 {
  text-align: center;
  margin-bottom: 15px;
  color: #333;
}

.chart {
  width: 100%;
  height: 350px;
}
</style>