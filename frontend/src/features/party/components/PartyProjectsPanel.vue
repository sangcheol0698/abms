<template>
  <div class="flex h-full flex-col gap-6">
    <Alert v-if="errorMessage" variant="destructive">
      <AlertTitle>프로젝트 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <div class="space-y-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="프로젝트명 또는 코드를 입력하세요"
        searchColumnId="name"
        :getColumnLabel="getColumnLabel"
        :applySearchOnEnter="true"
      >
        <template #filters>
          <DataTableFacetedFilter
            v-if="table.getColumn('status')"
            :column="table.getColumn('status')"
            title="상태"
            :options="statusFilterOptions"
          />
        </template>
      </DataTableToolbar>

      <div v-if="isLoading" class="py-10 text-center text-sm text-muted-foreground">
        프로젝트 정보를 불러오는 중입니다...
      </div>

      <template v-else>
        <DataTable
          :columns="columns"
          :data="projects"
          :table-instance="table"
          empty-message="관련 프로젝트가 없습니다."
          empty-description="이 협력사와 연결된 프로젝트가 여기에 표시됩니다."
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
import { Checkbox } from '@/components/ui/checkbox';
import {
  DataTable,
  DataTableFacetedFilter,
  DataTablePagination,
  DataTableToolbar,
} from '@/components/business';
import { valueUpdater } from '@/components/ui/table/utils';
import type { ProjectListItem } from '@/features/project/models/projectListItem';
import type { PartyProjectSearchParams } from '@/features/party/models/partyProject';
import { formatCurrency, formatProjectPeriod } from '@/features/project/models/projectListItem';
import { usePartyProjectsQuery } from '@/features/party/queries/usePartyQueries';
import { useProjectStatusesQuery } from '@/features/project/queries/useProjectQueries';
import { usePartyProjectsQuerySync } from '@/features/party/composables/usePartyProjectsQuerySync';

interface Props {
  partyId: number;
}

const props = defineProps<Props>();
const router = useRouter();
const partyId = toRef(props, 'partyId');
const page = ref(1);
const pageSize = ref(10);
const rowSelection = ref<RowSelectionState>({});
const columnFilters = ref<ColumnFiltersState>([]);

usePartyProjectsQuerySync({
  page,
  pageSize,
  columnFilters,
});

const searchParams = computed<PartyProjectSearchParams>(() => {
  const filterMap = new Map<string, unknown>(columnFilters.value.map((filter) => [filter.id, filter.value]));
  const params: PartyProjectSearchParams = {
    page: page.value,
    size: pageSize.value,
  };

  const name = filterMap.get('name');
  if (typeof name === 'string' && name.trim().length > 0) {
    params.name = name.trim();
  }

  const statuses = toArray(filterMap.get('status'));
  if (statuses.length > 0) {
    params.statuses = statuses;
  }

  return params;
});

const projectsQuery = usePartyProjectsQuery(partyId, searchParams);
const projectStatusesQuery = useProjectStatusesQuery();
const projectsPage = computed(() => projectsQuery.data.value);
const projects = computed(() => projectsPage.value?.content ?? []);
const totalPages = computed(() => projectsPage.value?.totalPages ?? 0);
const totalElements = computed(() => projectsPage.value?.totalElements ?? 0);
const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);
const isLoading = computed(() => projectsQuery.isLoading.value);
const errorMessage = computed(() => {
  const error = projectsQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof Error ? error.message : '프로젝트 정보를 불러오는 중 오류가 발생했습니다.';
});
const statusFilterOptions = computed(
  () => projectStatusesQuery.data.value ?? ([] as { value: string; label: string }[]),
);

const columns: ColumnDef<ProjectListItem>[] = [
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
    id: 'name',
    accessorFn: (row) => row.name,
    header: () => h('span', '프로젝트명'),
    cell: ({ row }) =>
      h(
        'button',
        {
          type: 'button',
          class:
            'cursor-pointer text-left font-medium text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline focus-visible:ring-0',
          onClick: () => handleProjectClick(row.original.projectId),
        },
        row.original.name,
      ),
    size: 220,
  },
  {
    id: 'status',
    accessorFn: (row) => row.statusLabel,
    header: () => h('span', '상태'),
    cell: ({ row }) => h(Badge, { variant: 'outline' }, () => row.original.statusLabel),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.status);
    },
    size: 120,
  },
  {
    id: 'leadDepartmentName',
    accessorFn: (row) => row.leadDepartmentName,
    header: () => h('span', '주관 부서'),
    cell: ({ row }) => {
      const departmentId = row.original.leadDepartmentId;
      const departmentName = row.original.leadDepartmentName;

      if (departmentId == null || !departmentName) {
        return h('span', { class: 'text-sm text-muted-foreground' }, '-');
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
    size: 160,
  },
  {
    id: 'period',
    accessorFn: (row) => row.startDate,
    header: () => h('span', '계약 기간'),
    cell: ({ row }) =>
      h('span', { class: 'text-sm text-muted-foreground' }, formatProjectPeriod(row.original.startDate, row.original.endDate)),
    size: 200,
  },
  {
    id: 'contractAmount',
    accessorFn: (row) => row.contractAmount,
    header: () => h('span', '계약 금액'),
    cell: ({ row }) => h('span', { class: 'text-sm font-medium text-foreground' }, formatCurrency(row.original.contractAmount)),
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

function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    name: '프로젝트명',
    status: '상태',
    leadDepartmentName: '주관 부서',
    period: '계약 기간',
    contractAmount: '계약 금액',
  };
  return labels[columnId] ?? columnId;
}

function handleProjectClick(projectIdValue: number) {
  router.push({ name: 'project-detail', params: { projectId: projectIdValue } });
}

function handleDepartmentClick(departmentIdValue: number) {
  router.push({ name: 'department', params: { departmentId: departmentIdValue } });
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
