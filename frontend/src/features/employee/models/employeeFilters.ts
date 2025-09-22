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

export const EMPLOYEE_STATUS_OPTIONS = toOptions(STATUS_ITEMS);
export const EMPLOYEE_TYPE_OPTIONS = toOptions(TYPE_ITEMS);
export const EMPLOYEE_GRADE_OPTIONS = toOptions(GRADE_ITEMS);
export const EMPLOYEE_POSITION_OPTIONS = toOptions(POSITION_ITEMS);

const STATUS_CODE_BY_LABEL = toCodeByLabel(STATUS_ITEMS);
const TYPE_CODE_BY_LABEL = toCodeByLabel(TYPE_ITEMS);
const GRADE_CODE_BY_LABEL = toCodeByLabel(GRADE_ITEMS);
const POSITION_CODE_BY_LABEL = toCodeByLabel(POSITION_ITEMS);

const STATUS_LABEL_BY_CODE = toLabelByCode(STATUS_ITEMS);
const TYPE_LABEL_BY_CODE = toLabelByCode(TYPE_ITEMS);
const GRADE_LABEL_BY_CODE = toLabelByCode(GRADE_ITEMS);
const POSITION_LABEL_BY_CODE = toLabelByCode(POSITION_ITEMS);

export function toStatusCode(description: string): string {
    return STATUS_CODE_BY_LABEL[description] ?? description;
}

export function toTypeCode(description: string): string {
    return TYPE_CODE_BY_LABEL[description] ?? description;
}

export function toGradeCode(description: string): string {
    return GRADE_CODE_BY_LABEL[description] ?? description;
}

export function toPositionCode(description: string): string {
    return POSITION_CODE_BY_LABEL[description] ?? description;
}

export function toStatusLabel(code: string): string {
    return STATUS_LABEL_BY_CODE[code] ?? code;
}

export function toTypeLabel(code: string): string {
    return TYPE_LABEL_BY_CODE[code] ?? code;
}

export function toGradeLabel(code: string): string {
    return GRADE_LABEL_BY_CODE[code] ?? code;
}

export function toPositionLabel(code: string): string {
    return POSITION_LABEL_BY_CODE[code] ?? code;
}
