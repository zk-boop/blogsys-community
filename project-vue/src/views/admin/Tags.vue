<template>
  <div class="admin-tags">
    <h1>标签管理</h1>

    <el-tabs type="card" v-model="activeTab">
      <el-tab-pane label="标签列表" name="list">
        <el-input v-model="searchKeyword" placeholder="搜索标签" class="search-input" clearable @keyup.enter="handleSearch">
          <template #suffix>
            <el-icon class="search-icon" @click="handleSearch">
              <Search />
            </el-icon>
          </template>
        </el-input>

        <div class="actions">
          <el-button type="primary" @click="showCreateDialog">创建标签</el-button>
        </div>

        <div v-if="loading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="error" class="error">
          {{ error }}
        </div>
        <div v-else>
          <el-table :data="tags" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="标签" min-width="180">
              <template #default="{ row }">
                <div class="tag-name">
                  <div class="tag-color" :style="{ backgroundColor: row.color }"></div>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="220" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1 || row.status === '1'" type="success">已批准</el-tag>
                <el-tag v-else-if="row.status === 2 || row.status === '2'" type="danger">已拒绝</el-tag>
                <el-tag v-else-if="row.status === 0 || row.status === '0'" type="warning">待审核</el-tag>
                <span v-else>{{ row.status }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-popconfirm title="确定删除该标签吗？" @confirm="handleDelete(row)">
                  <template #reference>
                    <el-button size="small" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="待审核标签" name="pending">
        <div v-if="pendingLoading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="pendingError" class="error">
          {{ pendingError }}
        </div>
        <div v-else>
          <el-empty v-if="pendingTags.length === 0" description="没有待审核的标签" />
          <el-table v-else :data="pendingTags" style="width: 100%" v-loading="pendingLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="标签" min-width="180">
              <template #default="{ row }">
                <div class="tag-name">
                  <div class="tag-color" :style="{ backgroundColor: row.color }"></div>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="220" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1 || row.status === '1'" type="success">已批准</el-tag>
                <el-tag v-else-if="row.status === 2 || row.status === '2'" type="danger">已拒绝</el-tag>
                <el-tag v-else-if="row.status === 0 || row.status === '0'" type="warning">待审核</el-tag>
                <span v-else>{{ row.status }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="handleApprove(row)">批准</el-button>
                <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建标签对话框 -->
    <el-dialog title="创建标签" v-model="dialogVisible" width="500px">
      <el-form ref="form" :model="form" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入标签名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入标签描述"></el-input>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color"></el-color-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { tagApi, adminApi } from '@/api'
import { formatDate } from '@/utils'
import { Search } from '@element-plus/icons-vue'

export default {
  name: 'AdminTags',
  setup() {
    const store = useStore()
    const tags = ref([])
    const pendingTags = ref([])
    const loading = ref(true)
    const pendingLoading = ref(true)
    const error = ref(null)
    const pendingError = ref(null)
    const searchKeyword = ref('')
    const dialogVisible = ref(false)
    const submitting = ref(false)
    const activeTab = ref('list')

    const form = reactive({
      name: '',
      description: '',
      color: '#002FA7',
      status: 0  // 创建的标签默认为待审核状态(数值0)
    })

    // 获取标签列表
    const fetchTags = async () => {
      loading.value = true
      error.value = null

      try {
        console.log('开始获取所有标签列表');
        const response = await tagApi.getAllTags()
        if (response.data.success) {
          console.log('获取到所有标签:', JSON.stringify(response.data.data));

          // 所有标签都显示在标签列表中
          tags.value = response.data.data;
          console.log('处理后的标签列表:', tags.value);
        } else {
          error.value = response.data.message || '获取标签列表失败'
        }
      } catch (err) {
        console.error('获取标签列表失败:', err)
        error.value = '获取标签列表失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    // 获取待审核标签列表
    const fetchPendingTags = async () => {
      pendingLoading.value = true
      pendingError.value = null

      try {
        console.log('开始获取待审核标签');

        // 获取所有标签
        const allTagsResponse = await tagApi.getAllTags();
        if (allTagsResponse.data.success) {
          const allTags = allTagsResponse.data.data;
          console.log('获取到所有标签:', allTags.length);
          console.log('标签状态示例:', allTags.slice(0, 3).map(t => ({
            id: t.id,
            name: t.name,
            status: t.status,
            statusType: typeof t.status
          })));

          // 过滤出待审核标签 - 严格检查数值0状态
          pendingTags.value = allTags.filter(tag => {
            // 严格检查数值0或字符串'0'
            const isPending = tag.status === 0 || tag.status === '0';
            if (isPending) {
              console.log(`找到待审核标签: ${tag.name} (ID: ${tag.id}), 状态值: ${tag.status}, 类型: ${typeof tag.status}`);
            }
            return isPending;
          });

          console.log(`过滤出的待审核标签数量: ${pendingTags.value.length}`);

          // 如果找到待审核标签，自动切换到待审核标签标签页
          if (pendingTags.value.length > 0) {
            activeTab.value = 'pending';
          }
        } else {
          pendingError.value = allTagsResponse.data.message || '获取标签列表失败';
          console.error('获取标签失败:', allTagsResponse.data.message);
        }
      } catch (err) {
        console.error('获取待审核标签列表失败:', err);
        pendingError.value = '获取待审核标签列表失败，请稍后重试';
      } finally {
        pendingLoading.value = false;
      }
    }

    const handleSearch = () => {
      // 搜索功能沿用当前列表的客户端筛选。
      console.log('搜索标签:', searchKeyword.value)
    }

    const showCreateDialog = () => {
      form.name = ''
      form.description = ''
      form.color = '#002FA7'
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      if (!form.name) {
        store.dispatch('showNotification', {
          type: 'warning',
          message: '请输入标签名称'
        })
        return
      }

      submitting.value = true

      try {
        // 确保状态是数值类型
        const tagData = {
          name: form.name,
          description: form.description,
          color: form.color,
          status: 0 // 确保是数值0而非字符串
        };

        console.log('创建标签数据:', JSON.stringify(tagData));

        // 创建标签
        const response = await tagApi.createTag(tagData);

        if (response.data.success) {
          dialogVisible.value = false
          store.dispatch('showNotification', {
            type: 'success',
            message: '标签已创建'
          })

          // 刷新标签列表
          fetchTags();
          fetchPendingTags();
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('保存标签失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '操作失败，请稍后重试'
        })
      } finally {
        submitting.value = false
      }
    }

    const handleDelete = async (tag) => {
      try {
        const response = await tagApi.deleteTag(tag.id)

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: '标签已删除'
          })
          // 刷新标签列表
          fetchTags();
          fetchPendingTags();
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '删除失败'
          })
        }
      } catch (err) {
        console.error('删除标签失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '删除标签失败，请稍后重试'
        })
      }
    }

    // 批准标签
    const handleApprove = async (tag) => {
      try {
        const response = await adminApi.approveTag(tag.id)

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: '标签已批准'
          })

          // 刷新标签列表
          fetchTags();
          fetchPendingTags();
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('批准标签失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '批准标签失败，请稍后重试'
        })
      }
    }

    // 拒绝标签（直接删除）
    const handleReject = async (tag) => {
      try {
        // 先标记为拒绝
        const response = await adminApi.rejectTag(tag.id)

        if (response.data.success) {
          // 然后直接删除
          const deleteResponse = await tagApi.deleteTag(tag.id)

          if (deleteResponse.data.success) {
            store.dispatch('showNotification', {
              type: 'success',
              message: '已拒绝并删除该标签'
            })

            // 刷新标签列表
            fetchTags();
            fetchPendingTags();
          } else {
            store.dispatch('showNotification', {
              type: 'warning',
              message: '标签已拒绝但未能自动删除，请手动删除'
            })

            // 刷新待审核列表
            fetchPendingTags()
          }
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('拒绝标签失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '拒绝标签失败，请稍后重试'
        })
      }
    }

    onMounted(() => {
      fetchTags()
      fetchPendingTags()
    })

    return {
      tags,
      pendingTags,
      loading,
      pendingLoading,
      error,
      pendingError,
      searchKeyword,
      dialogVisible,
      form,
      submitting,
      activeTab,
      handleSearch,
      showCreateDialog,
      handleSubmit,
      handleDelete,
      handleApprove,
      handleReject,
      formatDate
    }
  },
  components: {
    Search
  }
}
</script>

<style scoped>
.admin-tags {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
  margin-right: 15px;
  margin-bottom: 15px;
}

.actions {
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

.tag-name {
  display: flex;
  align-items: center;
}

.tag-color {
  width: 15px;
  height: 15px;
  border-radius: 0;
  margin-right: 10px;
}
</style>
