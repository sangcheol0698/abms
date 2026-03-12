import { hasStoredPermission } from '@/features/auth/session';

const PROJECT_READ = 'project.read';
const PROJECT_WRITE = 'project.write';
const PROJECT_EXCEL_DOWNLOAD = 'project.excel.download';
const PROJECT_EXCEL_UPLOAD = 'project.excel.upload';

export function canReadProjects(): boolean {
  return hasStoredPermission(PROJECT_READ);
}

export function canManageProjects(): boolean {
  return hasStoredPermission(PROJECT_WRITE);
}

export function canDownloadProjectExcel(): boolean {
  return hasStoredPermission(PROJECT_EXCEL_DOWNLOAD);
}

export function canUploadProjectExcel(): boolean {
  return hasStoredPermission(PROJECT_EXCEL_UPLOAD);
}
