import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  canAccessDepartmentEmployees,
  canEditOwnProfile,
  canManageEmployee,
  canViewEmployeeDetail,
} from '@/features/employee/permissions';

const departmentChart = [
  {
    departmentId: 10,
    departmentName: '개발본부',
    departmentCode: 'DEV',
    departmentType: 'DIVISION',
    departmentLeader: null,
    employeeCount: 3,
    children: [
      {
        departmentId: 11,
        departmentName: '개발팀',
        departmentCode: 'DEV-TEAM',
        departmentType: 'TEAM',
        departmentLeader: null,
        employeeCount: 2,
        children: [],
      },
    ],
  },
];

describe('employee permissions', () => {
  const storage = new Map<string, string>();

  beforeEach(() => {
    storage.clear();
    vi.stubGlobal('localStorage', {
      getItem: (key: string) => storage.get(key) ?? null,
      setItem: (key: string, value: string) => {
        storage.set(key, value);
      },
      removeItem: (key: string) => {
        storage.delete(key);
      },
      clear: () => {
        storage.clear();
      },
    });
  });

  it('OWN_DEPARTMENT는 같은 부서 직원만 상세/관리 가능하다', () => {
    localStorage.setItem(
      'user',
      JSON.stringify({
        name: '홍길동',
        email: 'hong@abms.co.kr',
        employeeId: 1,
        departmentId: 10,
        permissions: [
          { code: 'employee.read', scopes: ['OWN_DEPARTMENT'] },
          { code: 'employee.write', scopes: ['OWN_DEPARTMENT'] },
        ],
      }),
    );

    expect(canViewEmployeeDetail({ employeeId: 2, departmentId: 10 }, { departmentChart })).toBe(true);
    expect(canManageEmployee({ employeeId: 2, departmentId: 10 }, { departmentChart })).toBe(true);
    expect(canViewEmployeeDetail({ employeeId: 3, departmentId: 11 }, { departmentChart })).toBe(false);
    expect(canManageEmployee({ employeeId: 3, departmentId: 11 }, { departmentChart })).toBe(false);
  });

  it('OWN_DEPARTMENT_TREE는 하위 부서 직원까지 상세/관리 가능하다', () => {
    localStorage.setItem(
      'user',
      JSON.stringify({
        name: '홍길동',
        email: 'hong@abms.co.kr',
        employeeId: 1,
        departmentId: 10,
        permissions: [
          { code: 'employee.read', scopes: ['OWN_DEPARTMENT_TREE'] },
          { code: 'employee.write', scopes: ['OWN_DEPARTMENT_TREE'] },
        ],
      }),
    );

    expect(canViewEmployeeDetail({ employeeId: 3, departmentId: 11 }, { departmentChart })).toBe(true);
    expect(canManageEmployee({ employeeId: 3, departmentId: 11 }, { departmentChart })).toBe(true);
    expect(canAccessDepartmentEmployees(11, { departmentChart })).toBe(true);
  });

  it('SELF는 본인만 수정 가능하고 부서 직원 탭 접근은 허용하지 않는다', () => {
    localStorage.setItem(
      'user',
      JSON.stringify({
        name: '홍길동',
        email: 'hong@abms.co.kr',
        employeeId: 1,
        departmentId: 10,
        permissions: [
          { code: 'employee.read', scopes: ['SELF'] },
          { code: 'employee.write', scopes: ['SELF'] },
        ],
      }),
    );

    expect(canEditOwnProfile({ employeeId: 1, email: 'hong@abms.co.kr' })).toBe(true);
    expect(canManageEmployee({ employeeId: 1, departmentId: 10 }, { departmentChart })).toBe(false);
    expect(canAccessDepartmentEmployees(10, { departmentChart })).toBe(false);
  });
});
