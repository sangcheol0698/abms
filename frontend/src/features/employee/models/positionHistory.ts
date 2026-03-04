// 1. 직급 Enum 정의 (백엔드와 일치)
export enum EmployeePosition {
  ASSOCIATE = 'ASSOCIATE',
  STAFF = 'STAFF',
  SENIOR = 'SENIOR',
  PRINCIPAL = 'PRINCIPAL',
  LEAD = 'LEAD',
  DIRECTOR = 'DIRECTOR',
}

export const PositionLabel: Record<string, string> = {
  [EmployeePosition.ASSOCIATE]: '사원',
  [EmployeePosition.STAFF]: '선임',
  [EmployeePosition.SENIOR]: '책임',
  [EmployeePosition.PRINCIPAL]: '수석',
  [EmployeePosition.LEAD]: '팀장',
  [EmployeePosition.DIRECTOR]: '이사',
};

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
