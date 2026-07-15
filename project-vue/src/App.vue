<template>
  <div class="app">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <AppHeader />
    <main id="main-content" class="main-container">
      <router-view />
    </main>
    <AppFooter />
  </div>
</template>

<script>
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import AppHeader from './components/layout/AppHeader.vue'
import AppFooter from './components/layout/AppFooter.vue'

export default {
  name: 'App',
  components: {
    AppHeader,
    AppFooter
  },
  setup() {
    const store = useStore()

    onMounted(async () => {
      try {
        await store.dispatch('fetchUserInfo')
      } catch (error) {
        console.log('用户未登录或会话已过期')
      }
    })

    return {}
  }
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html,
body {
  min-height: 100%;
  font-size: 16px;
  color: var(--ui-text);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-container {
  flex: 1;
  width: min(var(--ui-content), calc(100% - 40px));
  margin: 104px auto 0;
  padding-bottom: 80px;
}

.skip-link {
  position: fixed;
  top: 8px;
  left: 50%;
  z-index: 2000;
  padding: 10px 16px;
  color: #FFFFFF;
  background: var(--ui-accent);
  transform: translate(-50%, -150%);
  transition: transform .2s ease;
}

.skip-link:focus {
  transform: translate(-50%, 0);
}

button,
input,
textarea {
  font: inherit;
}

.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.items-center {
  align-items: center;
}

.justify-between {
  justify-content: space-between;
}

.w-full {
  width: 100%;
}

.mt-4 {
  margin-top: 1rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.text-center {
  text-align: center;
}

@media (max-width: 768px) {
  .main-container {
    width: min(100% - 24px, var(--ui-content));
    margin-top: 84px;
    padding-bottom: 36px;
  }
}
</style>
