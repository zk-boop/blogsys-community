<template>
  <div class="admin-comments">
    <h1>评论管理</h1>

    <!-- 筛选和搜索区域 -->
    <div class="filter-section">
      <div class="search-container">
        <el-input v-model="searchKeyword" placeholder="搜索评论内容、用户名或文章标题" class="search-input" clearable
          @keyup.enter="handleSearch" @clear="clearSearch">
          <template #suffix>
            <el-icon class="search-icon" @click="handleSearch">
              <Search />
            </el-icon>
          </template>
        </el-input>
      </div>

      <div class="filter-container">
        <el-input v-model="userIdFilter" placeholder="用户ID" class="user-id-input" clearable
          @keyup.enter="handleUserIdSearch">
          <template #suffix>
            <el-icon class="search-icon" @click="handleUserIdSearch">
              <User />
            </el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="评论状态" clearable @change="handleStatusChange">
          <el-option label="全部" value="" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else>
      <div v-if="articleComments.length === 0" class="empty-state">
        <el-empty description="暂无评论数据" />
      </div>
      <div v-else class="articles-list">
        <!-- 按文章分组显示评论 -->
        <div v-for="article in articleComments" :key="article.id" class="article-comments-container">
          <div class="article-header">
            <h2 class="article-title">
              <router-link :to="`/articles/${article.id}`" target="_blank">
                {{ article.title }}
              </router-link>
            </h2>
            <div class="article-meta">
              <span>{{ article.commentsCount }}条评论</span>
              <el-button type="primary" size="small" plain @click="toggleArticleComments(article.id)">
                {{ expandedArticles.includes(article.id) ? '收起评论' : '展开评论' }}
              </el-button>
            </div>
          </div>

          <!-- 评论列表 -->
          <div v-if="expandedArticles.includes(article.id)" class="comments-list">
            <div v-for="comment in article.rootComments" :key="comment.id" class="comment-container">
              <!-- 主评论 -->
              <div class="comment-item">
                <div class="comment-header">
                  <div class="comment-user">
                    <el-avatar :size="40" :src="getAvatarUrl(comment.userAvatar)" @error="handleAvatarError">
                      {{ getInitials(comment.nickname || comment.username) }}
                    </el-avatar>
                    <div class="user-info">
                      <span class="user-name">{{ comment.nickname || comment.username || '匿名用户' }}</span>
                      <span class="user-id" v-if="comment.userId">(ID: {{ comment.userId }})</span>
                      <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                    </div>
                  </div>
                  <div class="comment-status">
                    <el-tag :type="getStatusType(comment.status)">
                      {{ getStatusText(comment.status) }}
                    </el-tag>
                  </div>
                </div>
                <div class="comment-content">
                  <p>{{ comment.content }}</p>
                </div>
                <div class="comment-actions">
                  <el-button type="primary" size="small" @click="showCommentDetail(comment)">
                    查看详情
                  </el-button>
                  <el-popconfirm title="确定删除该评论吗？" @confirm="handleDelete(comment)">
                    <template #reference>
                      <el-button type="danger" size="small">删除</el-button>
                    </template>
                  </el-popconfirm>
                  <el-button v-if="comment.replies && comment.replies.length > 0" type="info" size="small" plain
                    @click="toggleReplies(comment.id)">
                    {{ isRepliesExpanded(comment.id) ? '收起回复' : `查看回复(${comment.replies.length})` }}
                  </el-button>
                </div>

                <!-- 回复列表 (可折叠) -->
                <div v-if="comment.replies && comment.replies.length > 0 && isRepliesExpanded(comment.id)"
                  class="replies-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="reply-header">
                      <div class="reply-user">
                        <el-avatar :size="32" :src="getAvatarUrl(reply.userAvatar)" @error="handleAvatarError">
                          {{ getInitials(reply.nickname || reply.username) }}
                        </el-avatar>
                        <div class="user-info">
                          <div>
                            <span class="user-name">{{ reply.nickname || reply.username || '匿名用户' }}</span>
                            <span class="user-id" v-if="reply.userId">(ID: {{ reply.userId }})</span>
                          </div>
                          <div v-if="reply.replyToUsername" class="reply-to">
                            回复 {{ reply.replyToNickname || reply.replyToUsername }}
                          </div>
                          <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                        </div>
                      </div>
                      <div class="reply-status">
                        <el-tag :type="getStatusType(reply.status)">
                          {{ getStatusText(reply.status) }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="reply-content">
                      <p>{{ reply.content }}</p>
                    </div>
                    <div class="reply-actions">
                      <el-button type="primary" size="small" @click="showCommentDetail(reply)">
                        查看详情
                      </el-button>
                      <el-popconfirm title="确定删除该回复吗？" @confirm="handleDelete(reply)">
                        <template #reference>
                          <el-button type="danger" size="small">删除</el-button>
                        </template>
                      </el-popconfirm>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-pagination v-if="total > 0" class="pagination" layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]" :total="total" :page-size="pageSize" :current-page="currentPage"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 评论详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评论详情" width="600px">
      <div v-if="selectedComment" class="comment-detail">
        <div class="detail-header">
          <el-avatar :size="50" :src="getAvatarUrl(selectedComment.userAvatar)" @error="handleAvatarError">
            {{ getInitials(selectedComment.nickname || selectedComment.username) }}
          </el-avatar>
          <div class="detail-user-info">
            <h3>{{ selectedComment.nickname || selectedComment.username || '匿名用户' }}</h3>
            <div v-if="selectedComment.userId" class="detail-user-id">ID: {{ selectedComment.userId }}</div>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">评论ID:</div>
          <div class="detail-value">{{ selectedComment.id }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">评论内容:</div>
          <div class="detail-value content">{{ selectedComment.content }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">评论文章:</div>
          <div class="detail-value">
            <router-link :to="`/articles/${selectedComment.articleId}`" target="_blank">
              {{ selectedComment.articleTitle }}
            </router-link>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-label">评论时间:</div>
          <div class="detail-value">{{ formatDate(selectedComment.createTime) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">状态:</div>
          <div class="detail-value">
            <el-tag :type="getStatusType(selectedComment.status)">
              {{ getStatusText(selectedComment.status) }}
            </el-tag>
          </div>
        </div>
        <div v-if="selectedComment.parentId" class="detail-item">
          <div class="detail-label">回复评论:</div>
          <div class="detail-value">ID: {{ selectedComment.parentId }}</div>
        </div>
        <div v-if="selectedComment.replyToUsername" class="detail-item">
          <div class="detail-label">回复用户:</div>
          <div class="detail-value">{{ selectedComment.replyToNickname || selectedComment.replyToUsername }}</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="danger" @click="confirmDelete">删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { adminApi, fileApi } from '@/api'
import { formatDate } from '@/utils'
import { Search, User } from '@element-plus/icons-vue'

export default {
  name: 'AdminComments',
  components: {
    Search,
    User
  },
  setup() {
    const store = useStore()
    const loading = ref(true)
    const tableLoading = ref(false)
    const error = ref(null)
    const comments = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const searchKeyword = ref('')
    const userIdFilter = ref('')
    const statusFilter = ref('')
    const detailDialogVisible = ref(false)
    const selectedComment = ref(null)
    const expandedArticles = ref([])
    const expandedReplies = ref([]) // 存储已展开回复的评论ID
    const defaultAvatar = require('@/assets/default-avatar.png');

    // 切换回复的展开/折叠状态
    const toggleReplies = (commentId) => {
      const index = expandedReplies.value.indexOf(commentId);
      if (index === -1) {
        expandedReplies.value.push(commentId);
      } else {
        expandedReplies.value.splice(index, 1);
      }
    };

    // 检查回复是否已展开
    const isRepliesExpanded = (commentId) => {
      return expandedReplies.value.includes(commentId);
    };

    // 按文章分组的评论
    const articleComments = computed(() => {
      const articlesMap = new Map();

      // 遍历所有评论，按文章分组
      comments.value.forEach(comment => {
        if (!articlesMap.has(comment.articleId)) {
          articlesMap.set(comment.articleId, {
            id: comment.articleId,
            title: comment.articleTitle,
            comments: [],
            rootComments: [],
            commentsCount: 0
          });
        }

        const article = articlesMap.get(comment.articleId);

        // 将所有评论添加到文章的comments数组中
        article.comments.push(comment);
      });

      // 处理评论层级关系
      articlesMap.forEach(article => {
        // 将评论分为根评论和回复
        const rootComments = [];
        const repliesMap = new Map();

        // 首先找出所有根评论和回复
        article.comments.forEach(comment => {
          if (!comment.parentId) {
            // 这是根评论
            comment.replies = [];
            rootComments.push(comment);
          } else {
            // 这是回复，暂存到Map中
            if (!repliesMap.has(comment.parentId)) {
              repliesMap.set(comment.parentId, []);
            }
            repliesMap.get(comment.parentId).push(comment);
          }
        });

        // 将回复添加到对应的父评论中
        rootComments.forEach(rootComment => {
          if (repliesMap.has(rootComment.id)) {
            rootComment.replies = repliesMap.get(rootComment.id);
          }
        });

        // 更新文章的根评论和评论总数
        article.rootComments = rootComments;
        article.commentsCount = article.comments.length;
      });

      // 将Map转换为数组并按评论数量排序
      return Array.from(articlesMap.values())
        .sort((a, b) => b.commentsCount - a.commentsCount);
    });

    const fetchComments = async () => {
      loading.value = true;
      error.value = null;

      try {
        // 获取所有评论，需要处理分页
        const response = await adminApi.getAllComments(
          currentPage.value - 1,
          pageSize.value
        );

        if (response.data.success) {
          const commentsData = response.data.data;

          // 处理不同格式的响应数据
          if (Array.isArray(commentsData)) {
            comments.value = commentsData;
            total.value = commentsData.length;
          } else if (commentsData && commentsData.content) {
            comments.value = commentsData.content;
            total.value = commentsData.totalElements || commentsData.content.length;
          } else {
            comments.value = [];
            total.value = 0;
          }

          // 如果有搜索关键词，过滤评论
          if (searchKeyword.value) {
            const keyword = searchKeyword.value.toLowerCase();
            comments.value = comments.value.filter(comment =>
              (comment.content && comment.content.toLowerCase().includes(keyword)) ||
              (comment.username && comment.username.toLowerCase().includes(keyword)) ||
              (comment.nickname && comment.nickname.toLowerCase().includes(keyword)) ||
              (comment.articleTitle && comment.articleTitle.toLowerCase().includes(keyword))
            );
            total.value = comments.value.length;
          }

          // 如果有状态筛选
          if (statusFilter.value) {
            comments.value = comments.value.filter(comment =>
              comment.status === statusFilter.value
            );
            total.value = comments.value.length;
          }

          // 如果结果少于5个文章，自动展开所有文章的评论
          if (articleComments.value.length <= 5) {
            expandedArticles.value = articleComments.value.map(article => article.id);
          } else if (articleComments.value.length > 0) {
            // 否则只展开第一个文章的评论
            expandedArticles.value = [articleComments.value[0].id];
          }
        } else {
          error.value = response.data.message || '获取评论列表失败';
        }
      } catch (err) {
        error.value = '获取评论列表失败，请稍后重试';
        console.error('获取评论列表失败:', err);
      } finally {
        loading.value = false;
      }
    };

    // 通过用户ID搜索评论
    const fetchCommentsByUserId = async () => {
      if (!userIdFilter.value) {
        return fetchComments();
      }

      loading.value = true;
      error.value = null;

      try {
        const response = await adminApi.getCommentsByUserId(
          userIdFilter.value,
          currentPage.value - 1,
          pageSize.value
        );

        if (response.data.success) {
          const commentsData = response.data.data;

          // 处理不同格式的响应数据
          if (Array.isArray(commentsData)) {
            comments.value = commentsData;
            total.value = commentsData.length;
          } else if (commentsData && commentsData.content) {
            comments.value = commentsData.content;
            total.value = commentsData.totalElements || commentsData.content.length;
          } else {
            comments.value = [];
            total.value = 0;
          }

          // 如果有状态筛选
          if (statusFilter.value) {
            comments.value = comments.value.filter(comment =>
              comment.status === statusFilter.value
            );
            total.value = comments.value.length;
          }

          // 展开所有文章的评论
          expandedArticles.value = articleComments.value.map(article => article.id);
        } else {
          error.value = response.data.message || '获取用户评论失败';
        }
      } catch (err) {
        error.value = '获取用户评论失败，请稍后重试';
        console.error('获取用户评论失败:', err);
      } finally {
        loading.value = false;
      }
    };

    const toggleArticleComments = (articleId) => {
      const index = expandedArticles.value.indexOf(articleId);
      if (index === -1) {
        expandedArticles.value.push(articleId);
      } else {
        expandedArticles.value.splice(index, 1);
      }
    };

    const handleSizeChange = (size) => {
      pageSize.value = size;
      if (userIdFilter.value) {
        fetchCommentsByUserId();
      } else {
        fetchComments();
      }
    };

    const handleCurrentChange = (page) => {
      currentPage.value = page;
      if (userIdFilter.value) {
        fetchCommentsByUserId();
      } else {
        fetchComments();
      }
    };

    const handleSearch = () => {
      currentPage.value = 1;
      userIdFilter.value = ''; // 清空用户ID筛选
      fetchComments();
    };

    const clearSearch = () => {
      searchKeyword.value = '';
      fetchComments();
    };

    const handleUserIdSearch = () => {
      currentPage.value = 1;
      searchKeyword.value = ''; // 清空关键词搜索
      fetchCommentsByUserId();
    };

    const handleStatusChange = () => {
      currentPage.value = 1;
      if (userIdFilter.value) {
        fetchCommentsByUserId();
      } else {
        fetchComments();
      }
    };

    const handleDelete = async (comment) => {
      tableLoading.value = true;

      try {
        const response = await adminApi.deleteComment(comment.id);

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: '评论已删除'
          });

          // 如果是在详情对话框中删除，则关闭对话框
          if (detailDialogVisible.value) {
            detailDialogVisible.value = false;
          }

          // 重新获取评论
          if (userIdFilter.value) {
            fetchCommentsByUserId();
          } else {
            fetchComments();
          }
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          });
        }
      } catch (err) {
        console.error('删除评论失败:', err);
        store.dispatch('showNotification', {
          type: 'error',
          message: '删除评论失败，请稍后重试'
        });
      } finally {
        tableLoading.value = false;
      }
    };

    const getStatusType = (status) => {
      const upperStatus = (status || '').toUpperCase();
      switch (upperStatus) {
        case 'APPROVED': return 'success';
        case 'REJECTED': return 'danger';
        default: return 'info';
      }
    };

    const getStatusText = (status) => {
      const upperStatus = (status || '').toUpperCase();
      switch (upperStatus) {
        case 'APPROVED': return '已通过';
        case 'REJECTED': return '已拒绝';
        default: return '未知';
      }
    };

    const truncateText = (text, length) => {
      if (!text) return '';
      return text.length > length ? text.substring(0, length) + '...' : text;
    };

    const showCommentDetail = (comment) => {
      selectedComment.value = { ...comment };
      detailDialogVisible.value = true;
    };

    const confirmDelete = () => {
      if (selectedComment.value) {
        handleDelete(selectedComment.value);
      }
    };

    // 获取头像URL
    const getAvatarUrl = (avatar) => {
      if (!avatar) return '';
      if (avatar.startsWith('http')) return avatar;
      return fileApi.getAvatarUrl(avatar);
    };

    // 处理头像加载错误
    const handleAvatarError = (e) => {
      e.target.src = defaultAvatar;
    };

    // 获取用户名首字母（用于头像显示）
    const getInitials = (name) => {
      if (!name) return '?';
      return name.charAt(0).toUpperCase();
    };

    // 监听筛选条件变化
    watch([searchKeyword, statusFilter], () => {
      currentPage.value = 1;
    });

    onMounted(() => {
      fetchComments();
    });

    return {
      loading,
      tableLoading,
      error,
      comments,
      articleComments,
      total,
      currentPage,
      pageSize,
      searchKeyword,
      userIdFilter,
      statusFilter,
      detailDialogVisible,
      selectedComment,
      expandedArticles,
      expandedReplies,
      handleSizeChange,
      handleCurrentChange,
      handleDelete,
      handleSearch,
      clearSearch,
      handleUserIdSearch,
      handleStatusChange,
      formatDate,
      getStatusType,
      getStatusText,
      truncateText,
      showCommentDetail,
      confirmDelete,
      toggleArticleComments,
      toggleReplies,
      isRepliesExpanded,
      getAvatarUrl,
      handleAvatarError,
      getInitials
    };
  }
}
</script>

<style scoped>
.admin-comments {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

.search-container {
  flex-grow: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

.user-id-input {
  width: 120px;
}

.search-icon {
  cursor: pointer;
  color: #909399;
}

.search-icon:hover {
  color: #002FA7;
}

.filter-container {
  display: flex;
  gap: 10px;
}

.loading,
.error {
  padding: 20px;
  text-align: center;
}

.error {
  color: #f56c6c;
}

.empty-state {
  padding: 40px 0;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.article-comments-container {
  background-color: #fff;
  border-radius: 0;
  box-shadow: none;
  overflow: hidden;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6e6e6;
}

.article-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.article-title a {
  color: #303133;
  text-decoration: none;
}

.article-title a:hover {
  color: #002FA7;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.comments-list {
  padding: 0;
}

.comment-container {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.comment-container:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.comment-user,
.reply-user {
  display: flex;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.user-id {
  font-size: 12px;
  color: #909399;
  margin-left: 5px;
}

.comment-time,
.reply-time {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.comment-content,
.reply-content {
  margin: 10px 0;
  line-height: 1.6;
}

.comment-content p,
.reply-content p {
  margin: 0;
  word-break: break-word;
}

.comment-actions,
.reply-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.replies-list {
  margin-top: 15px;
  margin-left: 30px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 0;
  border-left: 3px solid #e0e0e0;
}

.reply-item {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.reply-to {
  color: #002FA7;
  font-size: 12px;
  margin-top: 2px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 评论详情样式 */
.comment-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.detail-user-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.detail-user-id {
  color: #909399;
  font-size: 14px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
}

.detail-label {
  width: 100px;
  font-weight: bold;
  color: #606266;
}

.detail-value {
  flex: 1;
}

.detail-value.content {
  white-space: pre-wrap;
  line-height: 1.5;
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 0;
  max-height: 200px;
  overflow-y: auto;
}
</style>
