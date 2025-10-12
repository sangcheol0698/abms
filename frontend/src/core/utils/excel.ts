const EXCEL_MIME_TYPES = [
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'application/vnd.ms-excel',
];

const MAX_EXCEL_FILE_SIZE = 10 * 1024 * 1024;

export function downloadBlob(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  window.URL.revokeObjectURL(url);
}

export function generateExcelFilename(prefix: string, extension = 'xlsx'): string {
  const now = new Date();
  const date = now.toISOString().slice(0, 10).replace(/-/g, '');
  const time = now.toTimeString().slice(0, 8).replace(/:/g, '');
  return `${prefix}_${date}_${time}.${extension}`;
}

export function createExcelFormData(file: File, fields?: Record<string, string>): FormData {
  const formData = new FormData();
  formData.append('file', file);

  if (fields) {
    Object.entries(fields).forEach(([key, value]) => {
      formData.append(key, value);
    });
  }

  return formData;
}

export function formatFileSize(bytes: number): string {
  if (bytes === 0) {
    return '0 Bytes';
  }
  const units = ['Bytes', 'KB', 'MB', 'GB'];
  const index = Math.floor(Math.log(bytes) / Math.log(1024));
  const size = bytes / 1024 ** index;
  return `${size.toFixed(2)} ${units[index]}`;
}

export function validateExcelFile(file: File): { isValid: boolean; message?: string } {
  if (!EXCEL_MIME_TYPES.includes(file.type)) {
    return { isValid: false, message: '엑셀 파일(.xlsx, .xls)만 업로드 가능합니다.' };
  }

  if (file.size > MAX_EXCEL_FILE_SIZE) {
    return { isValid: false, message: '파일 크기는 10MB를 초과할 수 없습니다.' };
  }

  return { isValid: true };
}

export function extractFilenameFromResponse(response: Response, fallback = 'download.xlsx'): string {
  const header = response.headers.get('Content-Disposition');
  if (header) {
    const match = header.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (match && match[1]) {
      return match[1].replace(/['"]/g, '');
    }
  }
  return fallback;
}

export function getExcelErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return '엑셀 작업 처리 중 오류가 발생했습니다.';
}
