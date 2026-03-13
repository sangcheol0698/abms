export type ThemePreference = 'light' | 'dark' | 'system';

export const themeOptions = [
  {
    value: 'light',
    label: '라이트',
    description: '밝은 화면으로 고정합니다.',
  },
  {
    value: 'dark',
    label: '다크',
    description: '어두운 화면으로 고정합니다.',
  },
  {
    value: 'system',
    label: '시스템',
    description: '운영체제 설정을 따라갑니다.',
  },
] as const satisfies ReadonlyArray<{
  value: ThemePreference;
  label: string;
  description: string;
}>;

export const chartColorVars = [
  'var(--chart-1)',
  'var(--chart-2)',
  'var(--chart-3)',
  'var(--chart-4)',
  'var(--chart-5)',
] as const;

export const statusColorVars = {
  info: 'var(--status-info)',
  success: 'var(--status-success)',
  warning: 'var(--status-warning)',
  error: 'var(--status-danger)',
  danger: 'var(--status-danger)',
  neutral: 'var(--status-neutral)',
} as const;

export function getChartColor(index: number): string {
  return chartColorVars[index % chartColorVars.length];
}
