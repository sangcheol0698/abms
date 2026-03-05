import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { Button } from '@/components/ui/button';

describe('Button', () => {
  it('기본적으로 button 태그로 렌더링한다', () => {
    const wrapper = mount(Button, {
      slots: {
        default: '버튼',
      },
    });

    expect(wrapper.element.tagName).toBe('BUTTON');
    expect(wrapper.text()).toContain('버튼');
  });

  it('as 속성으로 태그를 변경할 수 있다', () => {
    const wrapper = mount(Button, {
      props: {
        as: 'a',
        href: '/docs',
      },
      slots: {
        default: '링크 버튼',
      },
    });

    expect(wrapper.element.tagName).toBe('A');
    expect(wrapper.attributes('href')).toBe('/docs');
  });

  it('variant와 size에 따라 스타일 클래스가 반영된다', () => {
    const wrapper = mount(Button, {
      props: {
        variant: 'destructive',
        size: 'sm',
      },
      slots: {
        default: '삭제',
      },
    });

    expect(wrapper.classes()).toContain('bg-destructive');
    expect(wrapper.classes()).toContain('h-8');
  });

  it('disabled 속성을 전달하면 비활성화 상태가 된다', () => {
    const wrapper = mount(Button, {
      props: {
        disabled: true,
      },
      slots: {
        default: '저장',
      },
    });

    expect(wrapper.attributes('disabled')).toBeDefined();
  });

  it('사용자 class를 기본 class와 함께 병합한다', () => {
    const wrapper = mount(Button, {
      props: {
        class: 'my-btn',
      },
      slots: {
        default: '커스텀',
      },
    });

    expect(wrapper.classes()).toContain('my-btn');
    expect(wrapper.classes()).toContain('inline-flex');
  });
});
