<template>
  <div class="intent-stats-container">
    <h2>用户线索(订单)统计</h2>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="chart-card">
          <h3>按品牌统计线索数量</h3>
          <div ref="brandChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <h3>按车型统计线索数量</h3>
          <div ref="modelChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <h3>按状态统计线索数量</h3>
          <div ref="statusChart" class="chart"></div>
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

const brandChart = ref(null);
const modelChart = ref(null);
const statusChart = ref(null);
let brandChartInstance = null;
let modelChartInstance = null;
let statusChartInstance = null;

const fetchIntentStats = async () => {
  try {
    const res = await request.get('/intent/stats');
    if (res.code === 200) {
      const { brandStats, modelStats, statusStats } = res.data;

      // Render brand stats bar chart
      if (brandChartInstance) {
        const brandNames = brandStats.map(item => item.brand);
        const brandCounts = brandStats.map(item => item.count);
        brandChartInstance.setOption({
          xAxis: { data: brandNames },
          series: [{ data: brandCounts }],
        });
      }

      // Render model stats bar chart
      if (modelChartInstance) {
        const modelNames = modelStats.map(item => item.model);
        const modelCounts = modelStats.map(item => item.count);
        modelChartInstance.setOption({
          xAxis: { data: modelNames },
          series: [{ data: modelCounts }],
        });
      }

      // Render status pie chart
      if (statusChartInstance) {
        const pieData = statusStats.map(item => ({
          name: item.statusText, // 使用 statusText 作为名称
          value: item.count,
        }));
        statusChartInstance.setOption({
          series: [{ data: pieData }],
        });
      }
    } else {
      ElMessage.error(res.msg || '获取用户线索统计数据失败');
    }
  } catch (error) {
    console.error('获取用户线索统计数据失败:', error);
    ElMessage.error('网络异常，请稍后再试');
  }
};

onMounted(() => {
  // Initialize brand stats bar chart
  brandChartInstance = echarts.init(brandChart.value);
  brandChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', axisLabel: { interval: 0, rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{ name: '线索数量', type: 'bar', itemStyle: { color: '#5470C6' } }],
  });

  // Initialize model stats bar chart
  modelChartInstance = echarts.init(modelChart.value);
  modelChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', axisLabel: { interval: 0, rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{ name: '线索数量', type: 'bar', itemStyle: { color: '#91CC75' } }],
  });

  // Initialize status pie chart
  statusChartInstance = echarts.init(statusChart.value);
  statusChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '线索状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: '20', fontWeight: 'bold' } },
      labelLine: { show: false },
      data: [],
    }],
  });

  fetchIntentStats();

  // Listen for window resize to redraw charts
  window.addEventListener('resize', () => {
    brandChartInstance.resize();
    modelChartInstance.resize();
    statusChartInstance.resize();
  });
});

onUnmounted(() => {
  // Dispose chart instances to prevent memory leaks
  if (brandChartInstance) { brandChartInstance.dispose(); }
  if (modelChartInstance) { modelChartInstance.dispose(); }
  if (statusChartInstance) { statusChartInstance.dispose(); }
  window.removeEventListener('resize', () => {
    brandChartInstance.resize();
    modelChartInstance.resize();
    statusChartInstance.resize();
  });
});
</script>

<style scoped>
.intent-stats-container { padding: 20px; }
.chart-card { margin-bottom: 20px; }
.chart-card h3 { text-align: center; margin-bottom: 15px; color: #333; }
.chart { width: 100%; height: 400px; }
</style>
