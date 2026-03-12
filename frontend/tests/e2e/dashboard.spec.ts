import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;

async function mockDashboardApis(page: Page) {
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
          totalEmployeesCount: 42,
          activeProjectsCount: 9,
          newEmployeesCount: 3,
          onLeaveEmployeesCount: 2,
        }),
      });
      return;
    }

    if (path === '/api/monthlyRevenueSummary/sixMonthTrend') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    if (path === '/api/notifications') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    if (path === '/api/departments/organization-chart') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
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
}

test.describe('대시보드 화면', () => {
  test('핵심 위젯이 렌더링된다', async ({ page }) => {
    await mockAuthenticatedSession(page);
    await mockDashboardApis(page);

    await page.goto(`${BASE_URL}/`);

    await expect(page.getByRole('heading', { name: '최근 알림' })).toBeVisible();
    await expect(page.getByText('최근 알림이 없습니다.')).toBeVisible();
    await expect(page.getByRole('heading', { name: '직원 구성 현황' })).toBeVisible();
    await expect(page.getByRole('heading', { name: '근무 상태 현황' })).toBeVisible();
  });

  test('사이드바 메뉴로 부서 화면으로 이동한다', async ({ page }) => {
    await mockAuthenticatedSession(page);
    await mockDashboardApis(page);

    await page.goto(`${BASE_URL}/`);

    await page.getByRole('link', { name: '부서' }).click();

    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/departments/?$`));
    await expect(page.getByLabel('부서 사이드바 토글')).toBeVisible();
  });
});
