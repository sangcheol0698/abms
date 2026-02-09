/**
 * 프로젝트 상세 정보 인터페이스
 */
export interface ProjectDetail {
  projectId: number;
  partyId: number;
  partyName: string;
  code: string;
  name: string;
  description: string | null;
  status: string;
  statusLabel: string;
  contractAmount: number;
  startDate: string;
  endDate: string | null;
  leadDepartmentId: number | null;
  leadDepartmentName: string | null;
}

/**
 * 백엔드 API 응답을 프로젝트 상세 모델로 매핑
 */
export function mapProjectDetail(input: any): ProjectDetail {
  return {
    projectId: Number(input?.projectId ?? 0),
    partyId: Number(input?.partyId ?? 0),
    partyName: String(input?.partyName ?? ''),
    code: String(input?.code ?? ''),
    name: String(input?.name ?? ''),
    description: input?.description ?? null,
    status: String(input?.status ?? ''),
    statusLabel: String(input?.statusDescription ?? ''),
    contractAmount: Number(input?.contractAmount ?? 0),
    startDate: String(input?.startDate ?? ''),
    endDate: input?.endDate ?? null,
    leadDepartmentId: input?.leadDepartmentId ? Number(input.leadDepartmentId) : null,
    leadDepartmentName: input?.leadDepartmentName ? String(input.leadDepartmentName) : null,
  };
}

/**
 * 프로젝트 생성 요청 데이터
 */
export interface ProjectCreateData {
  partyId: number;
  code: string;
  name: string;
  description: string;
  status: string;
  contractAmount: number;
  startDate: string;
  endDate: string;
  leadDepartmentId: number | null;
}

/**
 * 프로젝트 수정 요청 데이터
 */
export interface ProjectUpdateData {
  partyId: number;
  name: string;
  description: string;
  status: string;
  contractAmount: number;
  startDate: string;
  endDate: string;
  leadDepartmentId: number | null;
}
