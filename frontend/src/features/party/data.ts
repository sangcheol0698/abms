export interface PartySummaryCard {
  id: string;
  title: string;
  value: string;
  description?: string;
  trend?: {
    label: string;
    direction: 'up' | 'down' | 'flat';
  };
}

export const partySummaryCards: PartySummaryCard[] = [
  {
    id: 'total',
    title: '전체 협력사',
    value: '24',
    description: 'UI 레이아웃용 샘플 데이터',
    trend: {
      label: '+2 이번 달',
      direction: 'up',
    },
  },
  {
    id: 'active',
    title: '거래중 협력사',
    value: '15',
    description: '최근 3개월 내 계약 유지',
  },
  {
    id: 'pending',
    title: '제안 진행',
    value: '5',
    description: '견적/제안 단계 협력사',
  },
  {
    id: 'contract',
    title: '누적 계약금액',
    value: 'KRW 4.8B',
    description: '샘플 데이터',
    trend: {
      label: '+6% MoM',
      direction: 'up',
    },
  },
];
