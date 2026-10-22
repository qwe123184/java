import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 5173,
    // 端口被占用时直接报错退出，不要偷偷换到 5174/5175，
    // 否则启动脚本提示的地址与实际监听端口会不一致。
    strictPort: true,
    open: false,
    // 前端通过 /api 代理访问后端（springBootTest，端口 8081）
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
