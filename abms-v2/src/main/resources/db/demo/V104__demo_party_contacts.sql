-- 데모: 새 DB 에서는 V7 이 데모 데이터(V100)보다 먼저 실행되므로 영업 담당자를 여기서 옮기고, 역할별 담당자를 추가한다.
INSERT INTO `tb_party_contact` (`party_id`, `name`, `role`, `phone`, `email`, `is_primary`, `created_at`, `updated_at`, `created_by`, `updated_by`)
SELECT p.`id`, p.`sales_rep_name`, 'SALES', p.`sales_rep_phone`, p.`sales_rep_email`, TRUE, NOW(6), NOW(6), 1, 1
FROM `tb_party` p
WHERE p.`sales_rep_name` IS NOT NULL AND p.`deleted` = FALSE
  AND NOT EXISTS (SELECT 1 FROM `tb_party_contact` c WHERE c.`party_id` = p.`id`);

INSERT INTO `tb_party_contact` (`party_id`, `name`, `role`, `title`, `phone`, `email`, `memo`, `is_primary`, `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
(1, '김계약', 'CONTRACT', '구매팀 과장', '02-6000-2101', 'contract@hanbit-cloud.example', '연간 유지보수 계약 담당', FALSE, NOW(6), NOW(6), 1, 1),
(1, '이정산', 'BILLING', '재무팀 대리', '02-6000-2102', 'billing@hanbit-cloud.example', '세금계산서 수신, 매월 25일 마감', FALSE, NOW(6), NOW(6), 1, 1),
(3, '박기술', 'TECH', 'IT전략팀 차장', '02-6000-2301', 'tech@daon-fintech.example', '보안 심사·인프라 협의 창구', FALSE, NOW(6), NOW(6), 1, 1),
(4, '최청구', 'BILLING', '경영지원팀 과장', '02-6000-2401', 'billing@saesol-telecom.example', NULL, FALSE, NOW(6), NOW(6), 1, 1);
