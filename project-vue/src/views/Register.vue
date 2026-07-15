<template>
  <div class="auth-page">
    <section class="auth-panel">
      <div class="auth-form">
        <h1 class="page-title">用户注册</h1>

        <el-form :model="formData" :rules="registerRules" ref="registerForm" label-width="86px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formData.username"></el-input>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email"></el-input>
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="formData.nickname"></el-input>
          </el-form-item>

          <el-form-item label="头像" prop="avatar">
            <div class="avatar-uploader">
              <el-upload
                class="avatar-upload"
                action="/api/files/upload/avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar-preview" alt="头像预览">
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="avatar-help">支持图片格式，大小不超过 2MB</div>
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="formData.password" type="password" show-password></el-input>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="formData.confirmPassword" type="password" show-password></el-input>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button type="primary" :loading="loading" @click="handleRegister">立即注册</el-button>
              <el-button @click="resetForm">重置</el-button>
            </div>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

export default {
  name: 'Register',
  components: {
    Plus
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const registerForm = ref(null)
    const loading = ref(false)
    const avatarUrl = ref('')

    const formData = reactive({
      username: '',
      email: '',
      nickname: '',
      password: '',
      confirmPassword: '',
      avatar: ''
    })

    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (formData.confirmPassword !== '') {
          registerForm.value.validateField('confirmPassword')
        }
        callback()
      }
    }

    const validateConfirmPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== formData.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      nickname: [
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
        { validator: validatePass, trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validateConfirmPass, trigger: 'blur' }
      ]
    }

    const handleAvatarSuccess = (response) => {
      if (response.success) {
        avatarUrl.value = response.data
        formData.avatar = response.data
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error(response.message || '上传失败，请重试')
      }
    }

    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        ElMessage.error('头像必须是图片格式')
        return false
      }

      if (!isLt2M) {
        ElMessage.error('头像大小不能超过 2MB')
        return false
      }

      return true
    }

    const handleRegister = () => {
      registerForm.value.validate(valid => {
        if (!valid) return

        loading.value = true
        const { confirmPassword, ...registerInfo } = formData
        store.dispatch('register', registerInfo)
          .then(() => {
            ElMessage.success('注册成功')
            router.push('/login')
          })
          .catch(error => {
            ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
          })
          .finally(() => {
            loading.value = false
          })
      })
    }

    const resetForm = () => {
      registerForm.value.resetFields()
      avatarUrl.value = ''
    }

    return {
      registerForm,
      registerRules,
      loading,
      formData,
      avatarUrl,
      handleAvatarSuccess,
      beforeAvatarUpload,
      handleRegister,
      resetForm
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
  width: min(860px, 100%);
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

.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
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

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar-upload {
  display: inline-block;
  border: 1px solid #111111;
  cursor: pointer;
  overflow: hidden;
}

.avatar-uploader-icon {
  width: 92px;
  height: 92px;
  display: grid;
  place-items: center;
  color: #111111;
  font-size: 28px;
}

.avatar-preview {
  width: 92px;
  height: 92px;
  display: block;
  object-fit: cover;
}

.avatar-help {
  color: #575757;
  font-size: 13px;
}

@media (max-width: 700px) {
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

@media (max-width: 520px) {
  .auth-form :deep(.el-form-item) {
    display: block;
  }

  .auth-form :deep(.el-form-item__label) {
    justify-content: flex-start;
  }

  .avatar-uploader {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

<style scoped>
.auth-page { min-height: calc(100vh - 220px); padding: 56px 0 80px; place-items: start center; }
.auth-panel {
  width: min(680px, 100%);
  display: block;
  background: transparent;
  border: 0;
  border-top: 1px solid var(--ui-line-strong);
}
.auth-index { display: none; }
.auth-form { padding: 34px 0 0; }
.auth-form .page-title { margin-bottom: 36px !important; font-size: clamp(36px, 7vw, 52px) !important; }
.form-footer { margin-top: 18px; border-top-color: var(--ui-line); }
.form-footer a { color: var(--ui-accent); font-weight: 500; }
.avatar-upload { border-color: var(--ui-line-strong); border-radius: 2px; }
</style>
