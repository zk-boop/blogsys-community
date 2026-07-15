<template>
  <div class="auth-page">
    <section class="auth-panel">
      <div class="auth-form">
        <h1 class="page-title">用户登录</h1>

        <el-form :model="formData" :rules="loginRules" ref="loginForm" label-width="0" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              aria-label="用户名"
              autocomplete="username"
              placeholder="用户名"
              prefix-icon="User"
            ></el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              aria-label="密码"
              autocomplete="current-password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            ></el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="auth-button" :loading="loading" @click="handleLogin">登录</el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    const loginForm = ref(null)
    const loading = ref(false)

    const formData = reactive({
      username: '',
      password: ''
    })

    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    }

    const handleLogin = () => {
      if (!loginForm.value) return

      loginForm.value.validate(valid => {
        if (!valid) return

        loading.value = true
        store.dispatch('login', formData)
          .then(() => {
            ElMessage.success('登录成功')
            return store.dispatch('fetchUserInfo')
          })
          .then(() => {
            const redirectPath = route.query.redirect || '/'
            router.push(redirectPath)
          })
          .catch(error => {
            console.error('登录失败:', error)
            ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
          })
          .finally(() => {
            loading.value = false
          })
      })
    }

    return {
      loginForm,
      loginRules,
      loading,
      formData,
      handleLogin
    }
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 260px);
  display: grid;
  place-items: center;
  padding: 48px 0;
}

.auth-panel {
  width: min(760px, 100%);
  display: grid;
  grid-template-columns: 180px 1fr;
  background: #FFFFFF;
  border: 1px solid #111111;
}

.auth-index {
  padding: 24px;
  color: #FFFFFF;
  background: #111111;
  font-size: clamp(42px, 7vw, 84px);
  font-weight: 700;
  line-height: .9;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.auth-form {
  padding: 40px;
}

.auth-button {
  width: 100%;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-top: 18px;
  border-top: 1px solid #D8D8DC;
  color: #575757;
  font-size: 14px;
}

.form-footer a {
  color: #002FA7;
  font-weight: 700;
}

@media (max-width: 640px) {
  .auth-panel {
    grid-template-columns: 1fr;
  }

  .auth-index {
    writing-mode: horizontal-tb;
    font-size: 48px;
  }

  .auth-form {
    padding: 24px;
  }
}
</style>

<style scoped>
.auth-page { min-height: calc(100vh - 260px); padding: 64px 0 80px; place-items: start center; }
.auth-panel {
  width: min(520px, 100%);
  display: block;
  background: transparent;
  border: 0;
  border-top: 1px solid var(--ui-line-strong);
}
.auth-index { display: none; }
.auth-form { padding: 34px 0 0; }
.auth-form .page-title { margin-bottom: 36px !important; font-size: clamp(36px, 7vw, 52px) !important; }
.auth-button { min-height: 44px; }
.form-footer { margin-top: 16px; padding-top: 20px; border-top-color: var(--ui-line); }
.form-footer a { color: var(--ui-accent); font-weight: 500; }
</style>
