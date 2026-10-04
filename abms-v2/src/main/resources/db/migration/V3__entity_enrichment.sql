-- 직원·협력사·부서 정보 보강

-- 직원: 연락처, 직무, 보유 기술, 근무 형태, 경력 시작일(총 경력 산정)
ALTER TABLE `tb_employee`
    ADD COLUMN `phone`             VARCHAR(20)  NULL AFTER `email`,
    ADD COLUMN `job`               VARCHAR(30)  NULL AFTER `grade`,
    ADD COLUMN `skills`            VARCHAR(500) NULL AFTER `job`,
    ADD COLUMN `work_type`         VARCHAR(20)  NULL AFTER `skills`,
    ADD COLUMN `career_start_date` DATE         NULL AFTER `join_date`;

-- 협력사: 구분, 사업자등록번호, 업종, 대표번호, 주소, 웹사이트, 메모
ALTER TABLE `tb_party`
    ADD COLUMN `party_type`      VARCHAR(20)  NOT NULL DEFAULT 'CLIENT' AFTER `name`,
    ADD COLUMN `business_number` VARCHAR(12)  NULL AFTER `party_type`,
    ADD COLUMN `industry`        VARCHAR(50)  NULL AFTER `business_number`,
    ADD COLUMN `phone`           VARCHAR(20)  NULL AFTER `industry`,
    ADD COLUMN `address`         VARCHAR(255) NULL AFTER `phone`,
    ADD COLUMN `website`         VARCHAR(255) NULL AFTER `address`,
    ADD COLUMN `memo`            TEXT         NULL AFTER `sales_rep_email`;

-- 부서: 소개(역할·담당 업무)
ALTER TABLE `tb_department`
    ADD COLUMN `description` VARCHAR(500) NULL AFTER `type`;
