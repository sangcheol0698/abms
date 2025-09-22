<template>
  <Button variant="outline" size="icon" class="h-9 w-9" @click="toggleTheme">
    <Sun v-if="!isDark" class="h-4 w-4" />
    <Moon v-else class="h-4 w-4" />
    <span class="sr-only">테마 전환</span>
  </Button>
</template>

<script setup lang="ts">
import { Button } from '@/components/ui/button';
import { useTheme } from '@/core/composables';
import { Moon, Sun } from 'lucide-vue-next';

const { isDark, toggleTheme } = useTheme();
</script>
