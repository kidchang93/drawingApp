import { defineConfig } from 'vite'
import {fileURLToPath, URL} from "node:url";
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  // outputDir: '../drawingApp_back/src/main/resources/static',
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server:{
    port: 8081
    /*proxy:{
      "/api":{
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }*/
  }
})
