import { computed, defineComponent, h, ref } from 'vue';
import type { Component } from 'vue';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';

const employeeData = ref({
  departmentId: 10,
  departmentName: '개발팀',
  employeeId: 1,
  name: '홍길동',
  email: 'hong@abms.co.kr',
  position: '사원',
  positionCode: 'ASSOCIATE',
  status: '재직',
  statusCode: 'ACTIVE',
  grade: '초급',
  gradeCode: 'JUNIOR',
  type: '정규직',
  typeCode: 'FULL_TIME',
  avatarCode: 'SKY_GLOW',
  avatarLabel: 'Sky Glow',
  avatarImageUrl: '',
  memo: '',
  joinDate: '2024-01-01',
  birthDate: '1990-01-01',
});

const departmentChartData = ref([
  {
    departmentId: 10,
    departmentName: '개발본부',
    departmentCode: 'DEV',
    departmentType: 'DIVISION',
    departmentLeader: null,
    employeeCount: 1,
    children: [
      {
        departmentId: 11,
        departmentName: '개발팀',
        departmentCode: 'DEV-TEAM',
        departmentType: 'TEAM',
        departmentLeader: null,
        employeeCount: 1,
        children: [],
      },
    ],
  },
]);

const dispatchOpenProfileDialogEventMock = vi.fn();
let storage: Record<string, string> = {};
let EmployeeDetailViewComponent: Component;

vi.mock('@/features/employee/queries/useEmployeeQueries', () => ({
  useEmployeeDetailQuery: () => ({
    data: computed(() => employeeData.value),
    isLoading: { value: false },
    error: { value: null },
    refetch: vi.fn(),
  }),
  useEmployeeTypesQuery: () => ({ data: { value: [] } }),
  useEmployeeGradesQuery: () => ({ data: { value: [] } }),
  useEmployeePositionsQuery: () => ({ data: { value: [] } }),
  useResignEmployeeMutation: () => ({ mutateAsync: vi.fn(), isPending: { value: false } }),
  useTakeLeaveEmployeeMutation: () => ({ mutateAsync: vi.fn(), isPending: { value: false } }),
  useActivateEmployeeMutation: () => ({ mutateAsync: vi.fn(), isPending: { value: false } }),
  useDeleteEmployeeMutation: () => ({ mutateAsync: vi.fn(), isPending: { value: false } }),
}));

vi.mock('@/features/department/queries/useDepartmentQueries', () => ({
  useDepartmentOrganizationChartQuery: () => ({
    data: departmentChartData,
  }),
}));

vi.mock('@/features/auth/profileDialogEvents', () => ({
  dispatchOpenProfileDialogEvent: dispatchOpenProfileDialogEventMock,
}));

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
  },
});

const ButtonStub = defineComponent({
  props: {
    disabled: Boolean,
    type: {
      type: String,
      default: 'button',
    },
  },
  emits: ['click'],
  setup(props, { emit, slots }) {
    return () =>
      h(
        'button',
        {
          type: props.type,
          disabled: props.disabled,
          onClick: (event: MouseEvent) => emit('click', event),
        },
        slots.default?.(),
      );
  },
});

const EmployeeDetailHeaderStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', [slots.actions?.()]);
  },
});

const EmployeeEmploymentPanelStub = defineComponent({
  props: {
    showManagementActions: Boolean,
  },
  setup(props) {
    return () => h('div', { 'data-test': 'employment-manage' }, String(props.showManagementActions));
  },
});

async function mountDetailView() {
  return renderWithProviders(EmployeeDetailViewComponent, {
    route: '/employees/1',
    routes: [
      { path: '/employees/:employeeId', name: 'employee-detail', component: EmployeeDetailViewComponent },
      { path: '/employees', name: 'employees', component: { template: '<div />' } },
      { path: '/departments/:departmentId', name: 'department', component: { template: '<div />' } },
    ],
    global: {
      stubs: {
        Alert: PassThrough,
        AlertDescription: PassThrough,
        AlertTitle: PassThrough,
        Button: ButtonStub,
        Tabs: PassThrough,
        TabsList: PassThrough,
        TabsTrigger: ButtonStub,
        TabsContent: PassThrough,
        AlertDialog: PassThrough,
        AlertDialogAction: ButtonStub,
        AlertDialogCancel: ButtonStub,
        AlertDialogContent: PassThrough,
        AlertDialogDescription: PassThrough,
        AlertDialogFooter: PassThrough,
        AlertDialogHeader: PassThrough,
        AlertDialogTitle: PassThrough,
        AlertDialogTrigger: PassThrough,
        EmployeeDetailHeader: EmployeeDetailHeaderStub,
        EmployeeOverviewPanel: true,
        EmployeeEmploymentPanel: EmployeeEmploymentPanelStub,
        EmployeeSalaryPanel: true,
        EmployeeProjectsPanel: true,
        EmployeeUpdateDialog: true,
        EmployeePromotionDialog: true,
        Pencil: true,
        Trash2: true,
        TrendingUp: true,
      },
    },
  });
}

describe('EmployeeDetailView', () => {
  beforeAll(async () => {
    vi.stubGlobal('localStorage', {
      getItem: vi.fn((key: string) => storage[key] ?? null),
      setItem: vi.fn((key: string, value: string) => {
        storage[key] = String(value);
      }),
      removeItem: vi.fn((key: string) => {
        delete storage[key];
      }),
      clear: vi.fn(() => {
        storage = {};
      }),
    });
    EmployeeDetailViewComponent = (await import('@/features/employee/views/EmployeeDetailView.vue')).default;
  });

  beforeEach(() => {
    vi.clearAllMocks();
    storage = {};
    employeeData.value = {
      departmentId: 10,
      departmentName: '개발팀',
      employeeId: 1,
      name: '홍길동',
      email: 'hong@abms.co.kr',
      position: '사원',
      positionCode: 'ASSOCIATE',
      status: '재직',
      statusCode: 'ACTIVE',
      grade: '초급',
      gradeCode: 'JUNIOR',
      type: '정규직',
      typeCode: 'FULL_TIME',
      avatarCode: 'SKY_GLOW',
      avatarLabel: 'Sky Glow',
      avatarImageUrl: '',
      memo: '',
      joinDate: '2024-01-01',
      birthDate: '1990-01-01',
    };
  });

  it('employee.write ALL 권한이면 관리자 액션이 보인다', async () => {
    storage.user = JSON.stringify({
      name: '관리자',
      email: 'admin@abms.co.kr',
      employeeId: 99,
      departmentId: 10,
      permissions: [
        { code: 'employee.read', scopes: ['ALL'] },
        { code: 'employee.write', scopes: ['ALL'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountDetailView();

    expect(wrapper.text()).toContain('승진');
    expect(wrapper.text()).toContain('직원 편집');
    expect(wrapper.text()).toContain('직원 삭제');
    expect(wrapper.get('[data-test="employment-manage"]').text()).toBe('true');
  });

  it('employee.write SELF 권한이면 내 정보 수정만 보인다', async () => {
    storage.user = JSON.stringify({
      name: '홍길동',
      email: 'hong@abms.co.kr',
      employeeId: 1,
      departmentId: 10,
      permissions: [
        { code: 'employee.read', scopes: ['SELF'] },
        { code: 'employee.write', scopes: ['SELF'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountDetailView();
    const selfEditButton = wrapper.findAll('button').find((button) => button.text().includes('내 정보 수정'));

    expect(selfEditButton).toBeDefined();
    expect(wrapper.text()).not.toContain('승진');
    expect(wrapper.text()).not.toContain('직원 편집');
    expect(wrapper.text()).not.toContain('직원 삭제');
    expect(wrapper.get('[data-test="employment-manage"]').text()).toBe('false');

    await selfEditButton?.trigger('click');
    expect(dispatchOpenProfileDialogEventMock).toHaveBeenCalled();
  });

  it('employee.write OWN_DEPARTMENT 권한이면 같은 부서 직원 관리 액션이 보인다', async () => {
    employeeData.value = {
      ...employeeData.value,
      employeeId: 2,
      departmentId: 10,
      email: 'same@abms.co.kr',
    };
    storage.user = JSON.stringify({
      name: '매니저',
      email: 'manager@abms.co.kr',
      employeeId: 99,
      departmentId: 10,
      permissions: [
        { code: 'employee.read', scopes: ['OWN_DEPARTMENT'] },
        { code: 'employee.write', scopes: ['OWN_DEPARTMENT'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountDetailView();

    expect(wrapper.text()).toContain('승진');
    expect(wrapper.text()).toContain('직원 편집');
    expect(wrapper.text()).toContain('직원 삭제');
  });

  it('employee.write OWN_DEPARTMENT_TREE 권한이면 하위 부서 직원 관리 액션이 보인다', async () => {
    employeeData.value = {
      ...employeeData.value,
      employeeId: 3,
      departmentId: 11,
      email: 'child@abms.co.kr',
    };
    storage.user = JSON.stringify({
      name: '매니저',
      email: 'manager@abms.co.kr',
      employeeId: 99,
      departmentId: 10,
      permissions: [
        { code: 'employee.read', scopes: ['OWN_DEPARTMENT_TREE'] },
        { code: 'employee.write', scopes: ['OWN_DEPARTMENT_TREE'] },
      ],
    });
    localStorage.setItem('user', storage.user);

    const { wrapper } = await mountDetailView();

    expect(wrapper.text()).toContain('승진');
    expect(wrapper.text()).toContain('직원 편집');
    expect(wrapper.text()).toContain('직원 삭제');
  });
});
