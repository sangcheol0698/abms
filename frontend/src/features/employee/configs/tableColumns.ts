/**
 * 직원 목록 테이블 컬럼 정의
 *
 * EmployeeListView의 TanStack Table 컬럼 정의
 */
import { h } from 'vue';
import type { ColumnDef } from '@tanstack/vue-table';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { DataTableColumnHeader } from '@/components/business';
import EmployeeRowActions from '@/features/employee/components/EmployeeRowActions.vue';

/**
 * 컬럼 정의를 위한 이벤트 핸들러 인터페이스
 */
export interface EmployeeTableHandlers {
  /** 직원 상세 보기 */
  onViewEmployee: (employee: EmployeeListItem) => void;
  /** 직원 수정 */
  onEditEmployee: (employee: EmployeeListItem) => void;
  /** 이메일 복사 */
  onCopyEmail: (employee: EmployeeListItem) => void;
  /** 직원 삭제 */
  onDeleteEmployee: (employee: EmployeeListItem) => void;
  /** 부서로 이동 */
  onNavigateToDepartment: (departmentId?: string) => void;
}

/**
 * 날짜를 한국어 형식으로 포맷
 *
 * @param value - 날짜 문자열
 * @returns 포맷된 날짜 문자열 (예: 2023. 12. 20.) 또는 '-'
 */
function formatDisplayDate(value?: string | null): string {
  if (!value) return '-';
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return parsed.toLocaleDateString('ko-KR');
}

/**
 * 직원 목록 테이블 컬럼 정의 생성
 *
 * TanStack Table에서 사용할 컬럼 정의 배열을 생성합니다.
 * 각 컬럼은 정렬, 필터링, 렌더링 로직을 포함합니다.
 *
 * @param handlers - 이벤트 핸들러 객체
 * @returns TanStack Table ColumnDef 배열
 *
 * @example
 * ```typescript
 * const columns = createEmployeeTableColumns({
 *   onViewEmployee: (emp) => router.push(`/employees/${emp.employeeId}`),
 *   onEditEmployee: (emp) => openEditDialog(emp),
 *   onCopyEmail: (emp) => copyToClipboard(emp.email),
 *   onDeleteEmployee: (emp) => confirmDelete(emp),
 *   onNavigateToDepartment: (id) => router.push(`/organization?dept=${id}`),
 * });
 * ```
 */
export function createEmployeeTableColumns(
  handlers: EmployeeTableHandlers,
): ColumnDef<EmployeeListItem>[] {
  return [
    // 체크박스 컬럼 (선택)
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

    // 이름 컬럼 (아바타 + 이름 + 이메일)
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
                  handlers.onViewEmployee(row.original);
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

    // 부서 컬럼 (클릭 시 부서 페이지로 이동)
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
              handlers.onNavigateToDepartment(row.original.departmentId);
            },
          },
          row.original.departmentName ?? '',
        ),
      enableSorting: false,
      size: 160,
      meta: { skeleton: 'text-short' },
    },

    // 부서 ID 컬럼 (필터 전용, 화면에 표시 안 됨)
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

    // 직책 컬럼
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
      enableSorting: true,
      size: 120,
      meta: { skeleton: 'text-short' },
    },

    // 등급 컬럼
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
      enableSorting: true,
      size: 80,
      meta: { skeleton: 'text-short' },
    },

    // 유형 컬럼
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

    // 상태 컬럼 (Badge로 표시)
    {
      id: 'status',
      accessorFn: (row) => row.statusLabel,
      header: ({ column }) => h(DataTableColumnHeader, { column, title: '상태', align: 'left' }),
      cell: ({ row }) =>
        h(
          Badge,
          { variant: 'outline', class: 'font-medium' },
          () => row.original.statusLabel ?? '',
        ),
      filterFn: (row, _columnId, filterValue) => {
        const candidate = Array.isArray(filterValue) ? filterValue : [filterValue];
        return candidate.length === 0 || candidate.includes(row.original.statusCode);
      },
      enableSorting: false,
      size: 90,
      meta: { skeleton: 'enum-badge' },
    },

    // 생년월일 컬럼
    {
      id: 'birthDate',
      accessorFn: (row) => row.birthDate ?? '',
      header: ({ column }) =>
        h(DataTableColumnHeader, { column, title: '생년월일', align: 'left' }),
      cell: ({ row }) =>
        h('span', { class: 'text-sm text-foreground' }, formatDisplayDate(row.original.birthDate)),
      enableSorting: true,
      enableHiding: true,
      size: 120,
      meta: { skeleton: 'text-short' },
    },

    // 입사일 컬럼
    {
      id: 'joinDate',
      accessorFn: (row) => row.joinDate ?? '',
      header: ({ column }) => h(DataTableColumnHeader, { column, title: '입사일', align: 'left' }),
      cell: ({ row }) =>
        h('span', { class: 'text-sm text-foreground' }, formatDisplayDate(row.original.joinDate)),
      enableSorting: true,
      enableHiding: true,
      size: 120,
      meta: { skeleton: 'text-short' },
    },

    // 액션 컬럼 (수정, 삭제 등)
    {
      id: 'actions',
      header: () => null,
      cell: ({ row }) =>
        h(EmployeeRowActions, {
          row: row.original,
          onEdit: () => handlers.onEditEmployee(row.original),
          onCopyEmail: () => handlers.onCopyEmail(row.original),
          onDelete: () => handlers.onDeleteEmployee(row.original),
        }),
      enableSorting: false,
      enableHiding: false,
      size: 56,
      meta: { skeleton: 'icon-button' },
    },
  ];
}
