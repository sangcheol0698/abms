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
            :options="departmentFilterOptions"
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
          <Button
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
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
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
import DepartmentRepository from '@/features/department/repository/DepartmentRepository';
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
import { ChevronDown, Download, FileSpreadsheet, Plus, Upload } from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import HttpError from '@/core/http/HttpError';
import { useEmployeeDeletion } from '@/features/employee/composables/useEmployeeDeletion';
import { getExcelErrorMessage } from '@/core/utils/excel';
import type { DepartmentChartNode } from '@/features/department/models/department';
import { useEmployeeQuerySync } from '@/features/employee/composables/useEmployeeQuerySync.ts';

interface DepartmentOption {
  label: string;
  value: number;
}

const employeeRepository = appContainer.resolve(EmployeeRepository);
const departmentRepository = appContainer.resolve(DepartmentRepository);
const router = useRouter();
const route = useRoute();

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

const departmentFilterOptions = computed(() =>
  departmentOptions.value.map((opt) => ({
    label: opt.label,
    value: String(opt.value),
  })),
);

// 테이블 컬럼 정의 (EmployeeTableColumns.ts로 분리됨)
const columns = createEmployeeTableColumns({
  onViewEmployee: handleViewEmployee,
  onEditEmployee: handleEditEmployee,
  onCopyEmail: handleCopyEmail,
  onDeleteEmployee: handleDeleteEmployee,
  onNavigateToDepartment: navigateToDepartment,
});

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
    const chart = await departmentRepository.fetchOrganizationChart();
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
    console.error('직원 목록을 불러오지 못했습니다.', error);
    employees.value = [];
    totalElements.value = 0;
    totalPages.value = 1;
  } finally {
    if (token === requestToken) {
      isLoading.value = false;
    }
  }
}

// URL 쿼리 동기화 Composable 초기화
const { buildSearchParams, applyRouteQuery } = useEmployeeQuerySync({
  page,
  pageSize,
  sorting,
  columnFilters,
  onLoadEmployees: loadEmployees,
});

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
    // page가 변경되면 Composable의 watch가 자동으로 loadEmployees 호출
  } else {
    // page가 1이면 직접 호출
    loadEmployees();
  }
}

function handleEmployeeUpdated() {
  isEmployeeUpdateDialogOpen.value = false;
  loadEmployees();
}

async function handleEditEmployee(row: EmployeeListItem) {
  editingEmployee.value = null;
  isLoadingEmployee.value = true;
  const loadingToast = toast.loading('직원 정보를 불러오는 중입니다.');
  try {
    const detail = await employeeRepository.findById(row.employeeId);
    editingEmployee.value = detail;
    isEmployeeUpdateDialogOpen.value = true;
    toast.dismiss(loadingToast);
  } catch (error) {
    toast.dismiss(loadingToast);
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
    await loadEmployees();
  }
});
</script>
