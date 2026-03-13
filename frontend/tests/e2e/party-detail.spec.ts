import { expect, test } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';
import { BASE_URL, fulfillJson, mockApi } from './support/api';

test.describe('협력사 상세', () => {
  test('관련 프로젝트를 표시하고 삭제 후 목록으로 돌아간다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    let deleted = false;

    await mockApi(page, async ({ route, path, method }) => {
      if (path === '/api/parties/1' && method === 'GET') {
        await fulfillJson(route, {
          partyId: 1,
          name: '협력사A',
          ceo: '대표A',
          manager: '담당A',
          contact: '010-1111-1111',
          email: 'party-a@abms.co.kr',
        });
        return true;
      }

      if (path === '/api/parties/1/projects' && method === 'GET') {
        await fulfillJson(route, [
          {
            projectId: 10,
            partyId: 1,
            partyName: '협력사A',
            code: 'P-010',
            name: '진행 프로젝트',
            description: null,
            status: 'IN_PROGRESS',
            statusDescription: '진행 중',
            contractAmount: 2000000,
            startDate: '2024-01-01',
            endDate: '2024-06-30',
          },
          {
            projectId: 11,
            partyId: 1,
            partyName: '협력사A',
            code: 'P-011',
            name: '완료 프로젝트',
            description: null,
            status: 'COMPLETED',
            statusDescription: '완료',
            contractAmount: 3000000,
            startDate: '2023-01-01',
            endDate: '2023-12-31',
          },
        ]);
        return true;
      }

      if (path === '/api/parties/1' && method === 'DELETE') {
        deleted = true;
        await route.fulfill({
          status: 204,
          contentType: 'application/json',
          body: '',
        });
        return true;
      }

      if (path === '/api/parties' && method === 'GET') {
        await fulfillJson(route, {
          content: deleted
            ? []
            : [
                {
                  partyId: 1,
                  name: '협력사A',
                  ceo: '대표A',
                  manager: '담당A',
                  contact: '010-1111-1111',
                  email: 'party-a@abms.co.kr',
                },
              ],
          number: 0,
          size: 10,
          totalPages: 1,
          totalElements: deleted ? 0 : 1,
        });
        return true;
      }

      if (path === '/api/parties/summary') {
        await fulfillJson(route, {
          totalCount: deleted ? 0 : 1,
          withProjectsCount: deleted ? 0 : 1,
          withInProgressProjectsCount: deleted ? 0 : 1,
          withoutProjectsCount: 0,
          totalContractAmount: deleted ? 0 : 5000000,
        });
        return true;
      }

      return false;
    });

    await page.goto(`${BASE_URL}/parties/1`);

    await expect(page.getByRole('heading', { name: '협력사A' })).toBeVisible();

    await page.getByRole('tab', { name: '프로젝트' }).click();
    await expect(page.getByRole('heading', { name: '진행 프로젝트' })).toBeVisible();
    await expect(page.getByRole('heading', { name: '완료 프로젝트' })).toBeVisible();

    await page.getByRole('button', { name: '더보기' }).click();
    await page.getByRole('menuitem', { name: '삭제' }).click();
    const dialog = page.getByRole('alertdialog');
    await expect(dialog.getByText('협력사를 삭제하시겠습니까?')).toBeVisible();
    await dialog.getByRole('button', { name: '삭제' }).click();

    await expect.poll(() => deleted).toBeTruthy();
    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/parties/?$`));
    await expect(page.getByText('협력사가 없습니다')).toBeVisible();
  });
});
