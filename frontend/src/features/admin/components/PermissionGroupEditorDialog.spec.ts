import { defineComponent, h } from 'vue';
import { describe, expect, it } from 'vitest';
import { renderWithProviders } from '@/test-utils';
import PermissionGroupEditorDialog from '@/features/admin/components/PermissionGroupEditorDialog.vue';

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
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        value: props.modelValue,
        placeholder: props.placeholder,
        onInput: (event: Event) =>
          emit('update:modelValue', (event.target as HTMLInputElement).value),
      });
  },
});

const TextareaStub = InputStub;

const CheckboxStub = defineComponent({
  props: {
    modelValue: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        type: 'checkbox',
        checked: props.modelValue,
        onChange: (event: Event) =>
          emit('update:modelValue', (event.target as HTMLInputElement).checked),
      });
  },
});

async function mountDialog() {
  return renderWithProviders(PermissionGroupEditorDialog, {
    props: {
      open: true,
      mode: 'create',
      catalog: {
        permissions: [
          {
            code: 'employee.read',
            name: '직원 조회',
            description: '직원 상세 조회',
            availableScopes: [
              {
                code: 'SELF',
                description: '본인',
                level: 5,
              },
            ],
          },
        ],
        scopes: [
          {
            code: 'SELF',
            description: '본인',
            level: 5,
          },
        ],
      },
    },
    global: {
      stubs: {
        Badge: PassThrough,
        Button: ButtonStub,
        Checkbox: CheckboxStub,
        Dialog: PassThrough,
        DialogContent: PassThrough,
        DialogDescription: PassThrough,
        DialogFooter: PassThrough,
        DialogHeader: PassThrough,
        DialogTitle: PassThrough,
        Input: InputStub,
        Label: PassThrough,
        ScrollArea: PassThrough,
        Textarea: TextareaStub,
      },
    },
  });
}

async function mountEditDialogWithLegacyScope() {
  return renderWithProviders(PermissionGroupEditorDialog, {
    props: {
      open: true,
      mode: 'edit',
      catalog: {
        permissions: [
          {
            code: 'party.read',
            name: '협력사 조회',
            description: '협력사 조회',
            availableScopes: [
              {
                code: 'ALL',
                description: '전체',
                level: 1,
              },
            ],
          },
        ],
        scopes: [
          {
            code: 'ALL',
            description: '전체',
            level: 1,
          },
          {
            code: 'SELF',
            description: '본인',
            level: 5,
          },
        ],
      },
      initialGroup: {
        id: 1,
        name: '레거시 그룹',
        description: '이전 정책 그룹',
        groupType: 'CUSTOM',
        grants: [
          {
            permissionCode: 'party.read',
            permissionName: '협력사 조회',
            permissionDescription: '협력사 조회',
            scopes: ['SELF'],
          },
        ],
        accounts: [],
      },
    },
    global: {
      stubs: {
        Badge: PassThrough,
        Button: ButtonStub,
        Checkbox: CheckboxStub,
        Dialog: PassThrough,
        DialogContent: PassThrough,
        DialogDescription: PassThrough,
        DialogFooter: PassThrough,
        DialogHeader: PassThrough,
        DialogTitle: PassThrough,
        Input: InputStub,
        Label: PassThrough,
        ScrollArea: PassThrough,
        Textarea: TextareaStub,
      },
    },
  });
}

describe('PermissionGroupEditorDialog', () => {
  it('선택한 scope를 grants payload로 submit 한다', async () => {
    const { wrapper } = await mountDialog();

    const inputs = wrapper.findAll('input');
    await inputs[0].setValue('운영 그룹');
    await inputs[1].setValue('설명');
    await inputs[2].setChecked(true);

    const submitButton = wrapper.findAll('button').find((button) => button.text() === '생성');
    await submitButton?.trigger('click');

    expect(wrapper.emitted('submit')).toEqual([
      [
        {
          name: '운영 그룹',
          description: '설명',
          grants: [
            {
              permissionCode: 'employee.read',
              scopes: ['SELF'],
            },
          ],
        },
      ],
    ]);
  });

  it('레거시 비허용 scope는 초기 상태와 submit payload에서 제거한다', async () => {
    const { wrapper } = await mountEditDialogWithLegacyScope();

    expect(wrapper.findAll('input[type="checkbox"]')).toHaveLength(1);
    expect((wrapper.find('input[type="checkbox"]').element as HTMLInputElement).checked).toBe(false);

    const submitButton = wrapper.findAll('button').find((button) => button.text() === '저장');
    await submitButton?.trigger('click');

    expect(wrapper.emitted('submit')).toEqual([
      [
        {
          name: '레거시 그룹',
          description: '이전 정책 그룹',
          grants: [],
        },
      ],
    ]);
  });
});
