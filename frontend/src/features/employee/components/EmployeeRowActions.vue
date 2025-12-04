<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon" class="h-8 w-8 p-0 data-[state=open]:bg-muted">
        <MoreHorizontal class="h-4 w-4" />
        <span class="sr-only">직원 메뉴 열기</span>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end" class="w-[180px]">
      <DropdownMenuItem @click="$emit('edit')">
        <Pencil class="mr-2 h-4 w-4" />
        직원 편집
      </DropdownMenuItem>
      <DropdownMenuItem @click="$emit('copyEmail')">
        <Copy class="mr-2 h-4 w-4" />
        이메일 복사
      </DropdownMenuItem>
      <DropdownMenuSeparator />
      <DropdownMenuItem class="text-destructive" @click="$emit('delete')">
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

defineProps<{ row: EmployeeListItem }>();

defineEmits<{
  edit: [];
  copyEmail: [];
  delete: [];
}>();
</script>
