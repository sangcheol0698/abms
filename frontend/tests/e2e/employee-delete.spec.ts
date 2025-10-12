import { test, expect } from '@playwright/test';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';

const employeeId = '02ed3834-6350-4e83-8eec-77352e1412b1';

const employeeListResponse = {
  content: [
    {
      employeeId,
      departmentId: '67766493-56a7-47f5-8c2a-4de7fcc50d54',
      departmentName: '백엔드팀',
      name: '박지훈',
      email: 'be3@abms.co',
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

const statusOptions = [
  { name: 'ACTIVE', description: '재직' },
  { name: 'ON_LEAVE', description: '휴직' },
];

const typeOptions = [
  { name: 'FULL_TIME', description: '정직원' },
  { name: 'FREELANCER', description: '프리랜서' },
];

const gradeOptions = [
  { name: 'JUNIOR', description: '초급', level: 1 },
  { name: 'MID_LEVEL', description: '중급', level: 2 },
];

const positionOptions = [
  { name: 'ASSOCIATE', description: '사원', rank: 1 },
  { name: 'MANAGER', description: '팀장', rank: 2 },
];

test.describe('구성원 삭제', () => {
  test('삭제 확인 후 API 호출과 목록 갱신이 이뤄진다', async ({ page }) => {
    let deleteCalled = false;

    await page.route('**/api/employees', async (route) => {
      const request = route.request();
      if (request.method() === 'GET') {
        const body = deleteCalled
          ? { ...employeeListResponse, content: [], totalElements: 0 }
          : employeeListResponse;
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(body),
        });
        return;
      }
      await route.continue();
    });

    await page.route('**/api/employees/statuses', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(statusOptions) }),
    );
    await page.route('**/api/employees/types', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(typeOptions) }),
    );
    await page.route('**/api/employees/grades', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(gradeOptions) }),
    );
    await page.route('**/api/employees/positions', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(positionOptions) }),
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

    await expect(page.getByRole('heading', { name: '구성원 목록' })).toBeVisible();
    const row = page.getByRole('row', { name: /박지훈/ });
    await expect(row).toBeVisible();

    await row.getByRole('button', { name: '구성원 메뉴 열기' }).click();
    await page.getByRole('menuitem', { name: '구성원 삭제' }).click();

    await expect(page.getByRole('dialog', { name: '구성원을 삭제할까요?' })).toBeVisible();
    await expect(page.getByText(/박지훈 구성원을 삭제하면/)).toBeVisible();

    await page.getByRole('button', { name: '삭제' }).click();

    await expect.poll(() => deleteCalled).toBeTruthy();

    await expect(page.getByText('구성원을 삭제했습니다.')).toBeVisible();
    await expect(page.getByRole('row', { name: /박지훈/ })).toHaveCount(0);
    await expect(page.getByText('구성원 정보를 찾을 수 없습니다')).toBeVisible();
  });
});
