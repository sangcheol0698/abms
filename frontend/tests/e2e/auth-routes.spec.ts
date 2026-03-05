import { test, expect } from '@playwright/test';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';

test.describe('인증 관련 화면', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('**/api/csrf', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ ok: true }),
      }),
    );
  });

  test('세션 만료 화면에서 로그인 링크가 redirect를 유지한다', async ({ page }) => {
    await page.goto(`${BASE_URL}/auths/session-expired?redirect=/employees`);

    await expect(page.getByRole('heading', { name: '세션이 만료되었습니다' })).toBeVisible();

    const loginLink = page.getByRole('link', { name: '로그인 페이지로 이동' });
    await expect(loginLink).toBeVisible();
    await expect(loginLink).toHaveAttribute('href', '/auths/login?redirect=/employees');
  });

  test('권한 없음 화면에서 홈 이동 버튼이 표시된다', async ({ page }) => {
    await page.goto(`${BASE_URL}/auths/forbidden`);

    await expect(page.getByRole('heading', { name: '접근 권한이 없습니다' })).toBeVisible();
    await expect(page.getByRole('link', { name: '홈으로 이동' })).toHaveAttribute('href', '/');
  });
});
