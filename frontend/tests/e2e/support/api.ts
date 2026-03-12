import type { Page, Route } from '@playwright/test';

export const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
export const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;
const DEFAULT_AUTH_ME = {
  email: 'tester@abms.co.kr',
  name: '테스터',
  employeeId: 1,
  departmentId: 10,
  permissions: [
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
  ],
};

export interface ApiRequestContext {
  route: Route;
  url: URL;
  path: string;
  method: string;
}

export function normalizeApiPath(pathname: string): string {
  return pathname.endsWith('/') && pathname !== '/' ? pathname.slice(0, -1) : pathname;
}

export async function fulfillJson(route: Route, body: unknown, status = 200) {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body),
  });
}

export async function mockApi(
  page: Page,
  handler: (context: ApiRequestContext) => Promise<boolean>,
) {
  await page.route(API_ROUTE, async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const context: ApiRequestContext = {
      route,
      url,
      path: normalizeApiPath(url.pathname),
      method: request.method(),
    };

    const handled = await handler(context);
    if (handled) {
      return;
    }

    if (context.path === '/api/csrf') {
      await fulfillJson(route, { ok: true });
      return;
    }

    if (context.path === '/api/auth/me') {
      await fulfillJson(route, DEFAULT_AUTH_ME);
      return;
    }

    if (context.path === '/api/notifications' || context.path.startsWith('/api/v1/chat/')) {
      await fulfillJson(route, []);
      return;
    }

    if (context.method === 'GET') {
      await fulfillJson(route, []);
      return;
    }

    await route.fulfill({
      status: 204,
      contentType: 'application/json',
      body: '',
    });
  });
}
