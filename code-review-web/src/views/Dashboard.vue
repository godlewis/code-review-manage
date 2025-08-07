<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="40" color="#409EFF"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalReviews }}</div>
            <div class="stat-label">总评审数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="40" color="#67C23A"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.completedReviews }}</div>
            <div class="stat-label">已完成评审</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="40" color="#E6A23C"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.pendingIssues }}</div>
            <div class="stat-label">待处理问题</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon size="40" color="#F56C6C"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.teamMembers }}</div>
            <div class="stat-label">团队成员</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近评审</span>
          </template>
          <div class="recent-reviews">
            <div v-if="recentReviews.length === 0" class="empty-state">
              暂无评审记录
            </div>
            <div
              v-for="review in recentReviews"
              :key="review.id"
              class="review-item"
            >
              <div class="review-title">{{ review.title }}</div>
              <div class="review-meta">
                <span>{{ review.reviewerName }}</span>
                <span>{{ formatDate(review.createdAt) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待办事项</span>
          </template>
          <div class="todo-list">
            <div v-if="todoItems.length === 0" class="empty-state">
              暂无待办事项
            </div>
            <div
              v-for="item in todoItems"
              :key="item.id"
              class="todo-item"
            >
              <el-icon class="todo-icon"><Clock /></el-icon>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-time">{{ formatDate(item.dueDate) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Stats {
  totalReviews: number
  completedReviews: number
  pendingIssues: number
  teamMembers: number
}

interface Review {
  id: number
  title: string
  reviewerName: string
  createdAt: string
}

interface TodoItem {
  id: number
  title: string
  dueDate: string
}

const stats = ref<Stats>({
  totalReviews: 0,
  completedReviews: 0,
  pendingIssues: 0,
  teamMembers: 0
})

const recentReviews = ref<Review[]>([])
const todoItems = ref<TodoItem[]>([])

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const loadDashboardData = async () => {
  // TODO: 调用API获取仪表板数据
  // 这里先使用模拟数据
  stats.value = {
    totalReviews: 156,
    completedReviews: 142,
    pendingIssues: 23,
    teamMembers: 12
  }
  
  recentReviews.value = [
    {
      id: 1,
      title: '用户登录模块代码评审',
      reviewerName: '张三',
      createdAt: '2023-12-01'
    },
    {
      id: 2,
      title: '订单处理逻辑优化',
      reviewerName: '李四',
      createdAt: '2023-11-30'
    }
  ]
  
  todoItems.value = [
    {
      id: 1,
      title: '完成支付模块评审',
      dueDate: '2023-12-05'
    },
    {
      id: 2,
      title: '修复安全漏洞问题',
      dueDate: '2023-12-03'
    }
  ]
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.stat-icon {
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.recent-reviews,
.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.review-item,
.todo-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.review-item:last-child,
.todo-item:last-child {
  border-bottom: none;
}

.review-title,
.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.review-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  justify-content: space-between;
}

.todo-icon {
  margin-right: 12px;
  color: #409EFF;
}

.todo-content {
  flex: 1;
}

.todo-time {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
</style>