import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';
import PageResponse from '@/core/common/PageResponse';
import type { PartyListItem } from '@/features/party/models/partyListItem';
import { mapPartyListItem } from '@/features/party/models/partyListItem';

@singleton()
export default class PartyRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  /**
   * 협력사 목록 조회 (페이징)
   */
  async list(params: { page: number; size: number; name?: string }): Promise<PageResponse<PartyListItem>> {
    const queryParams: Record<string, string> = {
      page: Math.max(params.page - 1, 0).toString(),
      size: params.size.toString(),
    };

    if (params.name) {
      queryParams.name = params.name;
    }

    const response = await this.httpRepository.get({
      path: '/api/parties',
      params: queryParams,
    });

    return PageResponse.fromPage(response, mapPartyListItem);
  }
}
