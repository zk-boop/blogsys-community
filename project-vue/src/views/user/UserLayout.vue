<template>
  <main class="user-space">
    <section class="space-hero" aria-label="个人空间信息">
      <div class="hero-grid" aria-hidden="true"><i></i><i></i><i></i></div>
      <div class="identity">
        <el-avatar :src="userAvatar" :size="92" class="space-avatar" />
        <div class="identity-copy">
          <h1>{{ profile.nickname || userNickname }}</h1>
          <p>{{ profile.bio || '还没有填写个人简介' }}</p>
        </div>
      </div>
      <router-link class="edit-profile" to="/user/profile">编辑资料</router-link>
    </section>

    <nav class="space-nav" aria-label="用户中心导航">
      <router-link to="/user/profile"><el-icon><User /></el-icon><span>资料</span></router-link>
      <router-link to="/user/articles"><el-icon><Document /></el-icon><span>投稿</span></router-link>
      <router-link to="/user/favorites"><el-icon><Star /></el-icon><span>收藏</span></router-link>
      <router-link to="/user/comments"><el-icon><ChatLineSquare /></el-icon><span>评论</span></router-link>
      <router-link class="create-entry" to="/user/write"><el-icon><EditPen /></el-icon><span>写文章</span></router-link>
    </nav>

    <section class="space-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in"><component :is="Component" /></transition>
      </router-view>
    </section>
  </main>
</template>

<script>
import { computed, onMounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { ChatLineSquare, Document, EditPen, Star, User } from '@element-plus/icons-vue'
import { userApi } from '@/api'

export default {
  name: 'UserLayout',
  components: { ChatLineSquare, Document, EditPen, Star, User },
  setup() {
    const store = useStore()
    const profile = reactive({ nickname: '', bio: '' })
    const userId = computed(() => store.getters.userId)
    const userNickname = computed(() => store.getters.userNickname)
    const userAvatar = computed(() => store.getters.userAvatar)
    onMounted(async () => {
      try {
        const response = await userApi.getUserById(userId.value)
        if (response.data.success) Object.assign(profile, response.data.data)
      } catch (error) {
        console.warn('个人空间资料读取失败', error)
      }
    })
    return { profile, userNickname, userAvatar }
  }
}
</script>

<style scoped>
.user-space { width: min(1180px, calc(100% - 32px)); margin: 0 auto; }
.space-hero {
  position: relative;
  height: 252px;
  overflow: hidden;
  color: #fff;
  background: #002fa7;
  border: 1px solid #002fa7;
}
.hero-grid { position: absolute; inset: 0; opacity: .22; }
.hero-grid::before, .hero-grid::after, .hero-grid i { content: ''; position: absolute; background: #fff; }
.hero-grid::before { top: 0; bottom: 0; left: 34%; width: 1px; }
.hero-grid::after { top: 0; bottom: 0; left: 72%; width: 1px; }
.hero-grid i:nth-child(1) { top: 38%; right: 0; left: 0; height: 1px; }
.hero-grid i:nth-child(2) { right: 7%; bottom: -58px; width: 220px; height: 220px; border-radius: 50%; background: transparent; border: 36px solid #fff; }
.hero-grid i:nth-child(3) { top: 26px; right: 22%; width: 66px; height: 66px; }
.identity { position: absolute; left: 42px; bottom: 30px; display: flex; align-items: center; gap: 22px; }
.space-avatar { flex-shrink: 0; border: 4px solid #fff; background: #f7f7f8; }
.identity-copy h1 { margin: 0 0 10px; color: #fff; font-size: clamp(28px, 4vw, 44px); line-height: 1; letter-spacing: -.04em; }
.identity-copy p { max-width: 540px; margin: 0; color: rgba(255,255,255,.78); font-size: 14px; }
.edit-profile { position: absolute; right: 28px; bottom: 30px; padding: 9px 15px; color: #fff; border: 1px solid rgba(255,255,255,.7); border-radius: 2px; text-decoration: none; }
.edit-profile:hover { color: #002fa7; background: #fff; }
.space-nav { min-height: 66px; display: flex; align-items: stretch; background: #fff; border: 1px solid #dedede; border-top: 0; }
.space-nav a { position: relative; min-width: 100px; padding: 0 18px; display: flex; align-items: center; justify-content: center; gap: 8px; color: #666; font-size: 14px; font-weight: 550; text-decoration: none; }
.space-nav a::after { content: ''; position: absolute; right: 18px; bottom: -1px; left: 18px; height: 3px; background: transparent; }
.space-nav a:hover, .space-nav a.router-link-active { color: #002fa7; }
.space-nav a.router-link-active::after { background: #002fa7; }
.space-nav .create-entry { margin-left: auto; color: #fff; background: #002fa7; }
.space-nav .create-entry::after { display: none; }
.space-nav .create-entry:hover { color: #fff; background: #001f70; }
.space-content { min-height: 480px; margin-top: 28px; padding: 34px 38px 48px; background: #fff; border: 1px solid #dedede; }
.page-fade-enter-active, .page-fade-leave-active { transition: opacity .16s ease; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; }
@media (max-width: 700px) {
  .user-space { width: calc(100% - 20px); }
  .space-hero { height: 210px; }
  .identity { left: 20px; bottom: 22px; gap: 14px; }
  .space-avatar { width: 70px !important; height: 70px !important; }
  .identity-copy h1 { font-size: 27px; }
  .identity-copy p { max-width: 230px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .edit-profile { top: 16px; right: 16px; bottom: auto; }
  .space-nav { overflow-x: auto; }
  .space-nav a { min-width: 78px; padding: 0 12px; }
  .space-nav .create-entry { margin-left: 0; }
  .space-content { margin-top: 16px; padding: 24px 18px 36px; }
}
</style>
