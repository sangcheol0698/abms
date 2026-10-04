-- 데모: 프로젝트 수행 장소
UPDATE `tb_project` SET `work_place` = 'OFFICE' WHERE `id` IN (1, 4);
UPDATE `tb_project` SET `work_place` = 'CLIENT_SITE' WHERE `id` IN (2, 3, 6);
UPDATE `tb_project` SET `work_place` = 'OTHER', `work_zip_code` = '06164', `work_address` = '서울특별시 강남구 영동대로 513',
                        `work_address_detail` = '프로젝트룸 3층', `work_latitude` = 37.5115000, `work_longitude` = 127.0595000 WHERE `id` = 5;
UPDATE `tb_project` SET `work_place` = 'REMOTE' WHERE `id` = 7;
