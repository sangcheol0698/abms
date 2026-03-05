import type { Page } from '@playwright/test';

interface MockAuthOptions {
  email?: string;
  name?: string;
}

export async function mockAuthenticatedSession(page: Page, options: MockAuthOptions = {}) {
  const email = options.email ?? 'tester@abms.co.kr';
  const name = options.name ?? '테스터';

  await page.addInitScript(({ storedEmail, storedName }) => {
    window.localStorage.setItem(
      'user',
      JSON.stringify({
        email: storedEmail,
        name: storedName,
      }),
    );
  }, { storedEmail: email, storedName: name });

  await page.route('**/api/csrf', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ok: true }),
    });
  });

  await page.route('**/api/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ email, name }),
    });
  });
}
