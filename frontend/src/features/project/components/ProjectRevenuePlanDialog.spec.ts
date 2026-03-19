import { defineComponent, h } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import type { Component } from 'vue';
import HttpError from '@/core/http/HttpError';

let ProjectRevenuePlanDialogComponent: Component;
const createMutateAsyncMock = vi.fn();
const updateMutateAsyncMock = vi.fn();
const toastSuccessMock = vi.fn();

vi.mock('vue-sonner', () => ({
  toast: {
    success: toastSuccessMock,
  },
}));

vi.mock('@/features/project/queries/useProjectQueries', () => ({
  useCreateProjectRevenuePlanMutation: () => ({
    mutateAsync: createMutateAsyncMock,
    isPending: { value: false },
  }),
  useUpdateProjectRevenuePlanMutation: () => ({
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
          onClick: () => emit('update:modelValue', new Date('2026-03-10')),
        },
        String(props.id),
      );
  },
});

const PassThrough = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.());
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
            'data-test': 'select-type',
            onClick: () => emit('update:modelValue', 'BALANCE_PAYMENT'),
          },
          '구분선택',
        ),
        slots.default?.(),
      ]);
  },
});

describe('ProjectRevenuePlanDialog', () => {
  beforeEach(async () => {
    vi.clearAllMocks();
    ProjectRevenuePlanDialogComponent = (await import('@/features/project/components/ProjectRevenuePlanDialog.vue')).default;
  }, 30000);

  it('edit mode에서 기존 값을 채우고 수정 요청을 보낸다', async () => {
    const { wrapper } = await renderWithProviders(ProjectRevenuePlanDialogComponent, {
      props: {
        open: true,
        projectId: 7,
        plan: {
          projectId: 7,
          sequence: 1,
          revenueDate: '2026-03-01',
          type: 'DOWN_PAYMENT',
          amount: 10000000,
          memo: '기존 메모',
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
          Alert: PassThrough,
          AlertTitle: PassThrough,
          AlertDescription: PassThrough,
          DatePicker: DatePickerStub,
          Select: SelectStub,
          SelectTrigger: true,
          SelectValue: true,
          SelectContent: true,
          SelectItem: true,
        },
      },
    });

    expect((wrapper.get('#sequence').element as HTMLInputElement).value).toBe('1');
    expect((wrapper.get('#description').element as HTMLTextAreaElement).value).toBe('기존 메모');

    await wrapper.get('#sequence').setValue('2');
    await wrapper.get('#amount').setValue('45000000');
    await wrapper.get('#description').setValue('수정 후');
    await wrapper.get('[data-test="plannedDate"]').trigger('click');
    await wrapper.get('[data-test="select-type"]').trigger('click');

    const saveButton = wrapper.findAll('button').find((button) => button.text() === '저장');
    await saveButton?.trigger('click');

    expect(updateMutateAsyncMock).toHaveBeenCalledWith({
      projectId: 7,
      sequence: 1,
      payload: {
        sequence: 2,
        revenueDate: '2026-03-10',
        type: 'BALANCE_PAYMENT',
        amount: 45000000,
        memo: '수정 후',
      },
    });
    expect(toastSuccessMock).toHaveBeenCalledWith('매출 일정을 수정했습니다.');
  });

  it('수정 실패 시 다이얼로그 내부에 오류 메시지를 표시한다', async () => {
    updateMutateAsyncMock.mockRejectedValueOnce(new HttpError({ code: '400', message: '회차가 중복됩니다.' }));

    const { wrapper } = await renderWithProviders(ProjectRevenuePlanDialogComponent, {
      props: {
        open: true,
        projectId: 7,
        plan: {
          projectId: 7,
          sequence: 1,
          revenueDate: '2026-03-01',
          type: 'DOWN_PAYMENT',
          amount: 10000000,
          memo: '기존 메모',
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
          Select: SelectStub,
          SelectTrigger: true,
          SelectValue: true,
          SelectContent: true,
          SelectItem: true,
        },
      },
    });

    const saveButton = wrapper.findAll('button').find((button) => button.text() === '저장');
    await saveButton?.trigger('click');

    expect(wrapper.text()).toContain('회차가 중복됩니다.');
  });
});
