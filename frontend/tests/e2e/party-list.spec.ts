import { expect, test } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';
import { BASE_URL, fulfillJson, mockApi } from './support/api';

test.describe('협력사 목록', () => {
  test('검색어 입력 후 Enter를 누르면 name 필터로 조회한다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    const requestedUrls: string[] = [];

    await mockApi(page, async ({ route, url, path, method }) => {
      if (path === '/api/parties' && method === 'GET') {
        requestedUrls.push(url.toString());
        const name = url.searchParams.get('name');

        await fulfillJson(route, {
          content:
            name === '검색'
              ? [
                  {
                    partyId: 2,
                    name: '검색 협력사',
                    ceo: '대표B',
                    manager: '담당B',
                    contact: '010-2222-2222',
                    email: 'search@abms.co.kr',
                  },
                ]
              : [
                  {
                    partyId: 1,
                    name: '기본 협력사',
                    ceo: '대표A',
                    manager: '담당A',
                    contact: '010-1111-1111',
                    email: 'base@abms.co.kr',
                  },
                ],
          number: 0,
          size: 10,
          totalPages: 1,
          totalElements: 1,
        });
        return true;
      }

      if (path === '/api/parties/summary') {
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

    await page.goto(`${BASE_URL}/parties`);

    await expect(page.getByText('기본 협력사')).toBeVisible();

    const searchInput = page.getByPlaceholder('협력사명을 입력하세요');
    await searchInput.fill('검색');
    await searchInput.press('Enter');

    await expect.poll(() => requestedUrls.some((value) => value.includes('name=%EA%B2%80%EC%83%89'))).toBeTruthy();
    await expect(page.getByText('검색 협력사')).toBeVisible();
  });

  test('협력사를 등록하면 성공 토스트와 함께 목록이 갱신된다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    let created = false;

    await mockApi(page, async ({ route, path, method }) => {
      if (path === '/api/parties' && method === 'GET') {
        await fulfillJson(route, {
          content: created
            ? [
                {
                  partyId: 1,
                  name: '기본 협력사',
                  ceo: '대표A',
                  manager: '담당A',
                  contact: '010-1111-1111',
                  email: 'base@abms.co.kr',
                },
                {
                  partyId: 2,
                  name: '신규 협력사',
                  ceo: '신대표',
                  manager: '신담당',
                  contact: '010-9999-9999',
                  email: 'new@abms.co.kr',
                },
              ]
            : [
                {
                  partyId: 1,
                  name: '기본 협력사',
                  ceo: '대표A',
                  manager: '담당A',
                  contact: '010-1111-1111',
                  email: 'base@abms.co.kr',
                },
              ],
          number: 0,
          size: 10,
          totalPages: 1,
          totalElements: created ? 2 : 1,
        });
        return true;
      }

      if (path === '/api/parties/summary') {
        await fulfillJson(route, {
          totalCount: created ? 2 : 1,
          withProjectsCount: 1,
          withInProgressProjectsCount: 1,
          withoutProjectsCount: created ? 1 : 0,
          totalContractAmount: 1000000,
        });
        return true;
      }

      if (path === '/api/parties' && method === 'POST') {
        created = true;
        await fulfillJson(route, {
          partyId: 2,
          name: '신규 협력사',
          ceo: '신대표',
          manager: '신담당',
          contact: '010-9999-9999',
          email: 'new@abms.co.kr',
        });
        return true;
      }

      return false;
    });

    await page.goto(`${BASE_URL}/parties`);

    await page.getByRole('button', { name: '협력사 추가' }).click();
    await expect(page.getByRole('dialog', { name: '협력사 등록' })).toBeVisible();

    await page.getByLabel('협력사명 *').fill('신규 협력사');
    await page.getByLabel('대표자').fill('신대표');
    await page.getByLabel('담당자').fill('신담당');
    await page.getByLabel('연락처').fill('01099999999');
    await page.getByLabel('이메일').fill('new@abms.co.kr');
    await page.getByRole('button', { name: '등록' }).click();

    await expect(page.getByText('협력사가 등록되었습니다.')).toBeVisible();
    await expect(page.getByText('신규 협력사')).toBeVisible();
  });
});
