<template>
  <div class="admin-comment-management">
    <h1>评论管理</h1>

    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="评论内容">
        <el-input v-model="searchForm.content" placeholder="请输入评论内容关键词"></el-input>
      </el-form-item>
      <el-form-item label="发布者ID">
        <el-input v-model="searchForm.userId" placeholder="请输入发布者ID"></el-input>
      </el-form-item>
      <el-form-item label="帖子ID">
        <el-input v-model="searchForm.postId" placeholder="请输入帖子ID"></el-input>
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
        <el-button type="primary" @click="fetchComments">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 评论列表 -->
    <el-table :data="comments" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="content" label="评论内容" show-overflow-tooltip></el-table-column>
      <el-table-column prop="username" label="评论者" width="120"></el-table-column>
      <el-table-column prop="postId" label="帖子ID" width="100"></el-table-column>
      <el-table-column prop="postTitle" label="帖子标题" show-overflow-tooltip></el-table-column>
      <el-table-column prop="likeCount" label="点赞数" width="80"></el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ statusMap[scope.row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="180"></el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="danger" size="small" @click="deleteComment(scope.row.id)">删除</el-button>
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

// 搜索表单数据
const searchForm = ref({
  content: null,
  userId: null,
  postId: null,
  status: null,
});

// 评论列表数据
const comments = ref([]);

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

// 获取评论列表
const fetchComments = async () => {
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      ...searchForm.value,
    };
    const response = await axios.get('/api/comment/admin/page', {
      params,
      headers: {
        token: localStorage.getItem('token') // 添加token请求头
      }
    });
    if (response.data.code === 200) {
      comments.value = response.data.data.records;
      pagination.value.total = response.data.data.total;
    } else {
      ElMessage.error(response.data.msg);
    }
  } catch (error) {
    ElMessage.error('获取评论失败');
    console.error('Error fetching comments:', error);
  }
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    content: null,
    userId: null,
    postId: null,
    status: null,
  };
  pagination.value.pageNum = 1;
  fetchComments();
};

// 删除评论
const deleteComment = async (commentId) => {
  ElMessageBox.confirm(
    `确定要彻底删除ID为 ${commentId} 的评论吗？该操作不可逆！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const response = await axios.delete(`/api/comment/admin/${commentId}`, {
          headers: {
            token: localStorage.getItem('token') // 添加token请求头
          }
        });
        if (response.data.code === 200) {
          ElMessage.success('评论删除成功');
          fetchComments(); // 刷新列表
        } else {
          ElMessage.error(response.data.msg);
        }
      } catch (error) {
        ElMessage.error('删除评论失败');
        console.error('Error deleting comment:', error);
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    });
};

// 处理每页显示数量变化
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize;
  fetchComments();
};

// 处理当前页码变化
const handleCurrentChange = (newPage) => {
  pagination.value.pageNum = newPage;
  fetchComments();
};

// 组件挂载时获取数据
onMounted(() => {
  fetchComments();
});
</script>

<style scoped>
.admin-comment-management {
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