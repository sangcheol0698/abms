<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="w-full max-w-2xl overflow-hidden">
      <DialogHeader>
        <DialogTitle>직원 선택</DialogTitle>
        <DialogDescription>프로젝트에 배정할 직원을 선택하세요.</DialogDescription>
      </DialogHeader>

      <div class="px-1">
        <Input
          v-model="searchQuery"
          placeholder="직원명으로 검색..."
          class="h-9"
          @keydown.enter.prevent="handleSearch"
        />
      </div>

      <div v-if="loading" class="flex items-center justify-center py-8">
        <p class="text-sm text-muted-foreground">로딩 중...</p>
      </div>

      <div v-else class="max-h-[55vh] overflow-y-auto rounded-md border">
        <table class="w-full">
          <thead class="sticky top-0 border-b bg-muted/50">
            <tr>
              <th class="px-4 py-2 text-left text-sm font-medium">이름</th>
              <th class="px-4 py-2 text-left text-sm font-medium">부서</th>
              <th class="px-4 py-2 text-left text-sm font-medium">직책</th>
              <th class="px-4 py-2 text-left text-sm font-medium">상태</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="employee in employees"
              :key="employee.employeeId"
              :class="[
                'cursor-pointer transition-colors hover:bg-muted/50',
                selectedEmployeeIdValue === employee.employeeId && 'bg-primary/10',
              ]"
              @click="handleSelect(employee)"
            >
              <td class="px-4 py-2 text-sm font-medium">{{ employee.name }}</td>
              <td class="px-4 py-2 text-sm">{{ employee.departmentName }}</td>
              <td class="px-4 py-2 text-sm">{{ employee.positionLabel }}</td>
              <td class="px-4 py-2 text-sm">{{ employee.statusLabel }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex items-center justify-between px-2 py-2">
        <p class="text-sm text-muted-foreground">
          전체 {{ totalElements }}개 중 {{ rangeStart }}-{{ rangeEnd }}개 표시
        </p>
        <div class="flex items-center gap-2">
          <Button variant="outline" size="sm" :disabled="page === 1" @click="handlePreviousPage">
            이전
          </Button>
          <span class="text-sm">{{ page }} / {{ totalPages }}</span>
          <Button variant="outline" size="sm" :disabled="page >= totalPages" @click="handleNextPage">
            다음
          </Button>
        </div>
      </div>

      <DialogFooter class="justify-between">
        <Button variant="outline" @click="$emit('update:open', false)">취소</Button>
        <Button :disabled="!selectedEmployeeValue" @click="confirmSelection">선택</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { toast } from 'vue-sonner';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useEmployeesQuery } from '@/features/employee/queries/useEmployeeQueries';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';

interface Props {
  open: boolean;
  selectedEmployeeId?: number;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (
    event: 'select',
    payload: {
      employeeId: number;
      employeeName: string;
      departmentId: number | null;
      departmentName: string | null;
    },
  ): void;
}>();

const selectedEmployeeIdValue = ref<number | undefined>();
const searchQuery = ref('');
const appliedSearchQuery = ref('');
const page = ref(1);
const pageSize = ref(5);
const searchParams = computed(() => ({
  page: page.value,
  size: pageSize.value,
  name: appliedSearchQuery.value || undefined,
  statuses: ['ACTIVE'],
}));
const employeesQuery = useEmployeesQuery(searchParams);
const loading = computed(() => employeesQuery.isLoading.value || employeesQuery.isFetching.value);
const employees = computed<EmployeeListItem[]>(() => employeesQuery.data.value?.content ?? []);
const totalPages = computed(() => Math.max(employeesQuery.data.value?.totalPages ?? 1, 1));
const totalElements = computed(() => employeesQuery.data.value?.totalElements ?? 0);
const rangeStart = computed(() => (totalElements.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1));
const rangeEnd = computed(() => Math.min(page.value * pageSize.value, totalElements.value));
const selectedEmployeeValue = computed(
  () => employees.value.find((employee) => employee.employeeId === selectedEmployeeIdValue.value) ?? null,
);

watch(
  () => props.open,
  (open) => {
    if (open) {
      selectedEmployeeIdValue.value = props.selectedEmployeeId;
      searchQuery.value = '';
      appliedSearchQuery.value = '';
      page.value = 1;
      return;
    }

    selectedEmployeeIdValue.value = undefined;
    searchQuery.value = '';
    appliedSearchQuery.value = '';
    page.value = 1;
  },
);

watch(
  () => employeesQuery.error.value,
  (error) => {
    if (!error || !props.open) {
      return;
    }
    console.warn('직원 목록을 불러오는 중 오류가 발생했습니다.', error);
    toast.error('직원 목록을 불러오지 못했습니다.');
  },
);

function handleSearch() {
  page.value = 1;
  appliedSearchQuery.value = searchQuery.value.trim();
}

function handlePreviousPage() {
  if (page.value > 1) {
    page.value--;
  }
}

function handleNextPage() {
  if (page.value < totalPages.value) {
    page.value++;
  }
}

function handleSelect(employee: EmployeeListItem) {
  selectedEmployeeIdValue.value = employee.employeeId;
}

function confirmSelection() {
  if (!selectedEmployeeValue.value) {
    toast.info('직원을 선택해 주세요.');
    return;
  }

  emit('select', {
    employeeId: selectedEmployeeValue.value.employeeId,
    employeeName: selectedEmployeeValue.value.name,
    departmentId: selectedEmployeeValue.value.departmentId,
    departmentName: selectedEmployeeValue.value.departmentName,
  });
  emit('update:open', false);
}
</script>
