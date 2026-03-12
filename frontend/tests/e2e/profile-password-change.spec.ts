import { expect, test } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;

test.describe('내 계정 비밀번호 변경', () => {
  test('사용자 메뉴에서 비밀번호 변경 요청을 보낸다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    let passwordChangePayload: Record<string, string> | null = null;

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
            permissions: [{ code: 'employee.write', scopes: ['SELF'] }],
          }),
        });
        return;
      }

      if (path === '/api/auth/password' && request.method() === 'PATCH') {
        passwordChangePayload = request.postDataJSON() as Record<string, string>;
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({}),
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
            newEmployeesCount: 1,
            onLeaveEmployeesCount: 0,
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

    await page.goto(`${BASE_URL}/`);

    await page.getByText('테스터').first().click();
    await page.getByRole('menuitem', { name: '내 계정' }).click();

    await expect(page.locator('[data-test="profile-section"]')).toBeVisible();
    await page.getByRole('button', { name: '보안' }).click();

    await page.locator('#currentPassword').fill('CurrentPassword123!');
    await page.locator('#newPassword').fill('NewPassword123!');
    await page.locator('#newPasswordConfirm').fill('NewPassword123!');
    await page.getByRole('button', { name: '비밀번호 변경' }).click();

    await expect.poll(() => passwordChangePayload).toEqual({
      currentPassword: 'CurrentPassword123!',
      newPassword: 'NewPassword123!',
    });
    await expect(page.locator('#currentPassword')).toHaveValue('');
    await expect(page.locator('#newPassword')).toHaveValue('');
    await expect(page.locator('#newPasswordConfirm')).toHaveValue('');
  });

  test('모바일에서 내 계정을 누르면 사이드바가 닫히고 계정 다이얼로그를 사용할 수 있다', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await mockAuthenticatedSession(page);
    let passwordChangePayload: Record<string, string> | null = null;

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
            permissions: [{ code: 'employee.write', scopes: ['SELF'] }],
          }),
        });
        return;
      }

      if (path === '/api/auth/password' && request.method() === 'PATCH') {
        passwordChangePayload = request.postDataJSON() as Record<string, string>;
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({}),
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
            newEmployeesCount: 1,
            onLeaveEmployeesCount: 0,
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

    await page.goto(`${BASE_URL}/`);

    await page.getByRole('button', { name: 'Toggle Sidebar' }).click();
    await page.getByText('테스터').first().click();
    await page.getByRole('menuitem', { name: '내 계정' }).click();

    await expect(page.locator('[data-test="profile-section"]')).toBeVisible();
    await expect(page.getByText('Abacus Inc')).not.toBeVisible();
    await expect(page.getByRole('button', { name: 'Close' })).toBeVisible();

    await page.getByRole('button', { name: '보안' }).click();
    await page.locator('#currentPassword').fill('CurrentPassword123!');
    await page.locator('#newPassword').fill('NewPassword123!');
    await page.locator('#newPasswordConfirm').fill('NewPassword123!');
    await page.getByRole('button', { name: '비밀번호 변경' }).click();

    await expect.poll(() => passwordChangePayload).toEqual({
      currentPassword: 'CurrentPassword123!',
      newPassword: 'NewPassword123!',
    });
  });
});
