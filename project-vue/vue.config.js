const { defineConfig } = require('@vue/cli-service')
const webpack = require('webpack')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    allowedHosts: 'all',
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
        pathRewrite: {
          '^/api': '/api' // 保持/api前缀，因为后端需要这个路径
        },
        // 支持会话cookie跨域
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      }
    }
  },
  configureWebpack: {
    plugins: [
      new webpack.DefinePlugin({
        __VUE_OPTIONS_API__: true,
        __VUE_PROD_DEVTOOLS__: false,
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false
      })
    ]
  },
  publicPath: process.env.NODE_ENV === 'production' ? '/' : '/'
})
