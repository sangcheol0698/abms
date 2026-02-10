/**
 * Enum API 응답 타입 및 유틸리티
 *
 * 백엔드의 EnumResponse를 처리하는 타입과 변환 함수들을 제공합니다.
 */

/**
 * Enum 응답 공통 타입
 *
 * 백엔드의 EnumResponse record와 일치합니다.
 *
 * @see kr.co.abacus.abms.adapter.web.EnumResponse
 *
 * @example
 * // API 응답 예시: GET /api/employees/statuses
 * [
 *   { "code": "ACTIVE", "description": "재직" },
 *   { "code": "ON_LEAVE", "description": "휴직" },
 *   { "code": "RESIGNED", "description": "퇴사" }
 * ]
 */
export interface EnumResponse {
  code: string;
  description: string;
  level: number;
}

/**
 * Select/Filter 옵션 타입
 *
 * UI 컴포넌트(Select, Combobox, Filter 등)에서 사용하는 표준 옵션 형태
 */
export interface SelectOption {
  value: string;
  label: string;
  level?: number;
}

/**
 * EnumResponse 객체 또는 문자열에서 code 추출
 *
 * 백엔드가 { code: string, description: string } 형태로 반환하거나
 * 레거시로 문자열을 반환하는 경우 모두 처리합니다.
 *
 * @param value - EnumResponse 객체 또는 문자열
 * @returns code 문자열
 *
 * @example
 * // EnumResponse 객체
 * extractEnumCode({ code: "ACTIVE", description: "재직" })
 * // "ACTIVE"
 *
 * // 문자열 (하위 호환)
 * extractEnumCode("ACTIVE")
 * // "ACTIVE"
 *
 * // null/undefined
 * extractEnumCode(null)
 * // ""
 */
export function extractEnumCode(value: any): string {
  if (!value) return '';
  // EnumResponse 객체인 경우
  if (typeof value === 'object' && value.code) {
    return String(value.code);
  }
  // 이미 문자열인 경우 (하위 호환)
  if (typeof value === 'string') {
    return value;
  }
  return '';
}

/**
 * EnumResponse를 SelectOption으로 변환
 *
 * 백엔드 Enum 응답을 UI에서 사용할 수 있는 형태로 변환합니다.
 *
 * @param response - 백엔드 EnumResponse
 * @returns SelectOption - UI용 옵션
 *
 * @example
 * const status = { code: "ACTIVE", description: "재직" };
 * const option = toSelectOption(status);
 * // { value: "ACTIVE", label: "재직" }
 */
export function toSelectOption(response: EnumResponse): SelectOption {
  return {
    value: response.code,
    label: response.description || response.code,
    level: response.level,
  };
}

/**
 * EnumResponse 배열을 SelectOption 배열로 변환
 *
 * @param responses - 백엔드 EnumResponse 배열
 * @returns SelectOption 배열
 *
 * @example
 * const statuses = [
 *   { code: "ACTIVE", description: "재직" },
 *   { code: "ON_LEAVE", description: "휴직" }
 * ];
 * const options = toSelectOptions(statuses);
 * // [
 * //   { value: "ACTIVE", label: "재직" },
 * //   { value: "ON_LEAVE", label: "휴직" }
 * // ]
 */
export function toSelectOptions(responses: EnumResponse[]): SelectOption[] {
  return responses.map(toSelectOption);
}

/**
 * 정렬 가능한 Enum 응답 타입
 *
 * level이나 rank 같은 순서값을 가진 Enum에 사용
 */
export interface SortableEnumResponse extends EnumResponse {
  order?: number;
}

/**
 * 정렬 가능한 EnumResponse 배열을 정렬 후 SelectOption으로 변환
 *
 * @param responses - 정렬 가능한 EnumResponse 배열
 * @returns 정렬된 SelectOption 배열
 *
 * @example
 * const grades = [
 *   { code: "SENIOR", description: "시니어", order: 2 },
 *   { code: "JUNIOR", description: "주니어", order: 3 },
 *   { code: "LEAD", description: "리드", order: 1 }
 * ];
 * const options = toSortedSelectOptions(grades);
 * // [
 * //   { value: "LEAD", label: "리드" },      // order: 1
 * //   { value: "SENIOR", label: "시니어" },  // order: 2
 * //   { value: "JUNIOR", label: "주니어" }   // order: 3
 * // ]
 */
export function toSortedSelectOptions(responses: SortableEnumResponse[]): SelectOption[] {
  return responses
    .slice() // 원본 배열 보존
    .sort((a, b) => {
      const orderA = a.order ?? Number.MAX_SAFE_INTEGER;
      const orderB = b.order ?? Number.MAX_SAFE_INTEGER;
      return orderA - orderB;
    })
    .map(toSelectOption);
}
