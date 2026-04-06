export interface WeeklyReportSnapshotSummary {
  totalEmployees: number;
  onLeaveEmployees: number;
  inProgressProjects: number;
  startedProjects: number;
  endedProjects: number;
  monthlyRevenueAvailable: boolean;
}

export interface WeeklyReportDraftResponse {
  id: number;
  title: string;
  weekStart: string;
  weekEnd: string;
  status: string;
  statusDescription: string;
  reportMarkdown: string;
  snapshotSummary: WeeklyReportSnapshotSummary;
  snapshotJson?: string;
  failureReason?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WeeklyReportDraftSummary {
  id: number;
  title: string;
  weekStart: string;
  weekEnd: string;
  status: string;
  statusDescription: string;
  reportMarkdown: string;
  snapshotSummary: WeeklyReportSnapshotSummary;
  failureReason: string | null;
  createdAt: Date;
  updatedAt: Date;
}

export interface WeeklyReportDraftDetail extends WeeklyReportDraftSummary {
  snapshotJson: string;
}

export interface WeeklyReportGenerateRequest {
  weekStart?: string;
  weekEnd?: string;
}

export function normalizeWeeklyReportDraftSummary(
  response: WeeklyReportDraftResponse,
): WeeklyReportDraftSummary {
  return {
    id: response.id,
    title: response.title,
    weekStart: response.weekStart,
    weekEnd: response.weekEnd,
    status: response.status,
    statusDescription: response.statusDescription,
    reportMarkdown: response.reportMarkdown,
    snapshotSummary: response.snapshotSummary,
    failureReason: response.failureReason ?? null,
    createdAt: new Date(response.createdAt),
    updatedAt: new Date(response.updatedAt),
  };
}

export function isWeeklyReportTerminalStatus(status: string): boolean {
  return status === 'DRAFT' || status === 'FAILED' || status === 'CANCELLED';
}

export function normalizeWeeklyReportDraftDetail(
  response: WeeklyReportDraftResponse,
): WeeklyReportDraftDetail {
  return {
    ...normalizeWeeklyReportDraftSummary(response),
    snapshotJson: response.snapshotJson ?? '',
  };
}
