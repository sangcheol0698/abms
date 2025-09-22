<template>
  <Popover>
    <PopoverTrigger as-child>
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
    </PopoverTrigger>
    <PopoverContent class="w-[200px] p-0" align="start">
      <Command>
        <CommandInput :placeholder="searchPlaceholder" />
        <CommandList>
          <CommandEmpty>결과가 없습니다.</CommandEmpty>
          <CommandGroup>
            <CommandItem
              v-for="option in options"
              :key="option.value"
              :value="option.value"
              @select="toggleOption(option.value)"
            >
              <div
                :class="cn(
                  'mr-2 flex h-4 w-4 items-center justify-center rounded-sm border border-primary',
                  selectedValues?.has(option.value)
                    ? 'bg-primary text-primary-foreground'
                    : 'opacity-50 [&_svg]:invisible'
                )"
              >
                <Check class="h-4 w-4" />
              </div>
              <component
                v-if="option.icon"
                :is="option.icon"
                class="mr-2 h-4 w-4 text-muted-foreground"
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
    </PopoverContent>
  </Popover>
</template>

<script setup lang="ts">
import type { Column } from '@tanstack/vue-table';
import { computed } from 'vue';
import { Check, Plus } from 'lucide-vue-next';
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
  searchPlaceholder?: string;
  clearFilterLabel?: string;
}

const props = withDefaults(defineProps<DataTableFacetedFilterProps>(), {
  title: '필터',
  searchPlaceholder: '검색...',
  clearFilterLabel: '필터 지우기',
});

const facets = computed(() => props.column?.getFacetedUniqueValues());
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
