import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PageResponse } from '@/core/api';
import type HttpRepository from '@/core/http/HttpRepository';
import ProjectRepository from '@/features/project/repository/ProjectRepository';

const excelMocks = vi.hoisted(() => ({
  downloadBlobMock: vi.fn(),
  extractFilenameFromResponseMock: vi.fn(() => 'projects.xlsx'),
  generateExcelFilenameMock: vi.fn((prefix: string) => `${prefix}.xlsx`),
}));

vi.mock('@/core/utils/excel', () => ({
  downloadBlob: excelMocks.downloadBlobMock,
  extractFilenameFromResponse: excelMocks.extractFilenameFromResponseMock,
  generateExcelFilename: excelMocks.generateExcelFilenameMock,
}));

describe('ProjectRepository', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let httpPost: ReturnType<typeof vi.fn>;
  let httpPut: ReturnType<typeof vi.fn>;
  let httpDelete: ReturnType<typeof vi.fn>;
  let httpPatch: ReturnType<typeof vi.fn>;
  let httpDownload: ReturnType<typeof vi.fn>;
  let httpUpload: ReturnType<typeof vi.fn>;
  let repository: ProjectRepository;

  beforeEach(() => {
    httpGet = vi.fn();
    httpPost = vi.fn();
    httpPut = vi.fn();
    httpDelete = vi.fn();
    httpPatch = vi.fn();
    httpDownload = vi.fn();
    httpUpload = vi.fn();
    repository = new ProjectRepository({
      get: httpGet,
      post: httpPost,
      put: httpPut,
      delete: httpDelete,
      patch: httpPatch,
      download: httpDownload,
      upload: httpUpload,
    } as unknown as HttpRepository);
    vi.clearAllMocks();
  });

  it('검색 파라미터를 API 계약에 맞게 변환한다', async () => {
    httpGet.mockResolvedValueOnce({
      content: [],
      number: 0,
      size: 20,
      totalPages: 0,
      totalElements: 0,
    });

    await repository.search({
      page: 2,
      size: 20,
      name: 'ABMS',
      statuses: ['IN_PROGRESS', 'COMPLETED'],
      partyIds: [10, 20],
      periodStart: '2024-01-01',
      periodEnd: '2024-12-31',
      sort: 'periodStart,desc',
    });

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/projects',
      params: {
        page: '1',
        size: '20',
        name: 'ABMS',
        statuses: 'IN_PROGRESS,COMPLETED',
        partyIds: '10,20',
        periodStart: '2024-01-01',
        periodEnd: '2024-12-31',
        sort: 'periodStart,desc',
      },
    });
  });

  it('검색 응답을 PageResponse로 매핑한다', async () => {
    httpGet.mockResolvedValueOnce({
      content: [
        {
          projectId: 3,
          partyId: 9,
          partyName: '협력사A',
          leadDepartmentId: 12,
          leadDepartmentName: '개발팀',
          code: 'P-003',
          name: '프로젝트',
          description: '설명',
          status: 'IN_PROGRESS',
          statusDescription: '진행 중',
          contractAmount: 12000000,
          startDate: '2024-01-01',
          endDate: '2024-12-31',
        },
      ],
      number: 1,
      size: 10,
      totalPages: 3,
      totalElements: 21,
    });

    const result = await repository.search({ page: 1, size: 10 });

    expect(result).toBeInstanceOf(PageResponse);
    expect(result.page).toBe(2);
    expect(result.content[0]).toMatchObject({
      projectId: 3,
      partyId: 9,
      partyName: '협력사A',
      leadDepartmentId: 12,
      leadDepartmentName: '개발팀',
      statusLabel: '진행 중',
      contractAmount: 12000000,
    });
  });

  it('요약 응답 숫자 필드를 정수로 보정한다', async () => {
    httpGet.mockResolvedValueOnce({
      totalCount: '2',
      scheduledCount: '1',
      inProgressCount: '3',
      completedCount: '4',
      onHoldCount: null,
      cancelledCount: undefined,
      totalContractAmount: '55000',
    });

    const result = await repository.fetchOverviewSummary({ name: 'ABMS' });

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/projects/summary',
      params: { name: 'ABMS' },
    });
    expect(result).toEqual({
      totalCount: 2,
      scheduledCount: 1,
      inProgressCount: 3,
      completedCount: 4,
      onHoldCount: 0,
      cancelledCount: 0,
      totalContractAmount: 55000,
    });
  });

  it('상세 조회 응답을 모델로 매핑한다', async () => {
    httpGet.mockResolvedValueOnce({
      projectId: 7,
      partyId: 2,
      partyName: '협력사B',
      code: 'P-007',
      name: '상세 프로젝트',
      description: null,
      status: 'ON_HOLD',
      statusDescription: '보류',
      contractAmount: 0,
      startDate: '2024-03-01',
      endDate: null,
      leadDepartmentId: 11,
      leadDepartmentName: '개발팀',
    });

    const result = await repository.find(7);

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/projects/7',
    });
    expect(result).toMatchObject({
      projectId: 7,
      partyId: 2,
      statusLabel: '보류',
      leadDepartmentId: 11,
      leadDepartmentName: '개발팀',
    });
  });

  it('생성 요청 payload를 전송하고 생성 id를 반환한다', async () => {
    httpPost.mockResolvedValueOnce({ projectId: '15' });

    const result = await repository.create({
      partyId: 3,
      leadDepartmentId: 8,
      code: 'P-015',
      name: '신규 프로젝트',
      description: '',
      status: 'SCHEDULED',
      contractAmount: 3000000,
      startDate: '2024-04-01',
      endDate: null,
    });

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/projects',
      data: {
        partyId: 3,
        leadDepartmentId: 8,
        code: 'P-015',
        name: '신규 프로젝트',
        description: null,
        status: 'SCHEDULED',
        contractAmount: 3000000,
        startDate: '2024-04-01',
        endDate: null,
      },
    });
    expect(result).toEqual({ projectId: 15 });
  });

  it('수정 요청 payload를 전송하고 수정 id를 반환한다', async () => {
    httpPut.mockResolvedValueOnce({ projectId: 22 });

    const result = await repository.update(22, {
      partyId: 9,
      leadDepartmentId: null,
      name: '수정 프로젝트',
      description: '메모',
      status: 'IN_PROGRESS',
      contractAmount: 9900000,
      startDate: '2024-05-01',
      endDate: '2024-09-30',
    });

    expect(httpPut).toHaveBeenCalledWith({
      path: '/api/projects/22',
      data: {
        partyId: 9,
        leadDepartmentId: null,
        name: '수정 프로젝트',
        description: '메모',
        status: 'IN_PROGRESS',
        contractAmount: 9900000,
        startDate: '2024-05-01',
        endDate: '2024-09-30',
      },
    });
    expect(result).toEqual({ projectId: 22 });
  });

  it('상태 목록을 value/label 구조로 변환한다', async () => {
    httpGet.mockResolvedValueOnce([
      { name: 'IN_PROGRESS', description: '진행 중' },
      { name: 'COMPLETED', description: '완료' },
    ]);

    await expect(repository.fetchStatuses()).resolves.toEqual([
      { value: 'IN_PROGRESS', label: '진행 중' },
      { value: 'COMPLETED', label: '완료' },
    ]);
  });

  it('다운로드 성공 시 blob과 파일명을 전달한다', async () => {
    const blob = new Blob(['excel']);
    httpDownload.mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: { get: vi.fn(() => 'attachment; filename=projects.xlsx') },
      blob: vi.fn().mockResolvedValue(blob),
    });

    await repository.downloadExcel({ page: 1, size: 10, statuses: ['IN_PROGRESS'] });

    expect(httpDownload).toHaveBeenCalledWith({
      path: '/api/projects/excel/download',
      params: {
        page: '0',
        size: '10',
        statuses: 'IN_PROGRESS',
      },
    });
    expect(excelMocks.downloadBlobMock).toHaveBeenCalledWith(blob, 'projects.xlsx');
  });

  it('다운로드 실패 시 예외를 던진다', async () => {
    httpDownload.mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    await expect(repository.downloadExcel({ page: 1, size: 10 })).rejects.toThrow(
      '엑셀 다운로드에 실패했습니다. (500)',
    );
  });

  it('샘플 다운로드 성공 시 fallback 파일명을 생성한다', async () => {
    const blob = new Blob(['sample']);
    excelMocks.extractFilenameFromResponseMock.mockReturnValueOnce('projects_sample.xlsx');
    httpDownload.mockResolvedValueOnce({
      ok: true,
      status: 200,
      headers: { get: vi.fn(() => null) },
      blob: vi.fn().mockResolvedValue(blob),
    });

    await repository.downloadExcelSample();

    expect(excelMocks.generateExcelFilenameMock).toHaveBeenCalledWith('projects_sample');
    expect(excelMocks.downloadBlobMock).toHaveBeenCalledWith(blob, 'projects_sample.xlsx');
  });

  it('업로드 시 FormData와 progress 콜백을 전달한다', async () => {
    const file = new File(['data'], 'projects.xlsx');
    const onProgress = vi.fn();

    await repository.uploadExcel(file, onProgress);

    expect(httpUpload).toHaveBeenCalledWith(
      expect.objectContaining({
        path: '/api/projects/excel/upload',
        onProgress,
      }),
    );
    const formData = httpUpload.mock.calls[0][0].data as FormData;
    expect(formData.get('file')).toBe(file);
  });

  it('삭제, 완료, 취소 API를 호출한다', async () => {
    await repository.delete(5);
    await repository.complete(5);
    await repository.cancel(5);

    expect(httpDelete).toHaveBeenCalledWith({ path: '/api/projects/5' });
    expect(httpPatch).toHaveBeenNthCalledWith(1, { path: '/api/projects/5/complete' });
    expect(httpPatch).toHaveBeenNthCalledWith(2, { path: '/api/projects/5/cancel' });
  });
});
