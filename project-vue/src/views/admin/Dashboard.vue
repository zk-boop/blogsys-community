<template>
  <div class="dashboard">
    <h1>控制台</h1>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.articleCount || 0 }}</div>
            <div class="stat-label">总文章数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.commentCount || 0 }}</div>
            <div class="stat-label">总评论数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.viewCount || 0 }}</div>
            <div class="stat-label">总浏览量</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card class="stat-card pending-card" v-if="stats.pendingArticleCount > 0">
            <div class="stat-value">{{ stats.pendingArticleCount || 0 }}</div>
            <div class="stat-label">待审核文章</div>
            <div class="action-link">
              <router-link to="/admin/articles?status=PENDING">立即处理</router-link>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="stat-card pending-card" v-if="stats.pendingCommentCount > 0">
            <div class="stat-value">{{ stats.pendingCommentCount || 0 }}</div>
            <div class="stat-label">待审核评论</div>
            <div class="action-link">
              <router-link to="/admin/comments?status=PENDING">立即处理</router-link>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="section">
        <h2>最近注册用户</h2>
        <el-table :data="recentUsers" style="width: 100%">
          <el-table-column prop="username" label="用户名" width="180" />
          <el-table-column prop="email" label="邮箱" width="220" />
          <el-table-column prop="createTime" label="注册时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="more-link">
          <router-link to="/admin/users">查看更多用户</router-link>
        </div>
      </div>

      <div class="section">
        <h2>最新发布文章</h2>
        <el-table :data="recentArticles" style="width: 100%">
          <el-table-column prop="title" label="标题" min-width="300">
            <template #default="{ row }">
              <router-link :to="`/articles/${row.id}`">{{ row.title }}</router-link>
            </template>
          </el-table-column>
          <el-table-column prop="authorName" label="作者" width="120" />
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="阅读量" width="100" />
        </el-table>
        <div class="more-link">
          <router-link to="/admin/articles">查看更多文章</router-link>
        </div>
      </div>

      <div class="section">
        <h2>最新评论</h2>
        <el-table :data="recentComments" style="width: 100%">
          <el-table-column prop="content" label="内容" min-width="300" :show-overflow-tooltip="true" />
          <el-table-column prop="userName" label="评论者" width="120" />
          <el-table-column prop="articleTitle" label="文章标题" width="200">
            <template #default="{ row }">
              <router-link :to="`/articles/${row.articleId}`">{{ row.articleTitle }}</router-link>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评论时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
        <div class="more-link">
          <router-link to="/admin/comments">查看更多评论</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { adminApi } from '@/api'
import { formatDate } from '@/utils'

export default {
  name: 'AdminDashboard',
  setup() {
    const store = useStore()
    const loading = ref(true)
    const error = ref(null)

    const stats = reactive({
      userCount: 0,
      articleCount: 0,
      commentCount: 0,
      viewCount: 0,
      pendingArticleCount: 0,
      pendingCommentCount: 0
    })

    const recentUsers = ref([])
    const recentArticles = ref([])
    const recentComments = ref([])

    const fetchDashboardData = async () => {
      loading.value = true
      error.value = null

      try {
        // 由于API文档中没有明确的dashboard接口，使用多个API请求合成数据
        const [usersRes, articlesRes, pendingArticlesRes, commentsRes, pendingCommentsRes] = await Promise.all([
          adminApi.getAllUsers(0, 1), // 只获取一条数据以获取总数
          adminApi.getAllArticles(0, 1), // 只获取一条数据以获取总数
          adminApi.getPendingArticles(0, 10),
          adminApi.getAllComments(0, 1), // 使用管理员接口获取所有评论总数
          adminApi.getPendingComments(0, 10) // 使用管理员接口获取待审核评论
        ])

        if (usersRes.data.success && articlesRes.data.success && commentsRes.data.success) {
          // 更新统计数据
          stats.userCount = usersRes.data.data.totalElements || 0
          stats.articleCount = articlesRes.data.data.totalElements || 0
          stats.commentCount = commentsRes.data.data.totalElements || 0
          stats.viewCount = 0 // 视图数据可能需要从其他地方获取

          // 待审核数据
          if (pendingArticlesRes.data.success) {
            const pendingArticlesData = pendingArticlesRes.data.data
            stats.pendingArticleCount = pendingArticlesData.totalElements ||
              (Array.isArray(pendingArticlesData) ? pendingArticlesData.length : 0)
            recentArticles.value = pendingArticlesData.content ||
              (Array.isArray(pendingArticlesData) ? pendingArticlesData : [])
          }

          if (pendingCommentsRes.data.success) {
            const pendingCommentsData = pendingCommentsRes.data.data
            stats.pendingCommentCount = pendingCommentsData.totalElements ||
              (Array.isArray(pendingCommentsData) ? pendingCommentsData.length : 0)
            recentComments.value = pendingCommentsData.content ||
              (Array.isArray(pendingCommentsData) ? pendingCommentsData : [])
          }

          // 获取最近用户
          const recentUsersRes = await adminApi.getAllUsers(0, 5)
          if (recentUsersRes.data.success) {
            recentUsers.value = recentUsersRes.data.data.content || []
          }
        } else {
          error.value = '获取控制台数据失败'
        }
      } catch (err) {
        error.value = '获取控制台数据失败，请稍后重试'
        console.error('获取控制台数据失败:', err)
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      fetchDashboardData()
    })

    return {
      loading,
      error,
      stats,
      recentUsers,
      recentArticles,
      recentComments,
      formatDate
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.loading,
.error {
  padding: 20px;
  text-align: center;
}

.error {
  color: #f56c6c;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #002FA7;
}

.stat-label {
  margin-top: 10px;
  color: #666;
}

.section {
  margin-top: 30px;
}

h2 {
  margin-bottom: 15px;
  font-size: 1.2rem;
}

.more-link {
  margin-top: 10px;
  text-align: right;
}

.more-link a {
  color: #002FA7;
  text-decoration: none;
}

a {
  color: #002FA7;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

.pending-card {
  position: relative;
  overflow: hidden;
}

.pending-card .stat-value {
  color: #e6a23c;
  /* 待审核状态的颜色 */
}

.pending-card .stat-label {
  color: #e6a23c;
}

.pending-card .action-link {
  position: absolute;
  bottom: 10px;
  right: 10px;
}

.pending-card .action-link a {
  color: #002FA7;
  text-decoration: none;
  background-color: #f5f7fa;
  padding: 5px 10px;
  border-radius: 0;
}

.pending-card .action-link a:hover {
  background-color: #ecf5ff;
  text-decoration: none;
}
</style>
