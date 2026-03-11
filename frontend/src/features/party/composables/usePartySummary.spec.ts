import { ref } from 'vue';
import { describe, expect, it } from 'vitest';
import { usePartySummary } from '@/features/party/composables/usePartySummary';

describe('usePartySummary', () => {
  it('summary가 없으면 기본값 카드들을 만든다', () => {
    const summary = usePartySummary({ summary: ref(null) });

    expect(summary.value.cards).toEqual([
      {
        id: 'total',
        title: '총 협력사',
        value: '0곳',
        description: '현재 필터 기준 협력사 수',
      },
      {
        id: 'with-projects',
        title: '프로젝트 보유 협력사',
        value: '0곳',
        description: '프로젝트 없는 협력사 0곳',
      },
      {
        id: 'with-in-progress',
        title: '진행 중 프로젝트 보유',
        value: '0곳',
        description: '진행 중 프로젝트를 1건 이상 가진 협력사',
      },
      {
        id: 'contract',
        title: '총 계약금액',
        value: '0원',
        description: '현재 필터 기준 계약 금액 합계',
      },
    ]);
  });

  it('summary 값이 있으면 카드 수치와 금액을 반영한다', () => {
    const summary = usePartySummary({
      summary: ref({
        totalCount: 12,
        withProjectsCount: 5,
        withInProgressProjectsCount: 2,
        withoutProjectsCount: 7,
        totalContractAmount: 1234567,
      }),
    });

    expect(summary.value.cards[0].value).toBe('12곳');
    expect(summary.value.cards[1].description).toBe('프로젝트 없는 협력사 7곳');
    expect(summary.value.cards[3].value).toBe('1,234,567원');
  });
});
