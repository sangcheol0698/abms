<template>
  <div class="flex flex-col gap-4">
    <!-- Search Bar -->
   <div class="flex items-center gap-2">
      <Input
        v-model="searchName"
        placeholder="이름으로 검색..."
        class="max-w-sm"
        @update:model-value="handleSearchInputChange"
      />
    </div>

    <div class="space-y-4">
      <DataTable
        :columns="columns"
        :data="employees"
        :tableInstance="table"
        :loading="isLoading"
        emptyMessage="구성원 정보를 찾을 수 없습니다"
        emptyDescription="아직 이 부서에 배정된 직원이 없습니다."
        :pageSize="pageSize"
      />

      <DataTablePagination
        :page="page"
        :pageSize="pageSize"
        :totalPages="totalPages"
        :totalElements="totalElements"
        :selectedRowCount="selectedRowCount"
        @pageChange="handlePageChange"
        @pageSizeChange="handlePageSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, ref, watch } from 'vue';
import { useDebounceFn } from '@vueuse/core';
import {
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  type ColumnDef,
  type RowSelectionState,
  type SortingState,
  type VisibilityState,
  useVueTable,
} from '@tanstack/vue-table';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import {
  DataTable,
  DataTableColumnHeader,
  DataTablePagination,
} from '@/components/business';
import { appContainer } from '@/core/di/container';
import OrganizationRepository from '@/features/organization/repository/OrganizationRepository';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';
import { useRouter } from 'vue-router';
import { valueUpdater } from '@/components/ui/table/utils';
import EmployeeRowActions from '@/features/employee/components/EmployeeRowActions.vue';
import { toast } from 'vue-sonner';

const props = defineProps<{
  departmentId: string;
}>();

const repository = appContainer.resolve(OrganizationRepository);
const router = useRouter();

const employees = ref<EmployeeListItem[]>([]);
const isLoading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);
const searchName = ref('');

const sorting = ref<SortingState>([]);
const columnVisibility = ref<VisibilityState>({});
const rowSelection = ref<RowSelectionState>({});

const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);

function formatDisplayDate(dateInput?: string | null): string {
  if (!dateInput || dateInput.trim() === '') {
    return '';
  }
  const normalized = dateInput.replace(/\./g, '-');
  const [year, month, day] = normalized.split('-');
  if (!year || !month || !day) {
    return dateInput;
  }
  return `${year}.${month.padStart(2, '0')}.${day.padStart(2, '0')}`;
}

function handleViewEmployee(employee: EmployeeListItem) {
  router.push({
    name: 'employee-detail',
    params: { employeeId: employee.employeeId },
  });
}

async function handleEditEmployee(employee: EmployeeListItem) {
  // Placeholder for edit functionality
  console.log('Edit employee:', employee);
}

function handleCopyEmail(employee: EmployeeListItem) {
  if (!employee.email) {
    toast.error('이메일 정보가 없습니다.');
    return;
  }
  navigator.clipboard.writeText(employee.email).then(() => {
    toast.success('이메일을 클립보드에 복사했습니다.');
  }).catch(() => {
    toast.error('이메일 복사에 실패했습니다.');
  });
}

async function handleDeleteEmployee(employee: EmployeeListItem) {
  // Placeholder for delete functionality
  console.log('Delete employee:', employee);
}

const columns: ColumnDef<EmployeeListItem>[] = [
  {
    id: 'select',
    header: ({ table }) =>
      h(Checkbox, {
        modelValue:
          table.getIsAllPageRowsSelected() ||
          (table.getIsSomePageRowsSelected() && 'indeterminate'),
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
    size: 48,
  },
  {
    accessorKey: 'name',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '이름', align: 'left' }),
    cell: ({ row }) => {
      const name = row.original.name ?? '';
      const email = row.original.email ?? '';
      const initials = name.trim().slice(0, 2).toUpperCase() || '??';
      return h('div', { class: 'flex items-center gap-3' }, [
        h(Avatar, { class: 'h-10 w-10 rounded-xl border border-border/60 bg-background' }, () => [
          h(AvatarImage, {
            src: row.original.avatarImageUrl,
            alt: name,
          }),
          h(AvatarFallback, { class: 'rounded-xl text-sm font-semibold' }, () => initials),
        ]),
        h('div', { class: 'flex flex-col gap-0.5' }, [
          h(
            'button',
            {
              type: 'button',
              class:
                'cursor-pointer text-left font-medium text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
              onClick: (event: MouseEvent) => {
                event.stopPropagation();
                handleViewEmployee(row.original);
              },
            },
            name,
          ),
          h('span', { class: 'text-xs text-muted-foreground' }, email),
        ]),
      ]);
    },
    enableSorting: true,
    size: 260,
    meta: { skeleton: 'title-subtitle' },
  },
 {
    id: 'position',
    accessorFn: (row) => row.positionLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '직책', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, row.original.positionLabel ?? ''),
    enableSorting: true,
    size: 120,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'grade',
    accessorFn: (row) => row.gradeLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '등급', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, row.original.gradeLabel ?? ''),
    enableSorting: true,
    size: 80,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'type',
    accessorFn: (row) => row.typeLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '유형', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, row.original.typeLabel ?? ''),
    enableSorting: false,
    size: 80,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'status',
    accessorFn: (row) => row.statusLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '상태', align: 'left' }),
    cell: ({ row }) =>
      h(Badge, { variant: 'outline', class: 'font-medium' }, () => row.original.statusLabel ?? ''),
    enableSorting: false,
    size: 90,
    meta: { skeleton: 'enum-badge' },
  },
  {
    id: 'birthDate',
    accessorFn: (row) => row.birthDate ?? '',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '생년월일', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, formatDisplayDate(row.original.birthDate)),
    enableSorting: true,
    enableHiding: true,
    size: 120,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'joinDate',
    accessorFn: (row) => row.joinDate ?? '',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '입사일', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, formatDisplayDate(row.original.joinDate)),
    enableSorting: true,
    enableHiding: true,
    size: 120,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'actions',
    header: () => null,
    cell: ({ row }) =>
      h(EmployeeRowActions, {
        row: row.original,
        onEdit: () => handleEditEmployee(row.original),
        onCopyEmail: () => handleCopyEmail(row.original),
        onDelete: () => handleDeleteEmployee(row.original),
      }),
    enableSorting: false,
    enableHiding: false,
    size: 56,
  },
];

const table = useVueTable({
  data: computed(() => employees.value),
  columns,
  manualPagination: true,
  manualFiltering: true,
  manualSorting: true,
  enableRowSelection: true,
  state: {
    get sorting() {
      return sorting.value;
    },
    get columnVisibility() {
      return columnVisibility.value;
    },
    get rowSelection() {
      return rowSelection.value;
    },
  },
  onSortingChange: (updater) => valueUpdater(updater, sorting),
  onColumnVisibilityChange: (updater) => valueUpdater(updater, columnVisibility),
  onRowSelectionChange: (updater) => valueUpdater(updater, rowSelection),
  getCoreRowModel: getCoreRowModel(),
  getFilteredRowModel: getFilteredRowModel(),
  getFacetedRowModel: getFacetedRowModel(),
  getFacetedUniqueValues: getFacetedUniqueValues(),
});

// Build sort param for API
function buildSortParam(): string | undefined {
  if (sorting.value.length === 0) {
    return undefined;
  }
  
  const sort = sorting.value[0];
  if (!sort) {
    return undefined;
  }
  const direction = sort.desc ? 'desc' : 'asc';
  return `${sort.id},${direction}`;
}

async function loadEmployees() {
  if (!props.departmentId) return;
  
  isLoading.value = true;
  try {
    const name = searchName.value.trim() || undefined;
    const sort = buildSortParam();
    
    const response = await repository.fetchDepartmentEmployees(props.departmentId, {
      page: page.value,
      size: pageSize.value,
      name,
      sort,
    });
    
    employees.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
    rowSelection.value = {};
  } catch (error) {
    console.error('부서 직원 목록을 불러오지 못했습니다.', error);
    employees.value = [];
    totalElements.value = 0;
    totalPages.value = 1;
  } finally {
    isLoading.value = false;
  }
}

const debouncedLoadEmployees = useDebounceFn(() => {
  loadEmployees();
}, 300);

function handleSearchInputChange() {
  page.value = 1;
  debouncedLoadEmployees();
}

function handlePageChange(newPage: number) {
  page.value = newPage;
  loadEmployees();
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize;
  page.value = 1;
  loadEmployees();
}

// Watch for department change
watch(
  () => props.departmentId,
  () => {
    page.value = 1;
    searchName.value = '';
    sorting.value = [];
    loadEmployees();
  },
  { immediate: true }
);

// Watch for sorting change
watch(
  sorting,
  () => {
    page.value = 1;
    loadEmployees();
  }
);
</script>

