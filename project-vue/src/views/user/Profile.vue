<template>
  <div class="profile-page">
    <header class="section-header">
      <div><h2>个人资料</h2><p>这些信息会展示在你的个人空间和文章作者信息中。</p></div>
      <span class="account-id">@{{ userForm.username }}</span>
    </header>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-alert v-else-if="error" :title="error" type="error" :closable="false" />
    <div v-else class="settings-grid">
      <section class="avatar-panel">
        <el-avatar :src="userForm.avatar || defaultAvatar" :size="112" />
        <div><strong>头像</strong><p>建议使用清晰的正方形图片。</p></div>
        <el-upload action="#" :http-request="uploadAvatar" :show-file-list="false" accept="image/*">
          <el-button>更换头像</el-button>
        </el-upload>
      </section>

      <el-form class="profile-form" label-position="top">
        <el-form-item label="昵称"><el-input v-model="userForm.nickname" maxlength="20" show-word-limit /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="userForm.email" type="email" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="userForm.bio" type="textarea" :rows="4" maxlength="200" show-word-limit /></el-form-item>
        <div class="form-actions"><el-button type="primary" :loading="saving" @click="handleSubmit">保存资料</el-button></div>
      </el-form>

      <section class="security-panel">
        <header><div><h3>账户安全</h3><p>修改密码后，请使用新密码完成下次登录。</p></div></header>
        <el-form class="password-form" label-position="top">
          <el-form-item label="当前密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认新密码"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
          <div class="form-actions"><el-button :loading="changingPassword" @click="handlePasswordChange">修改密码</el-button></div>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { fileApi, userApi } from '@/api'

export default {
  name: 'UserProfile',
  setup() {
    const store = useStore()
    const loading = ref(true)
    const saving = ref(false)
    const changingPassword = ref(false)
    const error = ref(null)
    const defaultAvatar = require('../../assets/default-avatar.png')
    const userForm = reactive({ username: '', nickname: '', email: '', bio: '', avatar: '' })
    const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
    const userId = computed(() => store.getters.userId)
    const notify = (type, message) => store.dispatch('showNotification', { type, message })

    const fetchUserProfile = async () => {
      try {
        const response = await userApi.getUserById(userId.value)
        if (!response.data.success) throw new Error(response.data.message || '获取用户资料失败')
        Object.assign(userForm, response.data.data)
      } catch (err) { error.value = err.message || '获取用户资料失败，请稍后重试' }
      finally { loading.value = false }
    }
    const handleSubmit = async () => {
      saving.value = true
      try {
        const response = await userApi.updateUser(userId.value, {
          nickname: userForm.nickname, email: userForm.email, bio: userForm.bio, avatar: userForm.avatar
        })
        if (!response.data.success) throw new Error(response.data.message || '保存失败')
        await store.dispatch('fetchUserInfo')
        notify('success', '个人资料已更新')
      } catch (err) { notify('error', err.message || '个人资料更新失败，请稍后重试') }
      finally { saving.value = false }
    }
    const handlePasswordChange = async () => {
      if (!passwordForm.oldPassword || !passwordForm.newPassword) return notify('error', '请完整填写密码信息')
      if (passwordForm.newPassword !== passwordForm.confirmPassword) return notify('error', '两次输入的新密码不一致')
      changingPassword.value = true
      try {
        const response = await userApi.updatePassword(userId.value, passwordForm.oldPassword, passwordForm.newPassword)
        if (!response.data.success) throw new Error(response.data.message || '修改失败')
        Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
        notify('success', '密码已修改')
      } catch (err) { notify('error', err.message || '密码修改失败，请稍后重试') }
      finally { changingPassword.value = false }
    }
    const uploadAvatar = async ({ file }) => {
      try {
        const response = await fileApi.uploadAvatar(file)
        if (!response.data.success) throw new Error(response.data.message || '上传失败')
        userForm.avatar = response.data.data
        notify('success', '头像已上传，保存资料后生效')
      } catch (err) { notify('error', err.message || '头像上传失败，请稍后重试') }
    }
    onMounted(fetchUserProfile)
    return { loading, saving, changingPassword, error, defaultAvatar, userForm, passwordForm, handleSubmit, handlePasswordChange, uploadAvatar }
  }
}
</script>

<style scoped>
.section-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; padding-bottom: 24px; border-bottom: 1px solid #dedede; }
.section-header h2 { margin: 0; font-size: 32px; letter-spacing: -.035em; }
.section-header p, .avatar-panel p, .security-panel p { margin: 7px 0 0; color: #858585; font-size: 13px; }
.account-id { color: #777; font-size: 13px; }
.settings-grid { display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 34px 46px; padding-top: 30px; }
.avatar-panel { display: flex; flex-direction: column; align-items: flex-start; gap: 16px; padding-right: 30px; border-right: 1px solid #dedede; }
.avatar-panel strong { font-size: 16px; }
.profile-form { max-width: 600px; }
.form-actions { display: flex; justify-content: flex-end; padding-top: 4px; }
.security-panel { grid-column: 1 / -1; display: grid; grid-template-columns: 220px minmax(0, 600px); gap: 46px; padding-top: 30px; border-top: 1px solid #dedede; }
.security-panel h3 { margin: 0; font-size: 20px; }
.password-form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.password-form .form-actions { align-items: flex-end; padding-bottom: 18px; }
@media (max-width: 720px) {
  .settings-grid, .security-panel { grid-template-columns: 1fr; gap: 26px; }
  .avatar-panel { padding: 0 0 26px; border-right: 0; border-bottom: 1px solid #dedede; }
  .password-form { grid-template-columns: 1fr; }
  .section-header { align-items: flex-start; flex-direction: column; }
}
</style>
