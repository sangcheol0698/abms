import path from 'node:path';
import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    environment: 'happy-dom',
    globals: true,
    setupFiles: ['src/test-utils/setup.ts'],
    include: ['src/**/*.{test,spec}.{js,ts,jsx,tsx}'],
    exclude: ['tests/**', 'node_modules/**'],
    coverage: {
      provider: 'v8',
      include: ['src/features/**/*.{ts,vue}', 'src/core/**/*.{ts,vue}'],
      exclude: [
        'src/features/chat/**',
        'src/test-utils/**',
        'src/**/*.spec.*',
        'src/**/*.test.*',
        'src/**/index.ts',
        'src/**/__tests__/**',
      ],
      reporter: ['text', 'json', 'html'],
      thresholds: {
        statements: 55,
        branches: 74,
        functions: 52,
        lines: 55,
      },
    },
  },
});
