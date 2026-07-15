<template>
  <div class="edit-article">
    <h1>编辑文章</h1>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else class="article-form">
      <el-form ref="form" :model="articleForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" maxlength="100" show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="摘要">
          <el-input v-model="articleForm.summary" type="textarea" placeholder="请输入文章摘要" maxlength="200"
            show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="分类" required>
          <el-select v-model="articleForm.categoryId" placeholder="请选择分类">
            <el-option v-for="category in categories" :key="category.id" :label="category.name"
              :value="category.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="articleForm.tagIds" multiple filterable allow-create default-first-option
            placeholder="请选择标签（可多选）">
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="封面图">
          <div class="cover-upload-container">
            <el-upload class="cover-uploader" action="#" :http-request="uploadCover" :show-file-list="false"
              accept="image/*">
              <div v-if="articleForm.coverImage" class="cover-preview">
                <img :src="articleForm.coverImage" class="cover-image" alt="文章封面预览">
                <div class="cover-actions">
                  <el-button type="danger" size="small" icon="Delete" circle @click.stop="removeCover"></el-button>
                </div>
              </div>
              <div v-else class="cover-placeholder">
                <el-icon class="upload-icon">
                  <Plus />
                </el-icon>
                <div>点击上传封面图</div>
              </div>
            </el-upload>
            <div class="upload-tip">建议上传宽高比为16:9的高清图片，最佳尺寸1920×1080px</div>
          </div>
        </el-form-item>

        <el-form-item label="内容" required>
          <div class="editor-container">
            <!-- 这里可以集成富文本编辑器，如 TinyMCE、CKEditor 或 Vue-Quill-Editor 等 -->
            <el-input v-model="articleForm.content" type="textarea" placeholder="请输入文章内容" rows="15"></el-input>
            <div class="editor-placeholder">
              注意：这里仅为临时编辑框，可以集成完整的富文本编辑器
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit('submit')" :loading="submitting">更新并提交审核</el-button>
          <el-button @click="handleSubmit('draft')" :loading="submitting">保存为草稿</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { articleApi, categoryApi, tagApi, fileApi } from '@/api'
import { Plus } from '@element-plus/icons-vue'

export default {
  name: 'EditArticle',
  props: {
    id: {
      type: [Number, String],
      required: true
    }
  },
  setup(props) {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const loading = ref(true)
    const error = ref(null)
    const categories = ref([])
    const tags = ref([])
    const submitting = ref(false)
    const originalStatus = ref('')

    const articleForm = reactive({
      title: '',
      content: '',
      summary: '',
      categoryId: null,
      tagIds: [],
      coverImage: ''
    })

    const userId = computed(() => store.getters.userId)

    const fetchArticle = async () => {
      loading.value = true
      error.value = null

      try {
        const response = await articleApi.getArticleById(props.id)

        if (response.data.success) {
          const article = response.data.data

          articleForm.title = article.title
          articleForm.content = article.content
          articleForm.summary = article.summary
          articleForm.categoryId = article.categoryId
          articleForm.coverImage = article.coverImage

          // 保存原始状态，用于后续判断
          originalStatus.value = article.status

          // 获取文章标签
          const tagsResponse = await tagApi.getTagsByArticleId(props.id)
          if (tagsResponse.data.success) {
            articleForm.tagIds = tagsResponse.data.data.map(tag => tag.id)
          }
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

    const fetchCategories = async () => {
      try {
        const response = await categoryApi.getAllCategories()
        if (response.data.success) {
          categories.value = response.data.data
        }
      } catch (error) {
        console.error('获取分类失败:', error)
        store.dispatch('showNotification', {
          type: 'error',
          message: '获取分类失败，请刷新页面重试'
        })
      }
    }

    const fetchTags = async () => {
      try {
        const response = await tagApi.getAllTags()
        if (response.data.success) {
          tags.value = response.data.data
        }
      } catch (error) {
        console.error('获取标签失败:', error)
        store.dispatch('showNotification', {
          type: 'error',
          message: '获取标签失败，请刷新页面重试'
        })
      }
    }

    const uploadCover = async (options) => {
      try {
        const response = await fileApi.uploadCover(options.file)
        if (response.data.success) {
          articleForm.coverImage = response.data.data
          store.dispatch('showNotification', {
            type: 'success',
            message: '封面图片上传成功'
          })
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '封面图片上传失败'
          })
        }
      } catch (error) {
        console.error('封面图片上传失败:', error)
        store.dispatch('showNotification', {
          type: 'error',
          message: '封面图片上传失败，请稍后重试'
        })
      }
    }

    const removeCover = () => {
      articleForm.coverImage = ''
      store.dispatch('showNotification', {
        type: 'success',
        message: '封面图片已移除'
      })
    }

    const handleSubmit = async (type) => {
      if (!articleForm.title) {
        store.dispatch('showNotification', {
          type: 'warning',
          message: '请输入文章标题'
        })
        return
      }

      if (!articleForm.content) {
        store.dispatch('showNotification', {
          type: 'warning',
          message: '请输入文章内容'
        })
        return
      }

      if (!articleForm.categoryId) {
        store.dispatch('showNotification', {
          type: 'warning',
          message: '请选择文章分类'
        })
        return
      }

      submitting.value = true

      try {
        // 创建文章对象，确保字段名与后端一致
        const article = {
          title: articleForm.title,
          content: articleForm.content,
          summary: articleForm.summary || articleForm.content.substring(0, 150),
          coverImage: articleForm.coverImage || null,
          // 文章状态始终为草稿，符合工作流程
          status: 'DRAFT',
          // 如果没有标签，提供空数组而不是undefined
          tagIds: articleForm.tagIds && articleForm.tagIds.length > 0 ? articleForm.tagIds : []
        }

        // 更新文章
        const response = await articleApi.updateArticle(props.id, article, articleForm.categoryId)

        if (response.data.success) {
          // 如果选择提交审核
          if (type === 'submit') {
            try {
              const submitResponse = await articleApi.submitArticle(props.id)

              if (submitResponse.data.success) {
                store.dispatch('showNotification', {
                  type: 'success',
                  message: '文章已更新并提交审核'
                })
              } else {
                store.dispatch('showNotification', {
                  type: 'error',
                  message: submitResponse.data.message || '提交审核失败'
                })
              }
            } catch (submitError) {
              console.error('提交审核失败:', submitError)
              store.dispatch('showNotification', {
                type: 'error',
                message: '提交审核失败，请稍后重试'
              })
            }
          } else {
            store.dispatch('showNotification', {
              type: 'success',
              message: '草稿保存成功'
            })
          }

          router.push('/user/articles')
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (error) {
        console.error('更新文章失败:', error)
        // 显示更详细的错误信息
        const errorMessage = error.response?.data?.message || '操作失败，请稍后重试'
        store.dispatch('showNotification', {
          type: 'error',
          message: errorMessage
        })
      } finally {
        submitting.value = false
      }
    }

    const handleCancel = () => {
      router.push('/user/articles')
    }

    onMounted(() => {
      Promise.all([
        fetchArticle(),
        fetchCategories(),
        fetchTags()
      ])
    })

    return {
      loading,
      error,
      categories,
      tags,
      articleForm,
      submitting,
      uploadCover,
      removeCover,
      handleSubmit,
      handleCancel
    }
  }
}
</script>

<style scoped>
.edit-article {
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

.article-form {
  max-width: 900px;
}

.editor-container {
  position: relative;
}

.editor-placeholder {
  color: #999;
  font-size: 0.8rem;
  margin-top: 5px;
}

.cover-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10px;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 0;
  cursor: pointer;
  width: 300px;
  height: 160px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.cover-uploader:hover {
  border-color: #002FA7;
}

.cover-preview {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-actions {
  position: absolute;
  top: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 0;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 10;
}

.cover-placeholder {
  font-size: 28px;
  color: #8c939d;
  text-align: center;
}

.upload-icon {
  font-size: 24px;
}

.upload-tip {
  margin-top: 5px;
  font-size: 0.8rem;
  color: #909399;
}
</style>
