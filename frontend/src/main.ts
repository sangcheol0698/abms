import 'reflect-metadata';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from '@/core/router';
import '@/index.css';
import '@/assets/fonts.css';
import { configureContainer } from '@/core/di/container';
import { initializeTheme } from '@/core/composables';

configureContainer();
initializeTheme();

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
