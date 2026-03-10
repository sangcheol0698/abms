<template>
  <DropdownMenu v-if="hasVisibleActions">
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon" class="h-8 w-8 p-0 data-[state=open]:bg-muted">
        <MoreHorizontal class="h-4 w-4" />
        <span class="sr-only">직원 메뉴 열기</span>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end" class="w-[180px]">
      <DropdownMenuItem v-if="showEdit" @click="$emit('edit')">
        <Pencil class="mr-2 h-4 w-4" />
        {{ editLabel }}
      </DropdownMenuItem>
      <DropdownMenuItem v-if="showCopyEmail" @click="$emit('copyEmail')">
        <Copy class="mr-2 h-4 w-4" />
        이메일 복사
      </DropdownMenuItem>
      <DropdownMenuSeparator v-if="showDelete && (showEdit || showCopyEmail)" />
      <DropdownMenuItem v-if="showDelete" class="text-destructive" @click="$emit('delete')">
        <Trash class="mr-2 h-4 w-4" />
        직원 삭제
      </DropdownMenuItem>
    </DropdownMenuContent>
  </DropdownMenu>
</template>

<script setup lang="ts">
import { Copy, MoreHorizontal, Pencil, Trash } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    row: EmployeeListItem;
    showEdit?: boolean;
    showDelete?: boolean;
    showCopyEmail?: boolean;
    editLabel?: string;
  }>(),
  {
    showEdit: true,
    showDelete: true,
    showCopyEmail: true,
    editLabel: '직원 편집',
  },
);

defineEmits<{
  edit: [];
  copyEmail: [];
  delete: [];
}>();

const hasVisibleActions = computed(() => props.showEdit || props.showDelete || props.showCopyEmail);
</script>
