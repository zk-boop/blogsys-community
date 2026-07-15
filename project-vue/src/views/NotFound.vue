<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被删除</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'NotFound',
  setup() {
    const router = useRouter()

    const goHome = () => {
      router.push('/')
    }

    return {
      goHome
    }
  }
}
</script>

<style scoped>
.not-found-container {
  height: calc(100vh - 200px);
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.not-found-content {
  max-width: 500px;
  padding: 30px;
}

.error-code {
  font-size: 120px;
  margin: 0;
  color: #002FA7;
  font-weight: 700;
  text-shadow: 2px 2px 0 rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 32px;
  margin: 0 0 20px;
}

.error-desc {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}
</style>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 240px);
  height: auto;
  align-items: center;
  justify-content: flex-start;
  border-top: 1px solid var(--ui-line-strong);
  text-align: left;
}
.not-found-content { max-width: 620px; padding: 48px 0; }
.error-code { margin-bottom: 18px; color: var(--ui-text); font-size: clamp(72px, 14vw, 128px); font-weight: 500; line-height: .8; letter-spacing: -.07em; text-shadow: none; }
.error-title { margin-bottom: 12px; font-size: clamp(28px, 4vw, 40px); font-weight: 600; }
.error-desc { color: var(--ui-text-soft); font-size: 16px; line-height: 1.7; }
</style>
