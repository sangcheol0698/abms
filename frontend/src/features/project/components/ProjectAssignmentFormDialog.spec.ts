import { defineComponent, h } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import type { Component } from 'vue';

let ProjectAssignmentFormDialogComponent: Component;
const createMutateAsyncMock = vi.fn();
const updateMutateAsyncMock = vi.fn();

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useCreateProjectAssignmentMutation: () => ({
    mutateAsync: createMutateAsyncMock,
    isPending: { value: false },
  }),
  useUpdateProjectAssignmentMutation: () => ({
    mutateAsync: updateMutateAsyncMock,
    isPending: { value: false },
  }),
}));

const DatePickerStub = defineComponent({
  props: ['id'],
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h(
        'button',
        {
          type: 'button',
          'data-test': props.id,
          onClick: () => emit('update:modelValue', new Date(props.id === 'startDate' ? '2026-03-10' : '2026-03-20')),
        },
        String(props.id),
      );
  },
});

const EmployeeSelectStub = defineComponent({
  emits: ['select', 'update:open'],
  setup(_, { emit }) {
    return () =>
      h(
        'button',
        {
          type: 'button',
          'data-test': 'select-employee',
          onClick: () =>
            emit('select', {
              employeeId: 10,
              employeeName: '김개발',
              departmentId: 100,
              departmentName: '백엔드팀',
            }),
        },
        '직원선택',
      );
  },
});

const SelectStub = defineComponent({
  emits: ['update:modelValue'],
  setup(_, { emit, slots }) {
    return () =>
      h('div', [
        h(
          'button',
          {
            type: 'button',
            'data-test': 'select-role',
            onClick: () => emit('update:modelValue', 'DEV'),
          },
          '역할선택',
        ),
        slots.default?.(),
      ]);
  },
});

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
  },
});

describe('ProjectAssignmentFormDialog', () => {
  beforeEach(async () => {
    vi.clearAllMocks();
    ProjectAssignmentFormDialogComponent = (await import('@/features/project/components/ProjectAssignmentFormDialog.vue')).default;
  }, 30000);

  it('신규 투입 인력을 생성한다', async () => {
    const { wrapper } = await renderWithProviders(ProjectAssignmentFormDialogComponent, {
      props: {
        open: true,
        projectId: 7,
        projectStartDate: '2026-03-01',
        projectEndDate: '2026-12-31',
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
          ProjectAssignmentEmployeeSelectDialog: EmployeeSelectStub,
          Select: SelectStub,
          SelectTrigger: true,
          SelectValue: true,
          SelectContent: true,
          SelectItem: true,
        },
      },
    });

    await wrapper.get('[data-test="select-employee"]').trigger('click');
    await wrapper.get('[data-test="select-role"]').trigger('click');
    await wrapper.get('[data-test="startDate"]').trigger('click');
    await wrapper.get('[data-test="endDate"]').trigger('click');

    const saveButton = wrapper.findAll('button').find((button) => button.text() === '저장');
    await saveButton?.trigger('click');

    expect(createMutateAsyncMock).toHaveBeenCalledWith({
      projectId: 7,
      employeeId: 10,
      role: 'DEV',
      startDate: '2026-03-10',
      endDate: '2026-03-20',
    });
  });

  it('기존 투입 인력을 수정한다', async () => {
    const { wrapper } = await renderWithProviders(ProjectAssignmentFormDialogComponent, {
      props: {
        open: true,
        projectId: 7,
        projectStartDate: '2026-03-01',
        projectEndDate: '2026-12-31',
        assignment: {
          id: 3,
          projectId: 7,
          employeeId: 10,
          employeeName: '김개발',
          departmentId: 100,
          departmentName: '백엔드팀',
          role: 'PM',
          roleLabel: 'PM',
          startDate: '2026-03-01',
          endDate: '2026-03-31',
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
          ProjectAssignmentEmployeeSelectDialog: EmployeeSelectStub,
          Select: SelectStub,
          SelectTrigger: true,
          SelectValue: true,
          SelectContent: true,
          SelectItem: true,
        },
      },
    });

    await wrapper.get('[data-test="select-role"]').trigger('click');
    await wrapper.get('[data-test="startDate"]').trigger('click');
    await wrapper.get('[data-test="endDate"]').trigger('click');

    const saveButton = wrapper.findAll('button').find((button) => button.text() === '저장');
    await saveButton?.trigger('click');

    expect(updateMutateAsyncMock).toHaveBeenCalledWith({
      assignmentId: 3,
      projectId: 7,
      payload: {
        employeeId: 10,
        role: 'DEV',
        startDate: '2026-03-10',
        endDate: '2026-03-20',
      },
    });
  });
});
