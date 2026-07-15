<template>
  <div class="user-comments">
    <h1>我的评论</h1>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else-if="comments.length === 0" class="no-comments">
      您还没有发表过评论
    </div>
    <div v-else>
      <div class="comment-list">
        <el-card v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-meta">
            <div class="article-info">
              评论于文章：
              <router-link :to="`/articles/${comment.articleId}`">{{ comment.articleTitle }}</router-link>
            </div>
            <div class="comment-date">{{ formatDate(comment.createTime) }}</div>
          </div>
          <div class="actions">
            <el-popconfirm title="确定删除这条评论吗？" @confirm="handleDelete(comment.id)">
              <template #reference>
                <el-button type="text" :disabled="loading">删除评论</el-button>
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
import { commentApi } from '@/api'
import { formatDate } from '@/utils'

export default {
  name: 'UserComments',
  setup() {
    const store = useStore()
    const loading = ref(true)
    const error = ref(null)
    const comments = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)

    const userId = computed(() => store.getters.userId)

    const fetchComments = async () => {
      loading.value = true
      error.value = null

      try {
        const response = await commentApi.getCommentsByUserId(
          userId.value,
          currentPage.value - 1,
          pageSize.value
        )

        if (response.data.success) {
          comments.value = response.data.data.content || [];
          total.value = response.data.data.totalElements || 0;
        } else {
          error.value = response.data.message || '获取评论失败'
        }
      } catch (err) {
        error.value = '获取评论失败，请稍后重试'
        console.error('获取评论失败:', err)
      } finally {
        loading.value = false
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      fetchComments()
    }

    const handleDelete = async (commentId) => {
      try {
        const response = await commentApi.deleteComment(commentId)

        if (response.data.success) {
          fetchComments()
          store.dispatch('showNotification', {
            type: 'success',
            message: '评论删除成功'
          })
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '评论删除失败'
          })
        }
      } catch (err) {
        console.error('评论删除失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '评论删除失败，请稍后重试'
        })
      }
    }

    onMounted(() => {
      fetchComments()
    })

    return {
      loading,
      error,
      comments,
      total,
      currentPage,
      pageSize,
      formatDate,
      handlePageChange,
      handleDelete
    }
  }
}
</script>

<style scoped>
.user-comments {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.loading,
.error,
.no-comments {
  padding: 20px;
  text-align: center;
}

.error {
  color: #f56c6c;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  position: relative;
}

.comment-content {
  margin-bottom: 10px;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 0.8rem;
}

.article-info a {
  color: #002FA7;
  text-decoration: none;
}

.article-info a:hover {
  text-decoration: underline;
}

.actions {
  margin-top: 10px;
  text-align: right;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
