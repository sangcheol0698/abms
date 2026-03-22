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
          <div class="flex items-center gap-1">
            <Button
              variant="field"
              size="sm"
              class="h-8 gap-2 border-dashed"
              @click="openDepartmentDialog"
            >
              <Building2 class="h-4 w-4" />
              <span>{{ selectedDepartmentName ?? '부서 선택' }}</span>
            </Button>
            <Button
              v-if="selectedDepartmentId"
              variant="ghost"
              size="sm"
              class="h-8 px-2"
              @click="clearDepartmentFilter"
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
              <DropdownMenuItem
                v-if="canDownloadEmployees"
                :disabled="isDownloadingExcel"
                @click="handleExcelDownload"
              >
                <Download class="mr-2 h-4 w-4" />
                <span>{{ isDownloadingExcel ? '다운로드 중...' : '현재 조건 다운로드' }}</span>
              </DropdownMenuItem>
              <DropdownMenuItem :disabled="isDownloadingSample" @click="handleExcelSampleDownload">
                <Download class="mr-2 h-4 w-4" />
                <span>{{ isDownloadingSample ? '샘플 다운로드 중...' : '샘플 다운로드' }}</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator v-if="canUploadEmployees" />
              <DropdownMenuItem v-if="canUploadEmployees" @click="openExcelUploadDialog">
                <Upload class="mr-2 h-4 w-4" />
                <span>엑셀 업로드</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button
            v-if="canCreateEmployees"
            variant="default"
            size="sm"
            class="h-8 px-2 sm:px-3 gap-1"
            @click="openCreateDialog"
          >
            <Plus class="h-4 w-4" />
            직원 추가
          </Button>
        </template>
      </DataTableToolbar>

      <DataTable
        :key="tableRenderKey"
        :columns="columns"
        :data="employees"
        :tableInstance="table"
        :loading="isLoading"
        emptyMessage="직원 정보를 찾을 수 없습니다"
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
  <DepartmentSelectDialog
    :open="isDepartmentSelectOpen"
    :selected-department-id="selectedDepartmentId ?? undefined"
    @update:open="isDepartmentSelectOpen = $event"
    @select="handleDepartmentSelected"
  />
  <AlertDialog :open="deletion.isDialogOpen.value">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>직원을 삭제할까요?</AlertDialogTitle>
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
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  type ColumnFiltersState,
  type RowSelectionState,
  type SortingState,
  type VisibilityState,
  useVueTable,
} from '@tanstack/vue-table';
import {
  DataTable,
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
import { createEmployeeTableColumns } from '@/features/employee/configs/tableColumns';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';
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
import DepartmentSelectDialog from '@/features/department/components/DepartmentSelectDialog.vue';
import {
  Building2,
  ChevronDown,
  Download,
  FileSpreadsheet,
  Plus,
  Upload,
  X,
} from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import HttpError from '@/core/http/HttpError';
import { useEmployeeDeletion } from '@/features/employee/composables/useEmployeeDeletion';
import { getExcelErrorMessage } from '@/core/utils/excel';
import { copyTextToClipboard } from '@/core/utils/clipboard';
import type { DepartmentChartNode } from '@/features/department/models/department';
import { useEmployeeQuerySync } from '@/features/employee/composables/useEmployeeQuerySync.ts';
import { useDepartmentOrganizationChartQuery } from '@/features/department/queries/useDepartmentQueries';
import {
  useEmployeeDetailQuery,
  useEmployeeGradesQuery,
  useEmployeeOverviewSummaryQuery,
  useEmployeePositionsQuery,
  useEmployeesQuery,
  useEmployeeStatusesQuery,
  useEmployeeTypesQuery,
} from '@/features/employee/queries/useEmployeeQueries';
import { employeeKeys, queryClient } from '@/core/query';
import {
  canDownloadEmployeeExcel,
  canEditOwnProfile,
  canManageEmployee,
  canManageEmployees,
  canUploadEmployeeExcel,
  canViewEmployeeDetail,
} from '@/features/employee/permissions';
import { dispatchOpenProfileDialogEvent } from '@/features/auth/profileDialogEvents';

interface DepartmentOption {
  label: string;
  value: number;
}

const employeeRepository = appContainer.resolve(EmployeeRepository);
const router = useRouter();

const tableRenderKey = ref(0);
const page = ref(1);
const pageSize = ref(10);

const sorting = ref<SortingState>([]);
const columnFilters = ref<ColumnFiltersState>([]);
const columnVisibility = ref<VisibilityState>({});
const rowSelection = ref<RowSelectionState>({});
const editingEmployeeId = ref<number | null>(null);

const isDepartmentSelectOpen = ref(false);
const isEmployeeCreateDialogOpen = ref(false);
const isEmployeeUpdateDialogOpen = ref(false);
const editingEmployee = ref<EmployeeSummary | null>(null);
const isLoadingEmployee = ref(false);
const isExcelUploadDialogOpen = ref(false);
const isDownloadingExcel = ref(false);
const isDownloadingSample = ref(false);

const deletion = useEmployeeDeletion(async () => {
  await queryClient.invalidateQueries({ queryKey: employeeKeys.all });
});

const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);
const canCreateEmployees = computed(() => canManageEmployees());
const canUploadEmployees = computed(() => canUploadEmployeeExcel());
const canDownloadEmployees = computed(() => canDownloadEmployeeExcel());
const employeePermissionContext = computed(() => ({
  departmentChart: departmentChartQuery.data.value ?? [],
}));

// 테이블 컬럼 정의 (EmployeeTableColumns.ts로 분리됨)
const columns = createEmployeeTableColumns({
  onViewEmployee: handleViewEmployee,
  onEditEmployee: handleEditEmployee,
  onCopyEmail: handleCopyEmail,
  onDeleteEmployee: handleDeleteEmployee,
  onNavigateToDepartment: navigateToDepartment,
  canViewEmployee: (employee) => canViewEmployeeDetail(employee, employeePermissionContext.value),
  canEditEmployee: (employee) =>
    canManageEmployee(employee, employeePermissionContext.value) || canEditOwnProfile(employee),
  canDeleteEmployee: (employee) => canManageEmployee(employee, employeePermissionContext.value),
  getEditActionLabel: (employee) =>
    !canManageEmployee(employee, employeePermissionContext.value) && canEditOwnProfile(employee)
      ? '내 정보 수정'
      : '직원 편집',
});

// URL 쿼리 동기화 Composable 초기화
const { buildSearchParams } = useEmployeeQuerySync({
  page,
  pageSize,
  sorting,
  columnFilters,
});

const employeeSearchParams = computed(() => buildSearchParams());
const employeeSummaryParams = computed(() => {
  const params = buildSearchParams();
  return {
    name: params.name,
    statuses: params.statuses,
    types: params.types,
    grades: params.grades,
    positions: params.positions,
    departmentIds: params.departmentIds,
  };
});

const employeesQuery = useEmployeesQuery(employeeSearchParams);
const employeeOverviewSummaryQuery = useEmployeeOverviewSummaryQuery(employeeSummaryParams);
const departmentChartQuery = useDepartmentOrganizationChartQuery();
const employeeStatusesQuery = useEmployeeStatusesQuery();
const employeeTypesQuery = useEmployeeTypesQuery();
const employeeGradesQuery = useEmployeeGradesQuery();
const employeePositionsQuery = useEmployeePositionsQuery();
const employeeDetailQuery = useEmployeeDetailQuery(editingEmployeeId);

const employees = computed(() => employeesQuery.data.value?.content ?? []);
const totalPages = computed(() => {
  const value = employeesQuery.data.value?.totalPages;
  return typeof value === 'number' && value > 0 ? value : 1;
});
const totalElements = computed(() => employeesQuery.data.value?.totalElements ?? 0);
const isLoading = computed(() => employeesQuery.isLoading.value || employeesQuery.isFetching.value);

const table = useVueTable({
  data: employees,
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

const statusOptions = computed<EmployeeFilterOption[]>(
  () => employeeStatusesQuery.data.value ?? getEmployeeStatusOptions(),
);
const typeOptions = computed<EmployeeFilterOption[]>(
  () => employeeTypesQuery.data.value ?? getEmployeeTypeOptions(),
);
const gradeOptions = computed<EmployeeFilterOption[]>(
  () => employeeGradesQuery.data.value ?? getEmployeeGradeOptions(),
);
const positionOptions = computed<EmployeeFilterOption[]>(
  () => employeePositionsQuery.data.value ?? getEmployeePositionOptions(),
);

const departmentOptions = computed<DepartmentOption[]>(() => {
  const chart = departmentChartQuery.data.value ?? [];
  const map = new Map<number, string>();

  const traverse = (nodes: DepartmentChartNode[]) => {
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
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }));
});

const selectedDepartmentId = computed(() => {
  const filter = columnFilters.value.find((item) => item.id === 'departmentId');
  const value = Array.isArray(filter?.value) ? filter?.value[0] : filter?.value;
  return value ? Number(value) : null;
});

const selectedDepartmentName = computed(() => {
  if (!selectedDepartmentId.value) {
    return null;
  }
  const match = departmentOptions.value.find(
    (option) => option.value === selectedDepartmentId.value,
  );
  return match?.label ?? `부서 #${selectedDepartmentId.value}`;
});

const employeeSummary = useEmployeeSummary({
  summary: computed(() => employeeOverviewSummaryQuery.data.value),
});

watch(
  () => employeesQuery.data.value?.totalPages,
  (nextTotalPages) => {
    if (typeof nextTotalPages !== 'number' || nextTotalPages < 1) {
      return;
    }
    if (page.value > nextTotalPages) {
      page.value = nextTotalPages;
    }
  },
);

watch(employees, () => {
  rowSelection.value = {};
  tableRenderKey.value += 1;
});

function openExcelUploadDialog() {
  if (!canUploadEmployees.value) {
    return;
  }
  isExcelUploadDialogOpen.value = true;
}

function openDepartmentDialog() {
  isDepartmentSelectOpen.value = true;
}

function setDepartmentFilter(departmentId: number | null) {
  const nextFilters = columnFilters.value.filter((filter) => filter.id !== 'departmentId');
  if (departmentId) {
    nextFilters.push({ id: 'departmentId', value: [String(departmentId)] });
  }
  columnFilters.value = nextFilters;
}

function handleDepartmentSelected(payload: { departmentId: number; departmentName: string }) {
  setDepartmentFilter(payload.departmentId);
  isDepartmentSelectOpen.value = false;
}

function clearDepartmentFilter() {
  setDepartmentFilter(null);
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
  await queryClient.invalidateQueries({ queryKey: employeeKeys.all });
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
  if (!canCreateEmployees.value) {
    return;
  }
  editingEmployee.value = null;
  isEmployeeCreateDialogOpen.value = true;
}

async function handleEmployeeCreated() {
  isEmployeeCreateDialogOpen.value = false;
  if (page.value !== 1) {
    page.value = 1;
  }
  await queryClient.invalidateQueries({ queryKey: employeeKeys.all });
}

async function handleEmployeeUpdated() {
  isEmployeeUpdateDialogOpen.value = false;
  await queryClient.invalidateQueries({ queryKey: employeeKeys.all });
}

async function handleEditEmployee(row: EmployeeListItem) {
  if (!(canManageEmployee(row, employeePermissionContext.value) || canEditOwnProfile(row))) {
    return;
  }
  if (!canManageEmployee(row, employeePermissionContext.value) && canEditOwnProfile(row)) {
    dispatchOpenProfileDialogEvent({ openSelfProfileEditor: true });
    return;
  }
  editingEmployee.value = null;
  isLoadingEmployee.value = true;
  try {
    editingEmployeeId.value = row.employeeId;
    const result = await employeeDetailQuery.refetch();
    const detail = result.data;
    if (!detail) {
      throw new Error('직원 정보를 불러오지 못했습니다.');
    }
    editingEmployee.value = detail;
    isEmployeeUpdateDialogOpen.value = true;
  } catch (error) {
    const message = error instanceof HttpError ? error.message : '직원 정보를 불러오지 못했습니다.';
    toast.error('직원 정보를 불러오지 못했습니다.', {
      description: message,
    });
  } finally {
    isLoadingEmployee.value = false;
  }
}

async function handleCopyEmail(row: EmployeeListItem) {
  try {
    await copyTextToClipboard(row.email);
    toast.success('이메일을 복사했어요.', {
      description: row.email,
    });
  } catch {
    toast.error('이메일 복사에 실패했습니다.');
  }
}

function handleDeleteEmployee(row: EmployeeListItem) {
  if (!canManageEmployee(row, employeePermissionContext.value)) {
    return;
  }
  deletion.open(row.employeeId, row.name);
}

function handleViewEmployee(row: EmployeeListItem) {
  if (!canViewEmployeeDetail(row, employeePermissionContext.value)) {
    return;
  }
  router.push({ name: 'employee-detail', params: { employeeId: row.employeeId } }).catch(() => {
    /* 라우팅 오류는 무시 */
  });
}

function navigateToDepartment(departmentId?: number) {
  if (!departmentId) {
    return;
  }
  router.push({ name: 'department', params: { departmentId } }).catch(() => {
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
  actions: '동작',
};

function getColumnLabel(columnId: string): string {
  return columnLabelMap[columnId] ?? columnId;
}
</script>
