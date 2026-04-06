import { hasStoredPermission } from '@/features/auth/session';

const REPORT_READ = 'report.read';

export function canReadWeeklyReports(): boolean {
  return hasStoredPermission(REPORT_READ);
}
