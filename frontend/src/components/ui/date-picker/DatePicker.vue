<template>
  <Popover>
    <PopoverTrigger as-child>
      <Button
        variant="outline"
        :class="cn('w-full justify-start text-left font-normal', !formattedValue && 'text-muted-foreground', className)"
        :disabled="disabled"
      >
        <CalendarIcon class="mr-2 h-4 w-4" />
        {{ formattedValue ?? placeholder }}
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-auto p-0" align="start">
      <CalendarRoot
        v-slot="{ date, grid, weekDays }"
        v-model:placeholder="placeholderDate"
        v-model="internalValue"
        @update:model-value="handleSelect"
        :disabled="disabled"
        class="rounded-md border p-3"
        initial-focus
      >
        <CalendarHeader>
          <CalendarHeading class="flex w-full items-center justify-between gap-2">
            <Select :model-value="monthString" @update:model-value="updateMonth">
              <SelectTrigger aria-label="월 선택" class="w-[60%]">
                <SelectValue placeholder="월 선택" />
              </SelectTrigger>
              <SelectContent class="max-h-[200px]">
                <SelectItem
                  v-for="monthOption in createYear({ dateObj: date })"
                  :key="monthOption.toString()"
                  :value="monthOption.month.toString()"
                >
                  {{ dateFormatter.custom(toDate(monthOption), { month: 'long' }) }}
                </SelectItem>
              </SelectContent>
            </Select>

            <Select :model-value="yearString" @update:model-value="updateYear">
              <SelectTrigger aria-label="년도 선택" class="w-[40%]">
                <SelectValue placeholder="년도 선택" />
              </SelectTrigger>
              <SelectContent class="max-h-[200px]">
                <SelectItem
                  v-for="yearOption in createDecade({ dateObj: date, startIndex: -100, endIndex: 50 })"
                  :key="yearOption.toString()"
                  :value="yearOption.year.toString()"
                >
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
              <CalendarGridRow
                v-for="(weekDates, index) in month.rows"
                :key="`week-${index}`"
                class="mt-2 w-full"
              >
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
</template>

<script setup lang="ts">
import type { DateValue } from '@internationalized/date';
import { DateFormatter, getLocalTimeZone, parseDate, today } from '@internationalized/date';
import { computed, ref, watch } from 'vue';
import { CalendarIcon } from 'lucide-vue-next';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
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

const displayFormatter = new DateFormatter('ko-KR', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
});

const dateFormatter = useDateFormatter('ko-KR');

const defaultPlaceholder = today(getLocalTimeZone()) as unknown as DateValue;
const internalValue = ref<any>();
const placeholderDate = ref<any>(defaultPlaceholder);

const formattedValue = computed(() => {
  if (!internalValue.value) {
    return null;
  }
  return displayFormatter.format(internalValue.value.toDate(getLocalTimeZone()));
});

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
      return;
    }

    const parsed = parseDate(
      `${source.getFullYear()}-${String(source.getMonth() + 1).padStart(2, '0')}-${String(source.getDate()).padStart(2, '0')}`,
    );
    internalValue.value = parsed;
    placeholderDate.value = parsed;
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
  } catch (error) {
    console.warn('Failed to convert DateValue to Date:', error);
    emit('update:modelValue', null);
  }
}
</script>
