<template>
  <SidebarGroup v-if="items.length">
    <SidebarGroupLabel>{{ label }}</SidebarGroupLabel>
    <SidebarMenu>
      <SidebarMenuItem v-for="item in items" :key="item.title">
        <SidebarMenuButton 
          asChild 
          :tooltip="item.title" 
          :isActive="isActiveRoute(item.url)"
          :class="isActiveRoute(item.url) ? 'bg-primary text-primary-foreground font-medium shadow-sm' : ''"
        >
          <router-link 
            :to="item.url" 
            @click.prevent.stop="navigateTo(item.url)"
            :class="isActiveRoute(item.url) ? 'text-primary-foreground' : 'text-foreground'"
          >
            <component :is="item.icon" v-if="item.icon" />
            <span>{{ item.title }}</span>
          </router-link>
        </SidebarMenuButton>
      </SidebarMenuItem>
    </SidebarMenu>
  </SidebarGroup>
</template>

<script setup lang="ts">
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';
import { type LucideIcon } from 'lucide-vue-next';
import { toRefs } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const props = withDefaults(
  defineProps<{
    items: {
      title: string;
      url: string;
      icon?: LucideIcon;
      isActive?: boolean;
    }[];
    label?: string;
  }>(),
  {
    label: '메뉴',
  },
);

const { items, label } = toRefs(props);

const route = useRoute();
const router = useRouter();

// Check if the current route path matches the menu item's URL
const isActiveRoute = (url: string) => {
  return route.path === url || route.path.startsWith(`${url}/`);
};

// Function to navigate to a URL
const navigateTo = async (url: string) => {
  if (route.path !== url) {
    await router.push(url);
  }
};
</script>
