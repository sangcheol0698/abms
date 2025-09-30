<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button variant="outline" size="sm" class="h-8 border-dashed">
        <Plus class="mr-2 h-4 w-4" />
        {{ title }}
        <template v-if="selectedValues?.size">
          <Separator orientation="vertical" class="mx-2 h-4" />
          <Badge variant="secondary" class="rounded-sm px-1 font-normal lg:hidden">
            {{ selectedValues.size }}
          </Badge>
          <div class="hidden space-x-1 lg:flex">
            <Badge
              v-if="selectedValues.size > 2"
              variant="secondary"
              class="rounded-sm px-1 font-normal"
            >
              {{ selectedValues.size }} 선택됨
            </Badge>
            <template v-else>
              <Badge
                v-for="option in options.filter((option) => selectedValues.has(option.value))"
                :key="option.value"
                variant="secondary"
                class="rounded-sm px-1 font-normal"
              >
                {{ option.label }}
              </Badge>
            </template>
          </div>
        </template>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="start" class="w-[240px] p-1">
      <Command :filter="commandFilter">
        <CommandInput :placeholder="searchPlaceholder" />
        <CommandList>
          <CommandEmpty>결과가 없습니다.</CommandEmpty>
          <CommandGroup>
            <CommandItem
              v-for="option in options"
              :key="option.value"
              :value="option.value"
              class="flex items-center gap-2"
              @select="toggleOption(option.value)"
            >
              <Checkbox
                :model-value="selectedValues?.has(option.value)"
                @click.stop
                @update:model-value="() => toggleOption(option.value)"
              />
              <component
                v-if="option.icon"
                :is="option.icon"
                class="h-4 w-4 text-muted-foreground"
              />
              <span>{{ option.label }}</span>
              <span
                v-if="option.count !== undefined"
                class="ml-auto flex h-4 w-4 items-center justify-center font-mono text-xs"
              >
                {{ option.count }}
              </span>
            </CommandItem>
          </CommandGroup>
          <template v-if="selectedValues?.size">
            <CommandSeparator />
            <CommandGroup>
              <CommandItem
                :value="clearFilterLabel"
                class="justify-center text-center"
                @select="clearFilter"
              >
                {{ clearFilterLabel }}
              </CommandItem>
            </CommandGroup>
          </template>
        </CommandList>
      </Command>
    </DropdownMenuContent>
  </DropdownMenu>
</template>

<script setup lang="ts">
import type { Column } from '@tanstack/vue-table';
import { computed } from 'vue';
import { Plus } from 'lucide-vue-next';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from '@/components/ui/command';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Separator } from '@/components/ui/separator';

interface Option {
  label: string;
  value: string;
  icon?: any;
  count?: number;
}

interface DataTableFacetedFilterProps {
  column?: Column<any, unknown>;
  title?: string;
  options: Option[];
  searchPlaceholder?: string;
  clearFilterLabel?: string;
}

const props = withDefaults(defineProps<DataTableFacetedFilterProps>(), {
  title: '필터',
  searchPlaceholder: '검색...',
  clearFilterLabel: '필터 지우기',
});

computed(() => props.column?.getFacetedUniqueValues());
const selectedValues = computed(() => {
  const filterValue = props.column?.getFilterValue();

  if (Array.isArray(filterValue)) {
    return new Set(filterValue as string[]);
  }

  if (filterValue) {
    return new Set([filterValue as string]);
  }

  return new Set();
});

const optionLookup = computed(() => {
  const map = new Map<string, Option>();
  props.options.forEach((option) => map.set(option.value, option));
  return map;
});

function commandFilter(value: string, search: string) {
  const option = optionLookup.value.get(value);
  if (!option) {
    return false;
  }
  const term = search.trim().toLowerCase();
  if (!term) {
    return true;
  }
  return option.label.toLowerCase().includes(term);
}

function toggleOption(value: string) {
  const newSelectedValues = new Set(selectedValues.value);
  if (newSelectedValues.has(value)) {
    newSelectedValues.delete(value);
  } else {
    newSelectedValues.add(value);
  }
  const filterValues = Array.from(newSelectedValues);
  props.column?.setFilterValue(filterValues.length ? filterValues : undefined);
}

function clearFilter() {
  props.column?.setFilterValue(undefined);
}
</script>
