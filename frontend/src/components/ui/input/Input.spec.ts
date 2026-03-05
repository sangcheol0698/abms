import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { Input } from '@/components/ui/input';

describe('Input', () => {
  it('modelValue를 입력창 값으로 표시한다', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '초기값',
      },
    });

    expect((wrapper.element as HTMLInputElement).value).toBe('초기값');
  });

  it('입력 변경 시 update:modelValue 이벤트를 발생시킨다', async () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
      },
    });

    await wrapper.setValue('변경된 값');

    const updates = wrapper.emitted('update:modelValue');
    expect(updates).toBeTruthy();
    expect(updates?.[0]).toEqual(['변경된 값']);
  });

  it('defaultValue가 있으면 초기값으로 반영한다', () => {
    const wrapper = mount(Input, {
      props: {
        defaultValue: '기본값',
      },
    });

    expect((wrapper.element as HTMLInputElement).value).toBe('기본값');
  });

  it('사용자 class를 기본 class와 함께 병합한다', () => {
    const wrapper = mount(Input, {
      props: {
        class: 'my-extra-class',
      },
    });

    expect(wrapper.classes()).toContain('my-extra-class');
    expect(wrapper.classes()).toContain('border-input');
  });

  it('숫자 modelValue도 문자열 형태로 렌더링한다', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: 1234,
      },
    });

    expect((wrapper.element as HTMLInputElement).value).toBe('1234');
  });
});
