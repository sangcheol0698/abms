<template>
  <div class="flex flex-col items-center justify-between gap-4 py-4 md:flex-row">
    <div class="order-2 w-full text-sm text-muted-foreground md:order-1 md:w-auto">
      <span v-if="selectedRowCount !== undefined"> {{ selectedRowCount }} / </span>
      {{ totalElements }} 행
      <span v-if="totalPages > 0"> | {{ page }} / {{ totalPages }} 페이지 </span>
    </div>

    <div class="order-1 flex w-full flex-col items-center gap-4 md:order-2 md:w-auto md:flex-row">
      <div class="flex w-full items-center gap-2 md:w-auto">
        <p class="whitespace-nowrap text-sm text-muted-foreground">페이지당 행 수</p>
        <Select :model-value="pageSize.toString()" @update:model-value="onPageSizeChange">
          <SelectTrigger class="h-8 w-[70px]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem
              v-for="option in pageSizeOptions || [5, 10, 20, 50]"
              :key="option"
              :value="option.toString()"
            >
              {{ option }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div class="flex w-full items-center justify-center gap-1 md:w-auto">
        <Button
          variant="outline"
          size="sm"
          class="hidden sm:inline-flex"
          :disabled="page <= 1 || loading"
          @click="onPageChange(1)"
        >
          처음
        </Button>
        <Button
          variant="outline"
          size="sm"
          :disabled="page <= 1 || loading"
          @click="onPageChange(page - 1)"
        >
          이전
        </Button>

        <div class="mx-1 flex items-center gap-1">
          <template v-for="(p, i) in pages" :key="`${p}-${i}`">
            <span v-if="p === '...'" class="hidden px-1 sm:inline-flex">...</span>
            <Button
              v-else
              :variant="p === page ? 'default' : 'outline'"
              size="sm"
              :class="{ 'pagination-button-active': p === page }"
              @click="onPageChange(p as number)"
            >
              {{ p }}
            </Button>
          </template>
        </div>

        <Button
          variant="outline"
          size="sm"
          :disabled="page >= totalPages || loading"
          @click="onPageChange(page + 1)"
        >
          다음
        </Button>
        <Button
          variant="outline"
          size="sm"
          class="hidden sm:inline-flex"
          :disabled="page >= totalPages || loading"
          @click="onPageChange(totalPages)"
        >
          마지막
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

interface DataTablePaginationProps {
  page: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  loading?: boolean;
  pageSizeOptions?: number[];
  selectedRowCount?: number;
}

const props = defineProps<DataTablePaginationProps>();

const emit = defineEmits<{
  (e: 'pageChange', page: number): void;
  (e: 'pageSizeChange', pageSize: number): void;
}>();

const pages = computed(() => {
  const total = props.totalPages;
  if (total <= 1) return [];

  const current = props.page;
  const maxVisible = 6;

  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  if (current < 5) {
    return [1, 2, 3, 4, 5, '...', total];
  }

  if (current > total - 4) {
    return [1, '...', total - 4, total - 3, total - 2, total - 1, total];
  }

  return [1, '...', current - 2, current - 1, current, current + 1, '...', total];
});

function onPageChange(page: number) {
  emit('pageChange', page);
}

function onPageSizeChange(pageSize: string) {
  emit('pageSizeChange', Number(pageSize));
}
</script>
