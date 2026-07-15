<template>
  <div class="author-articles-container">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else>
      <!-- 作者信息 -->
      <div v-if="author" class="author-info">
        <el-avatar :size="80" :src="getAvatarUrl(author.avatar)" @error="handleAvatarError" class="author-avatar"></el-avatar>
        <div class="author-details">
          <h2 class="author-name">{{ author.nickname || author.username }}</h2>
          <p class="author-bio">{{ author.bio || '这个人很懒，还没有写简介...' }}</p>
        </div>
      </div>

      <h2 class="section-title">{{ authorName }}的文章</h2>

      <div v-if="articles.length > 0" class="articles-list">
        <article-card v-for="article in articles" :key="article.id" :article="article" />

        <!-- 分页 -->
        <div class="pagination" v-if="totalElements > pageSize">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="totalElements"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="该作者暂无文章" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi, userApi, fileApi } from '@/api'
import ArticleCard from '@/components/ArticleCard.vue'

export default {
  name: 'AuthorArticles',
  components: {
    ArticleCard
  },
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  setup(props) {
    const route = useRoute()
    const author = ref(null)
    const articles = ref([])
    const loading = ref(true)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalElements = ref(0)
    const defaultAvatar = require('@/assets/default-avatar.png')

    const authorName = computed(() => {
      if (author.value) {
        return author.value.nickname || author.value.username
      }
      return '作者'
    })

    // 处理头像加载错误
    const handleAvatarError = (e) => {
      e.target.src = defaultAvatar
    }

    // 获取头像URL
    const getAvatarUrl = (avatar) => {
      if (!avatar) return defaultAvatar
      if (avatar.startsWith('http')) return avatar
      return fileApi.getAvatarUrl(avatar)
    }

    const fetchAuthor = async () => {
      try {
        const response = await userApi.getUserById(props.id)
        if (response.data.success) {
          author.value = response.data.data
        }
      } catch (error) {
        console.error('获取作者信息失败:', error)
      }
    }

    const fetchArticles = async (page = 0) => {
      try {
        const response = await articleApi.getArticlesByAuthor(props.id, page, pageSize.value)
        if (response.data.success) {
          articles.value = response.data.data.content || response.data.data
          totalElements.value = response.data.data.totalElements || response.data.total || 0
        }
      } catch (error) {
        console.error('获取作者文章失败:', error)
      } finally {
        loading.value = false
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      fetchArticles(page - 1)
      // 滚动到顶部
      window.scrollTo(0, 0)
    }

    onMounted(() => {
      fetchAuthor()
      fetchArticles()
    })

    return {
      author,
      articles,
      loading,
      currentPage,
      pageSize,
      totalElements,
      authorName,
      handlePageChange,
      handleAvatarError,
      getAvatarUrl
    }
  }
}
</script>

<style scoped>
.author-articles-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}

.author-info {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 0;
  box-shadow: none;
}

.author-avatar {
  margin-right: 20px;
}

.author-name {
  margin: 0 0 10px;
  font-size: 24px;
  color: #333;
}

.author-bio {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.section-title {
  margin: 30px 0;
  font-size: 20px;
  color: #333;
  border-left: 4px solid #002FA7;
  padding-left: 10px;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.loading-state,
.empty-state {
  padding: 40px 0;
}
</style>
