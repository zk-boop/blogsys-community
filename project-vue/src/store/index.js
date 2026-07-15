import { createStore } from 'vuex'
import { authApi, userApi, categoryApi, tagApi } from '@/api'

// 从本地存储中获取用户信息
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

export default createStore({
  state: {
    user: Object.keys(userInfo).length > 0 ? userInfo : null,
    isLoading: false,
    categories: [],
    tags: [],
    notifications: []
  },
  getters: {
    isAuthenticated(state) {
      return !!state.user
    },
    isAdmin(state) {
      return state.user?.role === 'ROLE_ADMIN'
    },
    userId(state) {
      return state.user?.id
    },
    userName(state) {
      return state.user?.username
    },
    userNickname(state) {
      return state.user?.nickname || state.user?.username
    },
    userAvatar(state) {
      if (!state.user?.avatar) {
        return require('@/assets/default-avatar.png')
      }
      // 如果头像已经是完整URL，则直接返回
      if (state.user.avatar.startsWith('http')) {
        return state.user.avatar
      }
      // 否则处理为完整URL
      return state.user.avatar
    }
  },
  mutations: {
    // 身份验证相关
    SET_USER(state, user) {
      state.user = user
      localStorage.setItem('userInfo', JSON.stringify(user))
    },
    LOGOUT(state) {
      state.user = null
      localStorage.removeItem('userInfo')
    },
    // 加载状态
    SET_LOADING(state, isLoading) {
      state.isLoading = isLoading
    },
    // 分类和标签
    SET_CATEGORIES(state, categories) {
      state.categories = categories
    },
    SET_TAGS(state, tags) {
      state.tags = tags
    },
    // 通知
    ADD_NOTIFICATION(state, notification) {
      state.notifications.push({
        ...notification,
        id: Date.now()
      })
    },
    REMOVE_NOTIFICATION(state, id) {
      state.notifications = state.notifications.filter(n => n.id !== id)
    }
  },
  actions: {
    // 用户登录
    async login({ commit }, credentials) {
      try {
        const response = await authApi.login(credentials)
        const { data } = response.data
        commit('SET_USER', {
          id: data.id,
          username: data.username,
          nickname: data.nickname,
          email: data.email,
          role: data.role,
          avatar: data.avatar
        })
        return Promise.resolve(data)
      } catch (error) {
        return Promise.reject(error)
      }
    },
    // 用户注册
    async register({ commit }, userData) {
      try {
        const response = await authApi.register(userData)
        return Promise.resolve(response.data)
      } catch (error) {
        return Promise.reject(error)
      }
    },
    // 退出登录
    async logout({ commit }) {
      try {
        await authApi.logout()
        commit('LOGOUT')
        return Promise.resolve()
      } catch (error) {
        // 即使退出失败也清除本地状态
        commit('LOGOUT')
        return Promise.reject(error)
      }
    },
    // 获取用户信息
    async fetchUserInfo({ commit }) {
      try {
        const response = await authApi.getUserInfo()
        if (response.data.success) {
          commit('SET_USER', response.data.data)
        }
        return Promise.resolve(response.data)
      } catch (error) {
        return Promise.reject(error)
      }
    },
    // 获取所有分类
    async fetchCategories({ commit }) {
      try {
        const response = await categoryApi.getAllCategories()
        if (response.data.success) {
          commit('SET_CATEGORIES', response.data.data)
        }
        return Promise.resolve(response.data.data)
      } catch (error) {
        return Promise.reject(error)
      }
    },
    // 获取所有标签
    async fetchTags({ commit }) {
      try {
        const response = await tagApi.getAllTags()
        if (response.data.success) {
          commit('SET_TAGS', response.data.data)
        }
        return Promise.resolve(response.data.data)
      } catch (error) {
        return Promise.reject(error)
      }
    },
    // 显示通知
    showNotification({ commit }, notification) {
      const id = Date.now()
      commit('ADD_NOTIFICATION', { ...notification, id })
      if (notification.duration !== 0) {
        setTimeout(() => {
          commit('REMOVE_NOTIFICATION', id)
        }, notification.duration || 3000)
      }
      return id
    },
    // 移除通知
    removeNotification({ commit }, id) {
      commit('REMOVE_NOTIFICATION', id)
    }
  },
  modules: {
    // 如果项目变大，可以在这里添加子模块
  }
})
