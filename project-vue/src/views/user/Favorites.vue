<template>
  <div class="user-favorites">
    <h1>我的收藏</h1>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else-if="favorites.length === 0" class="no-favorites">
      您还没有收藏任何文章
    </div>
    <div v-else>
      <div class="favorite-list">
        <el-card v-for="article in favorites" :key="article.id" class="favorite-item">
          <div class="article-info">
            <h2>
              <router-link :to="`/articles/${article.id}`">{{ article.title }}</router-link>
            </h2>
            <p class="article-summary">{{ truncateText(article.summary, 150) }}</p>
            <div class="article-meta">
              <span class="author">
                <i class="el-icon-user"></i>
                {{ article.authorName }}
              </span>
              <span class="date">
                <i class="el-icon-time"></i>
                {{ formatDate(article.createTime) }}
              </span>
              <span class="views">
                <i class="el-icon-view"></i>
                {{ article.viewCount || 0 }}
              </span>
            </div>
          </div>
          <div class="actions">
            <el-popconfirm title="确定取消收藏这篇文章吗？" @confirm="handleUnfavorite(article.id)">
              <template #reference>
                <el-button type="text" :disabled="loading">取消收藏</el-button>
              </template>
            </el-popconfirm>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-if="total > 0"
        class="pagination"
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { articleApi } from '@/api'
import { formatDate, truncateText } from '@/utils'

export default {
  name: 'UserFavorites',
  setup() {
    const store = useStore()
    const loading = ref(true)
    const error = ref(null)
    const favorites = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)

    const userId = computed(() => store.getters.userId)

    const fetchFavorites = async () => {
      loading.value = true
      error.value = null

      try {
        // 假设API有获取用户收藏的接口
        const response = await articleApi.getUserFavorites(
          userId.value,
          currentPage.value - 1,
          pageSize.value
        )

        if (response.data.success) {
          favorites.value = response.data.data.content
          total.value = response.data.data.totalElements
        } else {
          error.value = response.data.message || '获取收藏文章失败'
        }
      } catch (err) {
        error.value = '获取收藏文章失败，请稍后重试'
        console.error('获取收藏文章失败:', err)
      } finally {
        loading.value = false
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      fetchFavorites()
    }

    const handleUnfavorite = async (articleId) => {
      try {
        const response = await articleApi.unfavoriteArticle(articleId, userId.value)

        if (response.data.success) {
          fetchFavorites()
          store.dispatch('showNotification', {
            type: 'success',
            message: '取消收藏成功'
          })
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '取消收藏失败'
          })
        }
      } catch (err) {
        console.error('取消收藏失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '取消收藏失败，请稍后重试'
        })
      }
    }

    onMounted(() => {
      fetchFavorites()
    })

    return {
      loading,
      error,
      favorites,
      total,
      currentPage,
      pageSize,
      formatDate,
      truncateText,
      handlePageChange,
      handleUnfavorite
    }
  }
}
</script>

<style scoped>
.user-favorites {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.loading,
.error,
.no-favorites {
  padding: 20px;
  text-align: center;
}

.error {
  color: #f56c6c;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.favorite-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-info {
  flex: 1;
}

h2 {
  margin: 0 0 10px;
  font-size: 1.2rem;
}

h2 a {
  color: #333;
  text-decoration: none;
}

h2 a:hover {
  color: #002FA7;
}

.article-summary {
  color: #666;
  margin: 0 0 10px;
  font-size: 0.9rem;
}

.article-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 0.8rem;
}

.actions {
  margin-left: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
