import { describe, expect, it } from 'vitest';
import {
  formatCurrency,
  formatProjectDate,
  formatProjectPeriod,
  mapProjectListItem,
} from '@/features/project/models/projectListItem';
import { mapProjectDetail } from '@/features/project/models/projectDetail';

describe('project models', () => {
  it('목록 아이템을 기본값과 함께 매핑한다', () => {
    expect(mapProjectListItem(undefined)).toEqual({
      projectId: 0,
      partyId: 0,
      partyName: '',
      leadDepartmentId: null,
      leadDepartmentName: null,
      code: '',
      name: '',
      description: null,
      status: '',
      statusLabel: '',
      contractAmount: 0,
      startDate: '',
      endDate: null,
    });

    expect(
      mapProjectListItem({
        projectId: 1,
        partyId: 2,
        partyName: '협력사',
        leadDepartmentId: 12,
        leadDepartmentName: '개발팀',
        code: 'P-001',
        name: '프로젝트',
        description: '설명',
        status: 'IN_PROGRESS',
        statusDescription: '진행 중',
        contractAmount: '1000',
        startDate: '2024-01-01',
        endDate: '2024-12-31',
      }),
    ).toMatchObject({
      projectId: 1,
      partyId: 2,
      statusLabel: '진행 중',
      leadDepartmentId: 12,
      leadDepartmentName: '개발팀',
      contractAmount: 1000,
    });
  });

  it('날짜/기간/금액 포맷을 처리한다', () => {
    expect(formatCurrency(123456)).toBe('123,456원');
    expect(formatProjectDate('2024.1.2')).toBe('2024.01.02');
    expect(formatProjectDate('')).toBe('');
    expect(formatProjectDate('2024')).toBe('2024');
    expect(formatProjectPeriod('2024-01-01', null)).toBe('2024.01.01 ~ 진행중');
    expect(formatProjectPeriod('2024-01-01', '2024-12-31')).toBe('2024.01.01 ~ 2024.12.31');
  });

  it('상세 모델을 리드 부서 유무에 따라 매핑한다', () => {
    expect(
      mapProjectDetail({
        projectId: 3,
        partyId: 9,
        partyName: '협력사',
        code: 'P-003',
        name: '상세',
        description: null,
        status: 'DONE',
        statusDescription: '완료',
        contractAmount: '5000',
        startDate: '2024-03-01',
        endDate: null,
      }),
    ).toMatchObject({
      projectId: 3,
      contractAmount: 5000,
      leadDepartmentId: null,
      leadDepartmentName: null,
    });

    expect(
      mapProjectDetail({
        projectId: 4,
        partyId: 10,
        partyName: '협력사B',
        code: 'P-004',
        name: '상세2',
        description: '설명',
        status: 'IN_PROGRESS',
        statusDescription: '진행 중',
        contractAmount: 2000,
        startDate: '2024-04-01',
        endDate: '2024-05-01',
        leadDepartmentId: '12',
        leadDepartmentName: '개발팀',
      }),
    ).toMatchObject({
      leadDepartmentId: 12,
      leadDepartmentName: '개발팀',
    });
  });
});
