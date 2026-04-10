export interface DashboardSummary {
  totalEmployeesCount: number;
  activeProjectsCount: number;
  completedProjectsCount: number;
  newEmployeesCount: number;
  yearRevenue: number;
  yearProfit: number;
}

export interface DashboardUpcomingDeadlineItem {
  projectId: number;
  name: string;
  partyName: string;
  status: string;
  statusDescription: string;
  endDate: string;
  daysLeft: number;
}

export interface DashboardDepartmentFinancialItem {
  departmentId: number;
  departmentName: string;
  revenue: number;
  profit: number;
  profitMargin: number;
}

export interface DashboardMonthlyFinancialItem {
  targetMonth: string;
  revenue: number;
  cost: number;
  profit: number;
}

export interface DashboardProjectOverview {
  totalCount: number;
  scheduledCount: number;
  inProgressCount: number;
  completedCount: number;
  onHoldCount: number;
  cancelledCount: number;
}

export interface DashboardEmployeeOverview {
  totalCount: number;
  fullTimeCount: number;
  freelancerCount: number;
  outsourcingCount: number;
  partTimeCount: number;
}
