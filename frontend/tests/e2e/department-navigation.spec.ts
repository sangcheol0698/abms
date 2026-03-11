import { expect, test } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';
import { BASE_URL, fulfillJson, mockApi } from './support/api';

const organizationChart = [
  {
    departmentId: 1,
    departmentName: '플랫폼본부',
    departmentCode: 'PLATFORM',
    departmentType: '본부',
    departmentLeader: null,
    employeeCount: 12,
    children: [
      {
        departmentId: 2,
        departmentName: '백엔드팀',
        departmentCode: 'BACKEND',
        departmentType: '팀',
        departmentLeader: null,
        employeeCount: 5,
        children: [],
      },
      {
        departmentId: 3,
        departmentName: '프론트엔드팀',
        departmentCode: 'FRONTEND',
        departmentType: '팀',
        departmentLeader: null,
        employeeCount: 4,
        children: [],
      },
    ],
  },
];

function createDepartmentDetail(departmentId: number) {
  if (departmentId === 2) {
    return {
      departmentId: 2,
      departmentName: '백엔드팀',
      departmentCode: 'BACKEND',
      departmentType: '팀',
      departmentLeader: null,
      employees: [
        { employeeId: 1, employeeName: '박지훈', position: '사원' },
      ],
      employeeCount: 5,
    };
  }

  if (departmentId === 3) {
    return {
      departmentId: 3,
      departmentName: '프론트엔드팀',
      departmentCode: 'FRONTEND',
      departmentType: '팀',
      departmentLeader: null,
      employees: [
        { employeeId: 2, employeeName: '김프론트', position: '선임' },
      ],
      employeeCount: 4,
    };
  }

  return {
    departmentId: 1,
    departmentName: '플랫폼본부',
    departmentCode: 'PLATFORM',
    departmentType: '본부',
    departmentLeader: null,
    employees: [],
    employeeCount: 12,
  };
}

test.describe('부서 화면', () => {
  test('루트 진입 시 첫 부서를 선택하고 트리 선택과 검색이 동작한다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    await mockApi(page, async ({ route, path, method }) => {
      if (path === '/api/departments/organization-chart' && method === 'GET') {
        await fulfillJson(route, organizationChart);
        return true;
      }

      if (path.startsWith('/api/departments/') && method === 'GET') {
        const departmentId = Number(path.split('/')[3]);
        await fulfillJson(route, createDepartmentDetail(departmentId));
        return true;
      }

      return false;
    });

    await page.goto(`${BASE_URL}/departments`);

    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/departments/1/?$`));
    await expect(page.getByRole('heading', { name: '플랫폼본부' })).toBeVisible();

    await page.locator('[data-node-id="2"] > div').click();
    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/departments/2/?$`));
    await expect(page.getByRole('heading', { name: '백엔드팀' })).toBeVisible();
    await expect(page.getByText('BACKEND').first()).toBeVisible();
    await expect(page.getByText('직원 수').first()).toBeVisible();

    const searchInput = page.getByPlaceholder('부서명, 코드, 리더를 검색하세요');
    await searchInput.fill('프론트');
    await expect(page.locator('[data-node-id="3"] > div')).toBeVisible();
    await page.locator('[data-node-id="3"] > div').click();

    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/departments/3/?$`));
    await expect(page.getByRole('heading', { name: '프론트엔드팀' })).toBeVisible();
  });
});
