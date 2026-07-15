<template>
  <div class="article-detail-container">
    <div class="content-layout" v-if="!loading && article">
      <!-- 主要内容区 -->
      <div class="main-content">
        <!-- 文章头部 -->
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>

          <div class="article-meta">
            <div class="author-info">
              <el-avatar
                :size="36"
                :src="article.authorAvatar || defaultAvatar"
                :alt="`${article.authorName || article.authorUsername || '作者'}头像`"
              ></el-avatar>
              <div class="author-details">
                <div class="publish-time">
                  <strong class="author-name">{{ article.authorName || article.authorUsername }}</strong>
                  <span class="date">{{ formatDate(article.publishedAt) }}</span>
                </div>
              </div>
            </div>

            <div class="article-stats">
              <span class="stat-item">
                <i class="fas fa-eye"></i>
                {{ article.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <i class="fas fa-comment"></i>
                {{ article.commentCount || 0 }}
              </span>
              <span class="stat-item">
                <i class="fas fa-thumbs-up"></i>
                {{ article.likeCount || 0 }}
              </span>
            </div>
          </div>

          <!-- 文章封面图 -->
          <div class="article-cover" v-if="article.coverImage">
            <img :src="article.coverImage" :alt="article.title" @error="handleCoverImageError">
          </div>

          <!-- 分类和标签 -->
          <div class="article-taxonomy">
            <div class="article-category">
              <span class="label">分类：</span>
              <router-link :to="`/articles/category/${article.categoryId}`">
                <el-tag size="small">{{ article.categoryName }}</el-tag>
              </router-link>
            </div>

            <div class="article-tags" v-if="tagsList.length > 0">
              <span class="label">标签：</span>
              <router-link v-for="tag in tagsList" :key="tag.id" :to="`/articles/tag/${tag.id}`"
                style="margin-right: 5px;">
                <el-tag size="small" effect="light" :color="tag.color" :style="{ color: getTagTextColor(tag.color) }">{{
                  tag.name }}</el-tag>
              </router-link>
            </div>
          </div>
        </div>

        <!-- 文章内容 -->
        <div class="article-content">
          <div v-html="article.content"></div>
        </div>

        <!-- 文章底部操作栏 -->
        <div class="article-actions">
          <el-button :type="isLiked ? 'danger' : 'default'" @click="toggleLike" :disabled="!isAuthenticated"
            class="action-button like-button">
            <el-icon class="icon-margin">
              <svg v-if="isLiked" viewBox="0 0 1024 1024" width="16" height="16">
                <path fill="currentColor" d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52 0-98 35-111.8 85.1l-85.9 311H144c-17.7 0-32 14.3-32 32v364c0 17.7 14.3 32 32 32h601.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM184 852V568h81v284h-81zm636.4-353-21.9 19 13.9 25.4c4.6 8.4 6.9 17.6 6.9 27.3 0 16.5-7.2 32.2-19.6 43l-21.9 19 13.9 25.4c4.6 8.4 6.9 17.6 6.9 27.3 0 16.5-7.2 32.2-19.6 43l-21.9 19 13.9 25.4c4.6 8.4 6.9 17.6 6.9 27.3 0 22.4-13.2 42.6-33.6 51.8H329V564.8l99.5-360.5c5.2-18.9 22.5-32.2 42.2-32.3 7.6 0 15.1 2.2 21.1 6.7 9.9 7.4 15.2 18.6 14.6 30.5l-9.6 198.4h314.4C829 418.5 840 436.9 840 456c0 16.5-7.2 32.1-19.6 43z" />
              </svg>
              <svg v-else viewBox="0 0 1024 1024" width="16" height="16">
                <path fill="currentColor" d="M273 495.9v428l.3-428zm538.2-88.3H496.8l9.6-198.4c.6-11.9-4.7-23.1-14.6-30.5-6.1-4.5-13.6-6.8-21.1-6.7-19.6.1-36.9 13.4-42.2 32.3-37.1 134.4-64.9 235.2-85.5 302.5V852h399.4a56.85 56.85 0 0 0 33.6-51.8c0-9.7-2.3-18.9-6.9-27.3l-13.9-25.4 21.9-19a56.76 56.76 0 0 0 19.6-43c0-9.7-2.3-18.9-6.9-27.3l-13.9-25.4 21.9-19a56.76 56.76 0 0 0 19.6-43c0-9.7-2.3-18.9-6.9-27.3l-14-25.5 21.9-19a56.76 56.76 0 0 0 19.6-43c0-19.1-11-37.5-28.8-48.4z" />
                <path fill="currentColor" d="M112 528v364c0 17.7 14.3 32 32 32h65V496h-65c-17.7 0-32 14.3-32 32z" />
              </svg>
            </el-icon>
            {{ isLiked ? '已点赞' : '点赞' }} ({{ article.likeCount || 0 }})
          </el-button>

          <el-button :type="isFavorited ? 'primary' : 'default'" @click="toggleFavorite" :disabled="!isAuthenticated"
            class="action-button favorite-button">
            <el-icon class="icon-margin">
              <svg v-if="isFavorited" viewBox="0 0 1024 1024" width="16" height="16">
                <path fill="currentColor" d="M908.1 353.1l-253.9-36.9L540.7 86.1c-3.1-6.3-8.2-11.4-14.5-14.5-15.8-7.8-35-1.3-42.9 14.5L369.8 316.2l-253.9 36.9c-7 1-13.4 4.3-18.3 9.3a32.05 32.05 0 00.6 45.3l183.7 179.1-43.4 252.9a31.95 31.95 0 0046.4 33.7L512 754l227.1 119.4c6.2 3.3 13.4 4.4 20.3 3.2 17.4-3 29.1-19.5 26.1-36.9l-43.4-252.9 183.7-179.1c5-4.9 8.3-11.3 9.3-18.3 2.7-17.5-9.5-33.7-27-36.3z" />
              </svg>
              <svg v-else viewBox="0 0 1024 1024" width="16" height="16">
                <path fill="currentColor" d="M908.1 353.1l-253.9-36.9L540.7 86.1c-3.1-6.3-8.2-11.4-14.5-14.5-15.8-7.8-35-1.3-42.9 14.5L369.8 316.2l-253.9 36.9c-7 1-13.4 4.3-18.3 9.3a32.05 32.05 0 00.6 45.3l183.7 179.1-43.4 252.9a31.95 31.95 0 0046.4 33.7L512 754l227.1 119.4c6.2 3.3 13.4 4.4 20.3 3.2 17.4-3 29.1-19.5 26.1-36.9l-43.4-252.9 183.7-179.1c5-4.9 8.3-11.3 9.3-18.3 2.7-17.5-9.5-33.7-27-36.3zM664.8 561.6l36.1 210.3L512 672.7 323.1 772l36.1-210.3-152.8-149L417.6 382 512 190.7 606.4 382l211.2 30.7-152.8 148.9z" />
              </svg>
            </el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>

        <!-- 评论区 -->
        <div class="article-comments">
          <h3 class="comments-title">评论 ({{ article.commentCount || 0 }})</h3>

          <!-- 发表评论 -->
          <div class="comment-form" v-if="isAuthenticated">
            <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的评论..."></el-input>
            <div class="form-actions">
              <el-button type="primary" @click="submitComment" :loading="commentSubmitting">发表评论</el-button>
            </div>
          </div>

          <div class="login-to-comment" v-else>
            <router-link to="/login">登录</router-link> 后参与评论
          </div>

          <!-- 评论列表 -->
          <div class="comments-list" v-if="comments.length > 0">
            <comment-item v-for="comment in comments" :key="comment.id" :comment="comment" @reply="handleReply"
              @like="handleLikeComment" @unlike="handleUnlikeComment" />
          </div>

          <div class="no-comments" v-else>
            暂无评论，快来发表第一条评论吧！
          </div>

          <!-- 评论分页 -->
          <div class="comments-pagination" v-if="commentTotal > commentPageSize">
            <el-pagination background layout="prev, pager, next" :total="commentTotal" :page-size="commentPageSize"
              :current-page="commentCurrentPage" @current-change="handleCommentPageChange" />
          </div>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="sidebar">
        <!-- 作者信息 -->
        <div class="sidebar-widget author-widget">
          <h3 class="widget-title">作者信息</h3>
          <div class="author-card">
            <strong class="author-nickname">{{ article.authorName || article.authorUsername }}</strong>
            <el-avatar
              :size="60"
              :src="getAvatarUrl(article.authorAvatar)"
              :alt="`${article.authorName || article.authorUsername || '作者'}头像`"
              @error="handleAvatarError"
            ></el-avatar>
            <p class="author-bio" v-if="author && author.bio">{{ author.bio }}</p>
            <router-link :to="`/author/${article.authorId}`">
              <el-button size="small">查看更多文章</el-button>
            </router-link>
          </div>
        </div>

        <!-- 相关文章 -->
        <div class="sidebar-widget related-widget" v-if="relatedArticles.length > 0">
          <h3 class="widget-title">相关文章</h3>
          <ul class="related-articles">
            <li v-for="relatedArticle in relatedArticles" :key="relatedArticle.id">
              <router-link :to="`/articles/${relatedArticle.id}`" @click.native="handleRelatedArticleClick(relatedArticle.id)">
                {{ relatedArticle.title }}
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 文章不存在 -->
    <div class="article-not-found" v-if="!loading && !article">
      <el-empty description="文章不存在或已被删除" />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { articleApi, commentApi, fileApi } from '@/api'
import { formatDate, getContrastTextColor } from '@/utils'
import { ElMessage } from 'element-plus'
import { View, ChatLineSquare, Star } from '@element-plus/icons-vue'
import CommentItem from '@/components/CommentItem.vue'

export default {
  name: 'ArticleDetail',
  components: {
    CommentItem,
    View,
    ChatLineSquare,
    Star,
  },
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    const article = ref(null)
    const author = ref(null)
    const comments = ref([])
    const relatedArticles = ref([])
    const commentContent = ref('')
    const commentSubmitting = ref(false)
    const loading = ref(true)
    const isLiked = ref(false)
    const isFavorited = ref(false)
    const defaultAvatar = require('@/assets/default-avatar.png')
    const defaultCover = require('@/assets/default-cover.png')

    const commentCurrentPage = ref(1)
    const commentPageSize = ref(10)
    const commentTotal = ref(0)

    // 添加页面加载后自动滚动到顶部
    onMounted(() => {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
      fetchArticle()
    })

    const isAuthenticated = computed(() => store.getters.isAuthenticated)
    const userId = computed(() => store.getters.userId)

    // 处理封面图片加载错误
    const handleCoverImageError = (event) => {
      event.target.src = defaultCover
      console.warn('封面图片加载失败:', article.value.coverImage)
    }

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

    // 根据标签背景色获取合适的文字颜色
    const getTagTextColor = (backgroundColor) => {
      return getContrastTextColor(backgroundColor);
    }

    const tagsList = computed(() => {
      if (!article.value || !article.value.tags) return []
      if (Array.isArray(article.value.tags)) {
        return article.value.tags
      }
      return []
    })

    const fetchArticle = async () => {
      try {
        loading.value = true
        const response = await articleApi.getArticleById(props.id)
        if (response.data.success) {
          article.value = response.data.data
          // 公开详情使用文章响应中已提供的作者信息，避免额外请求受保护的用户接口
          fetchRelatedArticles()
          fetchComments()

          // 如果用户已登录，检查是否已收藏和点赞
          if (isAuthenticated.value) {
            checkIfFavorited()
            checkIfLiked()
          }
        }
      } catch (error) {
        console.error('获取文章详情失败:', error)
      } finally {
        loading.value = false
      }
    }

    const fetchComments = async (page = 1) => {
      try {
        const response = await commentApi.getCommentsByArticleId(props.id, page - 1, commentPageSize.value)
        if (response.data.success) {
          comments.value = response.data.data.content || response.data.data
          commentTotal.value = response.data.data.totalElements || response.data.total || 0
        }
      } catch (error) {
        console.error('获取评论失败:', error)
        store.dispatch('showNotification', {
          type: 'error',
          message: '获取评论失败，请稍后重试'
        })
      }
    }

    const fetchRelatedArticles = async () => {
      try {
        if (!article.value) return
        // 获取相同分类的文章
        const response = await articleApi.getArticlesByCategory(
          article.value.categoryId,
          0,
          5
        )
        if (response.data.success) {
          // 过滤掉当前文章
          relatedArticles.value = response.data.data
            .filter(item => item.id !== article.value.id)
            .slice(0, 5)
        }
      } catch (error) {
        console.error('获取相关文章失败:', error)
      }
    }

    // 点赞相关功能
    const checkIfLiked = async () => {
      try {
        const response = await articleApi.checkIfLiked(props.id)
        if (response.data.success) {
          isLiked.value = response.data.data
        }
      } catch (error) {
        console.error('检查点赞状态失败:', error)
      }
    }

    const toggleLike = async () => {
      if (!isAuthenticated.value) {
        ElMessage.warning('请先登录后再点赞文章')
        return
      }

      try {
        if (isLiked.value) {
          await articleApi.unlikeArticle(props.id)
          article.value.likeCount--
          ElMessage.success('已取消点赞')
        } else {
          await articleApi.likeArticle(props.id)
          article.value.likeCount++
          ElMessage.success('点赞成功')
        }
        isLiked.value = !isLiked.value
      } catch (error) {
        console.error('点赞操作失败:', error)
        ElMessage.error('操作失败')
      }
    }

    // 收藏相关功能
    const checkIfFavorited = async () => {
      try {
        const response = await articleApi.checkIfFavorited(props.id, userId.value)
        if (response.data.success) {
          isFavorited.value = response.data.data
        }
      } catch (error) {
        console.error('检查收藏状态失败:', error)
      }
    }

    const toggleFavorite = async () => {
      if (!isAuthenticated.value) {
        ElMessage.warning('请先登录后再收藏文章')
        return
      }

      try {
        if (isFavorited.value) {
          const response = await articleApi.unfavoriteArticle(props.id)
          if (response.data.success) {
            isFavorited.value = false
            ElMessage.success('已取消收藏')
          } else {
            ElMessage.error(response.data.message || '取消收藏失败')
          }
        } else {
          const response = await articleApi.favoriteArticle(props.id)
          if (response.data.success) {
            isFavorited.value = true
            ElMessage.success('收藏成功')
          } else {
            ElMessage.error(response.data.message || '收藏失败')
          }
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    }

    const submitComment = async () => {
      if (!commentContent.value.trim()) {
        ElMessage.warning('评论内容不能为空')
        return
      }

      try {
        commentSubmitting.value = true
        const comment = { content: commentContent.value }
        await commentApi.createComment(comment, props.id, userId.value)
        ElMessage.success('评论提交成功')
        commentContent.value = ''

        // 刷新评论列表
        fetchComments(commentCurrentPage.value)
      } catch (error) {
        console.error('提交评论失败:', error)
        ElMessage.error('提交评论失败，请稍后重试')
      } finally {
        commentSubmitting.value = false
      }
    }

    const handleCommentPageChange = (page) => {
      commentCurrentPage.value = page
      fetchComments(page)
    }

    const handleReply = async (comment) => {
      // 刷新评论列表以获取新的回复
      fetchComments(commentCurrentPage.value)
    }

    const handleLikeComment = async (commentId) => {
      try {
        await commentApi.likeComment(commentId)
        // 不显示提示信息，已经在CommentItem组件中显示了
        // ElMessage.success('点赞成功')

        // 刷新评论列表
        fetchComments(commentCurrentPage.value)
      } catch (error) {
        console.error('点赞失败:', error)
        ElMessage.error('点赞失败，请稍后重试')
      }
    }

    const handleUnlikeComment = async (commentId) => {
      try {
        await commentApi.unlikeComment(commentId)
        // 不显示提示信息，已经在CommentItem组件中显示了

        // 刷新评论列表
        fetchComments(commentCurrentPage.value)
      } catch (error) {
        console.error('取消点赞失败:', error)
        ElMessage.error('取消点赞失败，请稍后重试')
      }
    }

    const handleRelatedArticleClick = (articleId) => {
      // 如果是同一路由但参数不同，需要强制刷新
      if (props.id != articleId) {
        router.push(`/articles/${articleId}`);
        // 重置数据
        article.value = null;
        comments.value = [];
        relatedArticles.value = [];
        loading.value = true;

        // 重新获取文章数据
        setTimeout(() => {
          fetchArticle();
        }, 100);
      }
    }

    return {
      article,
      author,
      comments,
      relatedArticles,
      tagsList,
      loading,
      commentContent,
      commentSubmitting,
      commentTotal,
      commentPageSize,
      commentCurrentPage,
      handleCommentPageChange,
      handleReply,
      handleLikeComment,
      handleUnlikeComment,
      submitComment,
      isAuthenticated,
      formatDate,
      defaultAvatar,
      handleCoverImageError,
      handleAvatarError,
      getAvatarUrl,
      isLiked,
      toggleLike,
      isFavorited,
      toggleFavorite,
      getTagTextColor,
      handleRelatedArticleClick
    }
  }
}
</script>

<style scoped>
.article-detail-container {
  width: 100%;
}

.content-layout {
  display: flex;
  gap: 30px;
}

.main-content {
  flex: 3;
}

.sidebar {
  flex: 1;
}

.article-header {
  margin-bottom: 30px;
}

.article-title {
  font-size: 28px;
  margin: 0 0 20px;
  color: #333;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 500;
  font-size: 16px;
}

.publish-time {
  font-size: 14px;
  color: #999;
}

.article-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  font-size: 14px;
}

.article-cover {
  width: 100%;
  height: 400px;
  margin: 20px 0;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-cover:hover img {
  transform: scale(1.02);
}

.article-taxonomy {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.article-category,
.article-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.label {
  color: #666;
  font-size: 14px;
}

.article-content {
  line-height: 1.8;
  color: #333;
  margin-bottom: 40px;
}

.article-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 40px 0;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 16px;
}

.action-button .el-icon {
  font-size: 18px;
  margin-right: 8px;
}

.icon-margin {
  margin-right: 5px;
}

.article-comments {
  margin-top: 40px;
}

.comments-title {
  font-size: 20px;
  margin: 0 0 20px;
}

.comment-form {
  margin-bottom: 30px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

.login-to-comment {
  text-align: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 0;
  margin-bottom: 30px;
}

.login-to-comment a {
  color: #002FA7;
  text-decoration: none;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.no-comments {
  text-align: center;
  padding: 30px 0;
  color: #999;
}

.comments-pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.sidebar-widget {
  background-color: #fff;
  padding: 20px;
  margin-bottom: 30px;
  border-radius: 0;
  box-shadow: none;
}

.widget-title {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.author-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.author-nickname {
  font-size: 16px;
  margin-bottom: 10px;
  display: block;
}

.author-card .author-bio {
  color: #666;
  font-size: 14px;
  margin: 15px 0;
}

.author-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.author-details .publish-time {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-details .author-name {
  font-size: 16px;
  color: black;
  font-weight: bold;
}

.author-details .date {
  font-size: 13px;
  color: #999;
}

.related-articles {
  list-style: none;
  padding: 0;
  margin: 0;
}

.related-articles li {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.related-articles li:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.related-articles a {
  text-decoration: none;
  color: #333;
  font-size: 14px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.related-articles a:hover {
  color: #002FA7;
}

.loading-state,
.article-not-found {
  padding: 40px 0;
}

@media (max-width: 992px) {
  .content-layout {
    flex-direction: column;
  }

  .article-title {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .article-taxonomy {
    flex-direction: column;
    gap: 15px;
  }

  .article-actions {
    flex-wrap: wrap;
  }
}
</style>

<style scoped>
.content-layout { display: grid; grid-template-columns: minmax(0, 760px) 220px; justify-content: space-between; gap: 64px; }
.main-content { min-width: 0; }
.article-header { padding: 0 0 32px; background: transparent; border-bottom: 1px solid var(--ui-line); }
.article-title {
  margin: 0 0 24px;
  color: var(--ui-text);
  font-size: clamp(38px, 6vw, 64px);
  font-weight: 600;
  line-height: 1.08;
  letter-spacing: -.045em;
}
.article-meta { padding: 0; border: 0; color: var(--ui-text-soft); }
.publish-time { gap: 10px; }
.author-name { font-weight: 500; }
.date, .article-stats { color: var(--ui-text-faint); font-size: 13px; }
.article-cover { margin: 32px 0 0; border-radius: 0; }
.article-cover img { max-height: 520px; object-fit: cover; }
.article-taxonomy { margin-top: 22px; padding: 18px 0 0; border-top: 1px solid var(--ui-line); }
.article-taxonomy .label { color: var(--ui-text-faint); font-size: 13px; }

.article-content {
  padding: 44px 0 56px;
  color: #242424;
  font-size: 18px;
  line-height: 1.9;
}
.article-content :deep(h2) { margin: 2.2em 0 .8em; font-size: 1.65em; line-height: 1.25; }
.article-content :deep(h3) { margin: 1.8em 0 .7em; font-size: 1.28em; line-height: 1.3; }
.article-content :deep(p) { margin: 0 0 1.45em; }
.article-content :deep(a) { color: var(--ui-accent); }
.article-content :deep(blockquote) {
  margin: 2em 0;
  padding: 2px 0 2px 22px;
  color: var(--ui-text-soft);
  border-left: 2px solid var(--ui-accent);
}
.article-content :deep(img) { max-width: 100%; height: auto; margin: 24px 0; }

.article-actions { justify-content: flex-start; padding: 22px 0; border-top: 1px solid var(--ui-line); border-bottom: 1px solid var(--ui-line); }
.article-comments { margin-top: 56px; }
.comments-title { padding-bottom: 16px; border-bottom: 1px solid var(--ui-line-strong); font-size: 24px; font-weight: 600; }
.comment-form { padding: 24px 0; background: transparent; }
.login-to-comment, .no-comments { color: var(--ui-text-faint); background: transparent; }

.sidebar { gap: 38px; }
.sidebar-widget { padding: 0 !important; border: 0 !important; background: transparent !important; }
.widget-title { margin: 0; padding: 0 0 12px; border-bottom: 1px solid var(--ui-line-strong); font-size: 16px; font-weight: 600; }
.author-card { align-items: flex-start; padding: 18px 0; }
.author-bio { color: var(--ui-text-soft); line-height: 1.7; }
.related-articles li { padding: 12px 0; border-bottom-color: var(--ui-line); }
.related-articles a { color: var(--ui-text-soft); line-height: 1.55; }
.related-articles a:hover { color: var(--ui-accent); }

@media (max-width: 900px) {
  .content-layout { grid-template-columns: 1fr; gap: 52px; }
  .sidebar { display: grid; grid-template-columns: 1fr 1fr; gap: 36px; }
}
@media (max-width: 600px) {
  .article-title { font-size: clamp(34px, 11vw, 48px); }
  .article-content { padding-top: 34px; font-size: 17px; line-height: 1.85; }
  .article-meta { align-items: flex-start; gap: 14px; }
  .article-stats { width: auto; }
  .sidebar { grid-template-columns: 1fr; }
}
</style>
