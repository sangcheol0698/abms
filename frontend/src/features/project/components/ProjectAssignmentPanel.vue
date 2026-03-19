<template>
  <div class="flex h-full flex-col gap-6">
    <Alert v-if="errorMessage" variant="destructive">
      <AlertTitle>투입 인력 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <div class="space-y-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="직원명을 입력하세요"
        searchColumnId="employeeName"
        :getColumnLabel="getColumnLabel"
      >
        <template #filters>
          <DataTableFacetedFilter
            v-if="table.getColumn('assignmentStatus')"
            :column="table.getColumn('assignmentStatus')"
            title="상태"
            :options="statusFilterOptions"
          />
          <DataTableFacetedFilter
            v-if="table.getColumn('role')"
            :column="table.getColumn('role')"
            title="역할"
            :options="roleFilterOptions"
          />
        </template>

        <template #actions>
          <Button v-if="canManageAssignments" size="sm" @click="openCreateDialog">
            투입 인력 추가
          </Button>
        </template>
      </DataTableToolbar>

      <div v-if="isLoading" class="py-10 text-center text-sm text-muted-foreground">
        투입 인력 정보를 불러오는 중입니다...
      </div>

      <template v-else>
        <DataTable
          :columns="columns"
          :data="assignments"
          :table-instance="table"
          empty-message="등록된 투입 인력이 없습니다."
          empty-description="투입 인력을 추가하면 여기에 이력이 표시됩니다."
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
      </template>
    </div>
  </div>

  <ProjectAssignmentFormDialog
    :open="isFormDialogOpen"
    :project-id="project?.projectId ?? 0"
    :project-start-date="project?.startDate ?? ''"
    :project-end-date="project?.endDate ?? null"
    :assignment="editingAssignment"
    @update:open="handleFormDialogOpenChange"
    @saved="handleSaved"
  />

  <ProjectAssignmentEndDialog
    :open="isEndDialogOpen"
    :project-id="project?.projectId ?? 0"
    :project-end-date="project?.endDate ?? null"
    :assignment="endingAssignment"
    @update:open="handleEndDialogOpenChange"
    @ended="handleEnded"
  />
</template>

<script setup lang="ts">
import { computed, h, ref, toRef } from 'vue';
import {
  type ColumnDef,
  type ColumnFiltersState,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  type RowSelectionState,
  useVueTable,
} from '@tanstack/vue-table';
import { useRouter } from 'vue-router';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  DataTable,
  DataTableFacetedFilter,
  DataTablePagination,
  DataTableToolbar,
} from '@/components/business';
import { valueUpdater } from '@/components/ui/table/utils';
import HttpError from '@/core/http/HttpError';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import {
  type ProjectAssignmentItem,
  type ProjectAssignmentSearchParams,
  type ProjectAssignmentStatus,
  projectAssignmentRoleOptions,
} from '@/features/project/models/projectAssignment';
import { useProjectAssignmentsQuery } from '@/features/project/queries/useProjectQueries';
import { useProjectAssignmentQuerySync } from '@/features/project/composables/useProjectAssignmentQuerySync';
import ProjectAssignmentEndDialog from '@/features/project/components/ProjectAssignmentEndDialog.vue';
import ProjectAssignmentFormDialog from '@/features/project/components/ProjectAssignmentFormDialog.vue';
import ProjectAssignmentRowActions from '@/features/project/components/ProjectAssignmentRowActions.vue';
import { canManageProjects } from '@/features/project/permissions';
import { canReadEmployeeDetail } from '@/features/employee/permissions';

interface Props {
  project: ProjectDetail | null;
}

const props = defineProps<Props>();
const router = useRouter();
const projectId = computed(() => props.project?.projectId ?? 0);
const page = ref(1);
const pageSize = ref(10);
const rowSelection = ref<RowSelectionState>({});
const columnFilters = ref<ColumnFiltersState>([]);
const isFormDialogOpen = ref(false);
const isEndDialogOpen = ref(false);
const editingAssignment = ref<ProjectAssignmentItem | null>(null);
const endingAssignment = ref<ProjectAssignmentItem | null>(null);
useProjectAssignmentQuerySync({ page, pageSize, columnFilters });
const projectAssignmentSearchParams = computed<ProjectAssignmentSearchParams>(() => {
  const filterMap = new Map<string, unknown>(columnFilters.value.map((filter) => [filter.id, filter.value]));
  const params: ProjectAssignmentSearchParams = {
    page: page.value,
    size: pageSize.value,
  };

  const name = filterMap.get('employeeName');
  if (typeof name === 'string' && name.trim().length > 0) {
    params.name = name.trim();
  }

  const statuses = toArray(filterMap.get('assignmentStatus'));
  if (statuses.length > 0) {
    params.assignmentStatuses = statuses;
  }

  const roles = toArray(filterMap.get('role'));
  if (roles.length > 0) {
    params.roles = roles;
  }

  return params;
});
const assignmentsQuery = useProjectAssignmentsQuery(projectId, projectAssignmentSearchParams);
const assignmentsPage = computed(() => assignmentsQuery.data.value);
const assignments = computed(() => assignmentsPage.value?.content ?? []);
const isLoading = computed(() => assignmentsQuery.isLoading.value);
const errorMessage = computed(() => {
  const error = assignmentsQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof HttpError ? error.message : '투입 인력 정보를 불러오는 중 오류가 발생했습니다.';
});
const canManageAssignments = computed(() => canManageProjects());
const totalPages = computed(() => assignmentsPage.value?.totalPages ?? 0);
const totalElements = computed(() => assignmentsPage.value?.totalElements ?? 0);
const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);
const statusFilterOptions = computed(() => [
  { value: 'CURRENT', label: '현재 투입' },
  { value: 'SCHEDULED', label: '예정' },
  { value: 'ENDED', label: '종료' },
]);
const roleFilterOptions = computed(() => projectAssignmentRoleOptions);

const columns: ColumnDef<ProjectAssignmentItem>[] = [
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
    id: 'employeeName',
    accessorFn: (row) => row.employeeName,
    header: () => h('span', '직원명'),
    cell: ({ row }) =>
      canReadEmployeeDetail()
        ? h(
            'button',
            {
              type: 'button',
              class:
                'cursor-pointer text-left font-medium text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
              onClick: () => handleEmployeeClick(row.original.employeeId),
            },
            row.original.employeeName || '-',
          )
        : h('span', { class: 'text-sm font-medium text-foreground' }, row.original.employeeName || '-'),
    size: 180,
  },
  {
    id: 'role',
    accessorFn: (row) => row.roleLabel,
    header: () => h('span', '역할'),
    cell: ({ row }) => h('span', { class: 'text-sm text-foreground' }, row.original.roleLabel),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.role);
    },
    size: 120,
  },
  {
    id: 'departmentName',
    accessorFn: (row) => row.departmentName,
    header: () => h('span', '소속 부서'),
    cell: ({ row }) =>
      row.original.departmentId && row.original.departmentName
        ? h(
            'button',
            {
              type: 'button',
              class:
                'cursor-pointer text-left text-sm text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
              onClick: () => handleDepartmentClick(row.original.departmentId),
            },
            row.original.departmentName,
          )
        : h('span', { class: 'text-sm text-muted-foreground' }, '-'),
    size: 160,
  },
  {
    id: 'period',
    accessorFn: (row) => row.startDate,
    header: () => h('span', '투입 기간'),
    cell: ({ row }) =>
      h(
        'span',
        { class: 'text-sm text-muted-foreground' },
        `${formatDate(row.original.startDate)} ~ ${row.original.endDate ? formatDate(row.original.endDate) : '진행 중'}`,
      ),
    size: 200,
  },
  {
    id: 'assignmentStatus',
    accessorFn: (row) => row.assignmentStatusLabel,
    header: () => h('span', '상태'),
    cell: ({ row }) =>
      h(
        Badge,
        { variant: toStatusVariant(row.original.assignmentStatus) },
        () => row.original.assignmentStatusLabel,
      ),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.assignmentStatus);
    },
    size: 120,
  },
  {
    id: 'actions',
    cell: ({ row }) =>
      canManageAssignments.value
        ? h(ProjectAssignmentRowActions, {
            onEdit: () => openEditDialog(row.original),
            onEnd: () => openEndDialog(row.original),
          })
        : null,
    enableSorting: false,
    enableHiding: false,
    size: 56,
  },
];

const table = useVueTable({
  get data() {
    return assignments.value;
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

function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    employeeName: '직원명',
    role: '역할',
    departmentName: '소속 부서',
    assignmentStatus: '상태',
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

function toStatusVariant(status: ProjectAssignmentStatus) {
  if (status === 'CURRENT') {
    return 'default';
  }
  if (status === 'SCHEDULED') {
    return 'outline';
  }
  return 'secondary';
}

function handleEmployeeClick(employeeId: number) {
  router.push({ name: 'employee-detail', params: { employeeId } });
}

function handleDepartmentClick(departmentId: number) {
  router.push({ name: 'department', params: { departmentId } });
}

function openCreateDialog() {
  editingAssignment.value = null;
  isFormDialogOpen.value = true;
}

function openEditDialog(assignment: ProjectAssignmentItem) {
  editingAssignment.value = assignment;
  isFormDialogOpen.value = true;
}

function openEndDialog(assignment: ProjectAssignmentItem) {
  endingAssignment.value = assignment;
  isEndDialogOpen.value = true;
}

function handleFormDialogOpenChange(open: boolean) {
  isFormDialogOpen.value = open;
  if (!open) {
    editingAssignment.value = null;
  }
}

function handleEndDialogOpenChange(open: boolean) {
  isEndDialogOpen.value = open;
  if (!open) {
    endingAssignment.value = null;
  }
}

function handleSaved() {
  isFormDialogOpen.value = false;
  editingAssignment.value = null;
}

function handleEnded() {
  isEndDialogOpen.value = false;
  endingAssignment.value = null;
}

function handlePageChange(nextPage: number) {
  page.value = nextPage;
}

function handlePageSizeChange(nextPageSize: number) {
  pageSize.value = nextPageSize;
  page.value = 1;
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
</script>
