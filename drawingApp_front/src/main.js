import { createApp } from 'vue'
import router from './router';
import App from './App.vue'
import mitt from "mitt";

const app = createApp(App);

// 이벤트 버스 설정
app.config.globalProperties.$bus = mitt();

app.use(router)
app.mount('#app');

export default app;