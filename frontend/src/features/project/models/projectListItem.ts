/**
 * 프로젝트 목록 아이템 인터페이스
 */
export interface ProjectListItem {
  projectId: number;
  partyId: number;
  code: string;
  name: string;
  description: string | null;
  status: string;
  statusLabel: string;
  contractAmount: number;
  startDate: string;
  endDate: string | null;
}

/**
 * 백엔드 API 응답을 프론트엔드 모델로 매핑
 */
export function mapProjectListItem(input: any): ProjectListItem {
  return {
    projectId: Number(input?.projectId ?? 0),
    partyId: Number(input?.partyId ?? 0),
    code: String(input?.code ?? ''),
    name: String(input?.name ?? ''),
    description: input?.description ?? null,
    status: String(input?.status ?? ''),
    statusLabel: String(input?.statusDescription ?? ''),
    contractAmount: Number(input?.contractAmount ?? 0),
    startDate: String(input?.startDate ?? ''),
    endDate: input?.endDate ?? null,
  };
}

/**
 * 금액을 천 단위 구분자와 함께 포맷팅
 */
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('ko-KR').format(amount) + '원';
}

/**
 * 날짜를 YYYY.MM.DD 형식으로 포맷팅
 */
export function formatProjectDate(dateInput?: string | null): string {
  if (!dateInput || dateInput.trim() === '') {
    return '';
  }
  const normalized = dateInput.replace(/\./g, '-');
  const [year, month, day] = normalized.split('-');
  if (!year || !month || !day) {
    return dateInput;
  }
  return `${year}.${month.padStart(2, '0')}.${day.padStart(2, '0')}`;
}

/**
 * 프로젝트 기간을 포맷팅 (시작일 ~ 종료일)
 */
export function formatProjectPeriod(startDate: string, endDate: string | null): string {
  const start = formatProjectDate(startDate);
  const end = endDate ? formatProjectDate(endDate) : '진행중';
  return `${start} ~ ${end}`;
}
