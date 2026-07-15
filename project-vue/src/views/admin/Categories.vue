<template>
  <div class="admin-categories">
    <h1>分类管理</h1>

    <el-tabs type="card">
      <el-tab-pane label="分类列表">
        <div class="actions">
          <div class="search-container">
            <el-input v-model="searchKeyword" placeholder="搜索分类名称或描述" class="search-input" clearable
              @keyup.enter="handleSearch" @clear="clearSearch">
              <template #suffix>
                <el-icon class="search-icon" @click="handleSearch">
                  <Search />
                </el-icon>
              </template>
            </el-input>
            <el-button v-if="searchKeyword" type="info" plain size="small" @click="clearSearch">
              清除搜索
            </el-button>
          </div>

          <el-button type="primary" @click="showCreateDialog">创建分类</el-button>
        </div>

        <div v-if="loading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="error" class="error">
          {{ error }}
        </div>
        <div v-else>
          <div v-if="searchKeyword && rootCategories.length === 0" class="no-results">
            没有找到匹配 "{{ searchKeyword }}" 的分类
          </div>
          <div v-else-if="searchKeyword" class="search-results">
            找到 {{ getSearchResultsCount(rootCategories) }} 个匹配 "{{ searchKeyword }}" 的分类
          </div>

          <el-table :data="rootCategories" style="width: 100%" row-key="id" v-loading="loading"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="分类名称" min-width="180">
              <template #default="{ row }">
                <span v-if="searchKeyword" v-html="highlightKeyword(row.name, searchKeyword)"></span>
                <span v-else>{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="父分类" width="180">
              <template #default="{ row }">
                <span v-if="row.parentId">{{ getCategoryPath(row.parentId) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="描述" min-width="220">
              <template #default="{ row }">
                <span v-if="searchKeyword && row.description"
                  v-html="highlightKeyword(row.description, searchKeyword)"></span>
                <span v-else>{{ row.description }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'APPROVED' || row.status === 1 || row.status === '1'"
                  type="success">已批准</el-tag>
                <el-tag v-else-if="row.status === 'PENDING' || row.status === 0 || row.status === '0'"
                  type="warning">待审核</el-tag>
                <el-tag v-else-if="row.status === 'REJECTED' || row.status === 2 || row.status === '2'"
                  type="danger">已拒绝</el-tag>
                <span v-else>{{ row.status }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-popconfirm title="确定删除该分类吗？" @confirm="handleDelete(row)">
                  <template #reference>
                    <el-button size="small" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="待审核分类">
        <div v-if="pendingLoading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="pendingError" class="error">
          {{ pendingError }}
        </div>
        <div v-else>
          <el-empty v-if="pendingCategories.length === 0" description="没有待审核的分类" />
          <el-table v-else :data="pendingCategories" style="width: 100%" v-loading="pendingLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="分类名称" min-width="180" />
            <el-table-column prop="description" label="描述" min-width="220" />
            <el-table-column label="父分类" min-width="180">
              <template #default="{ row }">
                <span v-if="row.parentId">{{ getCategoryPath(row.parentId) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="handleApprove(row)"
                  :loading="row.isApproving">批准</el-button>
                <el-button size="small" type="danger" @click="handleReject(row)"
                  :loading="row.isRejecting">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑分类对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" label-width="80px" :rules="formRules">
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" show-word-limit>
          </el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入分类描述" maxlength="200" show-word-limit
            :autosize="{ minRows: 3, maxRows: 6 }">
          </el-input>
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="form.parentId" placeholder="选择父分类（可选）" clearable filterable :loading="loading">
            <el-option v-for="category in parentOptions" :key="category.id" :label="getCategoryFullPath(category)"
              :value="category.id">
            </el-option>
          </el-select>
          <div class="form-tip" v-if="editingId">
            <i class="el-icon-info"></i>
            编辑分类时，不能选择自己或其子分类作为父分类
          </div>
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
import { categoryApi, adminApi } from '@/api'
import { formatDate } from '@/utils'
import { Search } from '@element-plus/icons-vue'

export default {
  name: 'AdminCategories',
  setup() {
    const store = useStore()
    const categories = ref([])
    const pendingCategories = ref([])
    const loading = ref(true)
    const pendingLoading = ref(true)
    const error = ref(null)
    const pendingError = ref(null)
    const searchKeyword = ref('')
    const dialogVisible = ref(false)
    const submitting = ref(false)
    const editingId = ref(null)
    const allCategories = ref([]) // 存储所有分类，用于搜索

    const form = reactive({
      name: '',
      description: '',
      parentId: null,
    })

    // 表单验证规则
    const formRules = {
      name: [
        { required: true, message: '请输入分类名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      description: [
        { max: 200, message: '描述不能超过 200 个字符', trigger: 'blur' }
      ]
    };

    const dialogTitle = computed(() => {
      return editingId.value ? '编辑分类' : '创建分类'
    })

    const parentOptions = computed(() => {
      if (!categories.value.length) {
        return [];
      }

      // 如果不是编辑状态，返回所有分类
      if (!editingId.value) {
        return categories.value;
      }

      // 获取当前编辑的分类及其所有子分类ID
      const excludeIds = new Set();

      // 添加当前编辑的分类ID
      excludeIds.add(editingId.value);

      // 递归查找所有子分类
      const findChildrenIds = (parentId) => {
        categories.value.forEach(category => {
          if (category.parentId === parentId) {
            excludeIds.add(category.id);
            findChildrenIds(category.id);
          }
        });
      };

      // 查找当前编辑分类的所有子分类
      findChildrenIds(editingId.value);

      // 过滤掉当前编辑的分类及其所有子分类
      return categories.value.filter(category => !excludeIds.has(category.id));
    });

    // 计算根分类和子分类的树形结构
    const rootCategories = computed(() => {
      // 如果没有搜索关键词，直接构建完整的树形结构
      if (!searchKeyword.value.trim()) {
        return buildCategoryTree(categories.value);
      }

      // 有搜索关键词，先找出所有匹配的分类
      const keyword = searchKeyword.value.trim().toLowerCase();
      const matchedCategories = categories.value.filter(category =>
        category.name.toLowerCase().includes(keyword) ||
        (category.description && category.description.toLowerCase().includes(keyword))
      );

      // 如果没有匹配项，返回空数组
      if (matchedCategories.length === 0) {
        return [];
      }

      // 获取所有匹配分类的ID及其所有父分类的ID
      const relevantIds = new Set();

      // 添加所有匹配的分类ID
      matchedCategories.forEach(category => {
        relevantIds.add(category.id);
      });

      // 添加所有匹配分类的父分类ID
      const addParentIds = (categoryId) => {
        const category = categories.value.find(c => c.id === categoryId);
        if (category && category.parentId) {
          relevantIds.add(category.parentId);
          addParentIds(category.parentId); // 递归添加所有祖先分类
        }
      };

      // 为每个匹配的分类添加其所有父分类ID
      matchedCategories.forEach(category => {
        if (category.parentId) {
          addParentIds(category.parentId);
        }
      });

      // 过滤出所有相关的分类（匹配的分类及其父分类）
      const relevantCategories = categories.value.filter(category =>
        relevantIds.has(category.id)
      );

      // 构建过滤后的树形结构
      return buildCategoryTree(relevantCategories);
    });

    // 构建分类树形结构
    const buildCategoryTree = (categoriesList) => {
      // 深拷贝分类列表，避免修改原始数据
      const allCats = JSON.parse(JSON.stringify(categoriesList));

      // 创建ID到分类的映射，方便查找
      const categoryMap = new Map();
      allCats.forEach(category => {
        // 确保每个分类都有children数组
        category.children = [];
        categoryMap.set(category.id, category);
      });

      // 根分类（没有父分类的分类）
      const roots = [];

      // 构建树形结构
      allCats.forEach(category => {
        if (category.parentId) {
          // 如果有父分类，添加到父分类的children数组中
          const parent = categoryMap.get(category.parentId);
          if (parent) {
            parent.children.push(category);
          } else {
            // 如果找不到父分类，作为根分类处理
            roots.push(category);
          }
        } else {
          // 没有父分类，作为根分类
          roots.push(category);
        }
      });

      return roots;
    };

    // 获取分类列表
    const fetchCategories = async () => {
      loading.value = true
      error.value = null

      try {
        const response = await categoryApi.getAllCategories()
        if (response.data.success) {
          // 使用Map进行去重，确保每个ID只出现一次
          const uniqueCategories = new Map();
          response.data.data.forEach(category => {
            // 只保留每个ID的最新记录
            uniqueCategories.set(category.id, category);
          });
          allCategories.value = Array.from(uniqueCategories.values());

          // 处理父分类名称和状态格式化
          categories.value = allCategories.value.map(category => {
            // 确保状态值格式正确
            if (typeof category.status === 'string') {
              if (category.status === '1') category.status = 1;
              else if (category.status === '0') category.status = 0;
              else if (category.status === '2') category.status = 2;
            }
            return category;
          });
        } else {
          error.value = response.data.message || '获取分类列表失败'
        }
      } catch (err) {
        console.error('获取分类列表失败:', err)
        error.value = '获取分类列表失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    // 获取待审核分类列表
    const fetchPendingCategories = async () => {
      pendingLoading.value = true
      pendingError.value = null

      try {
        const response = await adminApi.getPendingCategories()
        if (response.data.success) {
          // 使用Map进行去重，确保每个ID只出现一次
          const uniquePendingCategories = new Map();
          response.data.data.forEach(category => {
            // 只保留每个ID的最新记录
            uniquePendingCategories.set(category.id, category);
          });

          pendingCategories.value = Array.from(uniquePendingCategories.values()).map(category => {
            // 确保状态值格式正确
            if (typeof category.status === 'string') {
              if (category.status === '0') category.status = 0;
              else if (category.status === '1') category.status = 1;
              else if (category.status === '2') category.status = 2;
            }

            // 添加操作状态属性
            category.isApproving = false;
            category.isRejecting = false;
            return category
          })
        } else {
          pendingError.value = response.data.message || '获取待审核分类列表失败'
        }
      } catch (err) {
        console.error('获取待审核分类列表失败:', err)
        pendingError.value = '获取待审核分类列表失败，请稍后重试'
      } finally {
        pendingLoading.value = false
      }
    }

    // 获取分类的完整路径（格式：父类-子类-...）
    const getCategoryPath = (categoryId) => {
      const path = [];
      let currentId = categoryId;

      // 防止循环依赖导致的无限循环
      const visitedIds = new Set();

      while (currentId && !visitedIds.has(currentId)) {
        visitedIds.add(currentId);
        const category = categories.value.find(c => c.id === currentId);

        if (category) {
          path.unshift(category.name); // 添加到路径的开头
          currentId = category.parentId;
        } else {
          break;
        }
      }

      return path.length > 0 ? path.join('-') : '-';
    };

    // 获取分类的完整路径（包括父分类）
    const getCategoryFullPath = (category) => {
      if (!category.parentId) {
        return category.name;
      }
      const parentPath = getCategoryPath(category.parentId);
      return `${parentPath}-${category.name}`;
    };

    // 批准分类
    const handleApprove = async (category) => {
      try {
        // 设置此分类的批准加载状态
        const categoryIndex = pendingCategories.value.findIndex(c => c.id === category.id);
        if (categoryIndex !== -1) {
          pendingCategories.value[categoryIndex].isApproving = true;
        }

        const response = await adminApi.approveCategory(category.id)

        if (response.data.success) {
          // 操作成功，立即从待审核列表中移除
          pendingCategories.value = pendingCategories.value.filter(c => c.id !== category.id);

          store.dispatch('showNotification', {
            type: 'success',
            message: '分类已批准'
          })

          // 局部更新分类列表
          if (response.data.data) {
            updateCategory(response.data.data);
          } else {
            // 如果响应中没有返回更新后的分类数据，则获取该分类
            const categoryResponse = await categoryApi.getCategoryById(category.id);
            if (categoryResponse.data.success) {
              updateCategory(categoryResponse.data.data);
            }
          }
        } else {
          // 重置加载状态
          if (categoryIndex !== -1) {
            pendingCategories.value[categoryIndex].isApproving = false;
          }

          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('批准分类失败:', err)

        // 重置所有分类的加载状态
        pendingCategories.value.forEach(c => {
          if (c.id === category.id) {
            c.isApproving = false;
          }
        });

        store.dispatch('showNotification', {
          type: 'error',
          message: '批准分类失败，请稍后重试'
        })
      }
    }

    // 拒绝分类
    const handleReject = async (category) => {
      try {
        // 设置此分类的拒绝加载状态
        const categoryIndex = pendingCategories.value.findIndex(c => c.id === category.id);
        if (categoryIndex !== -1) {
          pendingCategories.value[categoryIndex].isRejecting = true;
        }

        const response = await adminApi.rejectCategory(category.id)

        if (response.data.success) {
          // 操作成功，立即从待审核列表中移除
          pendingCategories.value = pendingCategories.value.filter(c => c.id !== category.id);

          store.dispatch('showNotification', {
            type: 'success',
            message: '已拒绝该分类'
          })

          // 局部更新分类列表
          if (response.data.data) {
            updateCategory(response.data.data);
          } else {
            removeCategory(category.id);
          }
        } else {
          // 重置加载状态
          if (categoryIndex !== -1) {
            pendingCategories.value[categoryIndex].isRejecting = false;
          }

          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('拒绝分类失败:', err)

        // 重置所有分类的加载状态
        pendingCategories.value.forEach(c => {
          if (c.id === category.id) {
            c.isRejecting = false;
          }
        });

        store.dispatch('showNotification', {
          type: 'error',
          message: '拒绝分类失败，请稍后重试'
        })
      }
    }

    const handleSearch = () => {
      // 搜索功能已通过rootCategories计算属性实现
      // 当searchKeyword变化时，rootCategories会自动重新计算并过滤分类
    }

    // 清除搜索
    const clearSearch = () => {
      searchKeyword.value = '';
    }

    const showCreateDialog = () => {
      // 重置表单
      resetForm();
      // 打开对话框
      dialogVisible.value = true;
    }

    const handleEdit = (category) => {
      console.log('开始编辑分类:', category);

      // 确保category对象存在且包含必要数据
      if (!category || !category.id) {
        store.dispatch('showNotification', {
          type: 'error',
          message: '无法编辑此分类，数据不完整'
        });
        return;
      }

      // 设置编辑ID
      editingId.value = category.id;

      // 预填表单数据
      form.name = category.name || '';
      form.description = category.description || '';
      form.parentId = category.parentId || null;

      console.log('表单已预填充:', {
        id: editingId.value,
        name: form.name,
        description: form.description,
        parentId: form.parentId
      });

      // 打开对话框
      dialogVisible.value = true;
    }

    const formRef = ref(null);

    const handleSubmit = async () => {
      // 表单验证
      if (!formRef.value) {
        console.error('表单元素未找到');
        return;
      }

      try {
        // 表单验证
        await formRef.value.validate();

        // 检查是否选择了自己作为父分类
        if (editingId.value && form.parentId === editingId.value) {
          store.dispatch('showNotification', {
            type: 'warning',
            message: '分类不能选择自己作为父分类'
          });
          return;
        }

        submitting.value = true;

        let response;

        if (editingId.value) {
          // 编辑分类
          response = await categoryApi.updateCategory(editingId.value, {
            name: form.name,
            description: form.description,
            parentId: form.parentId
          });
        } else {
          // 创建分类
          response = await categoryApi.createCategory({
            name: form.name,
            description: form.description,
            parentId: form.parentId
          });
        }

        if (response.data.success) {
          dialogVisible.value = false;
          store.dispatch('showNotification', {
            type: 'success',
            message: editingId.value ? '分类已更新' : '分类已创建'
          });

          // 局部更新分类列表
          if (response.data.data) {
            updateCategory(response.data.data);
          }
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          });
        }
      } catch (err) {
        console.error('保存分类失败:', err);
        store.dispatch('showNotification', {
          type: 'error',
          message: err.message || '操作失败，请稍后重试'
        });
      } finally {
        submitting.value = false;
      }
    }

    const handleDelete = async (category) => {
      try {
        const response = await categoryApi.deleteCategory(category.id)

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: '分类已删除'
          })

          // 从分类列表中移除该分类
          removeCategory(category.id);
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '删除失败'
          })
        }
      } catch (err) {
        console.error('删除分类失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '删除分类失败，请稍后重试'
        })
      }
    }

    // 计算搜索结果总数（包括子分类）
    const getSearchResultsCount = (categories) => {
      let count = 0;

      const countCategories = (cats) => {
        if (!cats || !cats.length) return;

        count += cats.length;

        cats.forEach(cat => {
          if (cat.children && cat.children.length > 0) {
            countCategories(cat.children);
          }
        });
      };

      countCategories(categories);
      return count;
    };

    // 监听对话框关闭事件，重置表单
    const resetForm = () => {
      form.name = '';
      form.description = '';
      form.parentId = null;
      editingId.value = null;
    };

    // 对话框关闭时重置表单
    const handleDialogClose = () => {
      resetForm();
    };

    // 更新或添加单个分类
    const updateCategory = (category) => {
      if (!category) return;

      // 检查分类是否已存在
      const index = categories.value.findIndex(c => c.id === category.id);

      if (index !== -1) {
        // 更新已有分类
        categories.value[index] = { ...categories.value[index], ...category };
      } else {
        // 添加新分类
        categories.value.push(category);
      }

      // 重新排序分类，确保视图更新
      categories.value = [...categories.value];
    };

    // 从分类列表中移除分类
    const removeCategory = (categoryId) => {
      if (!categoryId) return;

      // 移除分类
      categories.value = categories.value.filter(c => c.id !== categoryId);

      // 查找并处理该分类的子分类
      const childCategories = categories.value.filter(c => c.parentId === categoryId);

      // 将子分类的parentId设置为null
      childCategories.forEach(child => {
        const index = categories.value.findIndex(c => c.id === child.id);
        if (index !== -1) {
          categories.value[index] = { ...categories.value[index], parentId: null };
        }
      });
    };

    onMounted(() => {
      fetchCategories()
      fetchPendingCategories()
    })

    return {
      categories,
      pendingCategories,
      loading,
      pendingLoading,
      error,
      pendingError,
      searchKeyword,
      dialogVisible,
      dialogTitle,
      form,
      submitting,
      parentOptions,
      rootCategories,
      handleSearch,
      clearSearch,
      showCreateDialog,
      handleEdit,
      handleSubmit,
      handleDelete,
      handleApprove,
      handleReject,
      formatDate,
      getSearchResultsCount,
      getCategoryPath,
      getCategoryFullPath,
      highlightKeyword: (text, keyword) => {
        if (!keyword || !text) return text;
        const escapeRegExp = (string) => {
          return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        };
        const regex = new RegExp(`(${escapeRegExp(keyword)})`, 'gi');
        return text.replace(regex, '<span class="highlight">$1</span>');
      },
      handleDialogClose,
      formRef
    }
  },
  components: {
    Search
  }
}
</script>

<style scoped>
.admin-categories {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.actions {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-grow: 1;
  max-width: 500px;
}

.search-input {
  width: 100%;
}

.search-icon {
  cursor: pointer;
  color: #909399;
}

.search-icon:hover {
  color: #002FA7;
}

.el-table .cell {
  word-break: break-word;
}

.loading,
.error {
  padding: 20px;
  text-align: center;
}

.error {
  color: #f56c6c;
}

.no-results,
.search-results {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 0;
  background-color: #f0f9eb;
  color: #67c23a;
  font-size: 14px;
}

.no-results {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 解决v-html中样式不生效的问题 */
:deep(.highlight) {
  color: #002FA7;
  font-weight: bold;
}

/* 表单提示样式 */
.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.form-tip i {
  margin-right: 4px;
  color: #e6a23c;
}
</style>
