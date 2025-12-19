import 'reflect-metadata';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { PageResponse } from '@/core/api';
import type HttpRepository from '@/core/http/HttpRepository';
import { EmployeeRepository } from '../EmployeeRepository';
import type { EmployeeSearchParams } from '@/features/employee/models/employeeListItem';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import {
  resetEmployeeFilterOptions,
  setEmployeeGradeOptions,
  setEmployeePositionOptions,
  setEmployeeStatusOptions,
  setEmployeeTypeOptions,
} from '@/features/employee/models/employeeFilters';

const DEFAULT_STATUS_OPTIONS: EmployeeFilterOption[] = [
  { value: 'ACTIVE', label: '재직' },
  { value: 'ON_LEAVE', label: '휴직' },
  { value: 'RESIGNED', label: '퇴사' },
];

const DEFAULT_TYPE_OPTIONS: EmployeeFilterOption[] = [
  { value: 'FULL_TIME', label: '정직원' },
  { value: 'PART_TIME', label: '반프리' },
  { value: 'OUTSOURCING', label: '외주' },
];

const DEFAULT_GRADE_OPTIONS: EmployeeFilterOption[] = [
  { value: 'JUNIOR', label: '초급' },
  { value: 'MID_LEVEL', label: '중급' },
  { value: 'SENIOR', label: '고급' },
];

const DEFAULT_POSITION_OPTIONS: EmployeeFilterOption[] = [
  { value: 'ASSOCIATE', label: '사원' },
  { value: 'LEADER', label: '책임' },
  { value: 'MANAGER', label: '팀장' },
];

function primeFilterOptions() {
  setEmployeeStatusOptions(DEFAULT_STATUS_OPTIONS);
  setEmployeeTypeOptions(DEFAULT_TYPE_OPTIONS);
  setEmployeeGradeOptions(DEFAULT_GRADE_OPTIONS);
  setEmployeePositionOptions(DEFAULT_POSITION_OPTIONS);
}

describe('EmployeeRepository.search', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let repository: EmployeeRepository;

  beforeEach(() => {
    resetEmployeeFilterOptions();
    httpGet = vi.fn();
    const httpRepositoryStub = {
      get: httpGet,
    } as unknown as HttpRepository;

    repository = new EmployeeRepository(httpRepositoryStub);
  });

  it('요청 파라미터를 API 계약에 맞게 변환한다', async () => {
    primeFilterOptions();
    httpGet.mockResolvedValueOnce({
      content: [],
      number: 0,
      size: 15,
      totalPages: 0,
      totalElements: 0,
    });

    const params: EmployeeSearchParams = {
      page: 2,
      size: 15,
      name: '홍길동',
      statuses: ['ACTIVE', 'ON_LEAVE'],
      types: ['FULL_TIME', 'PART_TIME'],
      grades: ['JUNIOR'],
      positions: ['ASSOCIATE', 'MANAGER'],
      departmentIds: ['D01', 'D02'],
      sort: 'name,asc',
    };

    await repository.search(params);

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/employees',
      params: {
        page: '1',
        size: '15',
        name: '홍길동',
        statuses: 'ACTIVE,ON_LEAVE',
        types: 'FULL_TIME,PART_TIME',
        grades: 'JUNIOR',
        positions: 'ASSOCIATE,MANAGER',
        departmentIds: 'D01,D02',
        sort: 'name,asc',
      },
    });
  });

  it('응답을 PageResponse<EmployeeListItem>으로 변환한다', async () => {
    primeFilterOptions();
    const apiResponse = {
      content: [
        {
          employeeId: 'E-01',
          departmentId: 'DEV',
          departmentName: '개발팀',
          name: '홍길동',
          email: 'hong@example.com',
          position: 'ASSOCIATE',
          status: 'ACTIVE',
          grade: 'JUNIOR',
          type: 'FULL_TIME',
          avatar: 'SKY_GLOW',
          memo: '메모',
          joinDate: '2024-01-10',
          birthDate: '1990-05-15',
        },
      ],
      number: 0,
      size: 10,
      totalPages: 2,
      totalElements: 12,
    };

    httpGet.mockResolvedValueOnce(apiResponse);

    const result = await repository.search({ page: 1, size: 10 });

    expect(result).toBeInstanceOf(PageResponse);
    expect(result.page).toBe(1);
    expect(result.size).toBe(10);
    expect(result.totalPages).toBe(2);
    expect(result.totalElements).toBe(12);
    expect(result.content).toHaveLength(1);

    const item = result.content[0];
    expect(item).toBeDefined();
    if (!item) {
      throw new Error('변환된 직원 항목이 존재해야 합니다.');
    }

    expect(item.employeeId).toBe('E-01');
    expect(item.departmentId).toBe('DEV');
    expect(item.departmentName).toBe('개발팀');
    expect(item.name).toBe('홍길동');
    expect(item.statusCode).toBe('ACTIVE');
    expect(item.typeCode).toBe('FULL_TIME');
    expect(item.gradeCode).toBe('JUNIOR');
    expect(item.positionCode).toBe('ASSOCIATE');
    expect(item.joinDate).toBe('2024-01-10');
    expect(item.birthDate).toBe('1990-05-15');
    expect(item.avatarCode).toBe('SKY_GLOW');
    expect(item.avatarLabel).toBe('Sky Glow');
    expect(item.avatarImageUrl).toMatch(/(data:image\/svg\+xml|\.svg$)/);
  });

  it('직원 정보를 업데이트한다', async () => {
    primeFilterOptions();
    httpGet.mockResolvedValueOnce(null);
    const httpPut = vi.fn().mockResolvedValue({
      employeeId: 'E-01',
      departmentId: 'DEV',
      departmentName: '개발팀',
      name: '홍길동',
      email: 'hong@example.com',
      position: 'ASSOCIATE',
      status: 'ACTIVE',
      grade: 'JUNIOR',
      type: 'FULL_TIME',
      avatar: 'SKY_GLOW',
      memo: '메모',
    });

    const repository = new EmployeeRepository({
      get: httpGet,
      put: httpPut,
    } as unknown as HttpRepository);

    const result = await repository.update('E-01', {
      departmentId: 'DEV',
      name: '홍길동',
      email: 'hong@example.com',
      joinDate: '2024-01-01',
      birthDate: '1990-05-10',
      position: '사원',
      grade: '초급',
      type: '정직원',
      avatar: 'SKY_GLOW',
      memo: '메모',
    });

    expect(httpPut).toHaveBeenCalledWith({
      path: '/api/employees/E-01',
      data: {
        departmentId: 'DEV',
        name: '홍길동',
        email: 'hong@example.com',
        joinDate: '2024-01-01',
        birthDate: '1990-05-10',
        position: '사원',
        grade: '초급',
        type: '정직원',
        avatar: 'SKY_GLOW',
        memo: '메모',
      },
    });
    expect(result.employeeId).toBe('E-01');
    expect(result.name).toBe('홍길동');
    expect(result.avatarCode).toBe('SKY_GLOW');
  });
});

describe('EmployeeRepository.fetch filter options', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let repository: EmployeeRepository;

  beforeEach(() => {
    resetEmployeeFilterOptions();
    httpGet = vi.fn();
    const httpRepositoryStub = {
      get: httpGet,
    } as unknown as HttpRepository;
    repository = new EmployeeRepository(httpRepositoryStub);
  });

  it('상태 옵션을 API 응답으로 변환한다', async () => {
    httpGet.mockResolvedValueOnce([
      { name: 'ACTIVE', description: '재직' },
      { name: 'RESIGNED', description: '퇴사' },
    ]);

    const result = await repository.fetchStatuses();

    expect(result).toEqual<EmployeeFilterOption[]>([
      { value: 'ACTIVE', label: '재직' },
      { value: 'RESIGNED', label: '퇴사' },
    ]);
  });

  it('등급 옵션을 level 순으로 정렬한다', async () => {
    httpGet.mockResolvedValueOnce([
      { name: 'SENIOR', description: '고급', level: 3 },
      { name: 'JUNIOR', description: '초급', level: 1 },
      { name: 'MID', description: '중급', level: 2 },
    ]);

    const result = await repository.fetchGrades();

    expect(result).toEqual<EmployeeFilterOption[]>([
      { value: 'JUNIOR', label: '초급' },
      { value: 'MID', label: '중급' },
      { value: 'SENIOR', label: '고급' },
    ]);
  });

  it('직책 옵션을 rank 순으로 정렬한다', async () => {
    httpGet.mockResolvedValueOnce([
      { name: 'DIRECTOR', description: '이사', rank: 4 },
      { name: 'STAFF', description: '선임', rank: 2 },
      { name: 'ASSOCIATE', description: '사원', rank: 1 },
    ]);

    const result = await repository.fetchPositions();

    expect(result).toEqual<EmployeeFilterOption[]>([
      { value: 'ASSOCIATE', label: '사원' },
      { value: 'STAFF', label: '선임' },
      { value: 'DIRECTOR', label: '이사' },
    ]);
  });

  it('아바타 옵션을 API 응답과 정적 프리셋으로 병합한다', async () => {
    httpGet.mockResolvedValueOnce([
      { code: 'AQUA_SPLASH', displayName: 'Aqua Splash Custom' },
      { code: 'SKY_GLOW', displayName: 'Sky Glow Custom' },
      { code: 'UNKNOWN', displayName: '미정' },
    ]);

    const result = await repository.fetchAvatars();

    expect(httpGet).toHaveBeenCalledWith({ path: '/api/employees/avatars' });
    const aqua = result.find((option) => option.code === 'AQUA_SPLASH');
    const skyGlow = result.find((option) => option.code === 'SKY_GLOW');
    const unknown = result.find((option) => (option.code as string) === 'UNKNOWN');

    expect(aqua).toBeDefined();
    expect(aqua?.label).toBe('Aqua Splash Custom');
    expect(skyGlow?.label).toBe('Sky Glow Custom');
    expect(unknown).toBeUndefined();
    expect(result.length).toBeGreaterThanOrEqual(12);
  });
});
