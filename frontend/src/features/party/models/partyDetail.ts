/**
 * 협력사 상세 정보 인터페이스
 */
export interface PartyDetail {
  partyId: number;
  name: string;
  ceo: string | null;
  manager: string | null;
  contact: string | null;
  email: string | null;
}

/**
 * 백엔드 API 응답을 협력사 상세 모델로 매핑
 */
export function mapPartyDetail(input: any): PartyDetail {
  return {
    partyId: Number(input?.partyId ?? 0),
    name: String(input?.name ?? ''),
    ceo: input?.ceo ?? null,
    manager: input?.manager ?? null,
    contact: input?.contact ?? null,
    email: input?.email ?? null,
  };
}

/**
 * 협력사 생성 요청 데이터
 */
export interface PartyCreateData {
  name: string;
  ceoName: string | null;
  salesRepName: string | null;
  salesRepPhone: string | null;
  salesRepEmail: string | null;
}

/**
 * 협력사 수정 요청 데이터
 */
export interface PartyUpdateData {
  name: string;
  ceoName: string | null;
  salesRepName: string | null;
  salesRepPhone: string | null;
  salesRepEmail: string | null;
}
