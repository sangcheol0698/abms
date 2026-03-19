<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button variant="ghost" size="icon" class="h-8 w-8 p-0 data-[state=open]:bg-muted">
        <MoreHorizontal class="h-4 w-4" />
        <span class="sr-only">매출 일정 메뉴 열기</span>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end" class="w-[140px]">
      <DropdownMenuItem @click="$emit('edit')">
        <Pencil class="mr-2 h-4 w-4" />
        수정
      </DropdownMenuItem>
      <DropdownMenuItem @click="$emit('toggle-status')">
        <Receipt class="mr-2 h-4 w-4" />
        {{ isInvoiced ? '발행 취소' : '발행' }}
      </DropdownMenuItem>
    </DropdownMenuContent>
  </DropdownMenu>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { MoreHorizontal, Pencil, Receipt } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

const props = defineProps<{
  status?: 'PLANNED' | 'INVOICED';
}>();

defineEmits<{
  edit: [];
  'toggle-status': [];
}>();

const isInvoiced = computed(() => props.status === 'INVOICED');
</script>
