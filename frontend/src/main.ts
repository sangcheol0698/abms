import 'reflect-metadata';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from '@/core/router';
import '@/index.css';
import { configureContainer } from '@/core/di/container';

configureContainer();

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
