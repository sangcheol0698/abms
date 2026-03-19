import { defineComponent, h } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import type { Component } from 'vue';

let ProjectAssignmentEndDialogComponent: Component;
const endMutateAsyncMock = vi.fn();

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useEndProjectAssignmentMutation: () => ({
    mutateAsync: endMutateAsyncMock,
    isPending: { value: false },
  }),
}));

const DatePickerStub = defineComponent({
  emits: ['update:modelValue'],
  setup(_, { emit }) {
    return () =>
      h(
        'button',
        {
          type: 'button',
          'data-test': 'end-date',
          onClick: () => emit('update:modelValue', new Date('2026-03-15')),
        },
        '종료일선택',
      );
  },
});

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
  },
});

describe('ProjectAssignmentEndDialog', () => {
  beforeEach(async () => {
    vi.clearAllMocks();
    ProjectAssignmentEndDialogComponent = (await import('@/features/project/components/ProjectAssignmentEndDialog.vue')).default;
  }, 30000);

  it('종료일을 입력해 투입 종료를 처리한다', async () => {
    const { wrapper } = await renderWithProviders(ProjectAssignmentEndDialogComponent, {
      props: {
        open: true,
        projectId: 7,
        projectEndDate: '2026-12-31',
        assignment: {
          id: 3,
          projectId: 7,
          employeeId: 10,
          employeeName: '김개발',
          departmentId: 100,
          departmentName: '백엔드팀',
          role: 'DEV',
          roleLabel: '개발자',
          startDate: '2026-03-01',
          endDate: null,
          assignmentStatus: 'CURRENT',
          assignmentStatusLabel: '현재 투입',
        },
      },
      global: {
        stubs: {
          Dialog: PassThrough,
          DialogContent: PassThrough,
          DialogHeader: PassThrough,
          DialogTitle: PassThrough,
          DialogDescription: PassThrough,
          DialogFooter: PassThrough,
          DatePicker: DatePickerStub,
        },
      },
    });

    await wrapper.get('[data-test="end-date"]').trigger('click');

    const submitButton = wrapper.findAll('button').find((button) => button.text() === '종료 처리');
    await submitButton?.trigger('click');

    expect(endMutateAsyncMock).toHaveBeenCalledWith({
      assignmentId: 3,
      projectId: 7,
      payload: {
        endDate: '2026-03-15',
      },
    });
  });
});
