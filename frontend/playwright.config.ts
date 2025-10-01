import { defineConfig, devices } from '@playwright/test';

// 기본 포트는 Vite 개발 서버(5173)를 사용합니다.
const DEV_SERVER_PORT = Number(process.env.PLAYWRIGHT_PORT ?? 5173);
const BASE_URL = process.env.PLAYWRIGHT_TEST_BASE_URL ?? `http://localhost:${DEV_SERVER_PORT}`;

export default defineConfig({
  testDir: './tests/e2e',
  reporter: 'list', // 콘솔에서 바로 확인하기 좋은 기본 리포터
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  use: {
    baseURL: BASE_URL, // `page.goto('/')` 호출 시 사용할 기준 주소
    trace: 'on-first-retry', // 실패한 테스트는 첫 재시도에서 트레이스를 남깁니다.
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: `npm run dev -- --port ${DEV_SERVER_PORT}`, // 필요할 때 Vite 개발 서버를 자동으로 띄웁니다.
    url: BASE_URL,
    reuseExistingServer: true, // 이미 실행 중이면 재사용해서 속도를 높입니다.
  },
});
