import { nextTick } from 'vue';
import { describe, expect, it } from 'vitest';
import { shallowMount } from '@vue/test-utils';
import DateRangeFilter from './DateRangeFilter.vue';

describe('DateRangeFilter', () => {
  it('중간 자리 숫자를 입력해도 뒤쪽 범위 날짜를 지우지 않고 덮어쓴다', async () => {
    const wrapper = shallowMount(DateRangeFilter, {
      props: {
        modelValue: {
          start: new Date('2026-03-01'),
          end: new Date('2026-03-31'),
        },
      },
      global: {
        stubs: {
          Input: false,
        },
      },
    });

    const input = wrapper.get('input').element as HTMLInputElement;
    input.setSelectionRange(5, 5);

    input.dispatchEvent(new InputEvent('beforeinput', {
      bubbles: true,
      cancelable: true,
      data: '1',
      inputType: 'insertText',
    }));

    await nextTick();

    expect(input.value).toBe('2026-13-01 ~ 2026-03-31');
  });

  it('입력 길이를 브라우저 maxlength에 맡기지 않는다', () => {
    const wrapper = shallowMount(DateRangeFilter, {
      global: {
        stubs: {
          Input: false,
        },
      },
    });

    expect(wrapper.get('input').attributes('maxlength')).toBeUndefined();
  });
});
