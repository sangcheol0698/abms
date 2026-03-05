import { test, expect } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';

const employeeListResponse = {
  content: [
    {
      employeeId: 1,
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

const employeeDetailResponse = {
  departmentId: 10,
  departmentName: '백엔드팀',
  employeeId: 1,
  name: '박지훈',
  email: 'be3@abms.co.kr',
  joinDate: '2024-01-02',
  birthDate: '1999-03-09',
  position: '사원',
  status: '재직',
  grade: '초급',
  type: '프리랜서',
  memo: '배치/ETL',
};

test.describe('직원 수정 다이얼로그', () => {
  test('기존 입사일과 생년월일을 그대로 표시한다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    await page.route('**/api/employees?**', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(employeeListResponse),
      }),
    );

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

    await page.route('**/api/departments/organization-chart', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }),
    );

    await page.route('**/api/employees/1', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(employeeDetailResponse),
      }),
    );

    await page.goto(`${BASE_URL}/employees`);

    await expect(page.getByRole('row', { name: /박지훈/ })).toBeVisible();

    const targetRow = page.getByRole('row', { name: /박지훈/ });
    await targetRow.getByRole('button', { name: '직원 메뉴 열기' }).click();
    await page.getByRole('menuitem', { name: '직원 편집' }).click();

    const dialog = page.getByRole('dialog', { name: '직원 편집' });
    await expect(dialog).toBeVisible();

    await expect(dialog.getByRole('button', { name: /1999\.?\s*03\.?\s*09/ })).toBeVisible();
    await expect(dialog.getByRole('button', { name: /2024\.?\s*01\.?\s*02/ })).toBeVisible();
  });
});
