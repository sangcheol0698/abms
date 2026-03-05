import { test, expect } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';

const employeeId = 1;

const employeeListResponse = {
  content: [
    {
      employeeId,
      departmentId: 10,
      departmentName: '백엔드팀',
      name: '박지훈',
      email: 'be3@abms.co.kr',
      position: '사원',
      status: '재직',
      grade: '초급',
      type: '프리랜서',
      memo: '배치/ETL',
      joinDate: '2024-01-02',
      birthDate: '1999-03-09',
    },
  ],
  number: 0,
  size: 10,
  totalPages: 1,
  totalElements: 1,
};

test.describe('직원 삭제', () => {
  test('삭제 확인 후 API 호출과 목록 갱신이 이뤄진다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    let deleteCalled = false;

    await page.route('**/api/employees?**', async (route) => {
      const body = deleteCalled
        ? { ...employeeListResponse, content: [], totalElements: 0 }
        : employeeListResponse;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(body),
      });
    });

    await page.route('**/api/employees/statuses', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }),
    );
    await page.route('**/api/employees/types', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }),
    );
    await page.route('**/api/employees/grades', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }),
    );
    await page.route('**/api/employees/positions', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }),
    );

    await page.route(`**/api/employees/${employeeId}`, async (route) => {
      if (route.request().method() === 'DELETE') {
        deleteCalled = true;
        await route.fulfill({ status: 204, contentType: 'application/json', body: '' });
        return;
      }
      await route.continue();
    });

    await page.goto(`${BASE_URL}/employees`);

    const row = page.getByRole('row', { name: /박지훈/ });
    await expect(row).toBeVisible();

    await row.getByRole('button', { name: '직원 메뉴 열기' }).click();
    await page.getByRole('menuitem', { name: '직원 삭제' }).click();

    await expect(page.getByText('직원을 삭제할까요?')).toBeVisible();

    await page.getByRole('button', { name: '삭제' }).click();

    await expect.poll(() => deleteCalled).toBeTruthy();

    await expect(page.getByText('직원을 삭제했습니다.')).toBeVisible();
    await expect(page.getByRole('row', { name: /박지훈/ })).toHaveCount(0);
    await expect(page.getByText('직원 정보를 찾을 수 없습니다')).toBeVisible();
  });
});
