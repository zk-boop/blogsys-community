<template>
  <div class="all-articles-container">
    <h1 class="page-title">文章列表</h1>

    <!-- 文章列表 -->
    <div class="article-list">
      <!-- 置顶文章 -->
      <div v-for="(article, index) in topArticles" :key="`top-${article.id}`" class="top-article-wrapper">
        <div class="top-badge">置顶</div>
        <article-card :article="article" :index="index" @click="goToArticle(article.id)" />
      </div>

      <!-- 普通文章 -->
      <article-card
        v-for="(article, index) in regularArticles"
        :key="article.id"
        :article="article"
        :index="index + topArticles.length"
        @click="goToArticle(article.id)" />
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange" />
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 调试信息 -->
    <div class="debug-info" v-if="showDebug">
      <h4>调试信息</h4>
      <p>当前页: {{ currentPage }}</p>
      <p>每页大小: {{ pageSize }}</p>
      <p>总文章数: {{ total }}</p>
      <p>置顶文章数: {{ topArticles.length }}</p>
      <p>普通文章数: {{ regularArticles.length }}</p>
      <p>总页数: {{ Math.ceil(total / pageSize) }}</p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api'
import ArticleCard from '@/components/ArticleCard.vue'

export default {
  name: 'AllArticles',
  components: {
    ArticleCard
  },
  setup() {
    const router = useRouter()
    const articles = ref([])
    const topArticles = ref([])
    const regularArticles = computed(() => {
      // 过滤掉置顶文章，避免重复显示
      return articles.value.filter(article => !article.top)
    })
    const total = ref(100) // 设置一个初始总数值
    const currentPage = ref(1)
    const pageSize = ref(5)
    const loading = ref(false)
    const showDebug = ref(false) // 设置为true可以显示调试信息

    // 获取置顶文章
    const fetchTopArticles = async () => {
      try {
        const response = await articleApi.getTopArticles()
        if (response.data.success) {
          const articlesData = response.data.data.content || response.data.data

          topArticles.value = articlesData.map(article => {
            return {
              ...article,
              title: article.title || '无标题',
              excerpt: article.excerpt || article.summary || (article.content ? article.content.substring(0, 150) + '...' : ''),
              tags: article.tags || [],
              viewCount: article.viewCount || 0,
              commentCount: article.commentCount || 0,
              likeCount: article.likeCount || 0,
              authorName: article.authorName || article.authorUsername,
              authorNickname: article.authorName,
              top: true // 添加置顶标记
            }
          })

          console.log(`已加载 ${topArticles.value.length} 篇置顶文章`)
        }
      } catch (error) {
        console.error('获取置顶文章失败:', error)
      }
    }

    const fetchArticles = async (page = 1) => {
      try {
        loading.value = true
        console.log(`请求第 ${page} 页的文章，每页 ${pageSize.value} 篇`);

        // 先获取总文章数
        const countResponse = await articleApi.getArticles(0, 100); // 获取足够多的文章来确定总数

        if (countResponse.data.success) {
          let totalCount = 0;
          if (countResponse.data.data && Array.isArray(countResponse.data.data)) {
            totalCount = countResponse.data.data.length;
          } else if (countResponse.data.data && countResponse.data.data.totalElements) {
            totalCount = countResponse.data.data.totalElements;
          }
          total.value = totalCount;
          console.log(`总文章数: ${totalCount}`);
        }

        // 然后获取当前页的文章
        const response = await articleApi.getArticles(page - 1, pageSize.value);

        if (response.data.success) {
          // 确保返回的数据格式正确
          let articlesData = [];

          // 处理不同的后端响应格式
          if (response.data.data && Array.isArray(response.data.data)) {
            articlesData = response.data.data;
          } else if (response.data.data && response.data.data.content && Array.isArray(response.data.data.content)) {
            articlesData = response.data.data.content;
          } else if (response.data.content && Array.isArray(response.data.content)) {
            articlesData = response.data.content;
          }

          // 如果是第一页且有置顶文章，过滤掉置顶的文章以避免重复
          if (page === 1 && topArticles.value.length > 0) {
            const topArticleIds = topArticles.value.map(article => article.id);
            articlesData = articlesData.filter(article => !topArticleIds.includes(article.id));
          }

          // 处理文章数据，确保每篇文章都有必要的属性
          articles.value = articlesData.map(article => {
            return {
              ...article,
              title: article.title || '无标题',
              excerpt: article.excerpt || article.summary || (article.content ? article.content.substring(0, 150) + '...' : ''),
              tags: article.tags || [],
              viewCount: article.viewCount || 0,
              commentCount: article.commentCount || 0,
              likeCount: article.likeCount || 0,
              authorName: article.authorName || article.authorUsername,
              authorNickname: article.authorName
            }
          });

          console.log(`已加载 ${articles.value.length} 篇文章`);
        }
      } catch (error) {
        console.error('获取文章列表失败:', error)
      } finally {
        loading.value = false
      }
    }

    const handlePageChange = (page) => {
      console.log(`切换到第 ${page} 页`);
      currentPage.value = page

      // 只有第一页显示置顶文章
      if (page === 1) {
        fetchTopArticles()
      } else {
        topArticles.value = []
      }

      fetchArticles(page)
      window.scrollTo(0, 0)
    }

    const goToArticle = (id) => {
      router.push(`/articles/${id}`)
    }

    onMounted(() => {
      // 获取置顶文章（仅第一页显示）
      fetchTopArticles()
      // 获取普通文章
      fetchArticles()
    })

    return {
      articles,
      topArticles,
      regularArticles,
      total,
      currentPage,
      pageSize,
      loading,
      showDebug,
      handlePageChange,
      goToArticle
    }
  }
}
</script>

<style scoped>
.all-articles-container {
  max-width: var(--fd-content);
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  padding-bottom: 20px;
  border-bottom: 1px solid #111111;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.top-article-wrapper {
  position: relative;
  padding-top: 8px;
}

.top-badge {
  position: absolute;
  top: 0;
  left: 0;
  background-color: #002FA7;
  color: white;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: bold;
  border-radius: 0;
  z-index: 2;
  box-shadow: none;
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  gap: 10px;
  color: #002FA7;
}

.debug-info {
  margin-top: 30px;
  padding: 15px;
  border: 1px solid #eee;
  background-color: #f9f9f9;
  border-radius: 0;
}

.debug-info h4 {
  margin-top: 0;
  margin-bottom: 10px;
}

.debug-info p {
  margin: 5px 0;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
    margin-bottom: 20px;
  }
}
</style>

<style scoped>
.all-articles-container { padding: 0; }
.page-title { margin-bottom: 42px !important; font-size: clamp(36px, 5vw, 56px) !important; font-weight: 600 !important; }
.article-list { gap: 0; }
.top-article-wrapper { padding-top: 0; }
.top-badge {
  position: static;
  display: inline-block;
  margin: 18px 0 0;
  padding: 0;
  color: var(--ui-accent);
  background: transparent;
  font-size: 12px;
  font-weight: 600;
}
.pagination { justify-content: flex-start; margin-top: 36px; }
.loading-container { color: var(--ui-text-faint); }
.debug-info { display: none; }
</style>
