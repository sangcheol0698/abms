<template>
  <div v-if="!column.getCanSort()" :class="nonSortableClass">
    <span>{{ title }}</span>
  </div>

  <DropdownMenu v-else>
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="sm" :class="buttonClass">
        <span>{{ title }}</span>
        <template v-if="column.getIsSorted() === 'desc'">
          <ArrowDown class="ml-2 h-4 w-4" />
        </template>
        <template v-else-if="column.getIsSorted() === 'asc'">
          <ArrowUp class="ml-2 h-4 w-4" />
        </template>
        <template v-else>
          <ArrowUpDown class="ml-2 h-4 w-4" />
        </template>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="start">
      <DropdownMenuItem @click="column.toggleSorting(false)">
        <ArrowUp class="mr-2 h-3.5 w-3.5 text-muted-foreground/70" />
        오름차순
      </DropdownMenuItem>
      <DropdownMenuItem @click="column.toggleSorting(true)">
        <ArrowDown class="mr-2 h-3.5 w-3.5 text-muted-foreground/70" />
        내림차순
      </DropdownMenuItem>
      <DropdownMenuItem @click="column.clearSorting()">
        <ArrowUpDown class="mr-2 h-3.5 w-3.5 text-muted-foreground/70" />
        정렬 해제
      </DropdownMenuItem>
    </DropdownMenuContent>
  </DropdownMenu>
</template>

<script setup lang="ts">
import type { Column } from '@tanstack/vue-table';
import { computed } from 'vue';
import { ArrowDown, ArrowUp, ArrowUpDown } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

interface Props {
  column: Column<any>;
  title: string;
  align?: 'left' | 'center' | 'right';
}

const props = withDefaults(defineProps<Props>(), {
  align: 'left',
});

const nonSortableClass = computed(() => {
  if (props.align === 'right') return 'w-full flex items-center justify-end space-x-2 text-right';
  if (props.align === 'center') return 'w-full flex items-center justify-center space-x-2 text-center';
  return 'flex items-center space-x-2';
});

const buttonClass = computed(() => {
  if (props.align === 'right') return 'h-8 w-full justify-end pr-2 data-[state=open]:bg-accent';
  if (props.align === 'center') return 'h-8 w-full justify-center data-[state=open]:bg-accent';
  return '-ml-3 h-8 data-[state=open]:bg-accent';
});
</script>
