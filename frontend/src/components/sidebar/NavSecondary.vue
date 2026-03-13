<template>
  <SidebarGroup v-if="items.length">
    <SidebarGroupLabel>{{ label }}</SidebarGroupLabel>
    <SidebarMenu>
      <SidebarMenuItem v-for="item in items" :key="item.title">
        <SidebarMenuButton
          asChild
          size="sm"
          :tooltip="item.title"
          :isActive="isActiveRoute(item.url)"
          :class="
            isActiveRoute(item.url)
              ? 'bg-sidebar-accent text-sidebar-accent-foreground'
              : 'text-sidebar-foreground/72 hover:text-sidebar-foreground'
          "
        >
          <router-link :to="item.url" @click.prevent.stop="navigateTo(item.url)">
            <component :is="item.icon" v-if="item.icon" />
            <span>{{ item.title }}</span>
          </router-link>
        </SidebarMenuButton>
      </SidebarMenuItem>
    </SidebarMenu>
  </SidebarGroup>
</template>

<script setup lang="ts">
import type { LucideIcon } from 'lucide-vue-next';
import { useRoute, useRouter } from 'vue-router';
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';

withDefaults(
  defineProps<{
    items: Array<{
      title: string;
      url: string;
      icon?: LucideIcon;
    }>;
    label?: string;
  }>(),
  {
    label: '도구',
  },
);

const route = useRoute();
const router = useRouter();

function isActiveRoute(url: string) {
  return route.path === url || route.path.startsWith(`${url}/`);
}

async function navigateTo(url: string) {
  if (route.path !== url) {
    await router.push(url);
  }
}
</script>
