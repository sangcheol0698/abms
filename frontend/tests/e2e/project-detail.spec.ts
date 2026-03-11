import { expect, test } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';
import { BASE_URL, fulfillJson, mockApi } from './support/api';

test.describe('프로젝트 상세', () => {
  test('상세 정보와 탭을 렌더링하고 협력사 상세로 이동한다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    await mockApi(page, async ({ route, path, method, url }) => {
      if (path === '/api/projects/1' && method === 'GET') {
        await fulfillJson(route, {
          projectId: 1,
          partyId: 100,
          partyName: '협력사A',
          code: 'P-001',
          name: 'ABMS 리뉴얼',
          description: '프로젝트 상세 설명',
          status: 'IN_PROGRESS',
          statusDescription: '진행 중',
          contractAmount: 1000000,
          startDate: '2024-01-01',
          endDate: '2024-12-31',
          leadDepartmentId: 10,
          leadDepartmentName: '플랫폼팀',
        });
        return true;
      }

      if (path === '/api/project-assignments' && method === 'GET') {
        await fulfillJson(route, []);
        return true;
      }

      if (path === '/api/projectRevenuePlans/1' && method === 'GET') {
        await fulfillJson(route, []);
        return true;
      }

      if (path === '/api/parties/100' && method === 'GET') {
        await fulfillJson(route, {
          partyId: 100,
          name: '협력사A',
          ceo: '대표A',
          manager: '담당A',
          contact: '010-1111-1111',
          email: 'party-a@abms.co.kr',
        });
        return true;
      }

      if (path === '/api/parties/100/projects' && method === 'GET') {
        await fulfillJson(route, []);
        return true;
      }

      if (path === '/api/parties' && method === 'GET') {
        await fulfillJson(route, {
          content: [
            {
              partyId: 100,
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
          totalElements: 1,
        });
        return true;
      }

      if (path === '/api/parties/summary' && method === 'GET') {
        await fulfillJson(route, {
          totalCount: 1,
          withProjectsCount: 1,
          withInProgressProjectsCount: 1,
          withoutProjectsCount: 0,
          totalContractAmount: 1000000,
        });
        return true;
      }

      return false;
    });

    await page.goto(`${BASE_URL}/projects/1`);

    await expect(page.getByRole('heading', { name: 'ABMS 리뉴얼' })).toBeVisible();
    await expect(page.getByText('프로젝트 상세 설명')).toBeVisible();
    await expect(page.getByText('1,000,000원').first()).toBeVisible();

    await page.getByRole('tab', { name: '투입인력' }).click();
    await expect(page.getByText('현재 투입 중인 인력이 없습니다.')).toBeVisible();

    await page.getByRole('tab', { name: '매출 일정' }).click();
    await expect(page.getByText('등록된 매출 일정이 없습니다.')).toBeVisible();

    await page.getByRole('button', { name: '협력사A' }).first().click();

    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/parties/100/?$`));
    await expect(page.getByRole('heading', { name: '협력사A' })).toBeVisible();
  });
});
