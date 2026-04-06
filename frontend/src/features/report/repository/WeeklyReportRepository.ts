import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import type {
  WeeklyReportDraftDetail,
  WeeklyReportDraftResponse,
  WeeklyReportDraftSummary,
  WeeklyReportGenerateRequest,
} from '@/features/report/models/weeklyReport';
import {
  normalizeWeeklyReportDraftDetail,
  normalizeWeeklyReportDraftSummary,
} from '@/features/report/models/weeklyReport';

@singleton()
export default class WeeklyReportRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async createDraft(payload: WeeklyReportGenerateRequest): Promise<WeeklyReportDraftDetail> {
    const response = await this.httpRepository.post<WeeklyReportDraftResponse>({
      path: '/api/reports/weekly/drafts',
      data: payload,
    });
    return normalizeWeeklyReportDraftDetail(response);
  }

  async listDrafts(): Promise<WeeklyReportDraftSummary[]> {
    const response = await this.httpRepository.get<WeeklyReportDraftResponse[]>({
      path: '/api/reports/weekly/drafts',
    });
    return response.map(normalizeWeeklyReportDraftSummary);
  }

  async getDraftDetail(draftId: number): Promise<WeeklyReportDraftDetail> {
    const response = await this.httpRepository.get<WeeklyReportDraftResponse>({
      path: `/api/reports/weekly/drafts/${draftId}`,
    });
    return normalizeWeeklyReportDraftDetail(response);
  }

  async regenerateDraft(draftId: number): Promise<WeeklyReportDraftDetail> {
    const response = await this.httpRepository.post<WeeklyReportDraftResponse>({
      path: `/api/reports/weekly/drafts/${draftId}/regenerate`,
    });
    return normalizeWeeklyReportDraftDetail(response);
  }

  async cancelDraft(draftId: number): Promise<WeeklyReportDraftDetail> {
    const response = await this.httpRepository.post<WeeklyReportDraftResponse>({
      path: `/api/reports/weekly/drafts/${draftId}/cancel`,
    });
    return normalizeWeeklyReportDraftDetail(response);
  }
}
