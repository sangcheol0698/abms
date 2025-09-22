<template>
  <SidebarLayout>
    <RouterView />
  </SidebarLayout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import SidebarLayout from '@/core/layouts/SidebarLayout.vue';
</script>
