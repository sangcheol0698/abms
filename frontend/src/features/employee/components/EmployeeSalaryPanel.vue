<template>
  <div class="flex h-full flex-col gap-6">
    <Alert v-if="errorMessage" variant="destructive">
      <AlertTitle>연봉 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
      <div class="space-y-8 lg:col-span-2">
        <section class="rounded-xl border bg-card shadow-sm">
          <div class="flex items-center justify-between border-b px-6 py-4">
            <div class="flex items-center gap-3">
              <h3 class="text-base font-semibold text-foreground">현재 연봉 정보</h3>
              <Badge :variant="currentPayroll ? 'default' : 'secondary'">
                {{ currentPayroll ? statusLabel(currentPayroll.status) : '미등록' }}
              </Badge>
            </div>
            <Button v-if="showManagementActions" variant="outline" @click="isDialogOpen = true">
              연봉 수정
            </Button>
          </div>

          <div class="grid gap-6 px-6 py-6">
            <div>
              <p class="text-sm text-muted-foreground">연봉</p>
              <p class="mt-2 flex items-end gap-2 text-3xl font-bold tracking-tight text-foreground">
                <span>{{ currentPayroll ? formatCurrency(currentPayroll.annualSalary) : '등록되지 않음' }}</span>
                <span v-if="currentPayroll" class="pb-1 text-sm font-medium text-muted-foreground">
                  / Year
                </span>
              </p>
            </div>

            <div class="grid gap-6 border-t pt-6 sm:grid-cols-2">
              <div>
                <p class="text-sm text-muted-foreground">월 환산액</p>
                <p class="mt-2 text-lg font-semibold text-foreground">
                  {{ currentPayroll ? formatCurrency(currentPayroll.monthlySalary) : '—' }}
                </p>
              </div>
              <div>
                <p class="text-sm text-muted-foreground">적용 시작일</p>
                <p class="mt-2 text-lg font-semibold text-foreground">
                  {{ currentPayroll ? formatDate(currentPayroll.startDate) : '—' }}
                </p>
              </div>
            </div>
          </div>
        </section>

        <section class="space-y-4">
          <div class="flex items-center justify-between">
            <h3 class="text-base font-semibold text-foreground">연봉 변경 이력</h3>
          </div>

          <div v-if="isLoading" class="py-10 text-center text-sm text-muted-foreground">
            연봉 정보를 불러오는 중입니다...
          </div>

          <DataTable
            v-else
            :columns="columns"
            :data="payrollHistory"
            :table-instance="table"
            empty-message="등록된 연봉 이력이 없습니다."
            empty-description="연봉 변경이 발생하면 이력이 여기에 표시됩니다."
          />
        </section>
      </div>

      <aside class="space-y-8">
        <section class="rounded-xl border bg-card shadow-sm">
          <div class="border-b px-6 py-4">
            <h3 class="text-base font-semibold text-foreground">연봉 변경 가이드</h3>
          </div>
          <div class="space-y-4 px-6 py-6 text-sm leading-relaxed text-muted-foreground">
            <p>연봉 변경은 적용 시작일 기준으로 새 이력을 생성합니다.</p>
            <p>현재 연봉 시작일과 같거나 더 이른 날짜는 입력할 수 없습니다.</p>
            <p>변경 권한은 직원 관리 권한이 있는 사용자에게만 열립니다.</p>
          </div>
        </section>

      </aside>
    </div>

    <EmployeeSalaryChangeDialog
      :open="isDialogOpen"
      :employee-id="employeeId"
      @update:open="isDialogOpen = $event"
      @changed="handleChanged"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue';
import {
  type ColumnDef,
  getCoreRowModel,
  type RowSelectionState,
  useVueTable,
} from '@tanstack/vue-table';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { DataTable } from '@/components/business';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { valueUpdater } from '@/components/ui/table/utils';
import HttpError from '@/core/http/HttpError';
import { formatCurrency } from '@/features/project/models/projectListItem';
import type {
  EmployeePayrollHistoryItem,
  EmployeePayrollStatus,
} from '@/features/employee/models/payroll';
import {
  useEmployeeCurrentPayrollQuery,
  useEmployeePayrollHistoryQuery,
} from '@/features/employee/queries/useEmployeeQueries';
import EmployeeSalaryChangeDialog from '@/features/employee/components/EmployeeSalaryChangeDialog.vue';

interface Props {
  employeeId: number;
  showManagementActions?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showManagementActions: false,
});

const currentPayrollQuery = useEmployeeCurrentPayrollQuery(computed(() => props.employeeId));
const payrollHistoryQuery = useEmployeePayrollHistoryQuery(computed(() => props.employeeId));
const isDialogOpen = ref(false);

const isLoading = computed(
  () => currentPayrollQuery.isLoading.value || payrollHistoryQuery.isLoading.value,
);
const currentPayroll = computed(() => currentPayrollQuery.data.value ?? null);
const payrollHistory = computed(() => payrollHistoryQuery.data.value ?? []);
const rowSelection = ref<RowSelectionState>({});
const errorMessage = computed(() => {
  const error = currentPayrollQuery.error.value ?? payrollHistoryQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof HttpError ? error.message : '연봉 정보를 불러오는 중 오류가 발생했습니다.';
});

const columns: ColumnDef<EmployeePayrollHistoryItem>[] = [
  {
    id: 'select',
    header: ({ table }) =>
      h(Checkbox, {
        modelValue:
          table.getIsAllPageRowsSelected()
          || (table.getIsSomePageRowsSelected() && 'indeterminate'),
        'onUpdate:modelValue': (value: boolean | 'indeterminate') =>
          table.toggleAllPageRowsSelected(value === 'indeterminate' ? true : Boolean(value)),
        ariaLabel: '모두 선택',
      }),
    cell: ({ row }) =>
      h(Checkbox, {
        modelValue: row.getIsSelected(),
        'onUpdate:modelValue': (value: boolean | 'indeterminate') =>
          row.toggleSelected(value === 'indeterminate' ? true : Boolean(value)),
        ariaLabel: '행 선택',
      }),
    enableSorting: false,
    enableHiding: false,
    size: 28,
  },
  {
    accessorKey: 'startDate',
    header: () => h('span', '적용 시작일'),
    cell: ({ row }) => h('span', { class: 'text-sm' }, formatDate(row.original.startDate)),
    enableSorting: false,
    size: 130,
  },
  {
    accessorKey: 'endDate',
    header: () => h('span', '종료일'),
    cell: ({ row }) =>
      h(
        'span',
        { class: 'text-sm text-muted-foreground' },
        row.original.endDate
          ? formatDate(row.original.endDate)
          : row.original.status === 'CURRENT'
            ? '현재 적용 중'
            : '-',
      ),
    enableSorting: false,
    size: 130,
  },
  {
    accessorKey: 'annualSalary',
    header: () => h('div', { class: 'w-full text-right' }, '연봉'),
    cell: ({ row }) =>
      h('span', { class: 'block text-right text-sm font-medium' }, formatCurrency(row.original.annualSalary)),
    enableSorting: false,
    size: 140,
  },
  {
    accessorKey: 'monthlySalary',
    header: () => h('div', { class: 'w-full text-right' }, '월 환산액'),
    cell: ({ row }) =>
      h('span', { class: 'block text-right text-sm text-muted-foreground' }, formatCurrency(row.original.monthlySalary)),
    enableSorting: false,
    size: 140,
  },
  {
    accessorKey: 'status',
    header: () => h('span', '상태'),
    cell: ({ row }) =>
      h(Badge, { variant: row.original.status === 'CURRENT' ? 'default' : 'secondary' }, () =>
        statusLabel(row.original.status)),
    enableSorting: false,
    size: 120,
  },
];

const table = useVueTable({
  get data() {
    return payrollHistory.value;
  },
  columns,
  state: {
    get rowSelection() {
      return rowSelection.value;
    },
  },
  enableRowSelection: true,
  onRowSelectionChange: (updater) => valueUpdater(updater, rowSelection),
  getCoreRowModel: getCoreRowModel(),
});

async function handleChanged() {
  await Promise.all([
    currentPayrollQuery.refetch(),
    payrollHistoryQuery.refetch(),
  ]);
}

function formatDate(value?: string | null) {
  if (!value) {
    return '—';
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  const year = parsed.getFullYear();
  const month = String(parsed.getMonth() + 1).padStart(2, '0');
  const day = String(parsed.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function statusLabel(status: EmployeePayrollStatus) {
  if (status === 'CURRENT') {
    return '현재 적용';
  }
  if (status === 'SCHEDULED') {
    return '예정';
  }
  return '종료';
}
</script>
