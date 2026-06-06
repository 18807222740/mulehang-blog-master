import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { setAuthRouter } from './api/auth-handler'
import 'vue-sonner/style.css'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

setAuthRouter(router)

app.use(pinia)
app.use(router)

app.mount('#app')
