<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon" class="h-8 w-8 p-0 data-[state=open]:bg-muted">
        <MoreHorizontal class="h-4 w-4" />
        <span class="sr-only">계정 메뉴 열기</span>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end" class="w-[180px]">
      <DropdownMenuItem @click="$emit('copyEmail')">
        <Copy class="mr-2 h-4 w-4" />
        이메일 복사
      </DropdownMenuItem>
      <DropdownMenuSeparator />
      <DropdownMenuItem class="text-destructive" @click="$emit('remove')">
        <Trash class="mr-2 h-4 w-4" />
        그룹 해제
      </DropdownMenuItem>
    </DropdownMenuContent>
  </DropdownMenu>
</template>

<script setup lang="ts">
import { Copy, MoreHorizontal, Trash } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type { PermissionGroupAccountItem } from '@/features/admin/models/permissionGroup';

defineProps<{ row: PermissionGroupAccountItem }>();

defineEmits<{
  copyEmail: [];
  remove: [];
}>();
</script>
