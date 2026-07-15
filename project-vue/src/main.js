import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/design-system.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

if (process.env.NODE_ENV === 'development') {
  app.config.performance = true
  app.config.warnHandler = (msg, instance, trace) => {
    console.warn(`[Vue warn]: ${msg}`)
    console.warn(trace)
  }
}

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(store).use(router).use(ElementPlus, { size: 'default' }).mount('#app')
