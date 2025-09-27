<template>
  <SidebarProvider>
    <RouterView />
    <Toaster position="top-right" :rich-colors="true" />
  </SidebarProvider>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import { SidebarProvider } from '@/components/ui/sidebar';
import { Toaster } from '@/components/ui/sonner';
import 'vue-sonner/style.css';
</script>
