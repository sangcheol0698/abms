import { computed, defineComponent, h, ref, toValue, type MaybeRefOrGetter } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { renderWithProviders } from '@/test-utils';
import PartySelectDialog from '@/features/party/components/PartySelectDialog.vue';
import { toast } from 'vue-sonner';

const latestParams = vi.fn();
const queryError = ref<unknown>(null);

vi.mock('@/features/party/queries/usePartyQueries', () => ({
  usePartyListQuery: (paramsRef: MaybeRefOrGetter<{ page: number; size: number; name?: string }>) => {
    const data = computed(() => {
      const params = toValue(paramsRef);
      latestParams(params);

      if (params.name === '검색어') {
        return {
          content: [
            {
              partyId: 2,
              name: '검색 협력사',
              ceo: '대표B',
              manager: '담당B',
              contact: '010-2222-2222',
              email: 'b@party.co.kr',
            },
          ],
          totalPages: 1,
          totalElements: 1,
        };
      }

      return {
        content: [
          {
            partyId: 1,
            name: '기본 협력사',
            ceo: '대표A',
            manager: '담당A',
            contact: '010-1111-1111',
            email: 'a@party.co.kr',
          },
        ],
        totalPages: 2,
        totalElements: 11,
      };
    });

    return {
      data,
      isLoading: { value: false },
      isFetching: { value: false },
      error: queryError,
    };
  },
}));

vi.mock('vue-sonner', () => ({
  toast: {
    error: vi.fn(),
    info: vi.fn(),
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
    modelValue: {
      type: String,
      default: '',
    },
    placeholder: String,
  },
  emits: ['update:modelValue', 'keydown'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        value: props.modelValue,
        placeholder: props.placeholder,
        onInput: (event: Event) =>
          emit('update:modelValue', (event.target as HTMLInputElement).value),
        onKeydown: (event: KeyboardEvent) => emit('keydown', event),
      });
  },
});

async function mountPartySelectDialog(props: Record<string, unknown> = { open: true }) {
  return renderWithProviders(PartySelectDialog, {
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
      },
    },
  });
}

describe('PartySelectDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    queryError.value = null;
    vi.spyOn(console, 'warn').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('검색어를 적용하면 query key 파라미터가 변경된다', async () => {
    const { wrapper } = await mountPartySelectDialog();
    const searchInput = wrapper.get('input');

    latestParams.mockClear();
    await searchInput.setValue('검색어');
    await searchInput.trigger('keydown.enter');
    await flushPromises();

    expect(latestParams).toHaveBeenLastCalledWith({
      page: 1,
      size: 10,
      name: '검색어',
    });
    expect(wrapper.text()).toContain('검색 협력사');
  });

  it('협력사를 선택하고 확인하면 select와 close 이벤트를 발생시킨다', async () => {
    const { wrapper } = await mountPartySelectDialog({ open: true, selectedPartyId: 1 });

    const row = wrapper.findAll('tbody tr')[0];
    await row.trigger('click');
    await wrapper.findAll('button').find((item) => item.text() === '선택')?.trigger('click');

    expect(wrapper.emitted('select')).toEqual([[{ partyId: 1, partyName: '기본 협력사' }]]);
    expect(wrapper.emitted('update:open')).toEqual([[false]]);
  });

  it('열린 상태에서 query 오류가 있으면 토스트를 표시한다', async () => {
    await mountPartySelectDialog();
    queryError.value = new Error('network');
    await flushPromises();

    expect(toast.error).toHaveBeenCalledWith('협력사 목록을 불러오지 못했습니다.', {
      description: '네트워크 상태를 확인한 뒤 다시 시도해 주세요.',
    });
  });
});
