import type { Page } from '@playwright/test';

interface MockAuthOptions {
  email?: string;
  name?: string;
}

const DEFAULT_PERMISSIONS = [
  { code: 'employee.read', scopes: ['ALL'] },
  { code: 'employee.write', scopes: ['ALL'] },
  { code: 'employee.excel.download', scopes: ['ALL'] },
  { code: 'employee.excel.upload', scopes: ['ALL'] },
  { code: 'project.read', scopes: ['ALL'] },
  { code: 'project.write', scopes: ['ALL'] },
  { code: 'project.excel.download', scopes: ['ALL'] },
  { code: 'project.excel.upload', scopes: ['ALL'] },
  { code: 'party.read', scopes: ['ALL'] },
  { code: 'party.write', scopes: ['ALL'] },
  { code: 'dashboard.read', scopes: ['ALL'] },
];

export async function mockAuthenticatedSession(page: Page, options: MockAuthOptions = {}) {
  const email = options.email ?? 'tester@abms.co.kr';
  const name = options.name ?? '테스터';

  await page.addInitScript(({ storedEmail, storedName, permissions }) => {
    window.localStorage.setItem(
      'user',
      JSON.stringify({
        email: storedEmail,
        name: storedName,
        employeeId: 1,
        departmentId: 10,
        permissions,
      }),
    );
  }, { storedEmail: email, storedName: name, permissions: DEFAULT_PERMISSIONS });

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
      body: JSON.stringify({ email, name, employeeId: 1, departmentId: 10, permissions: DEFAULT_PERMISSIONS }),
    });
  });
}
