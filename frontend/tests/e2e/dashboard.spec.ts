import { test, expect } from '@playwright/test';

const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? 'http://localhost:5173';

test.describe('대시보드 화면', () => {
  test('핵심 위젯과 설명이 렌더링된다', async ({ page }) => {
    await page.goto(`${BASE_URL}/`);

    await expect(page.getByRole('heading', { level: 1, name: '대시보드' })).toBeVisible();
    await expect(page.getByText('서비스 주요 지표와 최근 활동을 한눈에 확인하세요.')).toBeVisible();

    await expect(page.getByText('총 구성원')).toBeVisible();
    await expect(page.getByText('활성 프로젝트')).toBeVisible();
    await expect(page.getByText('이번 달 신규 입사')).toBeVisible();
    await expect(page.getByText('휴가 중 구성원')).toBeVisible();

    await expect(page.getByRole('heading', { name: '최근 알림' })).toBeVisible();
    await expect(page.getByRole('heading', { name: '빠른 작업' })).toBeVisible();
  });

  test('빠른 작업 링크를 통해 다른 화면으로 이동한다', async ({ page }) => {
    await page.goto(`${BASE_URL}/`);

    await page.getByRole('link', { name: /조직도 확인/ }).click();

    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/organization/?$`));
    await expect(page.getByRole('heading', { level: 1, name: '조직도' })).toBeVisible();
  });
});
