<template>
  <Component :is="route.meta.layout || 'AppLayout'">
    <RouterView />
  </Component>
  <Toaster position="top-center" :rich-colors="true" />
</template>

<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router';
import { Toaster } from '@/components/ui/sonner';
import 'vue-sonner/style.css';

const route = useRoute();
</script>
