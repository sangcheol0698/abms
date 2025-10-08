<template>
  <div class="space-y-3">
    <div
      v-if="$slots.filters"
      :class="[
        'flex items-center space-x-2 flex-wrap gap-y-2',
        'md:flex',
        showFilters ? 'flex' : 'hidden',
      ]"
    >
      <slot name="filters" />

      <Button
        v-if="isFiltered"
        variant="ghost"
        size="sm"
        class="h-8 px-2 md:hidden"
        @click="resetFilters"
      >
        <X class="mr-2 h-4 w-4" />
        초기화
      </Button>
    </div>

    <div class="flex items-center justify-between gap-3">
      <div class="flex min-w-0 flex-1 items-center space-x-2">
        <div class="flex items-center gap-2">
          <Input
            :placeholder="searchPlaceholder"
            :model-value="internalSearchValue"
            @update:model-value="handleSearchInput"
            @compositionupdate="handleSearchComposition"
            @keydown.enter.prevent="props.applySearchOnEnter ? handleSearchEnter() : undefined"
            class="h-8 w-[120px] sm:w-[180px] lg:w-[250px]"
          />
          <Button
            v-if="props.applySearchOnEnter"
            variant="outline"
            size="sm"
            class="h-8 w-8 p-0"
            :disabled="internalSearchValue.trim().length === 0"
            @click="handleSearchEnter"
          >
            <Search class="h-4 w-4" />
          </Button>
        </div>

        <Button
          variant="outline"
          size="sm"
          class="h-8 md:hidden"
          @click="showFilters = !showFilters"
        >
          <Filter class="h-4 w-4" />
          <span
            v-if="hasActiveFilters"
            class="ml-1 rounded-full bg-primary px-1 text-xs text-primary-foreground"
          >
            {{ activeFilterCount }}
          </span>
        </Button>

        <Button
          v-if="isFiltered"
          variant="ghost"
          size="sm"
          class="hidden h-8 px-2 md:flex"
          @click="resetFilters"
        >
          <X class="mr-2 h-4 w-4" />
          <span class="hidden lg:inline">필터 초기화</span>
          <span class="lg:hidden">초기화</span>
        </Button>
      </div>

      <div class="flex shrink-0 items-center space-x-2">
        <div
          v-if="showSelectedInfo && selectedRowCount > 0"
          class="flex items-center space-x-2 text-sm text-muted-foreground"
        >
          <span class="hidden sm:inline">{{ selectedRowCount }}개 선택됨</span>
          <span class="sm:hidden">{{ selectedRowCount }}개</span>
          <Button
            variant="ghost"
            size="sm"
            class="h-8 px-2"
            @click="table.toggleAllPageRowsSelected(false)"
          >
            <span class="hidden sm:inline">선택 해제</span>
            <span class="sm:hidden">해제</span>
          </Button>
        </div>

        <slot name="actions" />

        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button variant="outline" size="sm" class="h-8">
              <Settings class="h-4 w-4" />
              <span class="ml-2 hidden sm:inline">컬럼</span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" class="w-[200px]">
            <DropdownMenuLabel>컬럼 표시/숨기기</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <div
              v-for="column in hideableColumns"
              :key="`${column.id}-${forceUpdate}`"
              class="flex cursor-pointer items-center space-x-2 rounded-sm p-2 hover:bg-muted"
              @click="() => onToggleColumn(column)"
            >
              <Checkbox
                :model-value="column.getIsVisible()"
                @click.stop
                @update:model-value="(value) =>
                  onToggleColumn(
                    column,
                    value === 'indeterminate' ? undefined : Boolean(value),
                  )"
              />
              <span class="text-sm">
                {{ getColumnLabel(column.id) }}
              </span>
            </div>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Table } from '@tanstack/vue-table';
import { computed, ref, watch } from 'vue';
import { Filter, Search, Settings, X } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Input } from '@/components/ui/input';

interface DataTableToolbarProps {
  table: Table<any>;
  searchPlaceholder?: string;
  searchColumnId?: string;
  getColumnLabel: (columnId: string) => string;
  showSelectedInfo?: boolean;
  normalizeSearchValue?: (value: string) => string;
  extractSearchValue?: (value: unknown) => string;
  applySearchOnEnter?: boolean;
}

const props = withDefaults(defineProps<DataTableToolbarProps>(), {
  showSelectedInfo: true,
  normalizeSearchValue: undefined,
  extractSearchValue: undefined,
  applySearchOnEnter: false,
});

const forceUpdate = ref(0);
const showFilters = ref(false);

const hideableColumns = computed(() => {
  return props.table.getAllColumns().filter((column) => column.getCanHide());
});

const isFiltered = computed(() => props.table.getState().columnFilters.length > 0);

const selectedRowCount = computed(() => Object.keys(props.table.getState().rowSelection).length);

const hasActiveFilters = computed(() => props.table.getState().columnFilters.length > 0);

const activeFilterCount = computed(() => props.table.getState().columnFilters.length);

const searchColumnId = computed(() => props.searchColumnId ?? 'name');

const searchInputValue = computed(() => {
  const column = props.table.getColumn(searchColumnId.value);
  if (!column) {
    return '';
  }
  const currentValue = column.getFilterValue();
  if (typeof props.extractSearchValue === 'function') {
    return props.extractSearchValue(currentValue);
  }
  return typeof currentValue === 'string' ? currentValue : '';
});

function defaultNormalizeSearchValue(value: string): string {
  return value.trim();
}

const internalSearchValue = ref('');

watch(
  searchInputValue,
  (value) => {
    internalSearchValue.value = value;
  },
  { immediate: true },
);

function setSearchFilter(rawValue: string) {
  const column = props.table.getColumn(searchColumnId.value);
  if (!column) {
    return;
  }
  const trimmed = rawValue.trim();
  if (!trimmed) {
    column.setFilterValue(undefined);
    return;
  }
  const normalize = props.normalizeSearchValue ?? defaultNormalizeSearchValue;
  const normalized = normalize(trimmed);
  column.setFilterValue(normalized.length > 0 ? normalized : undefined);
}

function handleSearchInput(value: string | number) {
  const stringValue = String(value);
  internalSearchValue.value = stringValue;
  if (!props.applySearchOnEnter) {
    setSearchFilter(stringValue);
  }
}

function handleSearchComposition(value: string) {
  internalSearchValue.value = value;
  if (!props.applySearchOnEnter) {
    setSearchFilter(value);
  }
}

function handleSearchEnter() {
  if (!props.applySearchOnEnter) {
    return;
  }
  setSearchFilter(internalSearchValue.value);
}

function resetFilters() {
  props.table.resetColumnFilters();
  showFilters.value = false;
}

function onToggleColumn(column: any, value?: boolean) {
  column.toggleVisibility(value ?? !column.getIsVisible());
  forceUpdate.value++;
}
</script>
