<template>
  <div class="flex h-full flex-col gap-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight">프로젝트</h1>
        <p class="text-sm text-muted-foreground">
          프로젝트를 관리하고 진행 상황을 확인하세요
        </p>
      </div>
    </div>

    <div class="flex flex-1 flex-col gap-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="프로젝트명 또는 코드를 입력하세요"
        searchColumnId="name"
        :getColumnLabel="getColumnLabel"
      >
        <template #filters>
          <DataTableFacetedFilter
            v-if="table.getColumn('status')"
            :column="table.getColumn('status')"
            title="상태"
            :options="statusFilterOptions"
          />
        </template>

        <template #actions>
        <Button variant="default" size="sm" class="h-8 px-2 sm:px-3 gap-1" @click="handleCreateProject">
          <Plus class="h-4 w-4" />
          프로젝트 추가
        </Button>
      </template>
      </DataTableToolbar>

      <DataTable
        :columns="columns"
        :data="projects"
        :tableInstance="table"
        :loading="isLoading"
        emptyMessage="프로젝트가 없습니다"
        emptyDescription="새로운 프로젝트를 생성해보세요"
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
  <ProjectCreateDialog
    :open="isProjectCreateDialogOpen"
    @update:open="isProjectCreateDialogOpen = $event"
    @created="handleProjectCreated"
  />
  <ProjectUpdateDialog
    :open="isProjectUpdateDialogOpen"
    :project="editingProject"
    @update:open="isProjectUpdateDialogOpen = $event"
    @updated="handleProjectUpdated"
  />
  <AlertDialog :open="deletion.isDialogOpen.value">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>프로젝트를 삭제할까요?</AlertDialogTitle>
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
import { computed, h, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Plus } from 'lucide-vue-next';
import {
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  type ColumnDef,
  type ColumnFiltersState,
  type SortingState,
  type RowSelectionState,
  useVueTable,
} from '@tanstack/vue-table';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  DataTable,
  DataTableColumnHeader,
  DataTablePagination,
  DataTableToolbar,
  DataTableFacetedFilter,
} from '@/components/business';
import { appContainer } from '@/core/di/container';
import ProjectRepository from '@/features/project/repository/ProjectRepository';
import ProjectCreateDialog from '@/features/project/components/ProjectCreateDialog.vue';
import ProjectUpdateDialog from '@/features/project/components/ProjectUpdateDialog.vue';
import ProjectRowActions from '@/features/project/components/ProjectRowActions.vue';
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
import type { ProjectListItem } from '@/features/project/models/projectListItem';
import {
  formatCurrency,
  formatProjectPeriod,
} from '@/features/project/models/projectListItem';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import { valueUpdater } from '@/components/ui/table/utils';
import { toast } from 'vue-sonner';

defineOptions({ name: 'ProjectListView' });

const router = useRouter();
const repository = appContainer.resolve(ProjectRepository);

const projects = ref<ProjectListItem[]>([]);
const isLoading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);

const sorting = ref<SortingState>([]);
const columnFilters = ref<ColumnFiltersState>([]);
const rowSelection = ref<RowSelectionState>({});
const isProjectCreateDialogOpen = ref(false);
const isProjectUpdateDialogOpen = ref(false);
const editingProject = ref<ProjectDetail | null>(null);

const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);
const statusFilterOptions = ref<{ value: string; label: string; icon?: any }[]>([]);

function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    name: '프로젝트명',
    status: '상태',
    contractAmount: '계약금액',
    period: '기간',
  };
  return labels[columnId] ?? columnId;
}

const columns: ColumnDef<ProjectListItem>[] = [
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
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '프로젝트명', align: 'left' }),
    cell: ({ row }) => {
      const name = row.original.name ?? '';
      const code = row.original.code ?? '';
      return h('div', { class: 'flex flex-col gap-0.5' }, [
        h(
          'button',
          {
            type: 'button',
            class:
              'cursor-pointer text-left font-medium text-primary underline underline-offset-4 hover:underline focus:outline-none',
            onClick: () => handleViewProject(row.original),
          },
          name,
        ),
        h('span', { class: 'text-xs text-muted-foreground' }, code),
      ]);
    },
    enableSorting: true,
    size: 280,
    meta: { skeleton: 'title-subtitle' },
  },
  {
    id: 'status',
    accessorFn: (row) => row.statusLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '상태', align: 'left' }),
    cell: ({ row }) => {
      return h(
        Badge,
        { variant: 'outline', class: 'font-medium' },
        () => row.original.statusLabel,
      );
    },
    enableSorting: false,
    size: 100,
  },
  {
    id: 'contractAmount',
    accessorFn: (row) => row.contractAmount,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '계약금액', align: 'left' }),
    cell: ({ row }) =>
      h('span', { class: 'text-sm font-medium' }, formatCurrency(row.original.contractAmount)),
    enableSorting: true,
    size: 150,
  },
  {
    id: 'period',
    accessorFn: (row) => row.startDate,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '계약기간', align: 'left' }),
    cell: ({ row }) =>
      h(
        'span',
        { class: 'text-sm' },
        formatProjectPeriod(row.original.startDate, row.original.endDate),
      ),
    enableSorting: true,
    size: 220,
  },
  {
    id: 'actions',
    cell: ({ row }) =>
      h(ProjectRowActions, {
        row: row.original,
        onEdit: () => handleEditProject(row.original),
        onCopyCode: () => handleCopyCode(row.original),
        onDelete: () => handleDeleteProject(row.original),
      }),
    enableSorting: false,
    enableHiding: false,
    size: 48,
  },
];

const table = useVueTable({
  data: computed(() => projects.value),
  columns,
  manualPagination: true,
  manualSorting: true,
  manualFiltering: true,
  enableRowSelection: true,
  state: {
    get sorting() {
      return sorting.value;
    },
    get columnFilters() {
      return columnFilters.value;
    },
    get rowSelection() {
      return rowSelection.value;
    },
  },
  onSortingChange: (updater) => valueUpdater(updater, sorting),
  onColumnFiltersChange: (updater) => valueUpdater(updater, columnFilters),
  onRowSelectionChange: (updater) => valueUpdater(updater, rowSelection),
  getCoreRowModel: getCoreRowModel(),
  getFilteredRowModel: getFilteredRowModel(),
  getFacetedRowModel: getFacetedRowModel(),
  getFacetedUniqueValues: getFacetedUniqueValues(),
});

async function loadProjects() {
  isLoading.value = true;
  try {
    const response = await repository.list({
      page: page.value,
      size: pageSize.value,
    });

    projects.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
    rowSelection.value = {};
  } catch (error) {
    console.error('프로젝트 목록을 불러오지 못했습니다.', error);
    projects.value = [];
    totalElements.value = 0;
    totalPages.value = 1;
  } finally {
    isLoading.value = false;
  }
}

function handleViewProject(project: ProjectListItem) {
  router.push({
    name: 'project-detail',
    params: { projectId: project.projectId },
  });
}

function handlePageChange(newPage: number) {
  page.value = newPage;
  loadProjects();
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize;
  page.value = 1;
  loadProjects();
}

function handleCreateProject() {
  isProjectCreateDialogOpen.value = true;
}

function handleProjectCreated() {
  isProjectCreateDialogOpen.value = false;
  loadProjects();
}

async function handleEditProject(project: ProjectListItem) {
  try {
    // 백엔드에서 최신 프로젝트 상세 정보 조회
    const projectDetail = await repository.find(project.projectId);
    editingProject.value = projectDetail;
    isProjectUpdateDialogOpen.value = true;
  } catch (error) {
    toast.error('프로젝트 정보를 불러오지 못했습니다.');
  }
}

function handleProjectUpdated() {
  isProjectUpdateDialogOpen.value = false;
  editingProject.value = null;
  loadProjects();
}

function handleCopyCode(project: ProjectListItem) {
  navigator.clipboard.writeText(project.code);
  toast.success('프로젝트 코드를 복사했습니다.', {
    description: project.code,
  });
}

const deletion = {
  isDialogOpen: ref(false),
  isProcessing: ref(false),
  description: ref(''),
  targetId: ref<string | null>(null),

  open(projectId: string, projectName: string) {
    this.targetId.value = projectId;
    this.description.value = `"${projectName}" 프로젝트를 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`;
    this.isDialogOpen.value = true;
  },

  cancel() {
    this.isDialogOpen.value = false;
    this.targetId.value = null;
    this.description.value = '';
  },

  async confirm() {
    if (!this.targetId.value) {
      return;
    }

    this.isProcessing.value = true;
    try {
      await repository.delete(this.targetId.value);
      toast.success('프로젝트를 삭제했습니다.');
      this.isDialogOpen.value = false;
      loadProjects();
    } catch (error) {
      toast.error('프로젝트 삭제에 실패했습니다.');
    } finally {
      this.isProcessing.value = false;
      this.targetId.value = null;
      this.description.value = '';
    }
  },
};

function handleDeleteProject(project: ProjectListItem) {
  deletion.open(project.projectId, project.name);
}

// Auto-load on mount
watch(() => true, loadProjects, { immediate: true });

onMounted(async () => {
  try {
    const statuses = await repository.fetchStatuses();
    statusFilterOptions.value = statuses;
  } catch (error) {
    console.error('Failed to fetch project statuses:', error);
  }
});
</script>
