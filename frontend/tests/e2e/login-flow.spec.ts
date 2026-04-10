import { test, expect } from '@playwright/test';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;

test.describe('로그인 플로우', () => {
  test('로그인 후 대시보드로 이동한다', async ({ page }) => {
    await page.route(API_ROUTE, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      const path = url.pathname;

      if (path === '/api/csrf') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ok: true }),
        });
        return;
      }

      if (path === '/api/auth/login' && request.method() === 'POST') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({}) });
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
            permissions: [{ code: 'dashboard.read', scopes: ['ALL'] }],
          }),
        });
        return;
      }

      if (path === '/api/dashboards/summary') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            totalEmployeesCount: 10,
            activeProjectsCount: 2,
            completedProjectsCount: 1,
            newEmployeesCount: 1,
            yearRevenue: 100000000,
            yearProfit: 30000000,
          }),
        });
        return;
      }

      if (path === '/api/dashboards/monthly-financials') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }

      if (path === '/api/notifications') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }

      if (request.method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }

      await route.fulfill({ status: 204, contentType: 'application/json', body: '' });
    });

    await page.goto(`${BASE_URL}/auths/login`);

    await page.getByLabel('이메일').fill('tester@abms.co.kr');
    await page.getByLabel('비밀번호').fill('password123!');
    await page.getByRole('button', { name: '로그인' }).click();

    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/$`));
    await expect(page.getByRole('heading', { name: '최근 알림' })).toBeVisible();
  });
});
