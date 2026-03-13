import { computed, ref } from 'vue';
import { mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ThemeToggle from './ThemeToggle.vue';
import type { ThemePreference } from '@/core/theme/theme';

const theme = ref<ThemePreference>('system');
const resolvedTheme = computed<'light' | 'dark'>(() =>
  theme.value === 'system' ? 'dark' : theme.value,
);
const setTheme = vi.fn((value: ThemePreference) => {
  theme.value = value;
});

vi.mock('@/core/composables', () => ({
  useTheme: () => ({
    theme,
    resolvedTheme,
    setTheme,
  }),
}));

describe('ThemeToggle', () => {
  beforeEach(() => {
    theme.value = 'system';
    setTheme.mockClear();
  });

  it('theme option 목록을 표시한다', () => {
    const wrapper = mount(ThemeToggle, {
      global: {
        stubs: {
          Button: { template: '<button type="button"><slot /></button>' },
          DropdownMenu: { template: '<div><slot /></div>' },
          DropdownMenuTrigger: { template: '<div><slot /></div>' },
          DropdownMenuContent: { template: '<div><slot /></div>' },
          DropdownMenuRadioGroup: { template: '<div><slot /></div>' },
          DropdownMenuRadioItem: {
            props: ['value'],
            template: '<button type="button" @click="$emit(\'click\')"><slot /></button>',
          },
          Sun: true,
          Moon: true,
          Monitor: true,
        },
      },
    });

    expect(wrapper.text()).toContain('라이트');
    expect(wrapper.text()).toContain('다크');
    expect(wrapper.text()).toContain('시스템');
  });

  it('메뉴 항목을 클릭하면 해당 테마를 설정한다', async () => {
    const wrapper = mount(ThemeToggle, {
      global: {
        stubs: {
          Button: { template: '<button type="button"><slot /></button>' },
          DropdownMenu: { template: '<div><slot /></div>' },
          DropdownMenuTrigger: { template: '<div><slot /></div>' },
          DropdownMenuContent: { template: '<div><slot /></div>' },
          DropdownMenuRadioGroup: { template: '<div><slot /></div>' },
          DropdownMenuRadioItem: {
            props: ['value'],
            template:
              '<button type="button" :data-value="value" @click="$emit(\'click\')"><slot /></button>',
          },
          Sun: true,
          Moon: true,
          Monitor: true,
        },
      },
    });

    await wrapper.get('[data-value="light"]').trigger('click');

    expect(setTheme).toHaveBeenCalledWith('light');
  });
});
