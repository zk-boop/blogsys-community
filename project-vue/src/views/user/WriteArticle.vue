<template>
  <div class="write-article">
    <h1>写文章</h1>

    <div class="article-form">
      <el-form ref="form" :model="articleForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" maxlength="100" show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="摘要">
          <el-input v-model="articleForm.summary" type="textarea" placeholder="请输入文章摘要" maxlength="200"
            show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="分类" required>
          <div class="category-selector">
            <el-select v-model="articleForm.categoryId" placeholder="请选择分类" style="width: 80%"
              popper-class="category-dropdown">
              <template #prefix v-if="selectedCategory">
                <span class="selected-category-prefix">
                  <el-icon>
                    <Folder />
                  </el-icon>
                  <span class="selected-name">{{ selectedCategory.name }}</span>
                  <el-tag v-if="selectedCategory.isNew" size="small" type="warning" class="inline-tag">审核中</el-tag>
                </span>
              </template>
              <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id">
                <div class="category-option">
                  <div class="category-main">
                    <span v-if="category.status === 'PENDING'" class="category-pending-tag">审核中</span>
                    <span v-else-if="category.status === '0'" class="category-pending-tag">审核中</span>
                    <el-icon>
                      <Folder />
                    </el-icon>
                    {{ category.name }}
                  </div>
                  <div v-if="category.description" class="category-description">
                    {{ category.description }}
                  </div>
                  <div v-if="category.parentName" class="category-parent">
                    <el-icon>
                      <Connection />
                    </el-icon> 父分类: {{ category.parentName }}
                  </div>
                </div>
              </el-option>
            </el-select>
            <el-button type="primary" @click="showNewCategoryDialog" plain size="small" style="margin-left: 10px">
              <el-icon>
                <Plus />
              </el-icon>新建分类
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="标签">
          <div class="tag-selector">
            <!-- 使用更稳定的渲染方式 -->
            <div class="select-wrapper">
              <el-select v-model="articleForm.tagIds" multiple filterable remote reserve-keyword
                :remote-method="searchTags" placeholder="请选择标签（可多选）" style="width: 100%" popper-class="tags-dropdown"
                :popper-options="{
                  gpuAcceleration: false,
                  boundariesElement: 'viewport',
                  forceAbsolute: true
                }">
                <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id">
                  <span class="tag-option">
                    <span class="tag-color" :style="{ backgroundColor: tag.color }"></span>
                    {{ tag.name }}
                  </span>
                </el-option>
                <template v-if="!tags.length && tagSearchKeyword">
                  <div class="no-tag-tip">
                    没有找到"{{ tagSearchKeyword }}"，请创建新标签
                  </div>
                </template>
              </el-select>
            </div>
            <el-button type="primary" @click="showNewTagDialog" plain size="small" style="margin-left: 10px">
              <el-icon>
                <Plus />
              </el-icon>新建标签
            </el-button>
          </div>
          <!-- 优化已选标签显示区域 -->
          <div class="tags-info" v-if="selectedTags.length > 0">
            <el-card class="tags-info-card">
              <template #header>
                <div class="card-header">
                  <span>
                    <el-icon class="card-icon">
                      <Collection />
                    </el-icon>
                    已选标签
                  </span>
                  <span class="tag-count">
                    <el-icon>
                      <PriceTag />
                    </el-icon>
                    {{ selectedTags.length }} 个标签
                  </span>
                </div>
              </template>
              <!-- 使用更稳定的方式渲染标签列表 -->
              <div class="tags-list">
                <div v-for="tag in selectedTags" :key="tag.id" class="tag-wrapper">
                  <el-tag :color="tag.color" effect="light" class="tag-item" :class="{ 'new-tag': tag.isNew }"
                    :style="{ color: getTagTextColor(tag.color) }">
                    {{ tag.name }}
                    <el-tag v-if="tag.isNew" size="small" type="warning" effect="plain" class="status-tag">
                      <el-icon>
                        <Loading />
                      </el-icon>
                      审核中
                    </el-tag>
                  </el-tag>
                </div>
              </div>
            </el-card>
          </div>
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
          <el-button type="primary" @click="handleSubmit('submit')" :loading="submitting">提交审核</el-button>
          <el-button @click="handleSubmit('draft')" :loading="submitting">保存草稿</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 新建分类对话框 -->
    <el-dialog title="新建分类" v-model="newCategoryDialogVisible" width="500px" append-to-body>
      <el-form ref="categoryForm" :model="newCategory" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="newCategory.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newCategory.description" type="textarea" placeholder="请输入分类描述"></el-input>
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="newCategory.parentId" placeholder="选择父分类（可选）" clearable>
            <el-option v-for="category in categories" :key="category.id" :label="category.name"
              :value="category.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newCategoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createNewCategory" :loading="creatingCategory">创建</el-button>
        </span>
      </template>
      <div class="dialog-note">
        注意：新建分类需要经过管理员审核后才能正式使用。
      </div>
    </el-dialog>

    <!-- 新建标签对话框 -->
    <el-dialog title="新建标签" v-model="newTagDialogVisible" width="500px" append-to-body>
      <el-form ref="tagForm" :model="newTag" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="newTag.name" placeholder="请输入标签名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newTag.description" type="textarea" placeholder="请输入标签描述"></el-input>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="newTag.color"></el-color-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="newTagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createNewTag" :loading="creatingTag">创建</el-button>
        </span>
      </template>
      <div class="dialog-note">
        注意：新建标签需要经过管理员审核后才能正式使用。
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, nextTick, onBeforeMount, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { articleApi, categoryApi, tagApi, fileApi } from '@/api'
import { getContrastTextColor } from '@/utils'
import { Plus, Delete, Folder, Connection, Loading, Collection, PriceTag } from '@element-plus/icons-vue'

// 自定义钩子用来处理ResizeObserver错误
function useFixResizeObserverError() {
  const originalResizeObserver = window.ResizeObserver;
  const originalConsoleError = console.error;
  let patchedObserver = null;

  onBeforeMount(() => {
    try {
      // 定义更稳定的ResizeObserver实现
      class StableResizeObserver {
        constructor(callback) {
          this.callback = callback;
          this.observer = null;
          this.isObserving = false;
          this.pendingEntries = [];
          this.rafId = null;

          // 使用防抖处理多次快速变化
          this.processEntries = this.processEntries.bind(this);

          // 初始化真实的observer但使用我们自己的回调
          this.observer = new originalResizeObserver((entries) => {
            if (!this.isObserving) return;

            // 收集变更
            this.pendingEntries = [...this.pendingEntries, ...entries];

            // 如果已经有requestAnimationFrame计划，不再创建新的
            if (this.rafId) return;

            // 使用requestAnimationFrame批量处理变更
            this.rafId = requestAnimationFrame(this.processEntries);
          });
        }

        // 处理收集到的所有变更
        processEntries() {
          this.rafId = null;
          if (this.pendingEntries.length > 0) {
            const uniqueEntries = this.getUniqueEntries(this.pendingEntries);
            this.callback(uniqueEntries, this.observer);
            this.pendingEntries = [];
          }
        }

        // 确保只有每个元素的最新变更被处理
        getUniqueEntries(entries) {
          const targetMap = new Map();

          // 只保留每个目标的最新条目
          entries.forEach(entry => {
            targetMap.set(entry.target, entry);
          });

          return Array.from(targetMap.values());
        }

        observe(target) {
          this.isObserving = true;
          this.observer.observe(target);
        }

        unobserve(target) {
          this.observer.unobserve(target);
        }

        disconnect() {
          this.isObserving = false;
          this.observer.disconnect();
          if (this.rafId) {
            cancelAnimationFrame(this.rafId);
            this.rafId = null;
          }
          this.pendingEntries = [];
        }
      }

      // 替换原始的ResizeObserver
      patchedObserver = StableResizeObserver;
      window.ResizeObserver = StableResizeObserver;

      // 过滤掉ResizeObserver错误日志
      console.error = (...args) => {
        if (args.length > 0 &&
          typeof args[0] === 'string' &&
          (args[0].includes('ResizeObserver') ||
            args[0].includes('loop') ||
            args[0].includes('resize'))) {
          // 忽略错误
          return;
        }
        originalConsoleError.apply(console, args);
      };
    } catch (e) {
      console.log('Failed to patch ResizeObserver:', e);
    }
  });

  // 在组件卸载时恢复原始实现
  onBeforeUnmount(() => {
    try {
      if (window.ResizeObserver === patchedObserver) {
        window.ResizeObserver = originalResizeObserver;
      }
      console.error = originalConsoleError;
    } catch (e) {
      // 忽略错误
    }
  });
}

export default {
  name: 'WriteArticle',
  setup() {
    const router = useRouter()
    const store = useStore()
    const categories = ref([])
    const tags = ref([])
    const submitting = ref(false)
    const tagSearchKeyword = ref('')

    // 使用自定义钩子来修复ResizeObserver错误
    useFixResizeObserverError();

    // 新分类相关状态
    const newCategoryDialogVisible = ref(false)
    const newCategory = reactive({
      name: '',
      description: '',
      parentId: null
    })
    const creatingCategory = ref(false)

    // 新标签相关状态
    const newTagDialogVisible = ref(false)
    const newTag = reactive({
      name: '',
      description: '',
      color: '#002FA7'
    })
    const creatingTag = ref(false)

    const articleForm = reactive({
      title: '',
      content: '',
      summary: '',
      categoryId: null,
      tagIds: [],
      coverImage: '',
      pendingTags: [], // 用于存储用户创建的但尚未审核的标签
      newCategory: null, // 用户新创建的分类
      newTags: [] // 用户新创建的标签列表
    })

    const userId = computed(() => store.getters.userId)

    // 添加计算属性：选中的分类
    const selectedCategory = computed(() => {
      if (!articleForm.categoryId) return null;
      return categories.value.find(category => category.id === articleForm.categoryId) || null;
    })

    // 添加计算属性：已选的标签
    const selectedTags = computed(() => {
      if (!articleForm.tagIds || articleForm.tagIds.length === 0) {
        return [];
      }
      // 防止不必要的重新计算和渲染
      return tags.value.filter(tag => articleForm.tagIds.includes(tag.id));
    });

    // 优化后的搜索标签方法，添加防抖
    const searchTagsTimeout = ref(null);
    const searchTags = async (query) => {
      if (searchTagsTimeout.value) {
        clearTimeout(searchTagsTimeout.value);
      }

      // 使用防抖，等待用户输入停止后再搜索
      searchTagsTimeout.value = setTimeout(async () => {
        tagSearchKeyword.value = query;

        if (query) {
          try {
            const response = await tagApi.searchTags(query, 0, 10);
            if (response.data.success) {
              // 使用nextTick确保DOM更新后再进行操作
              nextTick(() => {
                tags.value = response.data.data;
              });
            }
          } catch (error) {
            console.error('搜索标签失败:', error);
          }
        } else {
          tagSearchKeyword.value = '';
          fetchTags();
        }
      }, 300); // 300ms防抖延迟
    }

    const fetchCategories = async () => {
      try {
        const response = await categoryApi.getAllCategories()
        if (response.data.success) {
          console.log("所有分类数据:", response.data.data);

          // 修改过滤逻辑，同时支持数字1和字符串"1"
          const approvedCategories = response.data.data.filter(category =>
            category.status === '1' || category.status === 1);

          console.log("过滤后的分类数据:", approvedCategories);

          categories.value = approvedCategories.length > 0 ? approvedCategories : response.data.data;
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
          console.log("所有标签数据:", response.data.data);

          // 修改过滤逻辑，同时支持数字1和字符串"1"
          const approvedTags = response.data.data.filter(tag =>
            tag.status === 1 || tag.status === '1');

          console.log("过滤后的标签数据:", approvedTags);

          tags.value = approvedTags.length > 0 ? approvedTags : response.data.data;
        }
      } catch (error) {
        console.error('获取标签失败:', error)
        store.dispatch('showNotification', {
          type: 'error',
          message: '获取标签失败，请刷新页面重试'
        })
      }
    }

    // 处理封面图片上传
    const uploadCover = async (options) => {
      try {
        const response = await fileApi.uploadCover(options.file)
        if (response.data.success) {
          // 保存服务器返回的完整URL，而不是仅文件名
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

    // 根据标签背景色获取合适的文字颜色
    const getTagTextColor = (backgroundColor) => {
      return getContrastTextColor(backgroundColor);
    }

    const removeCover = () => {
      articleForm.coverImage = ''
      store.dispatch('showNotification', {
        type: 'success',
        message: '封面图片已移除'
      })
    }

    // 显示新建分类对话框
    const showNewCategoryDialog = () => {
      newCategory.name = ''
      newCategory.description = ''
      newCategory.parentId = null
      newCategoryDialogVisible.value = true
    }

    // 创建新分类
    const createNewCategory = async () => {
      if (!newCategory.name) {
        store.dispatch('showNotification', {
          type: 'warning',
          message: '请输入分类名称'
        })
        return
      }

      creatingCategory.value = true

      try {
        // 创建临时分类对象
        const newCategoryData = {
          id: 'temp_' + Date.now(), // 临时ID，便于前端识别
          name: newCategory.name,
          description: newCategory.description,
          parentId: newCategory.parentId,
          status: '0', // 设置为待审核状态
          isNew: true // 标记为新创建的分类
        }

        // 查找父分类名称（如果有）
        if (newCategoryData.parentId) {
          const parentCategory = categories.value.find(c => c.id === newCategoryData.parentId)
          newCategoryData.parentName = parentCategory ? parentCategory.name : ''
        }

        // 将新分类添加到分类列表
        categories.value.push(newCategoryData)

        // 自动选中新创建的分类
        articleForm.categoryId = newCategoryData.id

        // 保存新分类信息到文章表单，以便发布时一起提交
        articleForm.newCategory = {
          name: newCategory.name,
          description: newCategory.description,
          parentId: newCategory.parentId,
          status: '0'
        }

        // 提醒用户
        store.dispatch('showNotification', {
          type: 'success',
          message: '已创建新分类，将在文章发布时一并提交审核'
        })

        newCategoryDialogVisible.value = false
      } catch (error) {
        console.error('创建分类失败:', error)
        store.dispatch('showNotification', {
          type: 'error',
          message: '创建分类失败，请稍后重试'
        })
      } finally {
        creatingCategory.value = false
      }
    }

    // 显示新建标签对话框
    const showNewTagDialog = () => {
      newTag.name = tagSearchKeyword.value || ''
      newTag.description = ''
      newTag.color = '#002FA7'
      newTagDialogVisible.value = true
    }

    // 创建新标签
    const createNewTag = async () => {
      if (!newTag.name) {
        store.dispatch('showNotification', {
          type: 'warning',
          message: '请输入标签名称'
        })
        return
      }

      creatingTag.value = true

      try {
        // 创建临时标签对象
        const newTagData = {
          id: 'temp_' + Date.now(), // 临时ID，便于前端识别
          name: newTag.name,
          description: newTag.description,
          color: newTag.color,
          status: 0, // 设置为待审核状态
          isNew: true // 标记为新创建的标签
        }

        // 添加到本地标签列表
        tags.value.push(newTagData)

        // 添加到已选标签中
        if (!articleForm.tagIds.includes(newTagData.id)) {
          articleForm.tagIds.push(newTagData.id)
        }

        // 保存新标签信息到文章表单，以便发布时一起提交
        articleForm.newTags.push({
          name: newTag.name,
          description: newTag.description,
          color: newTag.color,
          status: 0
        })

        // 提醒用户
        store.dispatch('showNotification', {
          type: 'success',
          message: '已创建新标签，将在文章发布时一并提交审核'
        })

        newTagDialogVisible.value = false
      } catch (error) {
        console.error('创建标签失败:', error)
        store.dispatch('showNotification', {
          type: 'error',
          message: '创建标签失败，请稍后重试'
        })
      } finally {
        creatingTag.value = false
      }
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
        let categoryId = articleForm.categoryId;

        // 如果选择了新创建的分类，先创建分类
        if (articleForm.newCategory && articleForm.categoryId.toString().startsWith('temp_')) {
          const categoryResponse = await categoryApi.createCategory(articleForm.newCategory);
          if (categoryResponse.data.success) {
            categoryId = categoryResponse.data.data.id;
            console.log('新分类已创建，ID:', categoryId);
          } else {
            // 如果分类创建失败，使用"其他"分类
            categoryId = 8; // "其他"分类ID
            console.log('新分类创建失败，使用其他分类');
          }
        }

        // 处理新创建的标签
        const newTagIds = [];
        if (articleForm.newTags && articleForm.newTags.length > 0) {
          for (const newTag of articleForm.newTags) {
            try {
              const tagResponse = await tagApi.createTag(newTag);
              if (tagResponse.data.success) {
                newTagIds.push(tagResponse.data.data.id);
              }
            } catch (error) {
              console.error('创建标签失败:', error);
            }
          }
        }

        // 创建文章对象，调整结构以符合后端API期望
        const article = {
          title: articleForm.title,
          content: articleForm.content,
          summary: articleForm.summary ? articleForm.summary.substring(0, 100) : articleForm.content.substring(0, 100),
          coverImage: articleForm.coverImage || null,
          status: 'DRAFT'
        }

        // 合并原有标签ID和新创建的标签ID
        const validTagIds = articleForm.tagIds.filter(id => !id.toString().startsWith('temp_'));
        const finalTagIds = [...validTagIds, ...newTagIds];
        if (finalTagIds.length > 0) {
          article.tagIds = finalTagIds;
        }

        // 创建文章
        const response = await articleApi.createArticle(article, userId.value, categoryId)

        if (response.data.success) {
          const articleId = response.data.data.id;

          // 如果是提交审核，则创建文章后立即调用提交审核API
          if (type === 'submit') {
            const submitResponse = await articleApi.submitArticle(articleId);

            if (submitResponse.data.success) {
              store.dispatch('showNotification', {
                type: 'success',
                message: '文章及新创建的分类标签已提交审核'
              })
              router.push('/user/articles')
            } else {
              store.dispatch('showNotification', {
                type: 'error',
                message: submitResponse.data.message || '提交审核失败'
              })
            }
          } else {
            // 保存为草稿
            store.dispatch('showNotification', {
              type: 'success',
              message: '草稿保存成功'
            })
            router.push('/user/articles')
          }
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (error) {
        console.error('保存文章失败:', error)
        let errorMessage = '操作失败，请稍后重试'
        if (error.response) {
          console.log('错误详情:', error.response.data)
          errorMessage = error.response.data.message || error.response.data.error || errorMessage
        }
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
      fetchCategories()
      fetchTags()

      // 初始化各种列表
      articleForm.pendingTags = []
      articleForm.newCategory = null
      articleForm.newTags = []
    })

    return {
      categories,
      tags,
      articleForm,
      submitting,
      tagSearchKeyword,
      newCategoryDialogVisible,
      newCategory,
      creatingCategory,
      newTagDialogVisible,
      newTag,
      creatingTag,
      uploadCover,
      removeCover,
      searchTags,
      showNewCategoryDialog,
      createNewCategory,
      showNewTagDialog,
      createNewTag,
      handleSubmit,
      handleCancel,
      selectedCategory, // 添加选中的分类计算属性
      selectedTags, // 添加已选标签计算属性
      getTagTextColor // 添加获取标签文字颜色的方法
    }
  }
}
</script>

<style scoped>
.write-article {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 120px);
}

h1 {
  margin-bottom: 24px;
  font-size: 28px;
  color: #303133;
  position: relative;
  padding-bottom: 12px;
  font-weight: 600;
}

h1::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 3px;
  background: #002FA7;
  border-radius: 0;
}

.article-form {
  max-width: 900px;
  background-color: white;
  padding: 24px;
  border-radius: 0;
  box-shadow: none;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  box-shadow: none;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: none;
}

:deep(.el-button) {
  transition: all 0.3s;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: none;
}

.editor-container {
  position: relative;
}

.editor-placeholder {
  color: #999;
  font-size: 0.8rem;
  margin-top: 10px;
  background-color: #f5f7fa;
  padding: 8px 12px;
  border-radius: 0;
  font-style: italic;
}

.cover-upload-container {
  position: relative;
  margin-bottom: 15px;
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
  background-color: #f5f7fa;
  border-radius: 0;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 28px;
  color: #8c939d;
  text-align: center;
}

.cover-actions {
  position: absolute;
  top: 5px;
  right: 5px;
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

.upload-icon {
  font-size: 24px;
}

.upload-tip {
  font-size: 0.8rem;
  color: #909399;
  margin-top: 5px;
  text-align: center;
}

.category-selector,
.tag-selector {
  display: flex;
  align-items: flex-start;
}

.select-wrapper {
  width: 80%;
  position: relative;
  /* 防止布局抖动 */
  min-height: 40px;
}

.tag-option {
  display: flex;
  align-items: center;
}

.tag-color {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 0;
  margin-right: 8px;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.category-option {
  display: flex;
  flex-direction: column;
  padding: 6px 0;
}

.category-option .el-icon {
  margin-right: 6px;
  color: #002FA7;
}

.category-main {
  display: flex;
  align-items: center;
  font-weight: 500;
  font-size: 14px;
}

.category-pending-tag {
  padding: 2px 4px;
  margin-right: 5px;
  font-size: 12px;
  color: #e6a23c;
  background-color: #fdf6ec;
  border-radius: 0;
}

.category-description {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  margin-left: 20px;
  padding-left: 6px;
  border-left: 2px solid #f0f0f0;
}

.category-parent {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  margin-left: 20px;
  display: flex;
  align-items: center;
}

.category-parent .el-icon {
  font-size: 12px;
  margin-right: 4px;
}

/* 选中的分类样式 */
.selected-category-prefix {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #002FA7;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-category-prefix .el-icon {
  margin-right: 6px;
}

.selected-name {
  margin-right: 6px;
}

.inline-tag {
  font-size: 10px;
  margin-left: 4px;
  height: 18px;
  line-height: 16px;
  border-radius: 0;
  padding: 0 4px;
  transform: scale(0.9);
}

:deep(.category-dropdown) .el-select-dropdown__item {
  padding: 6px 12px;
  height: auto;
}

:deep(.category-dropdown) .el-select-dropdown__item.selected {
  background-color: #ecf5ff;
}

:deep(.category-dropdown) .el-select-dropdown__item:hover {
  background-color: #f5f7fa;
}

/* 优化标签选择器的弹出层 */
:deep(.tags-dropdown) {
  transform: translateZ(0);
  will-change: transform;
  contain: layout;
}

:deep(.tags-dropdown) .el-select-dropdown__item {
  will-change: background-color;
  contain: layout style;
  transition: background-color 0.15s ease;
  padding: 8px 12px;
  height: auto;
  min-height: 34px;
}

.dialog-note {
  font-size: 12px;
  color: #999;
  margin-top: 15px;
}

.no-tag-tip {
  padding: 10px;
  font-size: 13px;
  color: #909399;
  text-align: center;
}

/* 添加分类信息卡片样式 */
.category-info {
  margin-top: 15px;
  width: 80%;
  transition: all 0.3s ease;
}

.category-info-card {
  margin-bottom: 15px;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
  transition: all 0.3s ease-in-out;
}

.category-info-card:hover {
  transform: translateY(-2px);
  box-shadow: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.card-icon {
  margin-right: 6px;
  color: #002FA7;
  vertical-align: middle;
}

.info-content {
  position: relative;
  padding: 16px;
  background: #FFFFFF;
}

.info-content h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  padding-right: 75px;
  /* 为new-category-badge预留空间 */
}

.info-content p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  display: flex;
  align-items: center;
}

.info-content p .el-icon {
  margin-right: 6px;
  color: #909399;
}

.new-category-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  background: #002FA7;
  color: #fff;
  border-radius: 0;
  font-size: 12px;
  font-weight: 500;
  box-shadow: none;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.pending-note {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  align-items: center;
}

.pending-note span {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
  font-style: italic;
}

/* 添加已选标签显示区域样式 */
.tags-info {
  margin-top: 15px;
  width: 80%;
  transition: all 0.3s ease;
}

.tags-info-card {
  margin-bottom: 15px;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
  transition: all 0.3s ease-in-out;
}

.tags-info-card:hover {
  transform: translateY(-2px);
  box-shadow: none;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 16px;
  background: #FFFFFF;
  will-change: contents;
  contain: layout;
  position: relative;
  /* 设置最小高度防止布局抖动 */
  min-height: 60px;
}

.tag-wrapper {
  /* 固定尺寸可减少布局抖动 */
  margin: 5px 0;
  position: relative;
  /* 使用硬件加速减少重排 */
  transform: translateZ(0);
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 0;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: none;
  position: relative;
  overflow: hidden;
  margin: 0;
  max-width: 100%;
  /* 使用硬件加速减少重排 */
  transform: translateZ(0);
  will-change: transform;
  contain: content;
}

.tag-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent;
  z-index: 0;
  pointer-events: none;
  /* 防止拦截点击事件 */
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: none;
}

.new-tag {
  background: #F7F7F8;
  border: 1px solid #faad14;
  color: #d48806;
}

.status-tag {
  margin-left: 8px;
  border-radius: 0;
  font-size: 10px;
  padding: 1px 6px;
  background-color: rgba(250, 173, 20, 0.1);
  color: #d48806;
  display: inline-flex;
  align-items: center;
  transform: translateZ(0);
  /* 硬件加速 */
}

.status-tag .el-icon {
  margin-right: 4px;
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.tag-count {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
  background-color: #f2f6fc;
  padding: 2px 8px;
  border-radius: 0;
  font-weight: normal;
  display: inline-flex;
  align-items: center;
}

.tag-count .el-icon {
  margin-right: 4px;
}

/* 强制浏览器使用独立图层渲染标签组件 */
:deep(.el-select__tags),
:deep(.el-select__tags-text),
:deep(.el-tag) {
  transform: translateZ(0);
  will-change: transform;
  contain: content;
  backface-visibility: hidden;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 优化标签渲染性能 */
:deep(.el-select__tags-text) {
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 90px;
}

/* 设置固定高度，减少布局变化 */
:deep(.el-select__tags) {
  min-height: 24px;
  padding-top: 2px;
}
</style>
