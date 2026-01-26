<template>
  <div class="flex h-full flex-col gap-6">
    <ProjectSummaryCards :cards="projectSummaryCards" />

    <div class="flex flex-1 flex-col gap-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="프로젝트명 또는 코드를 입력하세요"
        searchColumnId="name"
        :getColumnLabel="getColumnLabel"
        :applySearchOnEnter="true"
        :isExternalFiltered="isDateFiltered"
        @reset="handleResetFilters"
      >
        <template #filters>
          <!-- 날짜 검색 유형 선택 -->
          <!-- 날짜 범위 필터 -->
          <div class="flex items-center gap-2">
            <DateRangeFilter v-model="dateRange" placeholder="계약일 날짜 범위 선택" />
          </div>

          <DataTableFacetedFilter
            v-if="table.getColumn('status')"
            :column="table.getColumn('status')"
            title="상태"
            :options="statusFilterOptions"
          />

          <div class="flex items-center gap-1">
            <Button
              variant="outline"
              size="sm"
              class="h-8 gap-2 border-dashed"
              @click="openPartyDialog"
            >
              <Building class="h-4 w-4" />
              <span>{{ selectedPartyName ?? '협력사 선택' }}</span>
            </Button>
            <Button
              v-if="selectedPartyId"
              variant="ghost"
              size="sm"
              class="h-8 px-2"
              @click="clearPartyFilter"
            >
              <X class="h-4 w-4" />
            </Button>
          </div>
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
              <DropdownMenuItem @click="handleExcelSampleDownloadFromMenu">
                <Download class="mr-2 h-4 w-4" />
                <span>샘플 다운로드</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem @click="openExcelUploadDialog">
                <Upload class="mr-2 h-4 w-4" />
                <span>엑셀 업로드</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button
            variant="default"
            size="sm"
            class="h-8 px-2 sm:px-3 gap-1"
            @click="handleCreateProject"
          >
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
  <PartySelectDialog
    :open="isPartySelectOpen"
    :selected-party-id="selectedPartyId ?? undefined"
    @update:open="isPartySelectOpen = $event"
    @select="handlePartySelected"
  />
  <ExcelUploadDialog
    :open="isExcelUploadDialogOpen"
    title="프로젝트 일괄 업로드"
    description="엑셀 파일을 업로드하여 프로젝트를 일괄 등록합니다."
    :on-download-sample="handleExcelSampleDownload"
    :on-upload="handleExcelUpload"
    @update:open="isExcelUploadDialogOpen = $event"
    @success="handleExcelUploadSuccess"
  />
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Building, ChevronDown, Download, FileSpreadsheet, Plus, Upload, X } from 'lucide-vue-next';
import {
  type ColumnDef,
  type ColumnFiltersState,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  type RowSelectionState,
  type SortingState,
  useVueTable,
} from '@tanstack/vue-table';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  DataTable,
  DataTableColumnHeader,
  DataTableFacetedFilter,
  DataTablePagination,
  DataTableToolbar,
  DateRangeFilter,
  ExcelUploadDialog,
} from '@/components/business';
import { appContainer } from '@/core/di/container';
import ProjectRepository from '@/features/project/repository/ProjectRepository';
import ProjectCreateDialog from '@/features/project/components/ProjectCreateDialog.vue';
import ProjectUpdateDialog from '@/features/project/components/ProjectUpdateDialog.vue';
import ProjectRowActions from '@/features/project/components/ProjectRowActions.vue';
import ProjectSummaryCards from '@/features/project/components/ProjectSummaryCards.vue';
import PartySelectDialog from '@/features/party/components/PartySelectDialog.vue';
import PartyRepository from '@/features/party/repository/PartyRepository';
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
import { formatCurrency, formatProjectPeriod } from '@/features/project/models/projectListItem';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import { valueUpdater } from '@/components/ui/table/utils';
import { toast } from 'vue-sonner';
import { useProjectQuerySync } from '@/features/project/composables/useProjectQuerySync';
import { projectSummaryCards } from '@/features/project/data';
import { useProjectDeletion } from '@/features/project/composables/useProjectDeletion';

defineOptions({ name: 'ProjectListView' });

const router = useRouter();
const route = useRoute();
const repository = appContainer.resolve(ProjectRepository);
const partyRepository = appContainer.resolve(PartyRepository);

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
const isPartySelectOpen = ref(false);

const isDownloadingExcel = ref(false);
const isExcelUploadDialogOpen = ref(false);

const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);
const statusFilterOptions = ref<{ value: string; label: string; icon?: any }[]>([]);
const partyOptions = ref<{ value: number; label: string }[]>([]);
const selectedPartyId = computed(() => {
  const filter = columnFilters.value.find((item) => item.id === 'partyId');
  const value = Array.isArray(filter?.value) ? filter?.value[0] : filter?.value;
  return value ? Number(value) : null;
});
const selectedPartyName = computed(() => {
  if (!selectedPartyId.value) {
    return null;
  }
  const match = partyOptions.value.find((option) => option.value === selectedPartyId.value);
  return match?.label ?? `협력사 #${selectedPartyId.value}`;
});

// 날짜 필터 상태
const dateRange = ref<{ start?: Date | string; end?: Date | string } | null>(null);

const isDateFiltered = computed(() => !!dateRange.value?.start || !!dateRange.value?.end);

function handleResetFilters() {
  dateRange.value = null;
}

function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    name: '프로젝트명',
    partyName: '협력사',
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
    header: ({ column }) =>
      h(DataTableColumnHeader, { column, title: '프로젝트명', align: 'left' }),
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
    id: 'partyName',
    accessorFn: (row) => row.partyName,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '협력사', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'text-sm' }, row.original.partyName || '-'),
    enableSorting: false,
    size: 180,
  },
  {
    id: 'status',
    accessorFn: (row) => row.statusLabel,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '상태', align: 'left' }),
    cell: ({ row }) => {
      return h(Badge, { variant: 'outline', class: 'font-medium' }, () => row.original.statusLabel);
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
    id: 'partyId',
    accessorKey: 'partyId',
    header: () => null,
    cell: () => null,
    filterFn: (row, _columnId, filterValue) => {
      const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
      return candidate.map(String).includes(String(row.original.partyId));
    },
    enableSorting: false,
    enableHiding: false,
    size: 0,
    meta: { isFilterOnly: true },
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

const { applyRouteQuery } = useProjectQuerySync({
  page,
  pageSize,
  sorting,
  columnFilters,
  dateRange,
  onLoadProjects: loadProjects,
});

const deletion = useProjectDeletion(async () => {
  if (page.value !== 1) {
    page.value = 1;
    return;
  }
  await loadProjects();
});

function getSearchParams(): ProjectSearchParams {
  const params: ProjectSearchParams = {
    page: page.value,
    size: pageSize.value,
  };

  const nameFilter = columnFilters.value.find((f) => f.id === 'name');
  if (nameFilter?.value) {
    params.name = nameFilter.value as string;
  }

  const statusFilter = columnFilters.value.find((f) => f.id === 'status');
  if (Array.isArray(statusFilter?.value) && statusFilter.value.length > 0) {
    params.statuses = statusFilter.value as string[];
  }

  if (selectedPartyId.value) {
    params.partyIds = [selectedPartyId.value];
  }

  if (dateRange.value?.start) {
    params.startDate =
      dateRange.value.start instanceof Date
        ? dateRange.value.start.toISOString().split('T')[0]
        : (dateRange.value.start as string);
  }

  if (dateRange.value?.end) {
    params.endDate =
      dateRange.value.end instanceof Date
        ? dateRange.value.end.toISOString().split('T')[0]
        : (dateRange.value.end as string);
  }

  if (sorting.value.length > 0) {
    const s = sorting.value[0];
    params.sort = `${s.id},${s.desc ? 'desc' : 'asc'}`;
  }

  return params;
}

async function loadProjects() {
  isLoading.value = true;
  try {
    const response = await repository.search(getSearchParams());

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
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize;
  page.value = 1;
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
  } catch {
    toast.error('프로젝트 정보를 불러오지 못했습니다.');
  }
}

function handleProjectUpdated() {
  isProjectUpdateDialogOpen.value = false;
  editingProject.value = null;
  loadProjects();
}

async function handleExcelDownload() {
  if (isDownloadingExcel.value) return;

  try {
    isDownloadingExcel.value = true;
    const params = getSearchParams();
    await repository.downloadExcel(params);
    toast.success('엑셀 다운로드가 완료되었습니다.');
  } catch {
    toast.error('엑셀 다운로드 중 오류가 발생했습니다.');
  } finally {
    isDownloadingExcel.value = false;
  }
}

async function handleExcelSampleDownload() {
  await repository.downloadExcelSample();
}

async function handleExcelSampleDownloadFromMenu() {
  try {
    await handleExcelSampleDownload();
    toast.success('샘플 파일 다운로드가 완료되었습니다.');
  } catch {
    toast.error('샘플 파일 다운로드 중 오류가 발생했습니다.');
  }
}

function openExcelUploadDialog() {
  isExcelUploadDialogOpen.value = true;
}

async function handleExcelUpload(file: File, onProgress: (percent: number) => void) {
  await repository.uploadExcel(file, onProgress);
  await loadProjects();
}

function handleExcelUploadSuccess() {
  toast.success('프로젝트가 업로드되었습니다.');
}

function handleCopyCode(project: ProjectListItem) {
  navigator.clipboard.writeText(project.code);
  toast.success('프로젝트 코드를 복사했습니다.', {
    description: project.code,
  });
}

function openPartyDialog() {
  isPartySelectOpen.value = true;
}

function setPartyFilter(partyId: number | null) {
  const nextFilters = columnFilters.value.filter((filter) => filter.id !== 'partyId');
  if (partyId) {
    nextFilters.push({ id: 'partyId', value: [String(partyId)] });
  }
  columnFilters.value = nextFilters;
}

function handlePartySelected(payload: { partyId: number; partyName: string }) {
  setPartyFilter(payload.partyId);
  isPartySelectOpen.value = false;
}

function clearPartyFilter() {
  setPartyFilter(null);
}

function handleDeleteProject(project: ProjectListItem) {
  deletion.open(project.projectId, project.name);
}

onMounted(async () => {
  try {
    const [statuses, parties] = await Promise.all([
      repository.fetchStatuses(),
      partyRepository.fetchAll(),
    ]);
    statusFilterOptions.value = statuses;
    partyOptions.value = parties;
  } catch (error) {
    console.error('Failed to fetch project filter options:', error);
  }
  if (Object.keys(route.query).length > 0) {
    applyRouteQuery({ ...route.query });
  } else {
    await loadProjects();
  }
});
</script>
