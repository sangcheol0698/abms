import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PageResponse } from '@/core/api';
import type HttpRepository from '@/core/http/HttpRepository';
import PartyRepository from '@/features/party/repository/PartyRepository';

describe('PartyRepository', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let httpPost: ReturnType<typeof vi.fn>;
  let httpPut: ReturnType<typeof vi.fn>;
  let httpDelete: ReturnType<typeof vi.fn>;
  let repository: PartyRepository;

  beforeEach(() => {
    httpGet = vi.fn();
    httpPost = vi.fn();
    httpPut = vi.fn();
    httpDelete = vi.fn();
    repository = new PartyRepository({
      get: httpGet,
      post: httpPost,
      put: httpPut,
      delete: httpDelete,
    } as unknown as HttpRepository);
  });

  it('목록 조회 파라미터를 API 형식으로 직렬화한다', async () => {
    httpGet.mockResolvedValueOnce({
      content: [],
      number: 0,
      size: 10,
      totalPages: 0,
      totalElements: 0,
    });

    await repository.list({
      page: 2,
      size: 10,
      name: '협력사',
      sort: 'name,asc',
    });

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/parties',
      params: {
        page: '1',
        size: '10',
        name: '협력사',
        sort: 'name,asc',
      },
    });
  });

  it('목록 응답을 PageResponse로 매핑한다', async () => {
    httpGet.mockResolvedValueOnce({
      content: [
        {
          partyId: 1,
          name: '협력사A',
          ceo: '대표',
          manager: '담당자',
          contact: '010-1234-1234',
          email: 'party@abacus.co.kr',
        },
      ],
      number: 0,
      size: 5,
      totalPages: 2,
      totalElements: 6,
    });

    const result = await repository.list({ page: 1, size: 5 });

    expect(result).toBeInstanceOf(PageResponse);
    expect(result.content[0]).toEqual({
      partyId: 1,
      name: '협력사A',
      ceo: '대표',
      manager: '담당자',
      contact: '010-1234-1234',
      email: 'party@abacus.co.kr',
    });
  });

  it('상세 응답을 모델로 매핑한다', async () => {
    httpGet.mockResolvedValueOnce({
      partyId: 7,
      name: '상세 협력사',
      ceo: '대표자',
      manager: '담당자',
      contact: '010-0000-0000',
      email: 'detail@abacus.co.kr',
    });

    await expect(repository.find(7)).resolves.toEqual({
      partyId: 7,
      name: '상세 협력사',
      ceo: '대표자',
      manager: '담당자',
      contact: '010-0000-0000',
      email: 'detail@abacus.co.kr',
    });
  });

  it('요약 응답 숫자를 정수로 보정한다', async () => {
    httpGet.mockResolvedValueOnce({
      totalCount: '4',
      withProjectsCount: '3',
      withInProgressProjectsCount: '2',
      withoutProjectsCount: '1',
      totalContractAmount: '1500000',
    });

    await expect(repository.fetchOverviewSummary({ name: '협력사' })).resolves.toEqual({
      totalCount: 4,
      withProjectsCount: 3,
      withInProgressProjectsCount: 2,
      withoutProjectsCount: 1,
      totalContractAmount: 1500000,
    });
  });

  it('생성 payload를 전송하고 응답을 상세 모델로 매핑한다', async () => {
    httpPost.mockResolvedValueOnce({
      partyId: 11,
      name: '신규 협력사',
      ceo: '대표',
      manager: '영업',
      contact: '010-7777-7777',
      email: 'new@abacus.co.kr',
    });

    const result = await repository.create({
      name: '신규 협력사',
      ceoName: '대표',
      salesRepName: '영업',
      salesRepPhone: '010-7777-7777',
      salesRepEmail: 'new@abacus.co.kr',
    });

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/parties',
      data: {
        name: '신규 협력사',
        ceoName: '대표',
        salesRepName: '영업',
        salesRepPhone: '010-7777-7777',
        salesRepEmail: 'new@abacus.co.kr',
      },
    });
    expect(result.partyId).toBe(11);
  });

  it('수정 payload를 전송한다', async () => {
    httpPut.mockResolvedValueOnce({
      partyId: 4,
      name: '수정 협력사',
      ceo: null,
      manager: null,
      contact: null,
      email: null,
    });

    await repository.update(4, {
      name: '수정 협력사',
      ceoName: null,
      salesRepName: null,
      salesRepPhone: null,
      salesRepEmail: null,
    });

    expect(httpPut).toHaveBeenCalledWith({
      path: '/api/parties/4',
      data: {
        name: '수정 협력사',
        ceoName: null,
        salesRepName: null,
        salesRepPhone: null,
        salesRepEmail: null,
      },
    });
  });

  it('삭제 API를 호출한다', async () => {
    await repository.delete(8);

    expect(httpDelete).toHaveBeenCalledWith({
      path: '/api/parties/8',
    });
  });

  it('협력사 프로젝트 조회 응답이 배열이 아니면 빈 배열을 반환한다', async () => {
    httpGet.mockResolvedValueOnce(null);
    await expect(repository.fetchProjects(3)).resolves.toEqual([]);
  });

  it('협력사 프로젝트 응답을 프로젝트 목록 모델로 매핑한다', async () => {
    httpGet.mockResolvedValueOnce([
      {
        projectId: 30,
        partyId: 3,
        partyName: '협력사A',
        code: 'P-030',
        name: '연계 프로젝트',
        description: null,
        status: 'IN_PROGRESS',
        statusDescription: '진행 중',
        contractAmount: 5000,
        startDate: '2024-02-01',
        endDate: null,
      },
    ]);

    await expect(repository.fetchProjects(3)).resolves.toEqual([
      {
        projectId: 30,
        partyId: 3,
        partyName: '협력사A',
        leadDepartmentId: null,
        leadDepartmentName: null,
        code: 'P-030',
        name: '연계 프로젝트',
        description: null,
        status: 'IN_PROGRESS',
        statusLabel: '진행 중',
        contractAmount: 5000,
        startDate: '2024-02-01',
        endDate: null,
      },
    ]);
  });
});
