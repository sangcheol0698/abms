function toIsoDateString(value: unknown): string {
  if (!value) {
    return '';
  }

  if (typeof value === 'string') {
    return value.length >= 10 ? value.slice(0, 10) : value;
  }

  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return value.toISOString().slice(0, 10);
  }

  if (
    Array.isArray(value) &&
    value.length >= 3 &&
    typeof value[0] === 'number' &&
    typeof value[1] === 'number' &&
    typeof value[2] === 'number'
  ) {
    const [year, month, day] = value as [number, number, number];
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }

  return '';
}

export type EmployeePayrollStatus = 'CURRENT' | 'ENDED' | 'SCHEDULED';

export interface EmployeeCurrentPayroll {
  employeeId: number;
  annualSalary: number;
  monthlySalary: number;
  startDate: string;
  status: Extract<EmployeePayrollStatus, 'CURRENT'>;
}

export interface EmployeePayrollHistoryItem {
  employeeId: number;
  annualSalary: number;
  monthlySalary: number;
  startDate: string;
  endDate: string | null;
  status: EmployeePayrollStatus;
}

export interface ChangeEmployeeSalaryPayload {
  annualSalary: number;
  startDate: string;
}

export function mapEmployeeCurrentPayroll(input: any): EmployeeCurrentPayroll {
  return {
    employeeId: Number(input?.employeeId ?? 0),
    annualSalary: Number(input?.annualSalary ?? 0),
    monthlySalary: Number(input?.monthlySalary ?? 0),
    startDate: toIsoDateString(input?.startDate),
    status: 'CURRENT',
  };
}

export function mapEmployeePayrollHistoryItem(input: any): EmployeePayrollHistoryItem {
  const rawStatus = String(input?.status ?? 'ENDED').toUpperCase();
  const status: EmployeePayrollStatus =
    rawStatus === 'CURRENT'
      ? 'CURRENT'
      : rawStatus === 'SCHEDULED'
        ? 'SCHEDULED'
        : 'ENDED';

  return {
    employeeId: Number(input?.employeeId ?? 0),
    annualSalary: Number(input?.annualSalary ?? 0),
    monthlySalary: Number(input?.monthlySalary ?? 0),
    startDate: toIsoDateString(input?.startDate),
    endDate: input?.endDate ? toIsoDateString(input.endDate) : null,
    status,
  };
}
