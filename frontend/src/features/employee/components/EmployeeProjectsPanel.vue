<template>
  <div class="flex h-full flex-col gap-6">
    <Alert v-if="errorMessage" variant="destructive">
      <AlertTitle>프로젝트 참여 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <div class="space-y-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="프로젝트명 또는 코드를 입력하세요"
        searchColumnId="projectName"
        :getColumnLabel="getColumnLabel"
        :applySearchOnEnter="true"
      >
        <template #filters>
          <DataTableFacetedFilter
            v-if="table.getColumn('assignmentStatus')"
            :column="table.getColumn('assignmentStatus')"
            title="배정 상태"
            :options="assignmentStatusFilterOptions"
          />
          <DataTableFacetedFilter
            v-if="table.getColumn('projectStatus')"
            :column="table.getColumn('projectStatus')"
            title="프로젝트 상태"
            :options="projectStatusFilterOptions"
          />
        </template>
      </DataTableToolbar>

      <div v-if="isLoading" class="py-10 text-center text-sm text-muted-foreground">
        프로젝트 참여 정보를 불러오는 중입니다...
      </div>

      <DataTable
        v-else
        :columns="columns"
        :data="projects"
        :table-instance="table"
        empty-message="참여한 프로젝트가 없습니다."
        empty-description="프로젝트에 배정되면 여기에 이력이 표시됩니다."
      />

      <DataTablePagination
        :page="page"
        :page-size="pageSize"
        :total-pages="totalPages"
        :total-elements="totalElements"
        :selected-row-count="selectedRowCount"
        @pageChange="handlePageChange"
        @pageSizeChange="handlePageSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, ref, toRef } from 'vue';
import {
  type ColumnFiltersState,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  type ColumnDef,
  type RowSelectionState,
  useVueTable,
} from '@tanstack/vue-table';
import { useRouter } from 'vue-router';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import {
  DataTable,
  DataTableFacetedFilter,
  DataTablePagination,
  DataTableToolbar,
} from '@/components/business';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import { valueUpdater } from '@/components/ui/table/utils';
import HttpError from '@/core/http/HttpError';
import type { EmployeeProjectAssignmentStatus, EmployeeProjectItem } from '@/features/employee/models/project';
import { useEmployeeProjectsQuery } from '@/features/employee/queries/useEmployeeQueries';
import { useEmployeeProjectsQuerySync } from '@/features/employee/composables/useEmployeeProjectsQuerySync';
import { useProjectStatusesQuery } from '@/features/project/queries/useProjectQueries';
import { canReadParties } from '@/features/party/permissions';

interface Props {
  employeeId: number;
}

const props = defineProps<Props>();
const router = useRouter();
const employeeId = toRef(props, 'employeeId');
const page = ref(1);
const pageSize = ref(10);
const columnFilters = ref<ColumnFiltersState>([]);

useEmployeeProjectsQuerySync({
  page,
  pageSize,
  columnFilters,
});

const projectSearchParams = computed(() => getSearchParams());

const projectsQuery = useEmployeeProjectsQuery(employeeId, projectSearchParams);
const projectStatusesQuery = useProjectStatusesQuery();

const isLoading = computed(() => projectsQuery.isLoading.value);
const projectsPage = computed(() => projectsQuery.data.value);
const projects = computed(() => projectsPage.value?.content ?? []);
const rowSelection = ref<RowSelectionState>({});
const totalPages = computed(() => projectsPage.value?.totalPages ?? 0);
const totalElements = computed(() => projectsPage.value?.totalElements ?? 0);
const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);
const assignmentStatusFilterOptions = computed(() => [
  { value: 'CURRENT', label: '현재 참여' },
  { value: 'SCHEDULED', label: '예정' },
  { value: 'ENDED', label: '종료' },
]);
const projectStatusFilterOptions = computed(() => projectStatusesQuery.data.value ?? []);
const errorMessage = computed(() => {
  const error = projectsQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof HttpError ? error.message : '프로젝트 참여 정보를 불러오는 중 오류가 발생했습니다.';
});

const columns: ColumnDef<EmployeeProjectItem>[] = [
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
    id: 'projectName',
    accessorFn: (row) => row.projectName,
    header: () => h('span', '프로젝트명'),
    cell: ({ row }) =>
      h(
        'button',
        {
          type: 'button',
          class:
            'cursor-pointer text-left font-medium text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
          onClick: () => router.push(`/projects/${row.original.projectId}`),
        },
        row.original.projectName,
      ),
    size: 220,
  },
  {
    accessorKey: 'role',
    header: () => h('span', '역할'),
    cell: ({ row }) => h('span', { class: 'text-sm text-foreground' }, row.original.role ?? '-'),
    size: 120,
  },
  {
    accessorKey: 'assignmentPeriod',
    header: () => h('span', '배정 기간'),
    cell: ({ row }) =>
      h(
        'span',
        { class: 'text-sm text-muted-foreground' },
        `${formatDate(row.original.assignmentStartDate)} ~ ${row.original.assignmentEndDate ? formatDate(row.original.assignmentEndDate) : '진행 중'}`,
      ),
    size: 180,
  },
  {
    id: 'assignmentStatus',
    accessorFn: (row) => assignmentStatusLabel(row.assignmentStatus),
    header: () => h('span', '상태'),
    cell: ({ row }) =>
      h(
        Badge,
        { variant: row.original.assignmentStatus === 'CURRENT' ? 'default' : 'secondary' },
        () => assignmentStatusLabel(row.original.assignmentStatus),
      ),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.assignmentStatus);
    },
    size: 120,
  },
  {
    id: 'projectStatus',
    accessorFn: (row) => row.projectStatusLabel,
    header: () => h('span', '프로젝트 상태'),
    cell: ({ row }) => h('span', { class: 'text-sm text-foreground' }, row.original.projectStatusLabel),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.projectStatus);
    },
    size: 140,
  },
  {
    accessorKey: 'leadDepartmentName',
    header: () => h('span', '주관 부서'),
    cell: ({ row }) => {
      const departmentId = row.original.leadDepartmentId;
      const departmentName = row.original.leadDepartmentName;

      if (departmentId == null || !departmentName) {
        return h('span', { class: 'text-sm text-foreground' }, '-');
      }

      return h(
        'button',
        {
          type: 'button',
          class:
            'cursor-pointer text-left text-sm text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
          onClick: () => handleDepartmentClick(departmentId),
        },
        departmentName,
      );
    },
    size: 140,
  },
  {
    accessorKey: 'partyName',
    header: () => h('span', '협력사'),
    cell: ({ row }) => {
      const partyId = row.original.partyId;
      const partyName = row.original.partyName;

      if (!canReadParties() || partyId == null || !partyName) {
        return h('span', { class: 'text-sm text-foreground' }, partyName || '-');
      }

      return h(
        'button',
        {
          type: 'button',
          class:
            'cursor-pointer text-left text-sm text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
          onClick: () => handlePartyClick(partyId),
        },
        partyName,
      );
    },
    size: 140,
  },
];

const table = useVueTable({
  get data() {
    return projects.value;
  },
  columns,
  manualPagination: true,
  manualFiltering: true,
  state: {
    get rowSelection() {
      return rowSelection.value;
    },
    get columnFilters() {
      return columnFilters.value;
    },
  },
  enableRowSelection: true,
  onRowSelectionChange: (updater) => valueUpdater(updater, rowSelection),
  onColumnFiltersChange: (updater) => valueUpdater(updater, columnFilters),
  getCoreRowModel: getCoreRowModel(),
  getFilteredRowModel: getFilteredRowModel(),
  getFacetedRowModel: getFacetedRowModel(),
  getFacetedUniqueValues: getFacetedUniqueValues(),
});

function handlePageChange(nextPage: number) {
  page.value = nextPage;
}

function handlePageSizeChange(nextPageSize: number) {
  pageSize.value = nextPageSize;
  page.value = 1;
}

function getSearchParams() {
  const filterMap = new Map<string, unknown>(
    columnFilters.value.map((filter) => [filter.id, filter.value]),
  );

  const params: {
    page: number;
    size: number;
    name?: string;
    assignmentStatuses?: string[];
    projectStatuses?: string[];
  } = {
    page: page.value,
    size: pageSize.value,
  };

  const nameFilter = filterMap.get('projectName');
  if (typeof nameFilter === 'string' && nameFilter.trim().length > 0) {
    params.name = nameFilter.trim();
  }

  const assignmentStatusFilter = toArray(filterMap.get('assignmentStatus'));
  if (assignmentStatusFilter.length > 0) {
    params.assignmentStatuses = assignmentStatusFilter;
  }

  const projectStatusFilter = toArray(filterMap.get('projectStatus'));
  if (projectStatusFilter.length > 0) {
    params.projectStatuses = projectStatusFilter;
  }

  return params;
}

function toArray(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value.filter((item): item is string => typeof item === 'string');
  }
  if (typeof value === 'string') {
    return [value];
  }
  return [];
}

function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    projectName: '프로젝트명',
    assignmentStatus: '배정 상태',
    projectStatus: '프로젝트 상태',
    role: '역할',
    assignmentPeriod: '배정 기간',
    leadDepartmentName: '주관 부서',
    partyName: '협력사',
  };
  return labels[columnId] ?? columnId;
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

function assignmentStatusLabel(status: EmployeeProjectAssignmentStatus) {
  if (status === 'CURRENT') {
    return '현재 참여';
  }
  if (status === 'SCHEDULED') {
    return '예정';
  }
  return '종료';
}

function handleDepartmentClick(departmentId: number) {
  router.push({
    path: `/departments/${departmentId}`,
  });
}

function handlePartyClick(partyId: number) {
  if (!canReadParties()) {
    return;
  }
  router.push({
    path: `/parties/${partyId}`,
  });
}
</script>
