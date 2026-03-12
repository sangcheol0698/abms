import { test, expect } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;

test.describe('프로젝트 목록 필터', () => {
  test('검색어 입력 후 Enter를 누르면 name 필터로 조회한다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    const requestedProjectUrls: string[] = [];

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
            permissions: [
              { code: 'project.read', scopes: ['ALL'] },
              { code: 'project.write', scopes: ['ALL'] },
              { code: 'project.excel.download', scopes: ['ALL'] },
              { code: 'project.excel.upload', scopes: ['ALL'] },
              { code: 'party.read', scopes: ['ALL'] },
            ],
          }),
        });
        return;
      }

      if (path === '/api/projects') {
        requestedProjectUrls.push(route.request().url());
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            content: [
              {
                projectId: 1,
                partyId: 100,
                partyName: '협력사A',
                code: 'P-001',
                name: 'ABMS 리뉴얼',
                status: 'IN_PROGRESS',
                statusDescription: '진행 중',
                contractAmount: 1000000,
                startDate: '2024-01-01',
                endDate: '2024-12-31',
              },
            ],
            number: 0,
            size: 10,
            totalPages: 1,
            totalElements: 1,
          }),
        });
        return;
      }

      if (path === '/api/projects/statuses') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([{ name: 'IN_PROGRESS', description: '진행 중' }]),
        });
        return;
      }

      if (path === '/api/parties') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            content: [
              {
                partyId: 100,
                name: '협력사A',
                ceoName: '대표',
                salesRepName: '담당자',
                salesRepPhone: '010-0000-0000',
                salesRepEmail: 'rep@partya.co.kr',
              },
            ],
            number: 0,
            size: 1000,
            totalPages: 1,
            totalElements: 1,
          }),
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

    await page.goto(`${BASE_URL}/projects`);

    await expect(page.getByText('ABMS 리뉴얼')).toBeVisible();

    const searchInput = page.getByPlaceholder('프로젝트명 또는 코드를 입력하세요');
    await searchInput.fill('ABMS');
    await searchInput.press('Enter');

    await expect
      .poll(() => requestedProjectUrls.some((url) => url.includes('name=ABMS')))
      .toBeTruthy();
  });
});
