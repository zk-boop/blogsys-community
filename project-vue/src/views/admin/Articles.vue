<template>
  <section class="admin-articles">
    <header class="page-header">
      <div>
        <p class="eyebrow">CONTENT OPERATIONS</p>
        <h1>文章管理</h1>
        <p class="page-description">审核投稿、维护展示顺序与管理已发布内容。</p>
      </div>
      <div class="summary" aria-label="文章统计">
        <span>共 {{ total }} 篇</span>
        <span v-if="statusFilter">当前：{{ getStatusText(statusFilter) }}</span>
      </div>
    </header>

    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索标题、作者或摘要"
        class="search-input"
        clearable
        @input="handleSearch"
      />
      <el-select v-model="statusFilter" class="status-select" aria-label="筛选文章状态" @change="handleFilterChange">
        <el-option label="全部状态" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
      </el-select>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
    <template v-else>
      <el-table :data="visibleArticles" row-key="id" class="article-table" empty-text="没有符合条件的文章">
        <el-table-column prop="id" label="ID" width="82" />
        <el-table-column label="文章" min-width="330">
          <template #default="{ row }">
            <button class="article-title" type="button" @click="openPreview(row)">{{ row.title }}</button>
            <p v-if="row.excerpt" class="article-excerpt">{{ row.excerpt }}</p>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" min-width="120" />
        <el-table-column label="状态" width="105">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="plain">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="展示" width="120">
          <template #default="{ row }">
            <div class="display-flags">
              <span v-if="row.isTop">置顶</span>
              <span v-if="row.isFeatured">推荐</span>
              <span v-if="!row.isTop && !row.isFeatured" class="muted">常规</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="86" align="right" />
        <el-table-column label="操作" width="142" align="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button v-if="isPending(row)" type="primary" size="small" @click="openPreview(row)">审核</el-button>
              <el-button v-else size="small" @click="openPreview(row)">预览</el-button>
              <el-dropdown trigger="click" @command="command => handleRowCommand(command, row)">
                <el-button class="more-button" text size="small" aria-label="更多文章操作">···</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="isPublished(row)" command="open">打开公开页</el-dropdown-item>
                    <el-dropdown-item v-if="isPublished(row)" command="featured">
                      {{ row.isFeatured ? '取消推荐' : '设为推荐' }}
                    </el-dropdown-item>
                    <el-dropdown-item v-if="isPublished(row)" command="top">
                      {{ row.isTop ? '取消置顶' : '设为置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item divided command="delete" class="danger-menu-item">删除文章</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        class="pagination"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </template>

    <el-drawer v-model="previewOpen" size="min(760px, 92vw)" :show-close="false" class="review-drawer">
      <template #header="{ close }">
        <div class="drawer-header">
          <div>
            <p class="eyebrow">{{ isPending(selectedArticle) ? 'EDITORIAL REVIEW' : 'ADMIN PREVIEW' }}</p>
            <span class="drawer-heading">{{ isPending(selectedArticle) ? '审核文章' : '文章预览' }}</span>
          </div>
          <el-button text @click="close">关闭</el-button>
        </div>
      </template>

      <article v-if="selectedArticle" class="article-preview">
        <div class="preview-status">
          <el-tag :type="getStatusType(selectedArticle.status)" effect="plain">
            {{ getStatusText(selectedArticle.status) }}
          </el-tag>
          <span>文章 ID {{ selectedArticle.id }}</span>
        </div>
        <h2>{{ selectedArticle.title }}</h2>
        <div class="preview-meta">
          <span>{{ selectedArticle.authorName || '未知作者' }}</span>
          <span>{{ selectedArticle.categoryName || '未分类' }}</span>
          <span>{{ formatDate(selectedArticle.createdAt) }}</span>
        </div>
        <p v-if="selectedArticle.excerpt" class="preview-excerpt">{{ selectedArticle.excerpt }}</p>
        <img v-if="selectedArticle.coverImage" class="preview-cover" :src="selectedArticle.coverImage" alt="文章封面" />
        <div class="preview-content" v-html="selectedArticle.content"></div>
      </article>

      <template #footer>
        <div class="drawer-footer">
          <span class="footer-note">此处仅用于后台审阅，不计入阅读，也不提供点赞、收藏或评论。</span>
          <div class="footer-actions">
            <el-button v-if="isPublished(selectedArticle)" @click="openPublicArticle(selectedArticle)">打开公开页</el-button>
            <el-button v-if="isPending(selectedArticle)" :loading="reviewing" @click="handleReject(selectedArticle)">退回修改</el-button>
            <el-button v-if="isPending(selectedArticle)" type="primary" :loading="reviewing" @click="handlePublish(selectedArticle)">通过并发布</el-button>
          </div>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import { adminApi, articleApi, categoryApi, tagApi } from '@/api'
import { formatDate } from '@/utils'

export default {
  name: 'AdminArticles',
  setup() {
    const store = useStore()
    const router = useRouter()
    const loading = ref(true)
    const reviewing = ref(false)
    const error = ref(null)
    const articles = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const searchKeyword = ref('')
    const statusFilter = ref('')
    const previewOpen = ref(false)
    const selectedArticle = ref(null)

    const normalizedStatus = article => (article?.status || '').toUpperCase()
    const isPending = article => normalizedStatus(article) === 'PENDING'
    const isPublished = article => normalizedStatus(article) === 'PUBLISHED'

    const visibleArticles = computed(() => {
      const keyword = searchKeyword.value.trim().toLowerCase()
      if (!keyword) return articles.value
      return articles.value.filter(article => [article.title, article.authorName, article.excerpt]
        .some(value => (value || '').toLowerCase().includes(keyword)))
    })

    const notify = (type, message) => store.dispatch('showNotification', { type, message })

    const fetchArticles = async () => {
      loading.value = true
      error.value = null
      try {
        const response = statusFilter.value === 'PENDING'
          ? await adminApi.getPendingArticles(currentPage.value - 1, pageSize.value)
          : await adminApi.getAllArticles(currentPage.value - 1, pageSize.value)
        if (!response.data.success) throw new Error(response.data.message || '获取文章列表失败')
        const page = response.data.data
        let list = page.content || page
        if (statusFilter.value && statusFilter.value !== 'PENDING') {
          list = list.filter(article => normalizedStatus(article) === statusFilter.value)
        }
        articles.value = list
        total.value = statusFilter.value && statusFilter.value !== 'PENDING'
          ? list.length
          : (page.totalElements ?? list.length)
      } catch (err) {
        error.value = err.message || '获取文章列表失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    const getStatusType = status => ({ PUBLISHED: 'success', PENDING: 'warning', DRAFT: 'info' }[(status || '').toUpperCase()] || 'info')
    const getStatusText = status => ({ PUBLISHED: '已发布', PENDING: '待审核', DRAFT: '草稿' }[(status || '').toUpperCase()] || '未知')
    const openPreview = article => {
      selectedArticle.value = article
      previewOpen.value = true
    }
    const openPublicArticle = article => {
      if (isPublished(article)) router.push(`/articles/${article.id}`)
    }

    const checkArticleDependencies = async articleId => {
      const articleResponse = await articleApi.getArticleById(articleId)
      if (!articleResponse.data.success) throw new Error('无法获取文章信息')
      const article = articleResponse.data.data
      const pendingTags = []
      const pendingCategories = []
      try {
        const response = await tagApi.getTagsByArticleId(articleId)
        if (response.data.success) pendingTags.push(...response.data.data.filter(tag => String(tag.status) === '0'))
      } catch (err) {
        console.warn('文章标签状态读取失败', err)
      }
      if (article.categoryId) {
        const response = await categoryApi.getCategoryById(article.categoryId)
        if (response.data.success && String(response.data.data.status) === '0') pendingCategories.push(response.data.data)
      }
      return { pendingTags, pendingCategories }
    }

    const handlePublish = async article => {
      reviewing.value = true
      try {
        const dependencies = await checkArticleDependencies(article.id)
        if (dependencies.pendingTags.length || dependencies.pendingCategories.length) {
          const names = [...dependencies.pendingCategories, ...dependencies.pendingTags].map(item => item.name).join('、')
          await ElMessageBox.alert(`请先审核关联的分类或标签：${names}`, '暂时无法发布', { confirmButtonText: '我知道了', type: 'warning' })
          return
        }
        const response = await adminApi.publishArticle(article.id)
        if (!response.data.success) throw new Error(response.data.message || '发布失败')
        previewOpen.value = false
        notify('success', '文章已通过审核并发布')
        await fetchArticles()
      } catch (err) {
        if (err !== 'close') notify('error', err.message || '发布文章失败，请稍后重试')
      } finally {
        reviewing.value = false
      }
    }

    const handleReject = async article => {
      try {
        await ElMessageBox.confirm('退回后文章将恢复为草稿，作者可修改后重新提交。', '退回文章', {
          confirmButtonText: '确认退回', cancelButtonText: '取消', type: 'warning'
        })
      } catch (_) { return }
      reviewing.value = true
      try {
        const response = await adminApi.rejectArticle(article.id)
        if (!response.data.success) throw new Error(response.data.message || '操作失败')
        previewOpen.value = false
        notify('success', '文章已退回作者修改')
        await fetchArticles()
      } catch (err) {
        notify('error', err.message || '退回文章失败，请稍后重试')
      } finally {
        reviewing.value = false
      }
    }

    const toggleFeatured = async article => {
      const response = await adminApi.toggleArticleFeatured(article.id, !article.isFeatured)
      if (!response.data.success) throw new Error(response.data.message || '操作失败')
      notify('success', article.isFeatured ? '已取消推荐' : '已设为推荐')
      await fetchArticles()
    }
    const toggleTop = async article => {
      const response = await adminApi.toggleArticleTop(article.id, !article.isTop)
      if (!response.data.success) throw new Error(response.data.message || '操作失败')
      notify('success', article.isTop ? '已取消置顶' : '已设为置顶')
      await fetchArticles()
    }
    const handleDelete = async article => {
      try {
        await ElMessageBox.confirm(`删除“${article.title}”后无法恢复。`, '删除文章', {
          confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error'
        })
      } catch (_) { return }
      const response = await adminApi.deleteArticle(article.id)
      if (!response.data.success) throw new Error(response.data.message || '删除失败')
      notify('success', '文章已删除')
      await fetchArticles()
    }
    const handleRowCommand = async (command, article) => {
      try {
        if (command === 'open') openPublicArticle(article)
        if (command === 'featured') await toggleFeatured(article)
        if (command === 'top') await toggleTop(article)
        if (command === 'delete') await handleDelete(article)
      } catch (err) {
        notify('error', err.message || '操作失败，请稍后重试')
      }
    }

    const handleFilterChange = () => { currentPage.value = 1; fetchArticles() }
    const handleSizeChange = size => { pageSize.value = size; fetchArticles() }
    const handleCurrentChange = page => { currentPage.value = page; fetchArticles() }
    const handleSearch = () => { currentPage.value = 1 }

    onMounted(fetchArticles)
    return {
      loading, reviewing, error, articles, visibleArticles, total, currentPage, pageSize, searchKeyword,
      statusFilter, previewOpen, selectedArticle, formatDate, getStatusType, getStatusText, isPending,
      isPublished, openPreview, openPublicArticle, handlePublish, handleReject, handleRowCommand,
      handleFilterChange, handleSizeChange, handleCurrentChange, handleSearch
    }
  }
}
</script>

<style scoped>
.admin-articles { padding: 32px; color: #171717; }
.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; padding-bottom: 24px; border-bottom: 1px solid #d9d9d9; }
.eyebrow { margin: 0 0 8px; color: #002fa7; font: 700 11px/1.2 Arial, sans-serif; letter-spacing: .14em; }
h1 { margin: 0; font-size: clamp(30px, 4vw, 46px); line-height: 1; letter-spacing: -.045em; }
.page-description { margin: 12px 0 0; color: #6b6b6b; }
.summary { display: flex; gap: 16px; color: #777; font-size: 13px; }
.toolbar { display: flex; gap: 12px; padding: 20px 0; }
.search-input { width: min(360px, 100%); }
.status-select { width: 150px; }
.article-table { border-top: 1px solid #d9d9d9; }
.article-title { display: block; max-width: 100%; padding: 0; border: 0; background: none; color: #002fa7; font: inherit; font-weight: 650; text-align: left; cursor: pointer; }
.article-title:hover { text-decoration: underline; text-underline-offset: 3px; }
.article-excerpt { overflow: hidden; margin: 6px 0 0; color: #818181; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.display-flags { display: flex; flex-wrap: wrap; gap: 4px 10px; font-size: 12px; }
.display-flags span { color: #002fa7; }
.display-flags .muted { color: #999; }
.row-actions { display: flex; justify-content: flex-end; align-items: center; gap: 2px; white-space: nowrap; }
.more-button { min-width: 30px; padding-inline: 6px; font-size: 18px; letter-spacing: 1px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 24px; }
.drawer-header { display: flex; width: 100%; align-items: center; justify-content: space-between; padding-right: 8px; }
.drawer-heading { font-size: 20px; font-weight: 700; }
.article-preview { max-width: 680px; margin: 0 auto; padding: 8px 8px 48px; }
.preview-status { display: flex; align-items: center; gap: 12px; color: #858585; font-size: 12px; }
.article-preview h2 { margin: 28px 0 16px; font-size: clamp(30px, 5vw, 48px); line-height: 1.08; letter-spacing: -.04em; }
.preview-meta { display: flex; flex-wrap: wrap; gap: 8px 18px; padding-bottom: 24px; border-bottom: 1px solid #ddd; color: #707070; font-size: 13px; }
.preview-excerpt { margin: 26px 0; color: #555; font-size: 18px; line-height: 1.7; }
.preview-cover { width: 100%; max-height: 360px; margin: 12px 0 28px; object-fit: cover; }
.preview-content { color: #292929; font-size: 16px; line-height: 1.9; overflow-wrap: anywhere; }
.preview-content :deep(img) { max-width: 100%; height: auto; }
.drawer-footer { display: flex; align-items: center; justify-content: space-between; gap: 20px; width: 100%; }
.footer-note { max-width: 380px; color: #888; font-size: 12px; line-height: 1.5; }
.footer-actions { display: flex; flex-shrink: 0; gap: 8px; }
:global(.danger-menu-item) { color: #c0392b; }
@media (max-width: 720px) {
  .admin-articles { padding: 20px 16px; }
  .page-header, .drawer-footer { align-items: flex-start; flex-direction: column; }
  .summary { display: none; }
  .toolbar { flex-direction: column; }
  .status-select { width: 100%; }
  .footer-actions { align-self: flex-end; }
}
</style>
