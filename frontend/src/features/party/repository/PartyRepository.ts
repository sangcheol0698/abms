import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import { PageResponse } from '@/core/api';
import type { PartyListItem, PartySearchParams } from '@/features/party/models/partyListItem';
import { mapPartyListItem } from '@/features/party/models/partyListItem';
import type { ProjectListItem } from '@/features/project/models/projectListItem';
import { mapProjectListItem } from '@/features/project/models/projectListItem';
import type {
  PartyDetail,
  PartyCreateData,
  PartyUpdateData,
} from '@/features/party/models/partyDetail';
import { mapPartyDetail } from '@/features/party/models/partyDetail';

@singleton()
export default class PartyRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  /**
   * 협력사 목록 조회 (페이징)
   */
  async list(params: PartySearchParams): Promise<PageResponse<PartyListItem>> {
    const queryParams: Record<string, string> = {
      page: Math.max(params.page - 1, 0).toString(),
      size: params.size.toString(),
    };

    if (params.name) {
      queryParams.name = params.name;
    }

    if (params.sort) {
      queryParams.sort = params.sort;
    }

    const response = await this.httpRepository.get({
      path: '/api/parties',
      params: queryParams,
    });

    return PageResponse.fromPage(response, mapPartyListItem);
  }

  /**
   * 협력사 상세 조회
   */
  async find(partyId: number): Promise<PartyDetail> {
    const response = await this.httpRepository.get({
      path: `/api/parties/${partyId}`,
    });

    return mapPartyDetail(response);
  }

  /**
   * 협력사 생성
   */
  async create(data: PartyCreateData): Promise<PartyDetail> {
    const response = await this.httpRepository.post({
      path: '/api/parties',
      data: {
        name: data.name,
        ceoName: data.ceoName,
        salesRepName: data.salesRepName,
        salesRepPhone: data.salesRepPhone,
        salesRepEmail: data.salesRepEmail,
      },
    });

    return mapPartyDetail(response);
  }

  /**
   * 협력사 수정
   */
  async update(partyId: number, data: PartyUpdateData): Promise<PartyDetail> {
    const response = await this.httpRepository.put({
      path: `/api/parties/${partyId}`,
      data: {
        name: data.name,
        ceoName: data.ceoName,
        salesRepName: data.salesRepName,
        salesRepPhone: data.salesRepPhone,
        salesRepEmail: data.salesRepEmail,
      },
    });

    return mapPartyDetail(response);
  }

  /**
   * 협력사 삭제
   */
  async delete(partyId: number): Promise<void> {
    await this.httpRepository.delete({
      path: `/api/parties/${partyId}`,
    });
  }

  /**
   * 전체 협력사 옵션 목록 조회
   */
  async fetchAll(): Promise<{ label: string; value: number }[]> {
    const response = await this.list({ page: 1, size: 1000 });
    return response.content.map((party) => ({
      label: party.name,
      value: party.partyId,
    }));
  }

  /**
   * 협력사 관련 프로젝트 조회
   */
  async fetchProjects(partyId: number): Promise<ProjectListItem[]> {
    const response = await this.httpRepository.get({
      path: `/api/parties/${partyId}/projects`,
    });

    if (!Array.isArray(response)) {
      return [];
    }

    return response.map(mapProjectListItem);
  }
}
