<template>
  <header class="app-header">
    <div class="header-container">
      <router-link class="brand" to="/" aria-label="BlogSys 首页" @click="closeMobileMenu">
        <span class="brand-name">BlogSys</span>
        <span class="brand-subtitle">多用户博客</span>
      </router-link>

      <form class="search-box" role="search" @submit.prevent="handleSearch">
        <el-input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索文章、作者或关键词"
          aria-label="搜索文章、作者或关键词"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
          <template #append>
            <button class="search-submit" type="submit" aria-label="搜索"><el-icon><Search /></el-icon></button>
          </template>
        </el-input>
      </form>

      <nav class="nav-menu" :class="{ 'mobile-active': mobileMenuActive }" aria-label="主导航">
        <router-link to="/" @click="closeMobileMenu">首页</router-link>
        <router-link to="/about-us" @click="closeMobileMenu">关于</router-link>
      </nav>

      <div class="user-actions">
        <router-link v-if="isAuthenticated" to="/user/write" class="write-link">
          <el-icon><EditPen /></el-icon><span>投稿</span>
        </router-link>

        <template v-if="isAuthenticated">
          <el-dropdown trigger="click">
            <button class="user-trigger" type="button">
              <el-avatar :src="userAvatar" :size="32" :alt="`${userNickname || '用户'}头像`" />
              <span class="username">{{ userNickname }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isAdmin"><router-link to="/admin/dashboard">管理后台</router-link></el-dropdown-item>
                <el-dropdown-item><router-link to="/user/profile">个人资料</router-link></el-dropdown-item>
                <el-dropdown-item><router-link to="/user/articles">我的文章</router-link></el-dropdown-item>
                <el-dropdown-item><router-link to="/user/favorites">我的收藏</router-link></el-dropdown-item>
                <el-dropdown-item><router-link to="/user/comments">我的评论</router-link></el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="text-link">登录</router-link>
          <router-link to="/register" class="register-link">注册</router-link>
        </template>

        <button
          class="mobile-menu-toggle"
          type="button"
          :aria-expanded="mobileMenuActive"
          aria-label="打开导航"
          @click="toggleMobileMenu"
        >
          <span></span><span></span><span></span>
        </button>
      </div>
    </div>
    <div v-if="mobileMenuActive" class="mobile-menu-overlay" @click="closeMobileMenu"></div>
  </header>
</template>

<script>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { EditPen, Search } from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: { EditPen, Search },
  setup() {
    const store = useStore()
    const router = useRouter()
    const searchKeyword = ref('')
    const mobileMenuActive = ref(false)
    const isAuthenticated = computed(() => store.getters.isAuthenticated)
    const isAdmin = computed(() => store.getters.isAdmin)
    const userNickname = computed(() => store.getters.userNickname)
    const userAvatar = computed(() => store.getters.userAvatar)

    const closeMobileMenu = () => {
      mobileMenuActive.value = false
      document.body.style.overflow = ''
    }
    const toggleMobileMenu = () => {
      mobileMenuActive.value = !mobileMenuActive.value
      document.body.style.overflow = mobileMenuActive.value ? 'hidden' : ''
    }
    const handleSearch = () => {
      const keyword = searchKeyword.value.trim()
      if (!keyword) return
      closeMobileMenu()
      router.push(`/search?keyword=${encodeURIComponent(keyword)}`)
    }
    const handleLogout = () => store.dispatch('logout').finally(() => router.push('/'))

    return {
      searchKeyword, mobileMenuActive, isAuthenticated, isAdmin, userNickname, userAvatar,
      closeMobileMenu, toggleMobileMenu, handleSearch, handleLogout
    }
  }
}
</script>

<style scoped>
.app-header {
  position: fixed;
  inset: 0 0 auto;
  z-index: 1000;
  height: 68px;
  background: rgba(255, 255, 255, .97);
  border-top: 3px solid #002fa7;
  border-bottom: 1px solid var(--ui-line, #dedede);
}
.header-container {
  width: min(var(--ui-content, 1280px), calc(100% - 40px));
  height: 65px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 150px minmax(280px, 560px) 1fr auto;
  align-items: center;
  gap: 24px;
}
.brand { display: flex; flex-direction: column; color: #111; text-decoration: none; line-height: 1; }
.brand-name { font-size: 22px; font-weight: 750; letter-spacing: -.045em; }
.brand-subtitle { margin-top: 5px; color: #8a8a8a; font-size: 10px; letter-spacing: .08em; }
.search-box { width: 100%; }
.search-input { width: 100%; }
.search-input :deep(.el-input__wrapper) {
  height: 42px;
  padding-left: 14px;
  background: #f7f7f8;
  border: 1px solid transparent;
  border-radius: 2px 0 0 2px;
  box-shadow: none;
  transition: background-color .18s ease, border-color .18s ease;
}
.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) { background: #fff; border-color: #002fa7; }
.search-input :deep(.el-input-group__append) { padding: 0; border: 0; border-radius: 0 2px 2px 0; box-shadow: none; }
.search-submit {
  width: 52px;
  height: 42px;
  display: grid;
  place-items: center;
  color: #fff;
  background: #002fa7;
  border: 1px solid #002fa7;
  cursor: pointer;
}
.search-submit:hover { background: #001f70; border-color: #001f70; }
.nav-menu { display: flex; justify-content: flex-end; align-items: center; gap: 4px; }
.nav-menu a {
  padding: 10px 12px;
  color: #606060;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  border-bottom: 2px solid transparent;
}
.nav-menu a:hover, .nav-menu a.router-link-exact-active { color: #002fa7; border-bottom-color: #002fa7; }
.user-actions { display: flex; align-items: center; justify-content: flex-end; gap: 10px; }
.write-link {
  min-height: 38px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #fff;
  background: #002fa7;
  border: 1px solid #002fa7;
  border-radius: 2px;
  font-size: 14px;
  font-weight: 650;
  text-decoration: none;
}
.write-link:hover { background: #001f70; }
.user-trigger {
  min-height: 38px;
  padding: 2px 8px 2px 3px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #222;
  background: #fff;
  border: 1px solid var(--ui-line, #dedede);
  border-radius: 2px;
  cursor: pointer;
}
.username { max-width: 96px; overflow: hidden; font-size: 13px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.text-link, .register-link { color: #333; font-size: 14px; text-decoration: none; }
.text-link:hover { color: #002fa7; }
.register-link { padding: 9px 14px; color: #fff; background: #002fa7; border-radius: 2px; }
.mobile-menu-toggle { display: none; width: 40px; height: 40px; padding: 8px; background: #fff; border: 1px solid var(--ui-line, #dedede); }
.mobile-menu-toggle span { display: block; width: 20px; height: 1px; margin: 5px auto; background: #111; }
.mobile-menu-overlay { position: fixed; inset: 68px 0 0; z-index: 998; background: rgba(17, 17, 17, .18); }
:deep(.el-dropdown-menu a) { display: block; width: 100%; color: inherit; text-decoration: none; }
@media (max-width: 980px) {
  .header-container { grid-template-columns: 120px minmax(240px, 1fr) auto; gap: 16px; }
  .nav-menu { display: none; }
}
@media (max-width: 700px) {
  .app-header { height: 62px; }
  .header-container { width: calc(100% - 24px); height: 59px; grid-template-columns: auto 1fr auto; gap: 12px; }
  .brand-subtitle, .username, .write-link span { display: none; }
  .brand-name { font-size: 20px; }
  .search-input :deep(.el-input__wrapper), .search-submit { height: 38px; }
  .search-submit { width: 42px; }
  .write-link { min-width: 38px; min-height: 38px; padding: 0; justify-content: center; }
}
@media (max-width: 520px) {
  .header-container { grid-template-columns: auto 1fr auto; }
  .brand { display: none; }
  .header-container { grid-template-columns: 1fr auto; }
  .user-trigger { display: none; }
}
</style>
