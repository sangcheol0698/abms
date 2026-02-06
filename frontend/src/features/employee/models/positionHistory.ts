// 1. 직급 Enum 정의 (백엔드와 일치)
export enum EmployeePosition {
  ASSOCIATE = 'ASSOCIATE',
  STAFF = 'STAFF',
  SENIOR = 'SENIOR',
  PRINCIPAL = 'PRINCIPAL',
  LEAD = 'LEAD',
  DIRECTOR = 'DIRECTOR',
}

// 2. [에러 원인 해결] 화면 표시용 한글 매핑 객체를 export 해야 합니다.

// 3. 기간 인터페이스
export interface PositionPeriod {
  startDate: string;
  endDate: string | null;
}

// 4. 이력 메인 인터페이스
export interface PositionHistory {
  id: number;
  employeeId: number;
  period: PositionPeriod;
  position: string; // Enum이나 string 모두 허용
}
