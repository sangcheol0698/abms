import { mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';
import EmployeeEmploymentPanel from '@/features/employee/components/EmployeeEmploymentPanel.vue';
import { createMockQueryState } from '@/test-utils';
import type { EmployeeSummary } from '@/features/employee/models/employee';

vi.mock('@/features/employee/queries/useEmployeeQueries', () => ({
  useEmployeePositionHistoryQuery: () => createMockQueryState({ data: [] }),
}));

const employee: EmployeeSummary = {
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
  avatarCode: 'default',
  avatarLabel: '기본',
  avatarImageUrl: '',
  memo: '메모',
  joinDate: '2024-01-01',
  birthDate: '1990-01-01',
};

function mountPanel(showManagementActions = true) {
  return mount(EmployeeEmploymentPanel, {
    props: {
      employee,
      formatDate: (value?: string | null) => value ?? '—',
      showManagementActions,
      isResigning: false,
      resignError: null,
      resignSuccess: null,
      isTakingLeave: false,
      takeLeaveError: null,
      takeLeaveSuccess: null,
      isActivating: false,
      activateError: null,
      activateSuccess: null,
    },
  });
}

describe('EmployeeEmploymentPanel', () => {
  it('관리 권한이 없으면 관리 액션 섹션을 숨긴다', () => {
    const wrapper = mountPanel(false);

    expect(wrapper.text()).not.toContain('관리 메모');
    expect(wrapper.text()).not.toContain('상태 변경');
    expect(wrapper.text()).not.toContain('퇴사 처리');
    expect(wrapper.text()).not.toContain('휴직 처리');
    expect(wrapper.text()).not.toContain('재직 처리');
  });

  it('관리 권한이 있으면 상태 변경 액션을 렌더링하고 실행할 수 있다', async () => {
    const wrapper = mountPanel(true);
    const takeLeaveButton = wrapper.findAll('button').find((button) => button.text().includes('휴직 처리'));
    const activateButton = wrapper.findAll('button').find((button) => button.text().includes('재직 처리'));
    const resignButton = wrapper.findAll('button').find((button) => button.text().includes('퇴사 처리'));
    const resignationDate = (wrapper.get('input[placeholder="퇴사일을 선택하세요"]').element as HTMLInputElement).value;

    expect(wrapper.text()).toContain('관리 메모');
    expect(wrapper.text()).toContain('상태 변경');
    expect(wrapper.text()).toContain('퇴사 처리');

    await takeLeaveButton?.trigger('click');
    await resignButton?.trigger('click');

    expect(wrapper.emitted('take-leave')).toHaveLength(1);
    expect(activateButton?.attributes('disabled')).toBeDefined();
    expect(wrapper.emitted('activate')).toBeUndefined();
    expect(wrapper.emitted('resign')).toEqual([[resignationDate]]);
  });
});
