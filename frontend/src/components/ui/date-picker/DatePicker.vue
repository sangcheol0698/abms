<template>
  <div :class="cn('relative w-full', className)">
    <div class="relative">
      <Input v-model="inputValue" type="text" :placeholder="placeholder" maxlength="10"
        :class="cn('w-full pr-10', !formattedValue && 'text-muted-foreground')" :disabled="disabled"
        @input="handleInput" @blur="handleInputBlur" @keydown.enter.prevent="handleInputBlur" />
      <Popover v-model:open="isOpen">
        <PopoverTrigger as-child>
          <Button variant="ghost" size="icon"
            class="absolute right-0 top-0 h-full w-10 px-0 opacity-50 hover:opacity-100" :disabled="disabled"
            tabindex="-1">
            <CalendarIcon class="h-4 w-4" />
          </Button>
        </PopoverTrigger>
        <PopoverContent class="w-auto p-0" align="end">
          <CalendarRoot v-slot="{ date, grid, weekDays }" v-model:placeholder="placeholderDate" v-model="internalValue"
            @update:model-value="handleSelect" :disabled="disabled" prevent-deselect class="rounded-md border p-3" initial-focus>
            <CalendarHeader>
              <CalendarHeading class="flex w-full items-center justify-between gap-2">
                <Select :model-value="monthString" @update:model-value="updateMonth">
                  <SelectTrigger aria-label="월 선택" class="w-[60%]">
                    <SelectValue placeholder="월 선택" />
                  </SelectTrigger>
                  <SelectContent class="max-h-[200px]">
                    <SelectItem v-for="monthOption in createYear({ dateObj: date })" :key="monthOption.toString()"
                      :value="monthOption.month.toString()">
                      {{ dateFormatter.custom(toDate(monthOption), { month: 'long' }) }}
                    </SelectItem>
                  </SelectContent>
                </Select>

                <Select :model-value="yearString" @update:model-value="updateYear">
                  <SelectTrigger aria-label="년도 선택" class="w-[40%]">
                    <SelectValue placeholder="년도 선택" />
                  </SelectTrigger>
                  <SelectContent class="max-h-[200px]">
                    <SelectItem v-for="yearOption in createDecade({ dateObj: date, startIndex: -100, endIndex: 50 })"
                      :key="yearOption.toString()" :value="yearOption.year.toString()">
                      {{ yearOption.year }}
                    </SelectItem>
                  </SelectContent>
                </Select>
              </CalendarHeading>
            </CalendarHeader>

            <div class="flex flex-col space-y-4 pt-4 sm:flex-row sm:gap-x-4 sm:gap-y-0">
              <CalendarGrid v-for="month in grid" :key="month.value.toString()">
                <CalendarGridHead>
                  <CalendarGridRow>
                    <CalendarHeadCell v-for="day in weekDays" :key="day">
                      {{ day }}
                    </CalendarHeadCell>
                  </CalendarGridRow>
                </CalendarGridHead>
                <CalendarGridBody class="grid">
                  <CalendarGridRow v-for="(weekDates, index) in month.rows" :key="`week-${index}`" class="mt-2 w-full">
                    <CalendarCell v-for="weekDate in weekDates" :key="weekDate.toString()" :date="weekDate">
                      <CalendarCellTrigger :day="weekDate" :month="month.value" />
                    </CalendarCell>
                  </CalendarGridRow>
                </CalendarGridBody>
              </CalendarGrid>
            </div>
          </CalendarRoot>
        </PopoverContent>
      </Popover>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DateValue } from '@internationalized/date';
import { getLocalTimeZone, parseDate, today } from '@internationalized/date';
import { computed, ref, watch } from 'vue';
import { CalendarIcon } from 'lucide-vue-next';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  CalendarCell,
  CalendarCellTrigger,
  CalendarGrid,
  CalendarGridBody,
  CalendarGridHead,
  CalendarGridRow,
  CalendarHeadCell,
  CalendarHeader,
  CalendarHeading,
} from '@/components/ui/calendar';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { CalendarRoot, useDateFormatter } from 'reka-ui';
import { createDecade, createYear, toDate } from 'reka-ui/date';
import { formatDate } from '@/lib/utils';

interface DatePickerProps {
  modelValue?: Date | string | null;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
}

interface DatePickerEmits {
  'update:modelValue': [value: Date | null];
}

const props = withDefaults(defineProps<DatePickerProps>(), {
  placeholder: '날짜를 선택하세요',
  disabled: false,
});

const emit = defineEmits<DatePickerEmits>();

const dateFormatter = useDateFormatter('ko-KR');

const defaultPlaceholder = today(getLocalTimeZone()) as unknown as DateValue;
const internalValue = ref<any>();
const placeholderDate = ref<any>(defaultPlaceholder);
const isOpen = ref(false);
const inputValue = ref('');

const formattedValue = computed(() => {
  if (!internalValue.value) {
    return null;
  }
  return formatDate(internalValue.value.toDate(getLocalTimeZone()));
});

watch(
  formattedValue,
  (newVal) => {
    inputValue.value = newVal || '';
  },
  { immediate: true },
);

const monthString = computed(
  () => (placeholderDate.value ?? defaultPlaceholder).month.toString(),
);
const yearString = computed(() => (placeholderDate.value ?? defaultPlaceholder).year.toString());

watch(
  () => props.modelValue,
  (next) => {
    if (!next) {
      internalValue.value = undefined;
      placeholderDate.value = defaultPlaceholder;
      return;
    }

    const source = typeof next === 'string' ? new Date(next) : next;
    if (Number.isNaN(source?.getTime())) {
      internalValue.value = undefined;
      placeholderDate.value = defaultPlaceholder;
      inputValue.value = '';
      return;
    }

    const parsed = parseDate(
      `${source.getFullYear()}-${String(source.getMonth() + 1).padStart(2, '0')}-${String(source.getDate()).padStart(2, '0')}`,
    );
    internalValue.value = parsed;
    placeholderDate.value = parsed;

    // Explicitly sync the input value when the model value changes from outside
    inputValue.value = formatDate(parsed.toDate(getLocalTimeZone()));
  },
  { immediate: true },
);

function updateMonth(newMonth: unknown) {
  if (newMonth === null || newMonth === undefined) {
    return;
  }
  const monthNumber =
    typeof newMonth === 'number'
      ? newMonth
      : typeof newMonth === 'bigint'
        ? Number(newMonth)
        : Number(newMonth);
  const base = placeholderDate.value ?? defaultPlaceholder;
  if (Number.isNaN(monthNumber) || monthNumber === base.month) {
    return;
  }
  placeholderDate.value = base.set({ month: monthNumber });
}

function updateYear(newYear: unknown) {
  if (newYear === null || newYear === undefined) {
    return;
  }
  const yearNumber =
    typeof newYear === 'number'
      ? newYear
      : typeof newYear === 'bigint'
        ? Number(newYear)
        : Number(newYear);
  const base = placeholderDate.value ?? defaultPlaceholder;
  if (Number.isNaN(yearNumber) || yearNumber === base.year) {
    return;
  }
  placeholderDate.value = base.set({ year: yearNumber });
}

function handleSelect(nextValue: DateValue | undefined) {
  internalValue.value = nextValue;

  if (!nextValue) {
    emit('update:modelValue', null);
    placeholderDate.value = defaultPlaceholder;
    return;
  }

  placeholderDate.value = nextValue;

  try {
    emit('update:modelValue', nextValue.toDate(getLocalTimeZone()));
    isOpen.value = false;
  } catch (error) {
    console.warn('Failed to convert DateValue to Date:', error);
    emit('update:modelValue', null);
  }
}
function handleInput(event: Event) {
  const target = event.target as HTMLInputElement;
  let val = target.value.replace(/[^0-9]/g, '');
  if (val.length > 4) val = val.slice(0, 4) + '-' + val.slice(4);
  if (val.length > 7) val = val.slice(0, 7) + '-' + val.slice(7, 9);
  inputValue.value = val;
}

function handleInputBlur() {
  const value = inputValue.value;
  if (!value) {
    internalValue.value = undefined;
    placeholderDate.value = defaultPlaceholder;
    emit('update:modelValue', null);
    return;
  }

  // 간단한 날짜 파싱 시도 (YYYY-MM-DD 또는 YYYY.MM.DD 등)
  const normalizedValue = value.replace(/[.\s/]/g, '-');
  const d = new Date(normalizedValue);

  if (!Number.isNaN(d.getTime())) {
    const parsed = parseDate(
      `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    );
    internalValue.value = parsed;
    placeholderDate.value = parsed;
    emit('update:modelValue', parsed.toDate(getLocalTimeZone()));
  } else {
    // 파싱 실패 시 원래 포맷값으로 되돌림
    inputValue.value = formattedValue.value || '';
  }
}
</script>
