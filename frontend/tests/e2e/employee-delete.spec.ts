import { test, expect } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;

const employeeId = 1;

const employeeListResponse = {
  content: [
    {
      employeeId,
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

test.describe('직원 삭제', () => {
  test('삭제 확인 후 API 호출과 목록 갱신이 이뤄진다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    let deleteCalled = false;
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
        const body = deleteCalled
          ? { ...employeeListResponse, content: [], totalElements: 0 }
          : employeeListResponse;
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(body),
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

      if (path === `/api/employees/${employeeId}` && request.method() === 'DELETE') {
        deleteCalled = true;
        await route.fulfill({ status: 204, contentType: 'application/json', body: '' });
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

    const row = page.getByRole('row', { name: /박지훈/ });
    await expect(row).toBeVisible();

    await row.getByRole('button', { name: '직원 메뉴 열기' }).click();
    await page.getByRole('menuitem', { name: '직원 삭제' }).click();

    await expect(page.getByText('직원을 삭제할까요?')).toBeVisible();

    await page.getByRole('button', { name: '삭제' }).click();

    await expect.poll(() => deleteCalled).toBeTruthy();

    await expect(page.getByText('직원을 삭제했습니다.')).toBeVisible();
    await expect(page.getByRole('row', { name: /박지훈/ })).toHaveCount(0);
    await expect(page.getByText('직원 정보를 찾을 수 없습니다')).toBeVisible();
  });
});
