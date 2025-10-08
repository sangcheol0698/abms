export interface EmployeeFilterOption {
  label: string;
  value: string;
}

const STATUS_ITEMS = [
  { code: 'ACTIVE', label: '재직' },
  { code: 'ON_LEAVE', label: '휴직' },
  { code: 'RESIGNED', label: '퇴사' },
] as const;

const TYPE_ITEMS = [
  { code: 'FULL_TIME', label: '정직원' },
  { code: 'FREELANCER', label: '프리랜서' },
  { code: 'OUTSOURCING', label: '외주' },
  { code: 'PART_TIME', label: '반프리' },
] as const;

const GRADE_ITEMS = [
  { code: 'JUNIOR', label: '초급' },
  { code: 'MID_LEVEL', label: '중급' },
  { code: 'SENIOR', label: '고급' },
  { code: 'EXPERT', label: '특급' },
] as const;

const POSITION_ITEMS = [
  { code: 'ASSOCIATE', label: '사원' },
  { code: 'STAFF', label: '선임' },
  { code: 'LEADER', label: '책임' },
  { code: 'MANAGER', label: '팀장' },
  { code: 'SENIOR_MANAGER', label: '수석' },
  { code: 'DIRECTOR', label: '이사' },
  { code: 'TECHNICAL_DIRECTOR', label: '기술이사' },
  { code: 'MANAGING_DIRECTOR', label: '상무' },
  { code: 'VICE_PRESIDENT', label: '부사장' },
  { code: 'PRESIDENT', label: '사장' },
] as const;

function toOptions(items: readonly { code: string; label: string }[]): EmployeeFilterOption[] {
  return items.map((item) => ({ label: item.label, value: item.code }));
}

function toCodeByLabel(items: readonly { code: string; label: string }[]): Record<string, string> {
  return items.reduce<Record<string, string>>((accumulator, current) => {
    accumulator[current.label] = current.code;
    return accumulator;
  }, {});
}

function toLabelByCode(items: readonly { code: string; label: string }[]): Record<string, string> {
  return items.reduce<Record<string, string>>((accumulator, current) => {
    accumulator[current.code] = current.label;
    return accumulator;
  }, {});
}

let employeeStatusOptions = toOptions(STATUS_ITEMS);
let employeeTypeOptions = toOptions(TYPE_ITEMS);
let employeeGradeOptions = toOptions(GRADE_ITEMS);
let employeePositionOptions = toOptions(POSITION_ITEMS);

let statusCodeByLabel = toCodeByLabel(STATUS_ITEMS);
let typeCodeByLabel = toCodeByLabel(TYPE_ITEMS);
let gradeCodeByLabel = toCodeByLabel(GRADE_ITEMS);
let positionCodeByLabel = toCodeByLabel(POSITION_ITEMS);

let statusLabelByCode = toLabelByCode(STATUS_ITEMS);
let typeLabelByCode = toLabelByCode(TYPE_ITEMS);
let gradeLabelByCode = toLabelByCode(GRADE_ITEMS);
let positionLabelByCode = toLabelByCode(POSITION_ITEMS);

export function getEmployeeStatusOptions(): EmployeeFilterOption[] {
  return employeeStatusOptions;
}

export function getEmployeeTypeOptions(): EmployeeFilterOption[] {
  return employeeTypeOptions;
}

export function getEmployeeGradeOptions(): EmployeeFilterOption[] {
  return employeeGradeOptions;
}

export function getEmployeePositionOptions(): EmployeeFilterOption[] {
  return employeePositionOptions;
}

export function setEmployeeStatusOptions(options: EmployeeFilterOption[]) {
  employeeStatusOptions = options;
  statusCodeByLabel = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.label] = option.value;
    return acc;
  }, {});
  statusLabelByCode = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.value] = option.label;
    return acc;
  }, {});
}

export function setEmployeeTypeOptions(options: EmployeeFilterOption[]) {
  employeeTypeOptions = options;
  typeCodeByLabel = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.label] = option.value;
    return acc;
  }, {});
  typeLabelByCode = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.value] = option.label;
    return acc;
  }, {});
}

export function setEmployeeGradeOptions(options: EmployeeFilterOption[]) {
  employeeGradeOptions = options;
  gradeCodeByLabel = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.label] = option.value;
    return acc;
  }, {});
  gradeLabelByCode = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.value] = option.label;
    return acc;
  }, {});
}

export function setEmployeePositionOptions(options: EmployeeFilterOption[]) {
  employeePositionOptions = options;
  positionCodeByLabel = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.label] = option.value;
    return acc;
  }, {});
  positionLabelByCode = options.reduce<Record<string, string>>((acc, option) => {
    acc[option.value] = option.label;
    return acc;
  }, {});
}

export function toStatusCode(description: string): string {
  return statusCodeByLabel[description] ?? description;
}

export function toTypeCode(description: string): string {
  return typeCodeByLabel[description] ?? description;
}

export function toGradeCode(description: string): string {
  return gradeCodeByLabel[description] ?? description;
}

export function toPositionCode(description: string): string {
  return positionCodeByLabel[description] ?? description;
}

export function toStatusLabel(code: string): string {
  return statusLabelByCode[code] ?? code;
}

export function toTypeLabel(code: string): string {
  return typeLabelByCode[code] ?? code;
}

export function toGradeLabel(code: string): string {
  return gradeLabelByCode[code] ?? code;
}

export function toPositionLabel(code: string): string {
  return positionLabelByCode[code] ?? code;
}
