<template>
  <div class="admin-layout">
    <div class="admin-layout-header">
      <h1 class="page-title">管理后台</h1>
    </div>

    <div class="admin-layout-container">
      <div class="admin-sidebar">
        <el-menu
          router
          :default-active="activeMenu"
          class="admin-menu"
        >
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/articles">
            <el-icon><Document /></el-icon>
            <span>文章管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/comments">
            <el-icon><ChatLineSquare /></el-icon>
            <span>评论管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/categories">
            <el-icon><Menu /></el-icon>
            <span>分类管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/tags">
            <el-icon><Collection /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'

export default {
  name: 'AdminLayout',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    // 确保用户是管理员
    if (!store.getters.isAdmin) {
      router.push('/')
    }

    const activeMenu = computed(() => {
      return route.path
    })

    return {
      activeMenu
    }
  }
}
</script>

<style scoped>
.admin-layout {
  width: 100%;
}

.admin-layout-header {
  display: grid;
  grid-template-columns: 112px 1fr;
  margin-bottom: 28px;
  background: #FFFFFF;
  border: 1px solid #111111;
}

.layout-index {
  padding: 20px;
  color: #002FA7;
  border-right: 1px solid #111111;
  font-size: 46px;
  font-weight: 700;
  line-height: 1;
}

.admin-layout-header .page-title {
  margin: 0 !important;
  padding: 20px 24px;
  font-size: clamp(38px, 5vw, 68px) !important;
}

.admin-layout-header .page-title::after {
  display: none;
}

.admin-layout-container {
  display: grid;
  grid-template-columns: 230px minmax(0, 1fr);
  gap: 24px;
}

.admin-sidebar {
  min-width: 0;
}

.admin-menu {
  counter-reset: admin-nav;
  background: #FFFFFF;
}

.admin-menu :deep(.el-menu-item) { counter-increment: admin-nav; }
.admin-menu :deep(.el-menu-item)::before {
  content: counter(admin-nav, decimal-leading-zero);
  width: 28px;
  margin-right: 10px;
  color: #575757;
  font-size: 11px;
}
.admin-menu :deep(.el-menu-item.is-active)::before,
.admin-menu :deep(.el-menu-item:hover)::before { color: #FFFFFF; }

.admin-content {
  min-width: 0;
  padding: clamp(20px, 3vw, 36px);
  background: #FFFFFF;
  border: 1px solid #111111;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .admin-layout-container {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    width: 100%;
  }

  .admin-menu {
    display: flex;
    overflow-x: auto;
    padding: 0;
    border: 1px solid #111111;
  }

  .el-menu {
    border-right: none !important;
  }

  .el-menu--horizontal>.el-menu-item {
    border-bottom: none !important;
  }

  .admin-layout-header { grid-template-columns: 72px 1fr; }
  .layout-index { padding: 16px 12px; font-size: 30px; }
}
</style>

<style scoped>
.admin-layout-header {
  display: block;
  margin-bottom: 44px;
  background: transparent;
  border: 0;
  border-bottom: 1px solid var(--ui-line-strong);
}
.layout-index { display: none; }
.admin-layout-header .page-title { padding: 0 0 24px; font-size: clamp(34px, 5vw, 52px) !important; }
.admin-layout-container { grid-template-columns: 190px minmax(0, 1fr); gap: 48px; }
.admin-menu { counter-reset: none; }
.admin-menu :deep(.el-menu-item)::before { display: none; }
.admin-content { padding: 0; background: transparent; border: 0; }

@media (max-width: 768px) {
  .admin-layout-container { grid-template-columns: 1fr; gap: 28px; }
  .admin-menu { display: grid; grid-auto-flow: column; grid-auto-columns: max-content; gap: 4px; overflow-x: auto; }
  .admin-menu :deep(.el-menu-item) { margin: 0; padding-inline: 14px; }
}
</style>
