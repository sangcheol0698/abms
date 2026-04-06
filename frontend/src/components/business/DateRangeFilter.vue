<template>
  <div class="relative w-full xl:min-w-[240px]">
    <div class="relative flex items-center">
      <Input
        v-model="inputValue"
        type="text"
        :placeholder="placeholder"
        inputmode="numeric"
        :class="cn('w-full pr-8 font-normal', !formattedDateRange && 'text-muted-foreground', dense ? 'h-6 text-xs' : 'h-8 text-sm')"
        @beforeinput="handleBeforeInput"
        @input="handleInput"
        @blur="handleInputBlur"
        @keydown.enter.prevent="handleInputBlur"
      />
      <Popover v-model:open="isOpen">
        <PopoverTrigger as-child>
          <Button
            variant="ghost"
            size="icon"
            class="absolute right-0 top-0 h-full w-8 px-0 opacity-50 hover:opacity-100"
            tabindex="-1"
          >
            <CalendarIcon class="h-4 w-4" />
          </Button>
        </PopoverTrigger>

    <PopoverContent class="w-auto p-0" align="start">
      <div class="flex">
        <!-- 빠른 선택 (맨 왼쪽) -->
        <div class="w-40 p-3 border-r bg-muted/10 flex flex-col">
          <div class="text-sm font-medium mb-3">빠른 선택</div>
          <div class="flex flex-col gap-1 flex-1">
            <Button
              v-for="preset in presets"
              :key="preset.label"
              variant="ghost"
              size="sm"
              class="h-8 justify-start w-full"
              @click="selectPreset(preset)"
            >
              {{ preset.label }}
            </Button>
          </div>

          <!-- 액션 버튼 -->
          <div class="mt-4 pt-3 border-t">
            <div class="flex flex-col gap-2">
              <Button variant="outline" size="sm" @click="clearSelection" class="w-full">
                초기화
              </Button>
              <Button size="sm" @click="applySelection" :disabled="!canApply" class="w-full">
                적용
              </Button>
            </div>
          </div>
        </div>

        <!-- 시작날짜 캘린더 (가운데) -->
        <div class="flex flex-col border-r">
          <div class="p-3 border-b">
            <div class="text-sm font-medium mb-3">시작 날짜</div>
            <!-- 년도/월 선택 -->
            <div class="flex gap-2">
              <Select v-model="startYear" @update:model-value="updateStartCalendar">
                <SelectTrigger class="w-24">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="year in yearOptions" :key="year" :value="String(year)">
                    {{ year }}년
                  </SelectItem>
                </SelectContent>
              </Select>
              <Select v-model="startMonth" @update:model-value="updateStartCalendar">
                <SelectTrigger class="w-20">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    v-for="month in monthOptions"
                    :key="month.value"
                    :value="String(month.value)"
                  >
                    {{ month.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <Calendar
            v-model:placeholder="startPlaceholder"
            :model-value="startDateValue as any"
            @update:model-value="(v: any) => (startDateValue = v)"
            :locale="locale"
            :key="startCalendarKey"
          />
        </div>

        <!-- 끝날짜 캘린더 (오른쪽) -->
        <div class="flex flex-col">
          <div class="p-3 border-b">
            <div class="text-sm font-medium mb-3">끝 날짜</div>
            <!-- 년도/월 선택 -->
            <div class="flex gap-2">
              <Select v-model="endYear" @update:model-value="updateEndCalendar">
                <SelectTrigger class="w-24">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="year in yearOptions" :key="year" :value="String(year)">
                    {{ year }}년
                  </SelectItem>
                </SelectContent>
              </Select>
              <Select v-model="endMonth" @update:model-value="updateEndCalendar">
                <SelectTrigger class="w-20">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    v-for="month in monthOptions"
                    :key="month.value"
                    :value="String(month.value)"
                  >
                    {{ month.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <Calendar
            v-model:placeholder="endPlaceholder"
            :model-value="endDateValue as any"
            @update:model-value="(v: any) => (endDateValue = v)"
            :locale="locale"
            :key="endCalendarKey"
          />
        </div>
      </div>
    </PopoverContent>
  </Popover>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { CalendarIcon } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { cn } from '@/lib/utils';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Calendar } from '@/components/ui/calendar';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { type DateValue, fromDate, getLocalTimeZone, today } from '@internationalized/date';

interface DateRange {
  start?: Date | string;
  end?: Date | string;
}

interface Preset {
  label: string;
  range: DateRange;
}

interface Props {
  modelValue?: DateRange | null;
  placeholder?: string;
  numberOfMonths?: number;
  dense?: boolean;
}

interface Emits {
  (e: 'update:modelValue', value: DateRange | null): void;

  (e: 'change', value: DateRange | null): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '날짜 범위 선택',
  numberOfMonths: 2,
  dense: false,
});

const emit = defineEmits<Emits>();

// Internal date range state
const dateRange = ref<DateRange>({});

// Calendar를 위한 상태 (reka-ui 타입 차이로 any 사용)
const startDateValue = ref<any>(undefined);
const endDateValue = ref<any>(undefined);

// 적용 버튼 활성화 조건
const canApply = computed(() => {
  return !!startDateValue.value && !!endDateValue.value;
});

// 한국 로케일 설정
const locale = 'ko-KR';

// 년도/월 선택을 위한 상태
const currentDate = new Date();
const startYear = ref(String(currentDate.getFullYear()));
const startMonth = ref(String(currentDate.getMonth() + 1));
const endYear = ref(String(currentDate.getFullYear()));
const endMonth = ref(String(currentDate.getMonth() + 1));

// 캘린더 강제 업데이트를 위한 key
const startCalendarKey = ref(0);
const endCalendarKey = ref(0);

// 년도 옵션 (현재 년도 기준 ±10년)
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear();
  const years: number[] = [];
  for (let i = currentYear - 100; i <= currentYear + 50; i++) years.push(i);
  return years;
});

// 월 옵션
const monthOptions = [
  { value: 1, label: '1월' },
  { value: 2, label: '2월' },
  { value: 3, label: '3월' },
  { value: 4, label: '4월' },
  { value: 5, label: '5월' },
  { value: 6, label: '6월' },
  { value: 7, label: '7월' },
  { value: 8, label: '8월' },
  { value: 9, label: '9월' },
  { value: 10, label: '10월' },
  { value: 11, label: '11월' },
  { value: 12, label: '12월' },
];

// 프리셋 옵션들
const presets = computed<Preset[]>(() => {
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  const last7Days = new Date(today);
  last7Days.setDate(last7Days.getDate() - 7);

  const last30Days = new Date(today);
  last30Days.setDate(last30Days.getDate() - 30);

  const thisMonthStart = new Date(today.getFullYear(), today.getMonth(), 1);

  const lastMonthStart = new Date(today.getFullYear(), today.getMonth() - 1, 1);
  const lastMonthEnd = new Date(today.getFullYear(), today.getMonth(), 0);

  const thisYearStart = new Date(today.getFullYear(), 0, 1);

  const lastYear = new Date(today);
  lastYear.setFullYear(lastYear.getFullYear() - 1);

  return [
    { label: '오늘', range: { start: today, end: today } },
    { label: '어제', range: { start: yesterday, end: yesterday } },
    { label: '지난 7일', range: { start: last7Days, end: today } },
    { label: '지난 30일', range: { start: last30Days, end: today } },
    { label: '이번 달', range: { start: thisMonthStart, end: today } },
    { label: '지난 달', range: { start: lastMonthStart, end: lastMonthEnd } },
    { label: '올해', range: { start: thisYearStart, end: today } },
    { label: '지난 1년', range: { start: lastYear, end: today } },
  ];
});

// 포맷된 날짜 범위 표시
const formattedDateRange = computed(() => {
  if (!props.modelValue?.start || !props.modelValue?.end) return '';
  const start =
    typeof props.modelValue.start === 'string'
      ? new Date(props.modelValue.start)
      : props.modelValue.start;
  const end =
    typeof props.modelValue.end === 'string'
      ? new Date(props.modelValue.end)
      : props.modelValue.end;
  return `${formatDate(start)} ~ ${formatDate(end)}`;
});

const isOpen = ref(false);
const inputValue = ref('');

watch(
  formattedDateRange,
  (newVal) => {
    inputValue.value = newVal || '';
  },
  { immediate: true },
);

function extractDigits(value: string) {
  return value.replace(/[^0-9]/g, '').slice(0, 16);
}

function formatDigits(value: string) {
  let formatted = value;
  if (formatted.length > 4) formatted = `${formatted.slice(0, 4)}-${formatted.slice(4)}`;
  if (formatted.length > 7) formatted = `${formatted.slice(0, 7)}-${formatted.slice(7)}`;
  if (formatted.length > 10) formatted = `${formatted.slice(0, 10)} ~ ${formatted.slice(10)}`;
  if (formatted.length > 17) formatted = `${formatted.slice(0, 17)}-${formatted.slice(17)}`;
  if (formatted.length > 20) formatted = `${formatted.slice(0, 20)}-${formatted.slice(20, 22)}`;
  return formatted;
}

function countDigitsBeforeCaret(value: string, caret: number) {
  return value.slice(0, caret).replace(/[^0-9]/g, '').length;
}

function findCaretFromDigitIndex(value: string, digitIndex: number) {
  if (digitIndex <= 0) {
    return 0;
  }

  let seenDigits = 0;
  for (let i = 0; i < value.length; i += 1) {
    if (/\d/.test(value[i] ?? '')) {
      seenDigits += 1;
      if (seenDigits === digitIndex) {
        return i + 1;
      }
    }
  }

  return value.length;
}

function handleBeforeInput(event: InputEvent) {
  const target = event.target as HTMLInputElement | null;
  if (!target || event.inputType !== 'insertText' || !/^\d$/.test(event.data ?? '')) {
    return;
  }

  const currentDigits = extractDigits(inputValue.value);
  const selectionStart = target.selectionStart ?? 0;
  const selectionEnd = target.selectionEnd ?? selectionStart;

  if (currentDigits.length === 16 && selectionStart === selectionEnd) {
    const digitIndex = countDigitsBeforeCaret(inputValue.value, selectionStart);
    if (digitIndex >= currentDigits.length) {
      return;
    }

    const nextDigits = `${currentDigits.slice(0, digitIndex)}${event.data}${currentDigits.slice(digitIndex + 1)}`;
    const nextValue = formatDigits(nextDigits);
    const nextCaret = findCaretFromDigitIndex(nextValue, digitIndex + 1);

    event.preventDefault();
    inputValue.value = nextValue;

    queueMicrotask(() => {
      target.setSelectionRange(nextCaret, nextCaret);
    });
  }
}

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement;
  const selectionStart = target.selectionStart ?? target.value.length;
  const digitIndex = countDigitsBeforeCaret(target.value, selectionStart);
  const nextValue = formatDigits(extractDigits(target.value));

  inputValue.value = nextValue;

  queueMicrotask(() => {
    const nextCaret = findCaretFromDigitIndex(nextValue, digitIndex);
    target.setSelectionRange(nextCaret, nextCaret);
  });
}

function handleInputBlur() {
  const val = inputValue.value;
  if (!val) {
    clearSelection();
    return;
  }
  
  if (val.length === 23) {
    const parts = val.split(' ~ ');
    if (parts.length === 2) {
      const s = new Date(parts[0]!);
      const e = new Date(parts[1]!);
      if (!Number.isNaN(s.getTime()) && !Number.isNaN(e.getTime())) {
        let finalStart = s;
        let finalEnd = e;
        if (s > e) {
          finalStart = e;
          finalEnd = s;
        }
        startDateValue.value = fromDate(finalStart, getLocalTimeZone());
        endDateValue.value = fromDate(finalEnd, getLocalTimeZone());
        startPlaceholder.value = startDateValue.value;
        endPlaceholder.value = endDateValue.value;
        startYear.value = String(finalStart.getFullYear());
        startMonth.value = String(finalStart.getMonth() + 1);
        endYear.value = String(finalEnd.getFullYear());
        endMonth.value = String(finalEnd.getMonth() + 1);
        dateRange.value = { start: finalStart, end: finalEnd };
        
        const result = { start: finalStart, end: finalEnd };
        emit('update:modelValue', result);
        emit('change', result);
        return;
      }
    }
  }
  
  inputValue.value = formattedDateRange.value;
}


function formatDate(date: Date | DateValue): string {
  if (date instanceof Date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  } else {
    return `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}`;
  }
}



// Calendar 표시 월/년도(placeholder)
const startPlaceholder = ref<any>(today(getLocalTimeZone()));
const endPlaceholder = ref<any>(today(getLocalTimeZone()));

// 캘린더 업데이트 함수들
function updateStartCalendar() {
  const y = Number(startYear.value);
  const m = Number(startMonth.value);
  const cur = startPlaceholder.value || today(getLocalTimeZone());
  if (!Number.isNaN(y) && !Number.isNaN(m)) {
    if (cur.year === y && cur.month === m) return; // no-op if same
    startPlaceholder.value = cur.set({ year: y, month: m });
  }
}

function updateEndCalendar() {
  const y = Number(endYear.value);
  const m = Number(endMonth.value);
  const cur = endPlaceholder.value || today(getLocalTimeZone());
  if (!Number.isNaN(y) && !Number.isNaN(m)) {
    if (cur.year === y && cur.month === m) return; // no-op if same
    endPlaceholder.value = cur.set({ year: y, month: m });
  }
}

// placeholder가 바뀌면 드롭다운(연/월)도 동기화
watch(startPlaceholder, (p) => {
  if (!p) return;
  const y = String(p.year);
  const m = String(p.month);
  if (startYear.value !== y) startYear.value = y;
  if (startMonth.value !== m) startMonth.value = m;
});

watch(endPlaceholder, (p) => {
  if (!p) return;
  const y = String(p.year);
  const m = String(p.month);
  if (endYear.value !== y) endYear.value = y;
  if (endMonth.value !== m) endMonth.value = m;
});

// 프리셋 선택
function selectPreset(preset: Preset) {
  if (preset.range.start && preset.range.end) {
    const startDateObj =
      typeof preset.range.start === 'string' ? new Date(preset.range.start) : preset.range.start;
    const endDateObj =
      typeof preset.range.end === 'string' ? new Date(preset.range.end) : preset.range.end;

    // Calendar 상태 업데이트 (DateValue로 변환)
    startDateValue.value = fromDate(startDateObj, getLocalTimeZone());
    endDateValue.value = fromDate(endDateObj, getLocalTimeZone());

    // 년도/월 드롭다운도 업데이트
    startYear.value = String(startDateObj.getFullYear());
    startMonth.value = String(startDateObj.getMonth() + 1);
    endYear.value = String(endDateObj.getFullYear());
    endMonth.value = String(endDateObj.getMonth() + 1);

    // placeholder 동기화
    startPlaceholder.value = fromDate(startDateObj, getLocalTimeZone());
    endPlaceholder.value = fromDate(endDateObj, getLocalTimeZone());

    // 내부 상태 업데이트 (자동 적용 제거)
    dateRange.value = { start: startDateObj, end: endDateObj };
  }
}

// 선택 적용
function applySelection() {
  if (dateRange.value.start && dateRange.value.end) {
    const start = new Date(dateRange.value.start);
    const end = new Date(dateRange.value.end);

    if (start > end) {
      dateRange.value = { start: end, end: start };
    }
  }
  const result = dateRange.value.start && dateRange.value.end ? dateRange.value : null;
  emit('update:modelValue', result);
  emit('change', result);
  isOpen.value = false;
}

// 선택 초기화
function clearSelection() {
  dateRange.value = {};
  startDateValue.value = undefined;
  endDateValue.value = undefined;
  const current = new Date();
  startYear.value = String(current.getFullYear());
  startMonth.value = String(current.getMonth() + 1);
  endYear.value = String(current.getFullYear());
  endMonth.value = String(current.getMonth() + 1);
  // 달력 보이는 월도 오늘로 이동 + 즉시 리렌더
  startPlaceholder.value = today(getLocalTimeZone());
  endPlaceholder.value = today(getLocalTimeZone());
  startCalendarKey.value++;
  endCalendarKey.value++;
  emit('update:modelValue', null);
  emit('change', null);
  isOpen.value = false;
}

// Props 변경 감지하여 내부 상태 업데이트
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue && newValue.start && newValue.end) {
      const startDateObj =
        typeof newValue.start === 'string' ? new Date(newValue.start) : newValue.start;
      const endDateObj = typeof newValue.end === 'string' ? new Date(newValue.end) : newValue.end;

      dateRange.value = { ...newValue };
      startDateValue.value = fromDate(startDateObj, getLocalTimeZone());
      endDateValue.value = fromDate(endDateObj, getLocalTimeZone());
      startPlaceholder.value = fromDate(startDateObj, getLocalTimeZone());
      endPlaceholder.value = fromDate(endDateObj, getLocalTimeZone());
      startYear.value = String(startDateObj.getFullYear());
      startMonth.value = String(startDateObj.getMonth() + 1);
      endYear.value = String(endDateObj.getFullYear());
      endMonth.value = String(endDateObj.getMonth() + 1);
    } else {
      dateRange.value = {};
      startDateValue.value = undefined;
      endDateValue.value = undefined;
      startPlaceholder.value = today(getLocalTimeZone());
      endPlaceholder.value = today(getLocalTimeZone());
    }
  },
  { immediate: true },
);

// Calendar 값 변경 시 placeholder도 선택된 달로 동기화
watch(
  [startDateValue, endDateValue],
  ([startVal, endVal]) => {
    if (startVal && startVal.toDate) {
      const s = startVal.toDate(getLocalTimeZone());
      startPlaceholder.value = fromDate(s, getLocalTimeZone());
      startYear.value = String(s.getFullYear());
      startMonth.value = String(s.getMonth() + 1);
    }
    if (endVal && endVal.toDate) {
      const e = endVal.toDate(getLocalTimeZone());
      endPlaceholder.value = fromDate(e, getLocalTimeZone());
      endYear.value = String(e.getFullYear());
      endMonth.value = String(e.getMonth() + 1);
    }
    if (startVal && endVal && startVal.toDate && endVal.toDate) {
      const startDateObj = startVal.toDate(getLocalTimeZone());
      const endDateObj = endVal.toDate(getLocalTimeZone());
      dateRange.value = { start: startDateObj, end: endDateObj };
    } else if (!startVal || !endVal) {
      dateRange.value = {};
    }
  },
  { deep: true },
);
</script>
