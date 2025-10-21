<template>
  <section class="flex h-full flex-col gap-6">
    <EmployeeSummaryCards :cards="employeeSummary.cards" />

    <div class="space-y-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="이름을 입력하세요"
        searchColumnId="name"
        :getColumnLabel="getColumnLabel"
        :applySearchOnEnter="true"
      >
        <template #filters>
          <DataTableFacetedFilter
            v-if="table.getColumn('status')"
            :column="table.getColumn('status')"
            title="상태"
            :options="statusOptions"
          />
          <DataTableFacetedFilter
            v-if="table.getColumn('type')"
            :column="table.getColumn('type')"
            title="유형"
            :options="typeOptions"
          />
          <DataTableFacetedFilter
            v-if="table.getColumn('grade')"
            :column="table.getColumn('grade')"
            title="등급"
            :options="gradeOptions"
          />
          <DataTableFacetedFilter
            v-if="table.getColumn('position')"
            :column="table.getColumn('position')"
            title="직책"
            :options="positionOptions"
          />
          <DataTableFacetedFilter
            v-if="departmentOptions.length && table.getColumn('departmentId')"
            :column="table.getColumn('departmentId')"
            title="부서"
            :options="departmentOptions"
          />
        </template>

        <template #actions>
          <DropdownMenu>
            <DropdownMenuTrigger as-child>
              <Button variant="outline" size="sm" class="h-8 px-2 sm:px-3 gap-1">
                <FileSpreadsheet class="h-4 w-4" />
                <span class="hidden sm:inline">엑셀</span>
                <ChevronDown class="h-3 w-3" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" class="w-48">
              <DropdownMenuItem :disabled="isDownloadingExcel" @click="handleExcelDownload">
                <Download class="mr-2 h-4 w-4" />
                <span>{{ isDownloadingExcel ? '다운로드 중...' : '현재 조건 다운로드' }}</span>
              </DropdownMenuItem>
              <DropdownMenuItem :disabled="isDownloadingSample" @click="handleExcelSampleDownload">
                <Download class="mr-2 h-4 w-4" />
                <span>{{ isDownloadingSample ? '샘플 다운로드 중...' : '샘플 다운로드' }}</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem @click="openExcelUploadDialog">
                <Upload class="mr-2 h-4 w-4" />
                <span>엑셀 업로드</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button variant="default" size="sm" class="h-8 px-3 sm:px-4" @click="openCreateDialog">
            구성원 추가
          </Button>
        </template>
      </DataTableToolbar>

      <DataTable
        :key="tableRenderKey"
        :columns="columns"
        :data="employees"
        :tableInstance="table"
        :loading="isLoading"
        emptyMessage="구성원 정보를 찾을 수 없습니다"
        emptyDescription="검색어나 필터 조건을 조정해 다시 시도해 보세요"
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
  </section>
  <ExcelUploadDialog
    :open="isExcelUploadDialogOpen"
    :on-download-sample="downloadSampleExcel"
    :on-upload="uploadExcelFile"
    @update:open="isExcelUploadDialogOpen = $event"
    @success="handleExcelUploadSuccess"
    @error="handleExcelDialogError"
    @sample="handleExcelSampleSuccess"
  />
  <EmployeeCreateDialog
    :open="isEmployeeCreateDialogOpen"
    :department-options="departmentOptions"
    :grade-options="gradeOptions"
    :position-options="positionOptions"
    :type-options="typeOptions"
    @update:open="handleEmployeeCreateDialogOpenChange"
    @created="handleEmployeeCreated"
  />
  <EmployeeUpdateDialog
    :open="isEmployeeUpdateDialogOpen"
    :department-options="departmentOptions"
    :grade-options="gradeOptions"
    :position-options="positionOptions"
    :type-options="typeOptions"
    :employee="editingEmployee ?? undefined"
    @update:open="handleEmployeeUpdateDialogOpenChange"
    @updated="handleEmployeeUpdated"
  />
  <AlertDialog :open="deletion.isDialogOpen.value">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>구성원을 삭제할까요?</AlertDialogTitle>
        <AlertDialogDescription>{{ deletion.description.value }}</AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <AlertDialogCancel :disabled="deletion.isProcessing.value" @click="deletion.cancel">
          취소
        </AlertDialogCancel>
        <AlertDialogAction
          :disabled="deletion.isProcessing.value"
          @pointerdown.prevent
          @click="deletion.confirm"
        >
          삭제
        </AlertDialogAction>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>

<script setup lang="ts">
import { computed, h, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDebounceFn } from '@vueuse/core';
import {
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  type ColumnDef,
  type ColumnFiltersState,
  type RowSelectionState,
  type SortingState,
  type VisibilityState,
  useVueTable,
} from '@tanstack/vue-table';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DataTable,
  DataTableColumnHeader,
  DataTableFacetedFilter,
  DataTablePagination,
  DataTableToolbar,
  ExcelUploadDialog,
} from '@/components/business';
import { Button } from '@/components/ui/button';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { valueUpdater } from '@/components/ui/table/utils';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import OrganizationRepository from '@/features/organization/repository/OrganizationRepository';
import type { OrganizationChartNode } from '@/features/organization/models/organization';
import type {
  EmployeeListItem,
  EmployeeSearchParams,
} from '@/features/employee/models/employeeListItem';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import {
  getEmployeeGradeOptions,
  getEmployeePositionOptions,
  getEmployeeStatusOptions,
  getEmployeeTypeOptions,
} from '@/features/employee/models/employeeFilters';
import EmployeeSummaryCards from '@/features/employee/components/EmployeeSummaryCards.vue';
import { useEmployeeSummary } from '@/features/employee/composables';
import EmployeeCreateDialog from '@/features/employee/components/EmployeeCreateDialog.vue';
import EmployeeUpdateDialog from '@/features/employee/components/EmployeeUpdateDialog.vue';
import { ChevronDown, Download, FileSpreadsheet, Upload } from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import EmployeeRowActions from '@/features/employee/components/EmployeeRowActions.vue';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import HttpError from '@/core/http/HttpError';
import { useEmployeeDeletion } from '@/features/employee/composables/useEmployeeDeletion';
import { getExcelErrorMessage } from '@/core/utils/excel';

interface DepartmentOption {
  label: string;
  value: string;
}

const employeeRepository = appContainer.resolve(EmployeeRepository);
const organizationRepository = appContainer.resolve(OrganizationRepository);
const router = useRouter();
const route = useRoute();

let isApplyingRoute = false;
let isUpdatingRoute = false;

const employees = ref<EmployeeListItem[]>([]);
const tableRenderKey = ref(0);
const isLoading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);

const sorting = ref<SortingState>([]);
const columnFilters = ref<ColumnFiltersState>([]);
const columnVisibility = ref<VisibilityState>({});
const rowSelection = ref<RowSelectionState>({});

const departmentOptions = ref<DepartmentOption[]>([]);
const statusOptions = ref<EmployeeFilterOption[]>(getEmployeeStatusOptions());
const typeOptions = ref<EmployeeFilterOption[]>(getEmployeeTypeOptions());
const gradeOptions = ref<EmployeeFilterOption[]>(getEmployeeGradeOptions());
const positionOptions = ref<EmployeeFilterOption[]>(getEmployeePositionOptions());
const isEmployeeCreateDialogOpen = ref(false);
const isEmployeeUpdateDialogOpen = ref(false);
const editingEmployee = ref<EmployeeSummary | null>(null);
const isLoadingEmployee = ref(false);
const isExcelUploadDialogOpen = ref(false);
const isDownloadingExcel = ref(false);
const isDownloadingSample = ref(false);
const deletion = useEmployeeDeletion(async () => {
  await loadEmployees();
});

const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);

const employeeSummary = useEmployeeSummary({ employees });

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
    id: 'departmentName',
    accessorFn: (row) => row.departmentName,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '부서', align: 'left' }),
    cell: ({ row }) =>
      h(
        'button',
        {
          type: 'button',
          class:
            'cursor-pointer text-left text-sm text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
          onClick: (event: MouseEvent) => {
            event.stopPropagation();
            navigateToDepartment(row.original.departmentId);
          },
        },
        row.original.departmentName ?? '',
      ),
    enableSorting: false,
    size: 160,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'departmentId',
    accessorKey: 'departmentId',
    header: () => null,
    cell: () => null,
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.includes(row.original.departmentId);
    },
    enableSorting: false,
    enableHiding: false,
    size: 0,
    meta: { isFilterOnly: true },
  },
  {
    id: 'position',
    accessorFn: (row) => row.positionLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '직책', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, row.original.positionLabel ?? ''),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.positionCode);
    },
    enableSorting: false,
    size: 120,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'grade',
    accessorFn: (row) => row.gradeLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '등급', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, row.original.gradeLabel ?? ''),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.gradeCode);
    },
    enableSorting: false,
    size: 80,
    meta: { skeleton: 'text-short' },
  },
  {
    id: 'type',
    accessorFn: (row) => row.typeLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '유형', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-foreground' }, row.original.typeLabel ?? ''),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.typeCode);
    },
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
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.statusCode);
    },
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
    enableSorting: false,
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
    enableSorting: false,
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
    get columnFilters() {
      return columnFilters.value;
    },
    get columnVisibility() {
      return columnVisibility.value;
    },
    get rowSelection() {
      return rowSelection.value;
    },
  },
  onSortingChange: (updater) => valueUpdater(updater, sorting),
  onColumnFiltersChange: (updater) => valueUpdater(updater, columnFilters),
  onColumnVisibilityChange: (updater) => valueUpdater(updater, columnVisibility),
  onRowSelectionChange: (updater) => valueUpdater(updater, rowSelection),
  getCoreRowModel: getCoreRowModel(),
  getFilteredRowModel: getFilteredRowModel(),
  getFacetedRowModel: getFacetedRowModel(),
  getFacetedUniqueValues: getFacetedUniqueValues(),
});

let requestToken = 0;

async function loadDepartments() {
  try {
    const chart = await organizationRepository.fetchOrganizationChart();
    const map = new Map<string, string>();

    const traverse = (nodes: OrganizationChartNode[]) => {
      nodes.forEach((node) => {
        if (!map.has(node.departmentId)) {
          map.set(node.departmentId, node.departmentName);
        }
        if (Array.isArray(node.children) && node.children.length > 0) {
          traverse(node.children);
        }
      });
    };

    traverse(chart);
    departmentOptions.value = Array.from(map.entries()).map(([value, label]) => ({ value, label }));
  } catch (error) {
    console.error('부서 정보를 불러오지 못했습니다.', error);
    departmentOptions.value = [];
  }
}

async function loadEmployeeOptions() {
  try {
    const shouldFetchOptions =
      !getEmployeeStatusOptions().length ||
      !getEmployeeTypeOptions().length ||
      !getEmployeeGradeOptions().length ||
      !getEmployeePositionOptions().length;

    if (shouldFetchOptions) {
      await Promise.all([
        employeeRepository.fetchStatuses(),
        employeeRepository.fetchTypes(),
        employeeRepository.fetchGrades(),
        employeeRepository.fetchPositions(),
      ]);
    }

    statusOptions.value = getEmployeeStatusOptions();
    typeOptions.value = getEmployeeTypeOptions();
    gradeOptions.value = getEmployeeGradeOptions();
    positionOptions.value = getEmployeePositionOptions();

    if (employees.value.length && shouldFetchOptions) {
      loadEmployees();
    }
  } catch (error) {
    const message = error instanceof HttpError ? error.message : '필터 정보를 불러오지 못했습니다.';
    toast.error('필터 정보를 불러오지 못했습니다.', {
      description: message,
    });
  }
}

function openExcelUploadDialog() {
  isExcelUploadDialogOpen.value = true;
}

async function handleExcelDownload() {
  if (isDownloadingExcel.value) {
    return;
  }
  isDownloadingExcel.value = true;
  try {
    await employeeRepository.downloadExcel(buildSearchParams());
    toast.success('엑셀 파일을 다운로드했습니다.');
  } catch (error) {
    const message = getExcelErrorMessage(error);
    toast.error('엑셀 다운로드에 실패했습니다.', { description: message });
  } finally {
    isDownloadingExcel.value = false;
  }
}

async function downloadSampleExcel(): Promise<void> {
  await employeeRepository.downloadSampleExcel();
}

async function handleExcelSampleDownload() {
  if (isDownloadingSample.value) {
    return;
  }
  isDownloadingSample.value = true;
  try {
    await downloadSampleExcel();
    toast.success('샘플 파일을 다운로드했습니다.');
  } catch (error) {
    const message = getExcelErrorMessage(error);
    toast.error('샘플 다운로드에 실패했습니다.', { description: message });
  } finally {
    isDownloadingSample.value = false;
  }
}

async function uploadExcelFile(file: File, onProgress: (progress: number) => void) {
  await employeeRepository.uploadExcel(file, onProgress);
  await loadEmployees();
}

function handleExcelUploadSuccess() {
  toast.success('엑셀 업로드를 완료했습니다.');
}

function handleExcelDialogError(message: string) {
  toast.error('엑셀 작업에 실패했습니다.', { description: message });
}

function handleExcelSampleSuccess() {
  toast.success('샘플 파일을 다운로드했습니다.');
}

function toArray(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value.map((item) => String(item));
  }
  if (value === undefined || value === null) {
    return [];
  }
  return [String(value)];
}

function buildSearchParams(): EmployeeSearchParams {
  const params: EmployeeSearchParams = {
    page: page.value,
    size: pageSize.value,
  };

  const filterMap = new Map<string, unknown>(
    columnFilters.value.map((filter) => [filter.id, filter.value]),
  );
  const nameFilter = filterMap.get('name');

  if (typeof nameFilter === 'string' && nameFilter.trim().length > 0) {
    params.name = nameFilter.trim();
  }

  const statusFilter = toArray(filterMap.get('status'));
  if (statusFilter.length > 0) {
    params.statuses = statusFilter;
  }

  const typeFilter = toArray(filterMap.get('type'));
  if (typeFilter.length > 0) {
    params.types = typeFilter;
  }

  const gradeFilter = toArray(filterMap.get('grade'));
  if (gradeFilter.length > 0) {
    params.grades = gradeFilter;
  }

  const positionFilter = toArray(filterMap.get('position'));
  if (positionFilter.length > 0) {
    params.positions = positionFilter;
  }

  const departmentFilter = toArray(filterMap.get('departmentId'));
  if (departmentFilter.length > 0) {
    params.departmentIds = departmentFilter;
  }

  const sortState = sorting.value[0];
  if (sortState && sortState.id) {
    params.sort = `${sortState.id},${sortState.desc ? 'desc' : 'asc'}`;
  }

  return params;
}

function toSingleString(value: unknown): string | undefined {
  if (Array.isArray(value)) {
    const first = value.find((item) => item != null);
    return first !== undefined ? String(first) : undefined;
  }
  if (typeof value === 'string') {
    return value;
  }
  return undefined;
}

function parseQueryArray(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value
      .flatMap((item) => String(item).split(','))
      .map((item) => item.trim())
      .filter((item) => item.length > 0);
  }
  if (typeof value === 'string') {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);
  }
  return [];
}

function parsePositiveInt(value: unknown, fallback: number): number {
  const source = Array.isArray(value) ? value[0] : value;
  const parsed = Number(source);
  if (Number.isFinite(parsed) && parsed > 0) {
    return Math.floor(parsed);
  }
  return fallback;
}

function buildQueryFromState(): Record<string, string> {
  const params = buildSearchParams();
  const query: Record<string, string> = {
    page: String(params.page),
    size: String(params.size),
  };

  if (params.name) {
    query.name = params.name;
  }
  if (params.statuses?.length) {
    query.statuses = params.statuses.join(',');
  }
  if (params.types?.length) {
    query.types = params.types.join(',');
  }
  if (params.grades?.length) {
    query.grades = params.grades.join(',');
  }
  if (params.positions?.length) {
    query.positions = params.positions.join(',');
  }
  if (params.departmentIds?.length) {
    query.departmentIds = params.departmentIds.join(',');
  }
  if (params.sort) {
    query.sort = params.sort;
  }

  return query;
}

function getRouteQueryRecord(): Record<string, string> {
  const record: Record<string, string> = {};
  Object.entries(route.query).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      const joined = value.join(',');
      if (joined) {
        record[key] = joined;
      }
    } else if (value !== null && value !== undefined) {
      const stringified = String(value);
      if (stringified.length > 0) {
        record[key] = stringified;
      }
    }
  });
  return record;
}

function queriesEqual(a: Record<string, string>, b: Record<string, string>): boolean {
  const keys = new Set([...Object.keys(a), ...Object.keys(b)]);
  for (const key of keys) {
    if ((a[key] ?? '') !== (b[key] ?? '')) {
      return false;
    }
  }
  return true;
}

function updateRouteFromState() {
  if (isApplyingRoute) {
    return;
  }
  const nextQuery = buildQueryFromState();
  const currentQuery = getRouteQueryRecord();

  if (queriesEqual(nextQuery, currentQuery)) {
    return;
  }

  isUpdatingRoute = true;
  router.replace({ query: nextQuery }).finally(() => {
    isUpdatingRoute = false;
  });
}

function applyRouteQuery(rawQuery: Record<string, unknown>) {
  isApplyingRoute = true;

  const nextPage = parsePositiveInt(rawQuery.page, page.value);
  const nextPageSize = parsePositiveInt(rawQuery.size, pageSize.value);
  const sortValue = toSingleString(rawQuery.sort);

  pageSize.value = nextPageSize;
  page.value = nextPage;

  if (sortValue) {
    const [columnId, direction = 'asc'] = sortValue.split(',');
    if (columnId) {
      sorting.value = [
        {
          id: columnId,
          desc: direction.toLowerCase() === 'desc',
        },
      ];
    } else {
      sorting.value = [];
    }
  } else {
    sorting.value = [];
  }

  const filters: ColumnFiltersState = [];
  const name = toSingleString(rawQuery.name);
  if (name) {
    filters.push({ id: 'name', value: name });
  }

  const statuses = parseQueryArray(rawQuery.statuses);
  if (statuses.length) {
    filters.push({ id: 'status', value: statuses });
  }

  const types = parseQueryArray(rawQuery.types);
  if (types.length) {
    filters.push({ id: 'type', value: types });
  }

  const grades = parseQueryArray(rawQuery.grades);
  if (grades.length) {
    filters.push({ id: 'grade', value: grades });
  }

  const positions = parseQueryArray(rawQuery.positions);
  if (positions.length) {
    filters.push({ id: 'position', value: positions });
  }

  const departments = parseQueryArray(rawQuery.departmentIds);
  if (departments.length) {
    filters.push({ id: 'departmentId', value: departments });
  }

  columnFilters.value = filters;

  nextTick(() => {
    isApplyingRoute = false;
    updateRouteFromState();
    loadEmployees();
  });
}

async function loadEmployees() {
  const token = ++requestToken;
  isLoading.value = true;

  try {
    const params = buildSearchParams();
    const response = await employeeRepository.search(params);

    if (token !== requestToken) {
      return;
    }

    if (response.totalPages > 0 && page.value > response.totalPages) {
      page.value = response.totalPages;
      return;
    }

    employees.value = response.content;
    totalElements.value = response.totalElements;
    totalPages.value = Math.max(response.totalPages, 1);
    rowSelection.value = {};
    tableRenderKey.value += 1;
  } catch (error) {
    console.error('구성원 목록을 불러오지 못했습니다.', error);
    employees.value = [];
    totalElements.value = 0;
    totalPages.value = 1;
  } finally {
    if (token === requestToken) {
      isLoading.value = false;
    }
  }
}

const triggerFilterFetch = useDebounceFn(() => {
  loadEmployees();
}, 300);

watch(
  columnFilters,
  () => {
    if (isApplyingRoute) {
      return;
    }
    if (page.value !== 1) {
      page.value = 1;
    } else {
      updateRouteFromState();
      triggerFilterFetch();
    }
  },
  { deep: true },
);

watch(
  sorting,
  () => {
    if (isApplyingRoute) {
      return;
    }
    if (page.value !== 1) {
      page.value = 1;
    } else {
      updateRouteFromState();
      loadEmployees();
    }
  },
  { deep: true },
);

watch(page, (next, previous) => {
  if (isApplyingRoute) {
    return;
  }
  if (next === previous) {
    return;
  }
  updateRouteFromState();
  loadEmployees();
});

watch(pageSize, (next, previous) => {
  if (isApplyingRoute) {
    return;
  }
  if (next === previous) {
    return;
  }
  if (page.value !== 1) {
    page.value = 1;
  } else {
    updateRouteFromState();
    loadEmployees();
  }
});

watch(
  () => route.query,
  (newQuery) => {
    if (isUpdatingRoute) {
      return;
    }
    const currentStateQuery = buildQueryFromState();
    const incomingQuery = Object.entries(newQuery).reduce<Record<string, string>>(
      (accumulator, [key, value]) => {
        if (Array.isArray(value)) {
          const joined = value.join(',');
          if (joined) {
            accumulator[key] = joined;
          }
        } else if (value !== null && value !== undefined) {
          const stringified = String(value);
          if (stringified.length > 0) {
            accumulator[key] = stringified;
          }
        }
        return accumulator;
      },
      {},
    );

    if (queriesEqual(currentStateQuery, incomingQuery)) {
      return;
    }

    applyRouteQuery({ ...newQuery });
  },
  { deep: true },
);

function handlePageChange(nextPage: number) {
  if (nextPage < 1) {
    return;
  }
  if (nextPage === page.value) {
    return;
  }
  page.value = nextPage;
}

function handlePageSizeChange(nextSize: number) {
  if (nextSize === pageSize.value) {
    return;
  }
  pageSize.value = nextSize;
}

function openCreateDialog() {
  editingEmployee.value = null;
  isEmployeeCreateDialogOpen.value = true;
}

function handleEmployeeCreated() {
  isEmployeeCreateDialogOpen.value = false;
  if (page.value !== 1) {
    page.value = 1;
    return;
  }
  updateRouteFromState();
  loadEmployees();
}

function handleEmployeeUpdated() {
  isEmployeeUpdateDialogOpen.value = false;
  loadEmployees();
}

async function handleEditEmployee(row: EmployeeListItem) {
  editingEmployee.value = null;
  isLoadingEmployee.value = true;
  const loadingToast = toast.loading('구성원 정보를 불러오는 중입니다.');
  try {
    const detail = await employeeRepository.findById(row.employeeId);
    editingEmployee.value = detail;
    isEmployeeUpdateDialogOpen.value = true;
    toast.dismiss(loadingToast);
  } catch (error) {
    toast.dismiss(loadingToast);
    const message =
      error instanceof HttpError ? error.message : '구성원 정보를 불러오지 못했습니다.';
    toast.error('구성원 정보를 불러오지 못했습니다.', {
      description: message,
    });
  } finally {
    isLoadingEmployee.value = false;
  }
}

async function handleCopyEmail(row: EmployeeListItem) {
  try {
    await navigator.clipboard.writeText(row.email);
    toast.success('이메일을 복사했어요.', {
      description: row.email,
    });
  } catch (error) {
    console.warn('클립보드에 이메일을 복사하지 못했습니다.', error);
    toast.error('이메일을 복사하지 못했습니다.', {
      description: '브라우저 클립보드 권한을 확인해 주세요.',
    });
  }
}

function handleDeleteEmployee(row: EmployeeListItem) {
  deletion.open(row.employeeId, row.name);
}

function handleViewEmployee(row: EmployeeListItem) {
  router.push({ name: 'employee-detail', params: { employeeId: row.employeeId } }).catch(() => {
    /* 라우팅 오류는 무시 */
  });
}

function navigateToDepartment(departmentId?: string) {
  if (!departmentId) {
    return;
  }
  router.push({ name: 'organization', query: { departmentId } }).catch(() => {
    /* 라우팅 오류는 무시 */
  });
}

function handleEmployeeCreateDialogOpenChange(value: boolean) {
  isEmployeeCreateDialogOpen.value = value;
}

function handleEmployeeUpdateDialogOpenChange(value: boolean) {
  isEmployeeUpdateDialogOpen.value = value;
  if (!value && !isLoadingEmployee.value) {
    editingEmployee.value = null;
  }
}

function formatDisplayDate(value?: string | null) {
  if (!value) {
    return '-';
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleDateString('ko-KR');
}

const columnLabelMap: Record<string, string> = {
  select: '선택',
  name: '이름',
  departmentName: '부서',
  departmentId: '부서',
  position: '직책',
  grade: '등급',
  type: '유형',
  status: '상태',
  joinDate: '입사일',
  birthDate: '생년월일',
  actions: '동작',
};

function getColumnLabel(columnId: string): string {
  return columnLabelMap[columnId] ?? columnId;
}

onMounted(async () => {
  loadDepartments();
  await loadEmployeeOptions();
  if (Object.keys(route.query).length > 0) {
    applyRouteQuery({ ...route.query });
  } else {
    updateRouteFromState();
    await loadEmployees();
  }
});
</script>
