import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import DashboardMonthlyFinancialTooltip from '@/features/dashboard/components/DashboardMonthlyFinancialTooltip.vue';

describe('DashboardMonthlyFinancialTooltip', () => {
  it('다크모드에서도 값을 읽을 수 있도록 테마 텍스트 색상을 사용한다', () => {
    const wrapper = mount(DashboardMonthlyFinancialTooltip, {
      props: {
        title: '1월',
        data: [
          { name: 'revenue', color: 'var(--chart-1)', value: 1000000 },
          { name: 'cost', color: 'var(--chart-4)', value: 400000 },
          { name: 'profit', color: 'var(--chart-3)', value: 600000 },
        ],
      },
    });

    expect(wrapper.classes()).toContain('text-foreground');
    expect(wrapper.classes()).toContain('border-border');
    expect(wrapper.findAll('.text-muted-foreground')).toHaveLength(3);
    expect(wrapper.text()).toContain('1,000,000원');
  });
});
