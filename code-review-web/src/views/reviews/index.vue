<template>
  <div class="reviews-page">
    <div class="page-header">
      <h2>评审管理</h2>
      <el-button type="primary" @click="createReview">
        <el-icon><Plus /></el-icon>
        创建评审
      </el-button>
    </div>
    
    <el-card>
      <div class="filter-bar">
        <el-form :model="filters" inline>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="请选择状态" clearable>
              <el-option label="全部" value="" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已提交" value="SUBMITTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="评审者">
            <el-input v-model="filters.reviewer" placeholder="请输入评审者" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadReviews">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="reviews" v-loading="loading">
        <el-table-column prop="title" label="评审标题" />
        <el-table-column prop="reviewerName" label="评审者" />
        <el-table-column prop="revieweeName" label="被评审者" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="viewReview(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editReview(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadReviews"
          @current-change="loadReviews"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

interface Review {
  id: number
  title: string
  reviewerName: string
  revieweeName: string
  status: string
  createdAt: string
}

const loading = ref(false)
const reviews = ref<Review[]>([])

const filters = reactive({
  status: '',
  reviewer: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'SUBMITTED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'SUBMITTED': '已提交'
  }
  return statusMap[status] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const loadReviews = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取评审列表
    // 模拟数据
    reviews.value = [
      {
        id: 1,
        title: '用户登录模块代码评审',
        reviewerName: '张三',
        revieweeName: '李四',
        status: 'IN_PROGRESS',
        createdAt: '2023-12-01 10:00:00'
      },
      {
        id: 2,
        title: '订单处理逻辑优化',
        reviewerName: '王五',
        revieweeName: '赵六',
        status: 'COMPLETED',
        createdAt: '2023-11-30 14:30:00'
      }
    ]
    pagination.total = 2
  } catch (error) {
    ElMessage.error('加载评审列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.status = ''
  filters.reviewer = ''
  loadReviews()
}

const createReview = () => {
  ElMessage.info('创建评审功能开发中')
}

const viewReview = (review: Review) => {
  ElMessage.info(`查看评审: ${review.title}`)
}

const editReview = (review: Review) => {
  ElMessage.info(`编辑评审: ${review.title}`)
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.reviews-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>