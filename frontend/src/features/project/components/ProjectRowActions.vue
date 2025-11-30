<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon" class="h-8 w-8 p-0 data-[state=open]:bg-muted">
        <MoreHorizontal class="h-4 w-4" />
        <span class="sr-only">프로젝트 메뉴 열기</span>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end" class="w-[180px]">
      <DropdownMenuItem @click="$emit('edit')">
        <Pencil class="mr-2 h-4 w-4" />
        프로젝트 편집
      </DropdownMenuItem>
      <DropdownMenuItem @click="$emit('copyCode')">
        <Copy class="mr-2 h-4 w-4" />
        코드 복사
      </DropdownMenuItem>
      <DropdownMenuSeparator />
      <DropdownMenuItem class="text-destructive" @click="$emit('delete')">
        <Trash class="mr-2 h-4 w-4" />
        프로젝트 삭제
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
import type { ProjectListItem } from '@/features/project/models/projectListItem';

defineProps<{ row: ProjectListItem }>();

defineEmits<{
  edit: [];
  copyCode: [];
  delete: [];
}>();
</script>
