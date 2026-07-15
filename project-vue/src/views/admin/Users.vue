<template>
  <div class="admin-users">
    <h1>用户管理</h1>

    <div class="filter-section">
      <el-input v-model="searchKeyword" placeholder="搜索用户名/邮箱" class="search-input" clearable
        @keyup.enter="handleSearch">
        <template #suffix>
          <el-icon class="search-icon" @click="handleSearch">
            <Search />
          </el-icon>
        </template>
      </el-input>

      <el-select v-model="statusFilter" placeholder="用户状态" class="status-select" @change="handleFilterChange">
        <el-option label="全部" value="" />
        <el-option label="正常" value="ACTIVE" />
        <el-option label="禁用" value="BANNED" />
        <el-option label="未激活" value="INACTIVE" />
      </el-select>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else>
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="220" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ROLE_ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ROLE_ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="isActiveStatus(row.status) ? 'success' : 'danger'">
              {{ isActiveStatus(row.status) ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isActiveStatus(row.status)" type="danger" size="small" @click="handleBan(row)"
              :disabled="row.role === 'ROLE_ADMIN' || row.id === currentUserId">
              禁用
            </el-button>
            <el-button v-else type="success" size="small" @click="handleActivate(row)">
              激活
            </el-button>
            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" :disabled="row.role === 'ROLE_ADMIN' || row.id === currentUserId">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" class="pagination" layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]" :total="total" :page-size="pageSize" :current-page="currentPage"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { adminApi } from '@/api'
import { formatDate } from '@/utils'

export default {
  name: 'AdminUsers',
  setup() {
    const store = useStore()
    const loading = ref(true)
    const error = ref(null)
    const users = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const searchKeyword = ref('')
    const statusFilter = ref('')

    const currentUserId = computed(() => store.getters.userId)

    // 判断用户状态是否为激活状态的函数
    const isActiveStatus = (status) => {
      // 兼容不同的状态值格式
      if (!status) return false

      // 转换为大写进行比较
      const upperStatus = status.toUpperCase()
      return upperStatus === 'ACTIVE' || upperStatus === 'NORMAL' || upperStatus === '正常'
    }

    const fetchUsers = async () => {
      loading.value = true
      error.value = null

      try {
        let response

        console.log('开始获取用户列表，当前参数：', {
          searchKeyword: searchKeyword.value,
          statusFilter: statusFilter.value,
          page: currentPage.value - 1,
          size: pageSize.value
        })

        if (searchKeyword.value) {
          console.log('执行搜索用户请求')
          response = await adminApi.searchUsers(
            searchKeyword.value,
            currentPage.value - 1,
            pageSize.value
          )
        } else if (statusFilter.value) {
          console.log('执行按状态筛选用户请求')
          response = await adminApi.getUsersByStatus(
            statusFilter.value,
            currentPage.value - 1,
            pageSize.value
          )
        } else {
          console.log('执行获取所有用户请求')
          response = await adminApi.getAllUsers(
            currentPage.value - 1,
            pageSize.value
          )
        }

        console.log('用户列表API响应:', response.data)

        if (response.data.success) {
          // 检查response.data.data是否存在
          if (response.data.data && response.data.data.content) {
            users.value = response.data.data.content
            total.value = response.data.data.totalElements
            console.log('获取到用户数据:', users.value.length, '条记录')

            // 打印每个用户的状态值，用于调试
            users.value.forEach(user => {
              console.log(`用户 ${user.username} 的状态值:`, user.status)
            })
          } else {
            // 如果不是分页格式，可能是直接返回数组
            if (Array.isArray(response.data.data)) {
              users.value = response.data.data
              total.value = response.data.data.length
              console.log('获取到用户数据(数组格式):', users.value.length, '条记录')
            } else {
              // 数据格式不符合预期
              users.value = []
              total.value = 0
              console.warn('API返回数据格式不符合预期:', response.data)
              error.value = '返回数据格式不符合预期'
            }
          }
        } else {
          error.value = response.data.message || '获取用户列表失败'
          console.error('API返回错误:', error.value)
        }
      } catch (err) {
        error.value = '获取用户列表失败，请稍后重试'
        console.error('获取用户列表失败:', err)
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
      fetchUsers()
    }

    const handleFilterChange = () => {
      currentPage.value = 1
      fetchUsers()
    }

    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchUsers()
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchUsers()
    }

    const handleBan = async (user) => {
      try {
        const response = await adminApi.banUser(user.id)

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: `用户 ${user.username} 已被禁用`
          })
          fetchUsers()
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('禁用用户失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '禁用用户失败，请稍后重试'
        })
      }
    }

    const handleActivate = async (user) => {
      try {
        const response = await adminApi.activateUser(user.id)

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: `用户 ${user.username} 已被激活`
          })
          fetchUsers()
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('激活用户失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '激活用户失败，请稍后重试'
        })
      }
    }

    const handleDelete = async (user) => {
      try {
        const response = await adminApi.deleteUser(user.id)

        if (response.data.success) {
          store.dispatch('showNotification', {
            type: 'success',
            message: `用户 ${user.username} 已被删除`
          })
          fetchUsers()
        } else {
          store.dispatch('showNotification', {
            type: 'error',
            message: response.data.message || '操作失败'
          })
        }
      } catch (err) {
        console.error('删除用户失败:', err)
        store.dispatch('showNotification', {
          type: 'error',
          message: '删除用户失败，请稍后重试'
        })
      }
    }

    onMounted(() => {
      fetchUsers()
    })

    return {
      loading,
      error,
      users,
      total,
      currentPage,
      pageSize,
      searchKeyword,
      statusFilter,
      currentUserId,
      formatDate,
      handleSearch,
      handleFilterChange,
      handleSizeChange,
      handleCurrentChange,
      handleBan,
      handleActivate,
      handleDelete,
      isActiveStatus
    }
  }
}
</script>

<style scoped>
.admin-users {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
}

.search-input {
  width: 300px;
}

.status-select {
  width: 150px;
}

.loading,
.error {
  padding: 20px;
  text-align: center;
}

.error {
  color: #f56c6c;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
