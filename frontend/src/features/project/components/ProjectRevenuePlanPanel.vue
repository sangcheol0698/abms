<template>
  <div class="flex flex-col gap-6">
    <Alert v-if="errorMessage" variant="destructive">
      <AlertTitle>매출 일정 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-xl border bg-card p-4 shadow-sm">
        <div class="text-sm font-medium text-muted-foreground">총 매출 금액</div>
        <div class="mt-2 text-2xl font-bold">{{ formatCurrency(totalAmount) }}</div>
      </div>
      <div class="rounded-xl border bg-card p-4 shadow-sm">
        <div class="text-sm font-medium text-muted-foreground">예정 금액</div>
        <div class="mt-2 text-2xl font-bold text-muted-foreground">
          {{ formatCurrency(plannedAmount) }}
        </div>
      </div>
      <div class="rounded-xl border bg-card p-4 shadow-sm">
        <div class="text-sm font-medium text-muted-foreground">발행된 금액</div>
        <div class="mt-2 text-2xl font-bold text-primary">
          {{ formatCurrency(invoicedAmount) }}
        </div>
      </div>
    </div>

    <div class="space-y-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="비고를 입력하세요"
        searchColumnId="memo"
        :getColumnLabel="getColumnLabel"
      >
        <template #filters>
          <DataTableFacetedFilter
            v-if="table.getColumn('type')"
            :column="table.getColumn('type')"
            title="구분"
            :options="typeFilterOptions"
          />
          <DataTableFacetedFilter
            v-if="table.getColumn('status')"
            :column="table.getColumn('status')"
            title="상태"
            :options="statusFilterOptions"
          />
        </template>

        <template #actions>
          <Button v-if="canManageProject" size="sm" @click="handleCreate">
            <Plus class="mr-2 h-4 w-4" />
            일정 추가
          </Button>
        </template>
      </DataTableToolbar>

      <div v-if="isLoading" class="py-10 text-center text-sm text-muted-foreground">
        매출 일정 정보를 불러오는 중입니다...
      </div>

      <DataTable
        v-else
        :columns="columns"
        :data="items"
        :table-instance="table"
        empty-message="등록된 매출 일정이 없습니다."
        empty-description="매출 일정을 추가하면 여기에 이력이 표시됩니다."
      />
    </div>

    <ProjectRevenuePlanDialog
      :open="isDialogOpen"
      :plan="editingPlan"
      :project-id="projectId"
      @update:open="handleDialogOpenChange"
      @saved="handleSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, h, ref, toRef } from 'vue';
import { toast } from 'vue-sonner';
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
import { Plus } from 'lucide-vue-next';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { valueUpdater } from '@/components/ui/table/utils';
import {
  DataTable,
  DataTableFacetedFilter,
  DataTableToolbar,
} from '@/components/business';
import HttpError from '@/core/http/HttpError';
import { formatCurrency } from '@/features/project/models/projectListItem';
import ProjectRevenuePlanDialog from './ProjectRevenuePlanDialog.vue';
import ProjectRevenuePlanRowActions from './ProjectRevenuePlanRowActions.vue';
import {
  useCancelProjectRevenuePlanMutation,
  useProjectDetailQuery,
  useIssueProjectRevenuePlanMutation,
  useProjectRevenuePlansQuery,
} from '@/features/project/queries/useProjectQueries';
import { canManageProjects } from '@/features/project/permissions';
import { useProjectRevenuePlanQuerySync } from '@/features/project/composables/useProjectRevenuePlanQuerySync';
import type { ProjectRevenuePlanResponse } from '@/features/project/repository/ProjectRevenueRepository';

interface Props {
  projectId: number;
}

const props = defineProps<Props>();
const projectId = toRef(props, 'projectId');
const revenuePlansQuery = useProjectRevenuePlansQuery(projectId);
const projectDetailQuery = useProjectDetailQuery(projectId);
const issueRevenuePlanMutation = useIssueProjectRevenuePlanMutation();
const cancelRevenuePlanMutation = useCancelProjectRevenuePlanMutation();
const isDialogOpen = ref(false);
const editingPlan = ref<ProjectRevenuePlanResponse | null>(null);
const canManageProject = computed(() => canManageProjects());
const columnFilters = ref<ColumnFiltersState>([]);
const rowSelection = ref<RowSelectionState>({});
useProjectRevenuePlanQuerySync({ columnFilters });

const isLoading = computed(
  () => revenuePlansQuery.isLoading.value || projectDetailQuery.isLoading.value,
);
const errorMessage = computed(() => {
  const error = revenuePlansQuery.error.value ?? projectDetailQuery.error.value;
  if (!error) {
    return null;
  }

  return error instanceof HttpError ? error.message : '매출 일정 정보를 불러오는 중 오류가 발생했습니다.';
});
const items = computed(() =>
  (revenuePlansQuery.data.value ?? [])
    .map((item) => ({
      ...item,
      status: item.status ?? 'PLANNED',
      typeLabel: typeLabelMap[item.type] || item.type,
      statusLabel: (item.status ?? 'PLANNED') === 'INVOICED' ? '발행' : '미발행',
    }))
    .sort((a, b) => a.sequence - b.sequence),
);
const contractAmount = computed(() => projectDetailQuery.data.value?.contractAmount ?? 0);
const totalAmount = computed(() => contractAmount.value);
const plannedAmount = computed(() =>
  items.value
    .filter((item) => item.status !== 'INVOICED')
    .reduce((sum, item) => sum + item.amount, 0),
);
const invoicedAmount = computed(() =>
  items.value
    .filter((item) => item.status === 'INVOICED')
    .reduce((sum, item) => sum + item.amount, 0),
);

const typeLabelMap: Record<string, string> = {
  DOWN_PAYMENT: '착수금',
  INTERMEDIATE_PAYMENT: '중도금',
  BALANCE_PAYMENT: '잔금',
  MAINTENANCE: '유지보수',
  ETC: '기타',
};
const typeFilterOptions = computed(() => [
  { value: 'DOWN_PAYMENT', label: '착수금' },
  { value: 'INTERMEDIATE_PAYMENT', label: '중도금' },
  { value: 'BALANCE_PAYMENT', label: '잔금' },
  { value: 'MAINTENANCE', label: '유지보수' },
  { value: 'ETC', label: '기타' },
]);
const statusFilterOptions = computed(() => [
  { value: 'PLANNED', label: '미발행' },
  { value: 'INVOICED', label: '발행' },
]);

const columns: ColumnDef<(typeof items.value)[number]>[] = [
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
    id: 'sequence',
    accessorFn: (row) => `${row.sequence}회차`,
    header: () => h('span', '회차'),
    cell: ({ row }) => h('span', { class: 'text-sm font-medium text-foreground' }, `${row.original.sequence}회차`),
    size: 90,
  },
  {
    id: 'type',
    accessorFn: (row) => row.typeLabel,
    header: () => h('span', '구분'),
    cell: ({ row }) => h(Badge, { variant: 'outline' }, () => row.original.typeLabel),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.type);
    },
    size: 140,
  },
  {
    id: 'status',
    accessorFn: (row) => row.statusLabel,
    header: () => h('span', '상태'),
    cell: ({ row }) =>
      h(Badge, { variant: row.original.status === 'INVOICED' ? 'default' : 'secondary' }, () => row.original.statusLabel),
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.length === 0 || candidate.includes(row.original.status);
    },
    size: 120,
  },
  {
    id: 'revenueDate',
    accessorFn: (row) => row.revenueDate,
    header: () => h('span', '예정일'),
    cell: ({ row }) => h('span', { class: 'text-sm text-muted-foreground' }, formatDate(row.original.revenueDate)),
    size: 140,
  },
  {
    id: 'amount',
    accessorFn: (row) => row.amount,
    header: () => h('span', '금액'),
    cell: ({ row }) => h('span', { class: 'text-sm font-medium text-foreground' }, formatCurrency(row.original.amount)),
    size: 140,
  },
  {
    id: 'memo',
    accessorFn: (row) => row.memo ?? '',
    header: () => h('span', '비고'),
    cell: ({ row }) => h('span', { class: 'text-sm text-muted-foreground' }, row.original.memo || '-'),
    size: 240,
  },
  {
    id: 'actions',
    cell: ({ row }) =>
      canManageProject.value
        ? h(ProjectRevenuePlanRowActions, {
            status: row.original.status,
            onEdit: () => openEditDialog(row.original),
            'onToggle-status': () => toggleIssued(row.original),
          })
        : null,
    enableSorting: false,
    enableHiding: false,
    size: 56,
  },
];

const table = useVueTable({
  get data() {
    return items.value;
  },
  columns,
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

function formatDate(value: string) {
  if (!value) return '-';
  const parsed = new Date(value);
  const y = parsed.getFullYear();
  const m = String(parsed.getMonth() + 1).padStart(2, '0');
  const d = String(parsed.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    sequence: '회차',
    type: '구분',
    status: '상태',
    revenueDate: '예정일',
    amount: '금액',
    memo: '비고',
  };
  return labels[columnId] ?? columnId;
}

function handleCreate() {
  if (!canManageProject.value) {
    return;
  }
  editingPlan.value = null;
  isDialogOpen.value = true;
}

function openEditDialog(plan: ProjectRevenuePlanResponse) {
  if (!canManageProject.value) {
    return;
  }
  editingPlan.value = plan;
  isDialogOpen.value = true;
}

function handleDialogOpenChange(value: boolean) {
  isDialogOpen.value = value;
  if (!value) {
    editingPlan.value = null;
  }
}

function handleSaved() {
  isDialogOpen.value = false;
  editingPlan.value = null;
}

async function toggleIssued(plan: ProjectRevenuePlanResponse & { status?: 'PLANNED' | 'INVOICED' }) {
  if (!canManageProject.value) {
    return;
  }

  try {
    if (plan.status === 'INVOICED') {
      await cancelRevenuePlanMutation.mutateAsync({
        projectId: plan.projectId,
        sequence: plan.sequence,
      });
      toast.success('매출 일정 발행을 취소했습니다.');
      return;
    }

    await issueRevenuePlanMutation.mutateAsync({
      projectId: plan.projectId,
      sequence: plan.sequence,
    });
    toast.success('매출 일정을 발행했습니다.');
  } catch (error) {
    const message = error instanceof HttpError ? error.message : '매출 일정 상태 변경 중 오류가 발생했습니다.';
    toast.error('매출 일정 상태 변경에 실패했습니다.', { description: message });
  }
}
</script>
