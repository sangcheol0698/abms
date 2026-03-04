import 'reflect-metadata';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { VueQueryPlugin } from '@tanstack/vue-query';
import App from './App.vue';
import router from '@/core/router';
import '@/index.css';
import '@/assets/fonts.css';
import { configureContainer } from '@/core/di/container';
import { initializeTheme } from '@/core/composables';
import { registerAuthHttpErrorHandler } from '@/features/auth/registerAuthHttpErrorHandler';
import { queryClient } from '@/core/query';

configureContainer();
initializeTheme();
registerAuthHttpErrorHandler(router);

const app = createApp(App);
app.use(createPinia());
app.use(VueQueryPlugin, { queryClient });
app.use(router);
app.mount('#app');
