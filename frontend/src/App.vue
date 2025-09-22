<template>
  <SidebarProvider>
    <RouterView />
  </SidebarProvider>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import { SidebarProvider } from '@/components/ui/sidebar';
</script>
