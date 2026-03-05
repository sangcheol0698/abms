import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';

async function mockDashboardApis(page: Page) {
  await page.route('**/api/dashboards/summary', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        totalEmployeesCount: 42,
        activeProjectsCount: 9,
        newEmployeesCount: 3,
        onLeaveEmployeesCount: 2,
      }),
    }),
  );

  await page.route('**/api/monthlyRevenueSummary/sixMonthTrend', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([]),
    }),
  );

  await page.route('**/api/notifications', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([]),
    }),
  );

  await page.route('**/api/departments/organization-chart', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([]),
    }),
  );
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
