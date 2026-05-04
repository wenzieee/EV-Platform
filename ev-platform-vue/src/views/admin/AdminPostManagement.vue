<template>
  <div class="admin-post-management">
    <h1>帖子管理</h1>

    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="帖子标题">
        <el-input v-model="searchForm.title" placeholder="请输入帖子标题"></el-input>
      </el-form-item>
      <el-form-item label="发布者ID">
        <el-input v-model="searchForm.userId" placeholder="请输入发布者ID"></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态">
          <el-option label="全部" :value="null"></el-option>
          <el-option label="正常" :value="1"></el-option>
          <el-option label="已删除" :value="0"></el-option>
          <el-option label="彻底删除" :value="-1"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchPosts">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 帖子列表 -->
    <el-table :data="posts" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip></el-table-column>
      <el-table-column prop="username" label="发布者" width="120"></el-table-column>

      <el-table-column prop="likeCount" label="点赞" width="80"></el-table-column>
      <el-table-column prop="commentCount" label="评论" width="80"></el-table-column>
      <el-table-column prop="collectCount" label="收藏" width="80"></el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ statusMap[scope.row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180"></el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="viewPostDetail(scope.row.id)">详情</el-button>
          <el-button link type="danger" size="small" @click="deletePost(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.pageNum"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total"
      background
      class="pagination"
    >
    </el-pagination>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

// 搜索表单数据
const searchForm = ref({
  title: null,
  userId: null,
  status: null,
});

// 帖子列表数据
const posts = ref([]);

// 分页数据
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

// 状态映射
const statusMap = ref({
  1: '正常',
  0: '已删除',
  '-1': '彻底删除',
});

// 获取帖子列表
const fetchPosts = async () => {
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      ...searchForm.value,
    };
    const response = await axios.get('/api/post/admin/page', {
      params,
      headers: {
        token: localStorage.getItem('token') // 添加token请求头
      }
    });
    if (response.data.code === 200) {
      posts.value = response.data.data.records;
      pagination.value.total = response.data.data.total;
    } else {
      ElMessage.error(response.data.msg);
    }
  } catch (error) {
    ElMessage.error('获取帖子失败');
    console.error('Error fetching posts:', error);
  }
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    title: null,
    userId: null,
    status: null,
  };
  pagination.value.pageNum = 1;
  fetchPosts();
};

// 查看帖子详情
const viewPostDetail = (postId) => {
  router.push(`/post/${postId}`); // 跳转到帖子详情页
};

// 删除帖子
const deletePost = async (postId) => {
  ElMessageBox.confirm(
    `确定要彻底删除ID为 ${postId} 的帖子吗？该操作不可逆！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const response = await axios.delete(`/api/post/admin/${postId}`, {
          headers: {
            token: localStorage.getItem('token') // 添加token请求头
          }
        });
        if (response.data.code === 200) {
          ElMessage.success('帖子删除成功');
          fetchPosts(); // 刷新列表
        } else {
          ElMessage.error(response.data.msg);
        }
      } catch (error) {
        ElMessage.error('删除帖子失败');
        console.error('Error deleting post:', error);
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    });
};

// 处理每页显示数量变化
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize;
  fetchPosts();
};

// 处理当前页码变化
const handleCurrentChange = (newPage) => {
  pagination.value.pageNum = newPage;
  fetchPosts();
};

// 组件挂载时获取数据
onMounted(() => {
  fetchPosts();
});
</script>

<style scoped>
.admin-post-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>