import { test, expect } from '@playwright/test';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';

const employeeListResponse = {
  content: [
    {
      employeeId: '02ed3834-6350-4e83-8eec-77352e1412b1',
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

const employeeDetailResponse = {
  departmentId: '67766493-56a7-47f5-8c2a-4de7fcc50d54',
  departmentName: '백엔드팀',
  employeeId: '02ed3834-6350-4e83-8eec-77352e1412b1',
  name: '박지훈',
  email: 'be3@abms.co',
  joinDate: '2024-01-02',
  birthDate: '1999-03-09',
  position: '사원',
  status: '재직',
  grade: '초급',
  type: '프리랜서',
  memo: '배치/ETL',
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

const organizationChartResponse: any[] = [];

test.describe('구성원 수정 다이얼로그', () => {
  test('기존 입사일과 생년월일을 그대로 표시한다', async ({ page }) => {
    await page.route('**/api/employees', async (route) => {
      const url = route.request().url();
      if (url.includes('/api/employees?')) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(employeeListResponse),
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

    await page.route('**/api/departments/organization-chart', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(organizationChartResponse) }),
    );

    await page.route('**/api/employees/02ed3834-6350-4e83-8eec-77352e1412b1', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(employeeDetailResponse) }),
    );

    await page.goto(`${BASE_URL}/employees`);

    await expect(page.getByRole('heading', { name: '구성원 목록' })).toBeVisible();
    await expect(page.getByRole('row', { name: /박지훈/ })).toBeVisible();

    const targetRow = page.getByRole('row', { name: /박지훈/ });
    await targetRow.getByRole('button', { name: '구성원 메뉴 열기' }).click();
    await page.getByRole('menuitem', { name: '구성원 편집' }).click();

    const dialog = page.getByRole('dialog', { name: '구성원 편집' });
    await expect(dialog).toBeVisible();

    await expect(dialog.getByRole('button', { name: /1999\.\s03\.\s09(?:\.)?/ })).toBeVisible();
    await expect(dialog.getByRole('button', { name: /2024\.\s01\.\s02(?:\.)?/ })).toBeVisible();
  });
});
