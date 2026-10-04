-- 프로젝트 수행 장소: 자사 / 고객사 상주 / 별도 장소 / 원격 + (별도 장소 등) 주소
ALTER TABLE `tb_project`
    ADD COLUMN `work_place`          VARCHAR(20)    NULL AFTER `end_date`,
    ADD COLUMN `work_zip_code`       VARCHAR(10)    NULL AFTER `work_place`,
    ADD COLUMN `work_address`        VARCHAR(255)   NULL AFTER `work_zip_code`,
    ADD COLUMN `work_address_detail` VARCHAR(100)   NULL AFTER `work_address`,
    ADD COLUMN `work_latitude`       DECIMAL(10, 7) NULL AFTER `work_address_detail`,
    ADD COLUMN `work_longitude`      DECIMAL(10, 7) NULL AFTER `work_latitude`;
