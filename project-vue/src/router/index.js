import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

// 路由守卫函数
const requireAuth = (to, from, next) => {
  if (store.getters.isAuthenticated) {
    next()
  } else {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  }
}

const requireAdmin = (to, from, next) => {
  if (store.getters.isAuthenticated && store.getters.isAdmin) {
    next()
  } else {
    next({ name: 'Home' })
  }
}

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/articles',
    name: 'AllArticles',
    component: () => import('@/views/AllArticles.vue'),
    meta: { title: '文章列表' }
  },
  {
    path: '/about-us',
    name: 'AboutUs',
    component: () => import('@/views/AboutUs.vue'),
    meta: { title: '关于我们' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/articles/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue'),
    props: true,
    meta: { title: '文章详情' }
  },
  {
    path: '/articles/category/:id',
    name: 'ArticlesByCategory',
    component: () => import('@/views/ArticlesByCategory.vue'),
    props: true,
    meta: { title: '分类文章' }
  },
  {
    path: '/articles/tag/:id',
    name: 'ArticlesByTag',
    component: () => import('@/views/ArticlesByTag.vue'),
    props: true,
    meta: { title: '标签文章' }
  },
  {
    path: '/author/:id',
    name: 'AuthorArticles',
    component: () => import('@/views/AuthorArticles.vue'),
    props: true,
    meta: { title: '作者文章' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: { title: '搜索结果' }
  },
  {
    path: '/user',
    name: 'UserLayout',
    component: () => import('@/views/user/UserLayout.vue'),
    meta: { title: '用户中心' },
    beforeEnter: requireAuth,
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'articles',
        name: 'UserArticles',
        component: () => import('@/views/user/Articles.vue'),
        meta: { title: '我的文章' }
      },
      {
        path: 'favorites',
        name: 'UserFavorites',
        component: () => import('@/views/user/Favorites.vue'),
        meta: { title: '我的收藏' }
      },
      {
        path: 'comments',
        name: 'UserComments',
        component: () => import('@/views/user/Comments.vue'),
        meta: { title: '我的评论' }
      },
      {
        path: 'write',
        name: 'WriteArticle',
        component: () => import('@/views/user/WriteArticle.vue'),
        meta: { title: '写文章' }
      },
      {
        path: 'edit/:id',
        name: 'EditArticle',
        component: () => import('@/views/user/EditArticle.vue'),
        props: true,
        meta: { title: '编辑文章' }
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { title: '管理后台' },
    beforeEnter: requireAdmin,
    children: [
      {
        path: '',
        name: 'AdminDefault', // 添加名称以避免Vue Router警告
        redirect: '/admin/users' // 添加默认重定向到用户管理页面
      },
      {
        path: 'dashboard',  // 添加处理旧的dashboard路径
        name: 'AdminDashboard', // 添加名称以保持一致性
        redirect: '/admin/users' // 重定向到用户管理
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'articles',
        name: 'AdminArticles',
        component: () => import('@/views/admin/Articles.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/Comments.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: () => import('@/views/admin/Tags.vue'),
        meta: { title: '标签管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404 - 页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 动态设置页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 博客系统` : '博客系统'
  next()
})

export default router
