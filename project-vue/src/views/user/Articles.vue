<template>
  <div class="user-articles">
    <h1>我的文章</h1>
    <div class="actions">
      <el-button type="primary" @click="goToWrite">写文章</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else-if="articles.length === 0" class="no-articles">
      您还没有发布过文章，快去写一篇吧！
    </div>
    <div v-else>
      <el-table :data="articles" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/articles/${row.id}`">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="createTime" label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button v-if="row.status && row.status.toUpperCase() === 'DRAFT'" type="primary" size="small"
              @click="handleSubmit(row.id)">
              提交审核
            </el-button>
            <el-button type="text" @click="handleEdit(row.id)">编辑</el-button>
            <el-popconfirm title="确定删除这篇文章吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="text" style="margin-left: 10px;" :disabled="loading">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" class="pagination" layout="prev, pager, next" :total="total" :page-size="pageSize"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { articleApi } from '@/api'
import { formatDate } from '@/utils'

export default {
  name: 'UserArticles',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(true)
    const error = ref(null)
    const articles = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)

    const userId = computed(() => store.getters.userId)

    const fetchArticles = async () => {
      loading.value = true
      error.value = null

      try {
        const response = await articleApi.getArticlesByAuthor(
          userId.value,
          currentPage.value - 1,
          pageSize.value
        )

        if (response.data.success) {
          const responseData = response.data.data;
          articles.value = (responseData && responseData.content) ? responseData.content : (responseData || []);
          total.value = (responseData && responseData.totalElements) ? responseData.totalElements : (articles.value.length);
        } else {
          error.value = response.data.message || '获取文章失败'
        }
      } catch (err) {
        error.value = '获取文章失败，请稍后重试'
        console.error('获取文章失败:', err)
      } finally {
        loading.value = false
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      fetchArticles()
    }

    const getStatusType = (status) => {
      const upperStatus = status ? status.toUpperCase() : '';
      switch (upperStatus) {
        case 'PUBLISHED': return 'success'
        case 'DRAFT': return 'info'
        case 'PENDING': return 'warning'
        case 'ARCHIVED': return 'danger'
        default: return 'info'
      }
    }

    const getStatusText = (status) => {
      const upperStatus = status ? status.toUpperCase() : '';
      switch (upperStatus) {
        case 'PUBLISHED': return '已发布'
        case 'DRAFT': return '草稿'
        case 'PENDING': return '待审核'
        case 'ARCHIVED': return '已归档'
        default: return '未知'
      }
    }

    const goToWrite = () => {
      router.push('/user/write')
    }

    const handleEdit = (id) => {
      router.push(`/user/edit/${id}`)
    }

    const handleSubmit = async (id) => {
      try {
        const response = await articleApi.submitArticle(id)

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: '文章已提交审核'
          })
          fetchArticles()
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '提交失败'
          })
        }
      } catch (err) {
        console.error('提交审核失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '提交审核失败，请稍后重试'
        })
      }
    }

    const handleDelete = async (id) => {
      try {
        const response = await articleApi.deleteArticle(id)

        if (response.data.success) {
          fetchArticles()
          store.dispatch('showNotification', {
            type: 'success',
            message: '文章删除成功'
          })
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '文章删除失败'
          })
        }
      } catch (err) {
        console.error('文章删除失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '文章删除失败，请稍后重试'
        })
      }
    }

    onMounted(() => {
      fetchArticles()
    })

    return {
      loading,
      error,
      articles,
      total,
      currentPage,
      pageSize,
      formatDate,
      getStatusType,
      getStatusText,
      handlePageChange,
      goToWrite,
      handleEdit,
      handleSubmit,
      handleDelete
    }
  }
}
</script>

<style scoped>
.user-articles {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.actions {
  margin-bottom: 20px;
}

.loading,
.error,
.no-articles {
  padding: 20px;
  text-align: center;
}

.error {
  color: #f56c6c;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

a {
  color: #002FA7;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style>
