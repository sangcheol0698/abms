import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Component } from 'vue';
import { createMockQueryState, renderWithProviders } from '@/test-utils';

let revenueTrendQueryMock = createMockQueryState({
  data: [
    { targetMonth: '2026-01-01', revenue: 100000000, cost: 60000000, profit: 40000000 },
    { targetMonth: '2026-02-01', revenue: 130000000, cost: 80000000, profit: 50000000 },
    { targetMonth: '2026-03-01', revenue: 150000000, cost: 90000000, profit: 60000000 },
  ],
});

vi.mock('@/features/department/queries/useDepartmentQueries', () => ({
  useDepartmentRevenueTrendQuery: () => revenueTrendQueryMock,
}));

let DepartmentRevenuePanelComponent: Component;

describe('DepartmentRevenuePanel', () => {
  beforeAll(async () => {
    DepartmentRevenuePanelComponent = (await import(
      '@/features/department/components/DepartmentRevenuePanel.vue'
    )).default as Component;
  });

  beforeEach(() => {
    revenueTrendQueryMock = createMockQueryState({
      data: [
        { targetMonth: '2026-01-01', revenue: 100000000, cost: 60000000, profit: 40000000 },
        { targetMonth: '2026-02-01', revenue: 130000000, cost: 80000000, profit: 50000000 },
        { targetMonth: '2026-03-01', revenue: 150000000, cost: 90000000, profit: 60000000 },
      ],
    });
  });

  it('가장 최근 월의 매출, 비용, 이익을 요약 카드에 표시한다', async () => {
    const { wrapper } = await renderWithProviders(DepartmentRevenuePanelComponent, {
      props: {
        departmentId: 10,
      },
    });

    expect(wrapper.get('[data-test="latest-revenue"]').text()).toContain('1.5억');
    expect(wrapper.get('[data-test="latest-cost"]').text()).toContain('90백만');
    expect(wrapper.get('[data-test="latest-profit"]').text()).toContain('60백만');
    expect(wrapper.text()).toContain('최근 6개월 매출 추이');
    expect(wrapper.text()).toContain('3월 기준');
  });

  it('조회 오류가 있으면 에러 메시지를 표시한다', async () => {
    revenueTrendQueryMock = createMockQueryState({
      data: [],
      error: new Error('매출 조회 실패'),
    });

    const { wrapper } = await renderWithProviders(DepartmentRevenuePanelComponent, {
      props: {
        departmentId: 10,
      },
    });

    expect(wrapper.text()).toContain('매출 조회 실패');
  });
});
