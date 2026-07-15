import axios from 'axios'
import config from './config'

// 创建一个新的axios实例
const api = axios.create({
  baseURL: config.baseURL,
  timeout: 10000, // 请求超时时间
  withCredentials: true, // 允许携带跨域Cookie
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器
api.interceptors.response.use(
  response => response,
  error => {
    // 对响应错误做点什么
    if (error.response) {
      console.error('响应错误状态:', error.response.status)
      console.error('响应错误数据:', error.response.data)

      if (error.response.status === 401) {
        // 401 未授权，跳转到登录页面
        localStorage.removeItem('userInfo')

        // 判断当前路径是否为文章详情页
        const isArticleDetailPage = window.location.pathname.includes('/articles/') &&
                                   !isNaN(window.location.pathname.split('/').pop());

        // 只有非文章详情页才跳转到登录页面
        if (!isArticleDetailPage && window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
    } else {
      console.error('网络错误:', error.message)
    }
    return Promise.reject(error)
  }
)

// 身份验证相关API
export const authApi = {
  login(credentials) {
    return api.post('/auth/login', credentials).then(async response => {
      await api.get('/auth/csrf')
      return response
    })
  },
  register(userData) {
    return api.post('/auth/register', userData)
  },
  getUserInfo() {
    return api.get('/auth/user-info')
  },
  getCsrfToken() {
    return api.get('/auth/csrf')
  },
  logout() {
    return api.post('/auth/logout')
  }
}

// 文章相关API
export const articleApi = {
  getArticles(page = 0, size = 10) {
    console.log(`请求文章列表，页码: ${page}, 每页大小: ${size}`)
    return api.get(`/articles?page=${page}&size=${size}`)
  },
  getArticleById(id) {
    return api.get(`/articles/${id}`)
  },
  getArticleBySlug(slug) {
    return api.get(`/articles/slug/${slug}`)
  },
  searchArticles(keyword, page = 0, size = 10) {
    return api.get(`/articles/search?keyword=${keyword}&page=${page}&size=${size}`)
  },
  getArticlesByCategory(categoryId, page = 0, size = 10) {
    return api.get(`/articles/category/${categoryId}?page=${page}&size=${size}`)
  },
  getArticlesByTag(tagId, page = 0, size = 10) {
    return api.get(`/articles/tag/${tagId}?page=${page}&size=${size}`)
  },
  getArticlesByAuthor(authorId, page = 0, size = 10) {
    return api.get(`/articles/author/${authorId}?page=${page}&size=${size}`)
  },
  getTopArticles() {
    return api.get('/articles/top')
  },
  getFeaturedArticles() {
    return api.get('/articles/featured')
  },
  createArticle(article, authorId, categoryId) {
    // authorId 参数仅保留调用兼容，身份由后端 Session 决定。
    const validCategoryId = parseInt(categoryId, 10);

    if (!validCategoryId) {
      return Promise.reject(new Error('分类ID必须为有效整数'));
    }

    // 构建请求参数
    const params = {
      categoryId: validCategoryId
    };

    // 如果有标签ID，将其作为请求参数传递
    // 注意：我们不使用params.tagIds = article.tagIds，因为axios会将数组转换为tagIds[]=1&tagIds[]=2的形式
    // 而是将标签ID作为文章对象的一部分传递，让后端从请求体中提取

    // 创建文章对象的副本
    const articleData = { ...article };

    return api.post('/articles', articleData, { params });
  },
  updateArticle(id, article, categoryId) {
    // 确保ID和categoryId是整数
    const validId = parseInt(id, 10);
    const validCategoryId = parseInt(categoryId, 10) || null;

    // 构建查询参数
    const params = {};

    if (validCategoryId !== null) {
      params.categoryId = validCategoryId;
    }

    // 注意：我们不使用params.tagIds = article.tagIds，因为axios会将数组转换为tagIds[]=1&tagIds[]=2的形式
    // 而是将标签ID作为文章对象的一部分传递，让后端从请求体中提取

    // 创建文章对象的副本
    const articleData = { ...article };

    return api.put(`/articles/${validId}`, articleData, { params });
  },
  publishArticle(id) {
    return api.patch(`/articles/${id}/publish`)
  },
  submitArticle(id) {
    return api.patch(`/articles/${id}/submit`)
  },
  deleteArticle(id) {
    return api.delete(`/articles/${id}`)
  },
  checkIfFavorited(articleId, userId) {
    return api.get(`/favorites/check?articleId=${articleId}`)
  },
  favoriteArticle(articleId) {
    return api.post(`/favorites?articleId=${articleId}`)
  },
  unfavoriteArticle(articleId) {
    return api.delete(`/favorites?articleId=${articleId}`)
  },
  getUserFavorites(userId, page = 0, size = 10) {
    return api.get(`/favorites/user/${userId}?page=${page}&size=${size}`)
  },
  // 文章点赞相关API
  checkIfLiked(articleId) {
    return api.get(`/likes/check?articleId=${articleId}`)
  },
  likeArticle(articleId) {
    return api.post(`/likes?articleId=${articleId}`)
  },
  unlikeArticle(articleId) {
    return api.delete(`/likes?articleId=${articleId}`)
  }
}

// 用户相关API
export const userApi = {
  getUserById(id) {
    return api.get(`/users/${id}`)
  },
  getAllUsers(page = 0, size = 10) {
    return api.get(`/users?page=${page}&size=${size}`)
  },
  searchUsers(keyword, page = 0, size = 10) {
    return api.get(`/users/search?keyword=${keyword}&page=${page}&size=${size}`)
  },
  updateUser(id, userData) {
    return api.put(`/users/${id}`, userData)
  },
  updatePassword(id, oldPassword, newPassword) {
    return api.patch(`/users/${id}/password`, { oldPassword, newPassword })
  }
}

// 评论相关API
export const commentApi = {
  getCommentById(id) {
    return api.get(`/comments/${id}`)
  },
  getCommentsByArticleId(articleId, page = 0, size = 10) {
    return api.get(`/comments/article/${articleId}?page=${page}&size=${size}`)
  },
  getRepliesByCommentId(commentId) {
    return api.get(`/comments/replies/${commentId}`)
  },
  getCommentsByUserId(userId, page = 0, size = 10) {
    return api.get(`/comments/user/${userId}?page=${page}&size=${size}`)
  },
  createComment(comment, articleId, userId, parentId = null, replyToUserId = null) {
    // userId 参数仅保留调用兼容，身份由后端 Session 决定。
    let url = `/comments?articleId=${articleId}`
    if (parentId) url += `&parentId=${parentId}`
    if (replyToUserId) url += `&replyToUserId=${replyToUserId}`
    return api.post(url, comment)
  },
  likeComment(id) {
    return api.patch(`/comments/${id}/like`)
  },
  unlikeComment(id) {
    return api.patch(`/comments/${id}/unlike`)
  },
  approveComment(id) {
    return api.patch(`/comments/${id}/approve`)
  },
  rejectComment(id) {
    return api.patch(`/comments/${id}/reject`)
  },
  deleteComment(id) {
    return api.delete(`/comments/${id}`)
  }
}

// 分类相关API
export const categoryApi = {
  getCategoryById(id) {
    return api.get(`/categories/${id}`)
  },
  getAllCategories() {
    return api.get('/categories')
  },
  getRootCategories() {
    return api.get('/categories/root')
  },
  getSubcategories(parentId) {
    return api.get(`/categories/subcategories/${parentId}`)
  },
  getHotCategories(limit = 5) {
    return api.get(`/categories/hot?limit=${limit}`)
  },
  createCategory(category) {
    return api.post('/categories', category)
  },
  updateCategory(id, category) {
    return api.put(`/categories/${id}`, category)
  },
  deleteCategory(id) {
    return api.delete(`/categories/${id}`)
  }
}

// 标签相关API
export const tagApi = {
  getTagById(id) {
    return api.get(`/tags/${id}`)
  },
  getAllTags() {
    return api.get('/tags')
  },
  searchTags(keyword, page = 0, size = 10) {
    return api.get(`/tags/search?keyword=${keyword}&page=${page}&size=${size}`)
  },
  getHotTags(limit = 10) {
    return api.get(`/tags/hot?limit=${limit}`)
  },
  getTagsByArticleId(articleId) {
    return api.get(`/tags/article/${articleId}`)
  },
  createTag(tag) {
    return api.post('/tags', tag)
  },
  updateTag(id, tag) {
    return api.put(`/tags/${id}`, tag)
  },
  deleteTag(id) {
    return api.delete(`/tags/${id}`)
  }
}

// 管理员相关API
export const adminApi = {
  // 用户管理
  getAllUsers(page = 0, size = 10) {
    return api.get(`/admin/users?page=${page}&size=${size}`)
  },
  searchUsers(keyword, page = 0, size = 10) {
    const encodedKeyword = encodeURIComponent(keyword);
    return api.get(`/admin/users/search?keyword=${encodedKeyword}&page=${page}&size=${size}`)
  },
  getUsersByStatus(status, page = 0, size = 10) {
    return api.get(`/admin/users/${status}?page=${page}&size=${size}`)
  },
  banUser(id) {
    return api.patch(`/admin/users/${id}/ban`)
  },
  activateUser(id) {
    return api.patch(`/admin/users/${id}/activate`)
  },
  deleteUser(id) {
    return api.delete(`/admin/users/${id}`)
  },

  // 文章管理
  getAllArticles(page = 0, size = 10) {
    return api.get(`/admin/articles?page=${page}&size=${size}`)
  },
  deleteArticle(id) {
    return api.delete(`/admin/articles/${id}`)
  },
  publishArticle(id) {
    return api.patch(`/admin/articles/${id}/publish`)
  },
  rejectArticle(id) {
    return api.patch(`/admin/articles/${id}/reject`)
  },
  getPendingArticles(page = 0, size = 10) {
    return api.get(`/admin/articles/pending?page=${page}&size=${size}`)
  },
  toggleArticleFeatured(id, featured) {
    return api.patch(`/admin/articles/${id}/featured?featured=${featured}`)
  },
  toggleArticleTop(id, top) {
    return api.patch(`/admin/articles/${id}/top?top=${top}`)
  },

  // 评论管理
  getAllComments(page = 0, size = 10) {
    return api.get(`/comments?page=${page}&size=${size}`)
  },
  getCommentsByUserId(userId, page = 0, size = 10) {
    return api.get(`/comments/user/${userId}?page=${page}&size=${size}`)
  },
  searchCommentsByKeyword(keyword, page = 0, size = 10) {
    const encodedKeyword = encodeURIComponent(keyword);
    return api.get(`/comments/search?keyword=${encodedKeyword}&page=${page}&size=${size}`)
  },
  approveComment(id) {
    return api.patch(`/comments/${id}/approve`)
  },
  rejectComment(id) {
    return api.patch(`/comments/${id}/reject`)
  },
  deleteComment(id) {
    return api.delete(`/comments/${id}`)
  },

  // 标签审核管理
  getPendingTags() {
    return api.get('/admin/tags/pending')
  },
  approveTag(id) {
    return api.patch(`/admin/tags/${id}/approve`)
  },
  rejectTag(id) {
    return api.patch(`/admin/tags/${id}/reject`)
  },

  // 分类审核管理
  getPendingCategories() {
    return api.get('/admin/categories/pending')
  },
  approveCategory(id) {
    return api.patch(`/admin/categories/${id}/approve`)
  },
  rejectCategory(id) {
    return api.patch(`/admin/categories/${id}/reject`)
  }
}

// 文件上传相关API
export const fileApi = {
  uploadAvatar(file) {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/files/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      transformResponse: [
        data => {
          // 尝试解析JSON响应
          let response
          try {
            response = JSON.parse(data)

            // 检查响应中的文件路径是否为完整URL
            if (response.success && response.data && typeof response.data === 'string') {
              // 如果返回的是文件名而不是完整URL，则构造完整URL
              if (!response.data.startsWith('http') && !response.data.startsWith('/api/')) {
                response.data = `${api.defaults.baseURL}/files/avatar/${response.data}`
              }
            }

            return response
          } catch (e) {
            return data
          }
        }
      ]
    })
  },
  uploadCover(file) {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/files/upload/cover', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      transformResponse: [
        data => {
          // 尝试解析JSON响应
          let response
          try {
            response = JSON.parse(data)

            // 检查响应中的文件路径是否为完整URL
            if (response.success && response.data && typeof response.data === 'string') {
              // 如果返回的是文件名而不是完整URL，则构造完整URL
              if (!response.data.startsWith('http') && !response.data.startsWith('/api/')) {
                response.data = `${api.defaults.baseURL}/files/cover/${response.data}`
              }
            }

            return response
          } catch (e) {
            return data
          }
        }
      ]
    })
  },
  getAvatarUrl(fileName) {
    return `${api.defaults.baseURL}/files/avatar/${fileName}`
  },
  getCoverUrl(fileName) {
    return `${api.defaults.baseURL}/files/cover/${fileName}`
  },
  deleteAvatar(fileName) {
    return api.delete(`/files/avatar/${fileName}`)
  },
  deleteCover(fileName) {
    return api.delete(`/files/cover/${fileName}`)
  }
}

export default {
  auth: authApi,
  article: articleApi,
  user: userApi,
  comment: commentApi,
  category: categoryApi,
  tag: tagApi,
  admin: adminApi,
  upload: fileApi
}
