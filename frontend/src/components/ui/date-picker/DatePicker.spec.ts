import { nextTick } from 'vue';
import { describe, expect, it } from 'vitest';
import { shallowMount } from '@vue/test-utils';
import DatePicker from './DatePicker.vue';

describe('DatePicker', () => {
  it('중간 자리 숫자를 입력해도 뒤쪽 날짜를 지우지 않고 덮어쓴다', async () => {
    const wrapper = shallowMount(DatePicker, {
      props: {
        modelValue: new Date('2026-03-20'),
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

    expect(input.value).toBe('2026-13-20');
  });

  it('입력 길이를 브라우저 maxlength에 맡기지 않는다', () => {
    const wrapper = shallowMount(DatePicker, {
      global: {
        stubs: {
          Input: false,
        },
      },
    });

    expect(wrapper.get('input').attributes('maxlength')).toBeUndefined();
  });
});
