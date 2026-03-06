import { defineComponent, h } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { renderWithProviders } from '@/test-utils';
import PartyFormDialog from '@/features/party/components/PartyFormDialog.vue';
import { toast } from 'vue-sonner';

const createMutateAsyncMock = vi.fn();
const updateMutateAsyncMock = vi.fn();

vi.mock('@/features/party/queries/usePartyQueries', () => ({
  useCreatePartyMutation: () => ({
    mutateAsync: createMutateAsyncMock,
  }),
  useUpdatePartyMutation: () => ({
    mutateAsync: updateMutateAsyncMock,
  }),
}));

vi.mock('vue-sonner', () => ({
  toast: {
    error: vi.fn(),
  },
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

const InputStub = defineComponent({
  props: {
    id: String,
    modelValue: {
      type: String,
      default: '',
    },
    type: String,
    placeholder: String,
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        id: props.id,
        value: props.modelValue,
        type: props.type,
        placeholder: props.placeholder,
        onInput: (event: Event) =>
          emit('update:modelValue', (event.target as HTMLInputElement).value),
      });
  },
});

const PhoneNumberInputStub = defineComponent({
  props: {
    id: String,
    modelValue: {
      type: String,
      default: '',
    },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        id: props.id,
        value: props.modelValue,
        onInput: (event: Event) =>
          emit('update:modelValue', (event.target as HTMLInputElement).value),
      });
  },
});

async function mountPartyFormDialog(
  props: Record<string, unknown> = {
    open: true,
    mode: 'create',
  },
) {
  return renderWithProviders(PartyFormDialog, {
    props,
    global: {
      stubs: {
        Dialog: PassThrough,
        DialogContent: PassThrough,
        DialogDescription: PassThrough,
        DialogFooter: PassThrough,
        DialogHeader: PassThrough,
        DialogTitle: PassThrough,
        Button: ButtonStub,
        Input: InputStub,
        Label: PassThrough,
        PhoneNumberInput: PhoneNumberInputStub,
      },
    },
  });
}

describe('PartyFormDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    createMutateAsyncMock.mockResolvedValue(undefined);
    updateMutateAsyncMock.mockResolvedValue(undefined);
  });

  it('생성 모드에서 mutation composable을 통해 협력사를 등록한다', async () => {
    const { wrapper } = await mountPartyFormDialog();

    await wrapper.get('#name').setValue('협력사A');
    await wrapper.get('#ceoName').setValue('대표A');
    await wrapper.get('#salesRepName').setValue('담당A');
    await wrapper.get('#salesRepPhone').setValue('010-1234-5678');
    await wrapper.get('#salesRepEmail').setValue('manager@party.co.kr');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(createMutateAsyncMock).toHaveBeenCalledWith({
      name: '협력사A',
      ceoName: '대표A',
      salesRepName: '담당A',
      salesRepPhone: '010-1234-5678',
      salesRepEmail: 'manager@party.co.kr',
    });
    expect(wrapper.emitted('created')).toHaveLength(1);
  });

  it('수정 모드에서 기존 값을 채우고 update mutation을 호출한다', async () => {
    const { wrapper } = await mountPartyFormDialog({
      open: true,
      mode: 'edit',
      party: {
        partyId: 3,
        name: '기존 협력사',
        ceo: '기존 대표',
        manager: '기존 담당',
        contact: '010-0000-0000',
        email: 'legacy@party.co.kr',
      },
    });

    expect((wrapper.get('#name').element as HTMLInputElement).value).toBe('기존 협력사');
    await wrapper.get('#name').setValue('변경 협력사');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(updateMutateAsyncMock).toHaveBeenCalledWith({
      partyId: 3,
      data: {
        name: '변경 협력사',
        ceoName: '기존 대표',
        salesRepName: '기존 담당',
        salesRepPhone: '010-0000-0000',
        salesRepEmail: 'legacy@party.co.kr',
      },
    });
    expect(wrapper.emitted('updated')).toHaveLength(1);
  });

  it('mutation 실패 시 기존 에러 토스트를 유지한다', async () => {
    createMutateAsyncMock.mockRejectedValueOnce(new Error('boom'));
    const { wrapper } = await mountPartyFormDialog();

    await wrapper.get('#name').setValue('실패 협력사');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(toast.error).toHaveBeenCalledWith('협력사 등록에 실패했습니다.');
  });
});
