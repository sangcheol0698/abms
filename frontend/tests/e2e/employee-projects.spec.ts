import { expect, test } from '@playwright/test';
import { mockAuthenticatedSession } from './support/auth';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';
const API_ROUTE = /^https?:\/\/[^/]+\/api(?:\/.*)?(?:\?.*)?$/;

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

const employeeProjectsResponse = {
  content: [
    {
      projectId: 11,
      projectCode: 'PRJ-11',
      projectName: 'ABMS 구축',
      role: '개발자',
      assignmentStartDate: '2026-01-01',
      assignmentEndDate: null,
      assignmentStatus: 'CURRENT',
      projectStatus: 'IN_PROGRESS',
      projectStatusDescription: '진행 중',
      leadDepartmentName: '백엔드팀',
      partyName: '아바쿠스',
    },
  ],
  pageNumber: 0,
  pageSize: 10,
  totalElements: 1,
  totalPages: 1,
};

test.describe('직원 프로젝트 탭', () => {
  test('프로젝트 탭에서 프로젝트 이력을 보여주고 상세로 이동한다', async ({ page }) => {
    await mockAuthenticatedSession(page);

    await page.route(API_ROUTE, async (route) => {
      const request = route.request();
      const url = new URL(request.url());
      const path = url.pathname.endsWith('/') && url.pathname !== '/'
        ? url.pathname.slice(0, -1)
        : url.pathname;

      if (path === '/api/csrf') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ ok: true }) });
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
              { code: 'employee.read', scopes: ['ALL'] },
              { code: 'employee.write', scopes: ['ALL'] },
              { code: 'project.read', scopes: ['ALL'] },
            ],
          }),
        });
        return;
      }

      if (
        path === '/api/employees/1'
        || path === '/api/employees/1/projects'
        || path === '/api/departments/organization-chart'
        || path === '/api/employees/types'
        || path === '/api/employees/grades'
        || path === '/api/employees/positions'
      ) {
        const body =
          path === '/api/employees/1'
            ? employeeDetailResponse
            : path === '/api/employees/1/projects'
              ? employeeProjectsResponse
              : [];
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(body),
        });
        return;
      }

      if (path === '/api/projects/11') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            projectId: 11,
            partyId: 3,
            partyName: '아바쿠스',
            code: 'PRJ-11',
            name: 'ABMS 구축',
            description: '',
            status: 'IN_PROGRESS',
            statusDescription: '진행 중',
            contractAmount: 1000000,
            startDate: '2026-01-01',
            endDate: null,
            leadDepartmentId: 10,
            leadDepartmentName: '백엔드팀',
          }),
        });
        return;
      }

      if (request.method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }

      await route.fulfill({ status: 204, contentType: 'application/json', body: '' });
    });

    await page.goto(`${BASE_URL}/employees/1?tab=projects`);
    await expect(page.getByRole('button', { name: 'ABMS 구축' })).toBeVisible();

    await page.getByRole('button', { name: 'ABMS 구축' }).click();
    await expect(page).toHaveURL(/\/projects\/11$/);
  });

  test('project.read 권한이 없으면 프로젝트 탭이 보이지 않는다', async ({ page }) => {
    await page.addInitScript(() => {
      window.localStorage.setItem(
        'user',
        JSON.stringify({
          email: 'tester@abms.co.kr',
          name: '테스터',
          employeeId: 1,
          departmentId: 10,
          permissions: [
            { code: 'employee.read', scopes: ['ALL'] },
            { code: 'employee.write', scopes: ['ALL'] },
          ],
        }),
      );
    });

    await page.route(API_ROUTE, async (route) => {
      const url = new URL(route.request().url());
      const path = url.pathname.endsWith('/') && url.pathname !== '/'
        ? url.pathname.slice(0, -1)
        : url.pathname;

      if (path === '/api/csrf') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ ok: true }) });
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
              { code: 'employee.read', scopes: ['ALL'] },
              { code: 'employee.write', scopes: ['ALL'] },
            ],
          }),
        });
        return;
      }

      if (
        path === '/api/employees/1'
        || path === '/api/departments/organization-chart'
        || path === '/api/employees/types'
        || path === '/api/employees/grades'
        || path === '/api/employees/positions'
      ) {
        const body = path === '/api/employees/1' ? employeeDetailResponse : [];
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(body),
        });
        return;
      }

      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }

      await route.fulfill({ status: 204, contentType: 'application/json', body: '' });
    });

    await page.goto(`${BASE_URL}/employees/1`);
    await expect(page.getByRole('tab', { name: '프로젝트' })).toHaveCount(0);
  });
});
