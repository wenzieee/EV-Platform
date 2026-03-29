<template>
  <div class="vehicle-stats-container">
    <h2>在售车辆数据统计</h2>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>按品牌统计车辆数量</h3>
          <div ref="brandChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>按驱动类型统计车辆数量</h3>
          <div ref="driveTypeChart" class="chart"></div>
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
const driveTypeChart = ref(null);
let brandChartInstance = null;
let driveTypeChartInstance = null;

const fetchVehicleStats = async () => {
  try {
    const res = await request.get('/vehicle/stats');
    if (res.code === 200) {
      const { brandStats, driveTypeStats } = res.data;
      
      // 渲染品牌统计柱状图
      if (brandChartInstance) {
        const brandNames = brandStats.map(item => item.brand);
        const brandCounts = brandStats.map(item => item.count);
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

      // 渲染驱动类型饼图
      if (driveTypeChartInstance) {
        const pieData = driveTypeStats.map(item => ({
          name: item.driveType,
          value: item.count,
        }));
        driveTypeChartInstance.setOption({
          series: [
            {
              data: pieData,
            },
          ],
        });
      }

    } else {
      ElMessage.error(res.msg || '获取车辆统计数据失败');
    }
  } catch (error) {
    console.error('获取车辆统计数据失败:', error);
    ElMessage.error('网络异常，请稍后再试');
  }
};

onMounted(() => {
  // 初始化品牌统计柱状图
  brandChartInstance = echarts.init(brandChart.value);
  brandChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      axisLabel: { interval: 0, rotate: 30 } // 旋转标签防止重叠
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '车辆数量',
        type: 'bar',
        itemStyle: {
          color: '#5470C6' // 柱状图颜色
        }
      },
    ],
  });

  // 初始化驱动类型饼图
  driveTypeChartInstance = echarts.init(driveTypeChart.value);
  driveTypeChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '驱动类型',
        type: 'pie',
        radius: ['40%', '70%'], // 甜甜圈图
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

  fetchVehicleStats();

  // 监听页面大小变化，重新渲染图表
  window.addEventListener('resize', () => {
    brandChartInstance.resize();
    driveTypeChartInstance.resize();
  });
});

onUnmounted(() => {
  // 销毁图表实例，防止内存泄漏
  if (brandChartInstance) {
    brandChartInstance.dispose();
  }
  if (driveTypeChartInstance) {
    driveTypeChartInstance.dispose();
  }
  window.removeEventListener('resize', () => {
    brandChartInstance.resize();
    driveTypeChartInstance.resize();
  });
});
</script>

<style scoped>
.vehicle-stats-container {
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
  height: 400px; /* 设置图表高度 */
}
</style>