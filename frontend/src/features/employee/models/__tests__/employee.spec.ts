import { describe, it, expect } from 'vitest';
import { mapEmployeeSummary } from '@/features/employee/models/employee';

describe('mapEmployeeSummary', () => {
  it('converts string based dates into ISO strings', () => {
    const result = mapEmployeeSummary({
      departmentId: 'DEV',
      departmentName: '개발팀',
      employeeId: 'E-01',
      name: '홍길동',
      email: 'hong@example.com',
      joinDate: '2024-01-10',
      birthDate: '1990-05-15',
      position: '사원',
      status: '재직',
      grade: '초급',
      type: '정직원',
      avatarCode: 'SKY_GLOW',
      avatarLabel: 'Sky Glow',
      memo: '메모',
    });

    expect(result.joinDate).toBe('2024-01-10');
    expect(result.birthDate).toBe('1990-05-15');
    expect(result.avatarCode).toBe('SKY_GLOW');
    expect(result.avatarLabel).toBe('Sky Glow');
    expect(result.avatarImageUrl).toMatch(/(data:image\/svg\+xml|\.svg$)/);
  });

  it('formats structured date objects into ISO strings', () => {
    const result = mapEmployeeSummary({
      departmentId: 'DEV',
      departmentName: '개발팀',
      employeeId: 'E-01',
      name: '홍길동',
      email: 'hong@example.com',
      joinDate: { year: 2024, month: 1, day: 10 },
      birthDate: { year: 1990, month: 5, day: 15 },
      position: '사원',
      status: '재직',
      grade: '초급',
      type: '정직원',
      avatarCode: 'SKY_GLOW',
      avatarLabel: 'Sky Glow',
      memo: '메모',
    });

    expect(result.joinDate).toBe('2024-01-10');
    expect(result.birthDate).toBe('1990-05-15');
  });

  it('normalizes array based dates into ISO strings', () => {
    const result = mapEmployeeSummary({
      departmentId: 'DEV',
      departmentName: '개발팀',
      employeeId: 'E-01',
      name: '홍길동',
      email: 'hong@example.com',
      joinDate: [2024, 1, 10],
      birthDate: [1990, 5, 15],
      position: '사원',
      status: '재직',
      grade: '초급',
      type: '정직원',
      avatarCode: 'SKY_GLOW',
      avatarLabel: 'Sky Glow',
      memo: '메모',
    });

    expect(result.joinDate).toBe('2024-01-10');
    expect(result.birthDate).toBe('1990-05-15');
  });

  it('trims time components from string based dates', () => {
    const result = mapEmployeeSummary({
      departmentId: 'DEV',
      departmentName: '개발팀',
      employeeId: 'E-01',
      name: '홍길동',
      email: 'hong@example.com',
      joinDate: '2024-01-10T00:00:00',
      birthDate: '1990-05-15T12:34:56',
      position: '사원',
      status: '재직',
      grade: '초급',
      type: '정직원',
      avatarCode: 'SKY_GLOW',
      avatarLabel: 'Sky Glow',
      memo: '메모',
    });

    expect(result.joinDate).toBe('2024-01-10');
    expect(result.birthDate).toBe('1990-05-15');
  });
});
