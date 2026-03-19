import { test, expect } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;

const employeeListResponse = {
  content: [
    {
      employeeId: 1,
      departmentId: 10,
      departmentName: '백엔드팀',
      name: '박지훈',
      email: 'be3@abms.co.kr',
      position: '사원',
      status: '재직',
      grade: '초급',
      type: '프리랜서',
      memo: '배치/ETL',
      joinDate: '2024-01-02',
      birthDate: '1999-03-09',
    },
  ],
  number: 0,
  size: 10,
  totalPages: 1,
  totalElements: 1,
};

const employeeDetailResponse = {
  departmentId: 10,
  departmentName: '백엔드팀',
  employeeId: 1,
  name: '박지훈',
  email: 'be3@abms.co.kr',
  joinDate: '2024-01-02',
  birthDate: '1999-03-09',
  position: '사원',
  status: '재직',
  grade: '초급',
  type: '프리랜서',
  memo: '배치/ETL',
};

test.describe('직원 수정 다이얼로그', () => {
  test('기존 입사일과 생년월일을 그대로 표시한다', async ({ page }) => {
    await mockAuthenticatedSession(page);
    let employeeListRequestCount = 0;

    await page.route(API_ROUTE, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      const path = url.pathname.endsWith('/') && url.pathname !== '/'
        ? url.pathname.slice(0, -1)
        : url.pathname;

      if (path === '/api/csrf') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ok: true }),
        });
        return;
      }

      if (path === '/api/auth/me') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            email: 'tester@abms.co.kr',
            name: '테스터',
            employeeId: 1,
            departmentId: 10,
            permissions: [
              { code: 'employee.read', scopes: ['ALL'] },
              { code: 'employee.write', scopes: ['ALL'] },
            ],
          }),
        });
        return;
      }

      if (path === '/api/employees') {
        employeeListRequestCount += 1;
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(employeeListResponse),
        });
        return;
      }

      if (
        path === '/api/employees/statuses' ||
        path === '/api/employees/types' ||
        path === '/api/employees/grades' ||
        path === '/api/employees/positions' ||
        path === '/api/departments/organization-chart' ||
        path === '/api/notifications'
      ) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
        return;
      }

      if (path === '/api/employees/1') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(employeeDetailResponse),
        });
        return;
      }

      if (path.startsWith('/api/v1/chat/')) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
        return;
      }

      if (request.method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
        return;
      }

      await route.fulfill({ status: 204, contentType: 'application/json', body: '' });
    });

    await page.goto(`${BASE_URL}/employees`);
    await expect.poll(() => employeeListRequestCount).toBeGreaterThan(0);

    await expect(page.getByRole('row', { name: /박지훈/ })).toBeVisible();

    const targetRow = page.getByRole('row', { name: /박지훈/ });
    await targetRow.getByRole('button', { name: '직원 메뉴 열기' }).click();
    await page.getByRole('menuitem', { name: '직원 편집' }).click();

    const dialog = page.getByRole('dialog', { name: '직원 편집' });
    await expect(dialog).toBeVisible();

    await expect(dialog.getByPlaceholder('생년월일을 선택하세요')).toHaveValue('1999-03-09');
    await expect(dialog.getByPlaceholder('입사일을 선택하세요')).toHaveValue('2024-01-02');
  });
});
