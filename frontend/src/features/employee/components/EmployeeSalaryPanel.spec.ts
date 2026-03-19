import { computed, ref } from 'vue';
import { mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';
import EmployeeSalaryPanel from '@/features/employee/components/EmployeeSalaryPanel.vue';

const currentPayroll = ref({
  employeeId: 1,
  annualSalary: 72000000,
  monthlySalary: 6000000,
  startDate: '2025-01-01',
  status: 'CURRENT' as const,
});

const payrollHistory = ref([
  {
    employeeId: 1,
    annualSalary: 72000000,
    monthlySalary: 6000000,
    startDate: '2025-01-01',
    endDate: null,
    status: 'CURRENT' as const,
  },
]);

const refetchMock = vi.fn().mockResolvedValue(undefined);

vi.mock('@/features/employee/queries/useEmployeeQueries', () => ({
  useEmployeeCurrentPayrollQuery: () => ({
    data: computed(() => currentPayroll.value),
    isLoading: { value: false },
    error: { value: null },
    refetch: refetchMock,
  }),
  useEmployeePayrollHistoryQuery: () => ({
    data: computed(() => payrollHistory.value),
    isLoading: { value: false },
    error: { value: null },
    refetch: refetchMock,
  }),
}));

vi.mock('@/features/employee/components/EmployeeSalaryChangeDialog.vue', () => ({
  default: {
    name: 'EmployeeSalaryChangeDialog',
    template: '<div data-test="salary-dialog" />',
    props: ['open', 'employeeId'],
    emits: ['update:open', 'changed'],
  },
}));

describe('EmployeeSalaryPanel', () => {
  it('관리 권한이 없으면 연봉 변경 버튼을 숨긴다', () => {
    const wrapper = mount(EmployeeSalaryPanel, {
      props: {
        employeeId: 1,
        showManagementActions: false,
      },
    });

    expect(wrapper.text()).toContain('72,000,000원');
    const changeButton = wrapper.findAll('button').find((button) => button.text().includes('연봉 변경'));
    expect(changeButton).toBeUndefined();
  });

  it('연봉 이력이 없으면 빈 상태를 보여준다', () => {
    currentPayroll.value = null;
    payrollHistory.value = [];

    const wrapper = mount(EmployeeSalaryPanel, {
      props: {
        employeeId: 1,
        showManagementActions: true,
      },
    });

    expect(wrapper.text()).toContain('등록된 연봉 이력이 없습니다.');
    expect(wrapper.text()).toContain('등록되지 않음');
  });

  it('미래 시작 연봉 이력은 예정 상태로 표시한다', () => {
    payrollHistory.value = [
      {
        employeeId: 1,
        annualSalary: 350000000,
        monthlySalary: 29166667,
        startDate: '2099-03-20',
        endDate: null,
        status: 'SCHEDULED',
      },
    ];

    const wrapper = mount(EmployeeSalaryPanel, {
      props: {
        employeeId: 1,
        showManagementActions: true,
      },
    });

    expect(wrapper.text()).toContain('예정');
    expect(wrapper.text()).toContain('2099-03-20');
  });
});
