<template>
  <div class="flex flex-col gap-4">
    <DataTableToolbar
      :table="table"
      searchPlaceholder="이름을 입력하세요"
      searchColumnId="name"
      :getColumnLabel="getColumnLabel"
      :applySearchOnEnter="true"
    />

    <div class="space-y-4">
      <DataTable
        :columns="columns"
        :data="employees"
        :tableInstance="table"
        :loading="isLoading"
        emptyMessage="직원 정보를 찾을 수 없습니다"
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

    <!-- 직원 수정 다이얼로그 -->
    <EmployeeUpdateDialog
      v-model:open="isEmployeeUpdateDialogOpen"
      :employee="editingEmployee"
      :departmentOptions="departmentOptions"
      :statusOptions="statusOptions"
      :typeOptions="typeOptions"
      :gradeOptions="gradeOptions"
      :positionOptions="positionOptions"
      @updated="handleEmployeeUpdated"
    />

    <!-- 직원 삭제 확인 다이얼로그 -->
    <AlertDialog :open="deletion.isDialogOpen.value" @update:open="deletion.close">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>직원을 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {{ deletion.description.value }}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel :disabled="deletion.isProcessing.value" @click="deletion.cancel">
            취소
          </AlertDialogCancel>
          <AlertDialogAction
            :disabled="deletion.isProcessing.value"
            @click="deletion.confirm"
            class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
          >
            {{ deletion.isProcessing.value ? '삭제 중...' : '삭제' }}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  getCoreRowModel,
  type ColumnFiltersState,
  type RowSelectionState,
  type SortingState,
  useVueTable,
} from '@tanstack/vue-table';
import { DataTable, DataTablePagination, DataTableToolbar } from '@/components/business';
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
import { appContainer } from '@/core/di/container';
import { useQuerySync } from '@/core/composables/useQuerySync';
import { toast } from 'vue-sonner';
import HttpError from '@/core/http/HttpError';
import OrganizationRepository from '@/features/department/repository/OrganizationRepository';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import { createEmployeeTableColumns } from '@/features/employee/configs/tableColumns';
import { valueUpdater } from '@/components/ui/table/utils';
import { useEmployeeDeletion } from '@/features/employee/composables/useEmployeeDeletion';
import EmployeeUpdateDialog from '@/features/employee/components/EmployeeUpdateDialog.vue';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import {
  getEmployeeGradeOptions,
  getEmployeePositionOptions,
  getEmployeeStatusOptions,
  getEmployeeTypeOptions,
} from '@/features/employee/models/employeeFilters';
import type { OrganizationChartNode } from '@/features/department/models/organization';

interface DepartmentOption {
  label: string;
  value: string;
}

const props = defineProps<{
  departmentId: string;
}>();

const repository = appContainer.resolve(OrganizationRepository);
const employeeRepository = appContainer.resolve(EmployeeRepository);
const router = useRouter();

// 상태
const employees = ref<EmployeeListItem[]>([]);
const isLoading = ref(false);
const page = ref(1);
const pageSize = ref(5);
const totalPages = ref(1);
const totalElements = ref(0);
const sorting = ref<SortingState>([]);
const columnFilters = ref<ColumnFiltersState>([]);
const rowSelection = ref<RowSelectionState>({});

const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);

// 부서 옵션 및 필터 옵션
const departmentOptions = ref<DepartmentOption[]>([]);
const statusOptions = ref<EmployeeFilterOption[]>(getEmployeeStatusOptions());
const typeOptions = ref<EmployeeFilterOption[]>(getEmployeeTypeOptions());
const gradeOptions = ref<EmployeeFilterOption[]>(getEmployeeGradeOptions());
const positionOptions = ref<EmployeeFilterOption[]>(getEmployeePositionOptions());

// 직원 수정 다이얼로그 상태
const isEmployeeUpdateDialogOpen = ref(false);
const editingEmployee = ref<EmployeeSummary | null>(null);
const isLoadingEmployee = ref(false);

// 직원 삭제 Composable
const deletion = useEmployeeDeletion(async () => {
  await loadEmployees();
});

// EnumResponse 로드 완료 플래그
const isEnumLoaded = ref(false);

// 컬럼 라벨 매핑 (DataTableToolbar용)
function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    name: '이름',
    position: '직책',
    grade: '등급',
    type: '유형',
    status: '상태',
    birthDate: '생년월일',
    joinDate: '입사일',
  };
  return labels[columnId] ?? columnId;
}

// columnFilters에서 이름 검색어 추출
const searchName = computed(() => {
  const nameFilter = columnFilters.value.find((filter) => filter.id === 'name');
  return (nameFilter?.value as string) ?? '';
});

// URL 동기화 - 검색어
const searchNameForQuery = computed({
  get: () => searchName.value,
  set: (value: string) => {
    const existingIndex = columnFilters.value.findIndex((filter) => filter.id === 'name');
    if (value.trim()) {
      if (existingIndex >= 0) {
        columnFilters.value[existingIndex] = { id: 'name', value: value.trim() };
      } else {
        columnFilters.value.push({ id: 'name', value: value.trim() });
      }
    } else {
      if (existingIndex >= 0) {
        columnFilters.value.splice(existingIndex, 1);
      }
    }
  },
});

useQuerySync({
  state: searchNameForQuery,
  queryKey: 'name',
  serialize: (value) => (value.trim() ? value.trim() : undefined),
  deserialize: (value) => {
    if (typeof value === 'string') return value;
    if (Array.isArray(value)) return value[0] ?? '';
    return '';
  },
});

// URL 동기화 - 페이지
useQuerySync({
  state: page,
  queryKey: 'page',
  serialize: (value) => (value > 1 ? value.toString() : undefined),
  deserialize: (value) => {
    if (typeof value === 'string') {
      const parsed = parseInt(value, 10);
      return parsed > 0 ? parsed : 1;
    }
    return 1;
  },
  defaultValue: 1,
});

// URL 동기화 - 정렬
const sortQuery = computed({
  get: () => {
    const sort = sorting.value[0];
    if (!sort) return undefined;
    return `${sort.id},${sort.desc ? 'desc' : 'asc'}`;
  },
  set: (value) => {
    if (!value) {
      sorting.value = [];
      return;
    }
    const [id, direction] = value.split(',');
    if (id) {
      sorting.value = [{ id, desc: direction === 'desc' }];
    }
  },
});

useQuerySync({
  state: sortQuery,
  queryKey: 'sort',
  serialize: (value) => value,
  deserialize: (value) => {
    if (typeof value === 'string' && value.includes(',')) {
      return value;
    }
    return undefined;
  },
});

// 이벤트 핸들러
function handleViewEmployee(employee: EmployeeListItem) {
  router.push({
    name: 'employee-detail',
    params: { employeeId: employee.employeeId },
  });
}

async function handleEditEmployee(employee: EmployeeListItem) {
  editingEmployee.value = null;
  isLoadingEmployee.value = true;
  const loadingToast = toast.loading('직원 정보를 불러오는 중입니다.');

  try {
    editingEmployee.value = await employeeRepository.findById(employee.employeeId);
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

function handleCopyEmail(employee: EmployeeListItem) {
  if (!employee.email) {
    toast.error('이메일 정보가 없습니다.');
    return;
  }
  navigator.clipboard
    .writeText(employee.email)
    .then(() => {
      toast.success('이메일을 클립보드에 복사했습니다.');
    })
    .catch(() => {
      toast.error('이메일 복사에 실패했습니다.');
    });
}

function handleDeleteEmployee(employee: EmployeeListItem) {
  deletion.open(employee.employeeId, employee.name);
}

function handleEmployeeUpdated() {
  isEmployeeUpdateDialogOpen.value = false;
  loadEmployees();
}

function handleNavigateToDepartment(departmentId?: string) {
  if (!departmentId) return;
  router.push({ name: 'organization', query: { departmentId } });
}

// 컬럼 정의 (공통 함수 재사용, 부서 컬럼 제외, 체크박스 포함)
const columns = createEmployeeTableColumns(
  {
    onViewEmployee: handleViewEmployee,
    onEditEmployee: handleEditEmployee,
    onCopyEmail: handleCopyEmail,
    onDeleteEmployee: handleDeleteEmployee,
    onNavigateToDepartment: handleNavigateToDepartment,
  },
  {
    excludeDepartmentColumn: true,
    excludeSelection: false, // 체크박스 포함
  },
);

// 테이블 인스턴스
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
    get rowSelection() {
      return rowSelection.value;
    },
  },
  onSortingChange: (updater) => valueUpdater(updater, sorting),
  onColumnFiltersChange: (updater) => valueUpdater(updater, columnFilters),
  onRowSelectionChange: (updater) => valueUpdater(updater, rowSelection),
  getCoreRowModel: getCoreRowModel(),
});

// API 호출
async function loadDepartments() {
  try {
    const chart = await repository.fetchOrganizationChart();
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

async function loadEmployees() {
  if (!props.departmentId) return;

  // EnumResponse 로드 대기
  if (!isEnumLoaded.value) {
    try {
      await Promise.all([
        employeeRepository.fetchStatuses(),
        employeeRepository.fetchTypes(),
        employeeRepository.fetchGrades(),
        employeeRepository.fetchPositions(),
        loadDepartments(),
      ]);

      // 옵션 값 업데이트
      statusOptions.value = getEmployeeStatusOptions();
      typeOptions.value = getEmployeeTypeOptions();
      gradeOptions.value = getEmployeeGradeOptions();
      positionOptions.value = getEmployeePositionOptions();

      isEnumLoaded.value = true;
    } catch (error) {
      console.error('Enum 옵션을 불러오지 못했습니다.', error);
    }
  }

  isLoading.value = true;
  try {
    const name = searchNameForQuery.value.trim() || undefined;
    const sort = sortQuery.value;

    const response = await repository.fetchDepartmentEmployees(props.departmentId, {
      page: page.value,
      size: pageSize.value,
      name,
      sort,
    });

    employees.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
  } catch (error) {
    console.error('부서 직원 목록을 불러오지 못했습니다.', error);
    toast.error('직원 목록을 불러오는데 실패했습니다.');
    employees.value = [];
    totalElements.value = 0;
    totalPages.value = 1;
  } finally {
    isLoading.value = false;
  }
}

// 이벤트 핸들러
function handlePageChange(newPage: number) {
  page.value = newPage;
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize;
  page.value = 1;
}

// 감시자 - 부서 변경 시
let previousDepartmentId: string | undefined;

watch(
  () => props.departmentId,
  (newDeptId, oldDeptId) => {
    // 초기 로드인지 부서 변경인지 구분
    const isDepartmentChange = previousDepartmentId !== undefined && newDeptId !== oldDeptId;
    previousDepartmentId = newDeptId;

    if (isDepartmentChange) {
      // 실제 부서가 변경된 경우에만 검색 조건 초기화
      page.value = 1;
      columnFilters.value = [];
      sorting.value = [];
      rowSelection.value = {};
    }

    // 항상 직원 목록 로드
    loadEmployees();
  },
  { immediate: true },
);

// 감시자 - URL 쿼리 파라미터 변경 시 (검색어, 페이지, 정렬)
watch([searchNameForQuery, page, pageSize, sortQuery], () => {
  // departmentId가 있을 때만 로드
  if (props.departmentId) {
    loadEmployees();
  }
});
</script>
