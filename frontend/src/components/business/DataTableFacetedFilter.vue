<template>
  <Popover>
    <PopoverTrigger as-child>
      <Button variant="outline" size="sm" class="h-8 border-dashed">
        <PlusCircle class="mr-2 h-4 w-4" />
        {{ title }}
        <template v-if="selectedValues.size > 0">
          <Separator orientation="vertical" class="mx-2 h-4" />
          <Badge
            variant="secondary"
            class="rounded-sm px-1 font-normal lg:hidden"
          >
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
                v-for="option in options
                  .filter((option) => selectedValues.has(option.value))"
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
    </PopoverTrigger>
    <PopoverContent class="w-[200px] p-0" align="start">
      <Command :filter="commandFilter">
        <CommandInput :placeholder="title" />
        <CommandList>
          <CommandEmpty>결과가 없습니다.</CommandEmpty>
          <CommandGroup>
            <CommandItem
              v-for="option in options"
              :key="option.value"
              :value="option.value"
              class="h-8"
              @select="toggleOption(option.value)"
            >
              <div
                :class="cn(
                  'mr-2 flex h-4 w-4 items-center justify-center rounded-sm border border-primary',
                  selectedValues.has(option.value)
                    ? 'bg-primary text-primary-foreground'
                    : 'opacity-50 [&_svg]:invisible',
                )"
              >
                <Check :class="cn('h-4 w-4')" />
              </div>
              <component :is="option.icon" v-if="option.icon" class="mr-2 h-4 w-4 text-muted-foreground" />
              <span>{{ option.label }}</span>
              <span v-if="option.count !== undefined" class="ml-auto flex h-4 w-4 items-center justify-center font-mono text-xs">
                {{ option.count }}
              </span>
            </CommandItem>
          </CommandGroup>

          <template v-if="selectedValues.size > 0">
            <CommandSeparator />
            <CommandGroup>
              <CommandItem
                :value="{ label: '필터 지우기' }"
                class="justify-center text-center h-8"
                @select="clearFilter"
              >
                필터 지우기
              </CommandItem>
            </CommandGroup>
          </template>
        </CommandList>
      </Command>
    </PopoverContent>
  </Popover>
</template>

<script setup lang="ts">
import type { Column } from '@tanstack/vue-table';
import { computed } from 'vue';
import { Check, PlusCircle } from 'lucide-vue-next';
import { cn } from '@/lib/utils';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
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
}

const props = withDefaults(defineProps<DataTableFacetedFilterProps>(), {
  title: '필터',
});

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
