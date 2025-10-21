export interface EmployeeFilterOption {
  label: string;
  value: string;
}

let employeeStatusOptions: EmployeeFilterOption[] = [];
let employeeTypeOptions: EmployeeFilterOption[] = [];
let employeeGradeOptions: EmployeeFilterOption[] = [];
let employeePositionOptions: EmployeeFilterOption[] = [];

let statusCodeByLabel: Record<string, string> = {};
let typeCodeByLabel: Record<string, string> = {};
let gradeCodeByLabel: Record<string, string> = {};
let positionCodeByLabel: Record<string, string> = {};

let statusLabelByCode: Record<string, string> = {};
let typeLabelByCode: Record<string, string> = {};
let gradeLabelByCode: Record<string, string> = {};
let positionLabelByCode: Record<string, string> = {};

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

export function resetEmployeeFilterOptions() {
  employeeStatusOptions = [];
  employeeTypeOptions = [];
  employeeGradeOptions = [];
  employeePositionOptions = [];

  statusCodeByLabel = {};
  typeCodeByLabel = {};
  gradeCodeByLabel = {};
  positionCodeByLabel = {};

  statusLabelByCode = {};
  typeLabelByCode = {};
  gradeLabelByCode = {};
  positionLabelByCode = {};
}
