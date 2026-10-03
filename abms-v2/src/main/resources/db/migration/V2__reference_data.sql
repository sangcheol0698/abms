-- 기준 데이터: 권한, 시스템 권한 그룹, 원가 정책

INSERT INTO `tb_permission` (`id`, `code`, `name`, `description`, `created_at`, `updated_at`)
VALUES (1, 'dashboard.read', '대시보드/손익 조회', '대시보드와 월별 손익 집계를 조회한다.', NOW(6), NOW(6)),
       (2, 'employee.read', '직원 상세 조회', '직원 상세 정보(급여, 이력 포함)를 조회한다.', NOW(6), NOW(6)),
       (3, 'employee.write', '직원 생성 및 변경', '직원 생성, 정보 수정, 상태 변경, 삭제를 한다.', NOW(6), NOW(6)),
       (4, 'employee.excel.download', '직원 목록 내보내기', '직원 목록을 CSV(엑셀) 파일로 내려받는다.', NOW(6), NOW(6)),
       (5, 'department.write', '부서 관리', '부서 생성, 수정, 부서장 지정, 삭제를 한다.', NOW(6), NOW(6)),
       (6, 'party.read', '협력사 조회', '협력사 목록과 상세 정보를 조회한다.', NOW(6), NOW(6)),
       (7, 'party.write', '협력사 관리', '협력사 생성, 수정, 삭제를 한다.', NOW(6), NOW(6)),
       (8, 'project.read', '프로젝트 조회', '프로젝트 목록, 상세, 매출 계획, 투입 인력을 조회한다.', NOW(6), NOW(6)),
       (9, 'project.write', '프로젝트 관리', '프로젝트와 매출 계획, 투입 인력을 생성/수정/삭제한다.', NOW(6), NOW(6)),
       (10, 'project.excel.download', '프로젝트 목록 내보내기', '프로젝트 목록을 CSV(엑셀) 파일로 내려받는다.', NOW(6), NOW(6)),
       (11, 'summary.manage', '손익 집계 관리', '월 손익 재집계, 월 마감, 원가 정책을 관리한다.', NOW(6), NOW(6)),
       (12, 'report.read', '주간 보고서', '주간 운영 보고서를 생성하고 조회한다.', NOW(6), NOW(6)),
       (13, 'account.manage', '계정 관리', '직원 계정 발급, 비밀번호 초기화, 활성/비활성을 관리한다.', NOW(6), NOW(6)),
       (14, 'permission.group.manage', '권한 그룹 관리', '권한 그룹과 그룹별 계정 할당을 관리한다.', NOW(6), NOW(6));

INSERT INTO `tb_permission_group` (`id`, `name`, `description`, `group_type`, `created_at`, `updated_at`)
VALUES (1, '일반 사용자', '신규 계정에 기본으로 부여되는 그룹. 본인 정보와 참여 프로젝트를 조회할 수 있다.', 'SYSTEM', NOW(6), NOW(6)),
       (2, '최고 관리자', '시스템의 모든 기능을 사용할 수 있는 그룹.', 'SYSTEM', NOW(6), NOW(6));

-- 일반 사용자: 본인 상세/본인 정보 수정, 참여 프로젝트 조회, 협력사 조회
INSERT INTO `tb_group_permission_grant` (`permission_group_id`, `permission_id`, `scope`, `created_at`, `updated_at`)
VALUES (1, 2, 'SELF', NOW(6), NOW(6)),
       (1, 3, 'SELF', NOW(6), NOW(6)),
       (1, 6, 'ALL', NOW(6), NOW(6)),
       (1, 8, 'CURRENT_PARTICIPATION', NOW(6), NOW(6)),
       (1, 1, 'CURRENT_PARTICIPATION', NOW(6), NOW(6));

-- 최고 관리자: 모든 권한 전체 범위
INSERT INTO `tb_group_permission_grant` (`permission_group_id`, `permission_id`, `scope`, `created_at`, `updated_at`)
SELECT 2, `id`, 'ALL', NOW(6), NOW(6)
FROM `tb_permission`;

-- 원가 정책 (제경비율, 판관비율)
INSERT INTO `tb_employee_cost_policy` (`apply_year`, `employee_type`, `overhead_rate`, `sga_rate`, `created_at`, `updated_at`)
VALUES (2025, 'FULL_TIME', 0.1000, 0.0700, NOW(6), NOW(6)),
       (2025, 'FREELANCER', 0.0200, 0.0300, NOW(6), NOW(6)),
       (2025, 'OUTSOURCING', 0.0000, 0.0300, NOW(6), NOW(6)),
       (2025, 'PART_TIME', 0.0500, 0.0500, NOW(6), NOW(6)),
       (2026, 'FULL_TIME', 0.1000, 0.0500, NOW(6), NOW(6)),
       (2026, 'FREELANCER', 0.0000, 0.0300, NOW(6), NOW(6)),
       (2026, 'OUTSOURCING', 0.0000, 0.0300, NOW(6), NOW(6)),
       (2026, 'PART_TIME', 0.0500, 0.0500, NOW(6), NOW(6));
