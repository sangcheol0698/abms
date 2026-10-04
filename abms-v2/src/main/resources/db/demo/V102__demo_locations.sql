-- 데모: 사업장, 협력사 좌표, 부서 근무 사업장

INSERT INTO `tb_site` (`id`, `name`, `site_type`, `phone`, `zip_code`, `address`, `address_detail`, `latitude`, `longitude`, `memo`,
                       `created_at`, `updated_at`, `created_by`, `updated_by`) VALUES
(1, '본사', 'HEADQUARTERS', '02-2000-0000', '08379', '서울특별시 구로구 디지털로 300', '12층', 37.4846000, 126.8972000,
 '방문객은 1층 안내데스크에서 출입증을 받습니다. 지하 2층 방문 주차 2시간 무료.', NOW(6), NOW(6), 1, 1),
(2, '판교 연구소', 'RESEARCH', '031-700-0000', '13494', '경기도 성남시 분당구 대왕판교로 660', 'A동 7층', 37.4016000, 127.1078000,
 '연구개발본부·기술연구소 상주.', NOW(6), NOW(6), 1, 1),
(3, '을지로 프로젝트 사무소', 'OFFICE', NULL, '04548', '서울특별시 중구 을지로 170', '9층', 37.5664000, 126.9916000,
 '통신 고객사 상주 프로젝트용 분소.', NOW(6), NOW(6), 1, 1),
(4, '원격 근무', 'ETC', NULL, NULL, NULL, NULL, NULL, NULL,
 '주소 없이 운영하는 원격·재택 근무 단위.', NOW(6), NOW(6), 1, 1);

UPDATE `tb_party` SET `zip_code` = '04524', `latitude` = 37.5662952, `longitude` = 126.9779451 WHERE `id` = 1;
UPDATE `tb_party` SET `zip_code` = '13494', `address_detail` = '8층', `latitude` = 37.4020000, `longitude` = 127.1086000 WHERE `id` = 2;
UPDATE `tb_party` SET `zip_code` = '07326', `latitude` = 37.5251000, `longitude` = 126.9255000 WHERE `id` = 3;
UPDATE `tb_party` SET `zip_code` = '03159', `latitude` = 37.5709000, `longitude` = 126.9830000 WHERE `id` = 4;
UPDATE `tb_party` SET `zip_code` = '21984', `latitude` = 37.3826000, `longitude` = 126.6566000 WHERE `id` = 5;
UPDATE `tb_party` SET `zip_code` = '06159', `latitude` = 37.5063000, `longitude` = 127.0533000 WHERE `id` = 6;

UPDATE `tb_department` SET `site_id` = 1 WHERE `id` IN (1, 2, 3, 4, 7, 8, 10, 101, 102, 103, 104, 105, 106, 107, 108, 115, 116, 117, 118, 119);
UPDATE `tb_department` SET `site_id` = 2 WHERE `id` IN (5, 6, 100);
UPDATE `tb_department` SET `site_id` = 3 WHERE `id` IN (9, 110, 111, 112, 113, 114);
UPDATE `tb_department` SET `site_id` = 4 WHERE `id` = 109;
