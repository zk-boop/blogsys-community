<template>
  <div class="articles-by-tag">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else>
      <h1>{{ tag?.name || '标签文章' }}</h1>

      <div v-if="articles.length === 0" class="no-articles">
        该标签下暂无文章
      </div>
      <div v-else class="articles-list">
        <!-- 替换ArticleCard组件为内联的文章卡片模板 -->
        <div
          v-for="article in articles"
          :key="article.id"
          class="article-card"
          role="link"
          tabindex="0"
          @click="goToArticleDetail(article.id)"
          @keydown.enter="goToArticleDetail(article.id)"
          @keydown.space.prevent="goToArticleDetail(article.id)"
        >
          <div class="article-card-layout">
            <div class="article-content">
              <h3 class="article-title">{{ article.title || '无标题' }}</h3>
              <p class="article-excerpt">{{ article.excerpt || article.summary || (article.content ? article.content.substring(0, 150) + '...' : '') }}</p>

              <div class="article-meta">
                <div class="meta-left">
                  <div class="author-info">
                    <el-avatar :size="24" :src="article.authorAvatar || defaultAvatar"></el-avatar>
                    <span class="author-name">{{ article.authorNickname || article.authorUsername || '匿名' }}</span>
                  </div>

                  <div class="category" v-if="article.categoryName">
                    <el-tag size="small" effect="plain">{{ article.categoryName }}</el-tag>
                  </div>
                </div>

                <div class="meta-right">
                  <div class="stats">
                    <span class="stat-item">
                      <el-icon><View /></el-icon>
                      {{ article.viewCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <el-icon><ChatLineSquare /></el-icon>
                      {{ article.commentCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <el-icon><Star /></el-icon>
                      {{ article.likeCount || 0 }}
                    </span>
                    <span class="stat-item">
                      <el-icon><Clock /></el-icon>
                      {{ formatDate(article.publishedAt || article.createTime) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div class="article-image" v-if="article.coverImage">
              <img :src="article.coverImage" :alt="`${article.title || '文章'}封面`">
            </div>
          </div>
        </div>

        <el-pagination
          v-if="total > 0"
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi, tagApi } from '@/api'
import { formatDate } from '@/utils'

export default {
  name: 'ArticlesByTag',
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(true)
    const error = ref(null)
    const articles = ref([])
    const tag = ref(null)
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const defaultAvatar = require('@/assets/default-avatar.png')

    const fetchArticles = async () => {
      loading.value = true
      error.value = null
      try {
        const response = await articleApi.getArticlesByTag(props.id, currentPage.value - 1, pageSize.value)
        if (response.data.success) {
          // 确保返回的数据格式正确
          const articlesData = response.data.data.content || response.data.data;

          // 处理文章数据，确保每篇文章都有必要的属性
          articles.value = articlesData.map(article => {
            return {
              ...article,
              // 确保这些属性存在，防止渲染错误
              title: article.title || '无标题',
              excerpt: article.excerpt || article.summary || (article.content ? article.content.substring(0, 150) + '...' : ''),
              tags: article.tags || [],
              viewCount: article.viewCount || 0,
              commentCount: article.commentCount || 0,
              likeCount: article.likeCount || 0,
              publishedAt: article.publishedAt || article.createTime
            }
          });

          total.value = response.data.data.totalElements || response.data.total || articlesData.length
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

    const fetchTag = async () => {
      try {
        const response = await tagApi.getTagById(props.id)
        if (response.data.success) {
          tag.value = response.data.data
          document.title = `${tag.value.name} - 标签文章 - 博客系统`
        }
      } catch (err) {
        console.error('获取标签信息失败:', err)
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      fetchArticles()
    }

    const goToArticleDetail = (articleId) => {
      router.push({ path: `/articles/${articleId}` })
    }

    onMounted(() => {
      fetchTag()
      fetchArticles()
    })

    watch(() => props.id, () => {
      currentPage.value = 1
      fetchTag()
      fetchArticles()
    })

    return {
      loading,
      error,
      articles,
      tag,
      total,
      currentPage,
      pageSize,
      handlePageChange,
      formatDate,
      goToArticleDetail
    }
  }
}
</script>

<style scoped>
.articles-by-tag {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

h1 {
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

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.el-pagination {
  margin-top: 20px;
  text-align: center;
}

/* 文章卡片样式 */
.article-card {
  background-color: #fff;
  border-radius: 0;
  box-shadow: none;
  padding: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: none;
}

.article-card-layout {
  display: flex;
  gap: 20px;
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-size: 18px;
  margin: 0 0 10px;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-excerpt {
  color: #666;
  font-size: 14px;
  margin: 0 0 15px;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.6;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.meta-left, .meta-right {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 5px;
}

.author-name {
  font-weight: 500;
}

.stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 3px;
}

.article-image {
  width: 200px;
  height: 150px;
  flex-shrink: 0;
  border-radius: 0;
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 768px) {
  .article-card {
    padding: 15px;
  }

  .article-card-layout {
    flex-direction: column-reverse;
  }

  .article-image {
    width: 100%;
    height: 180px;
    margin-bottom: 15px;
  }

  .article-title {
    font-size: 16px;
    -webkit-line-clamp: 1;
  }

  .article-excerpt {
    -webkit-line-clamp: 2;
    margin-bottom: 10px;
  }

  .meta-left, .meta-right {
    width: 100%;
  }

  .meta-left {
    margin-bottom: 8px;
  }
}
</style>
