-- 위치 정보: 협력사 좌표·상세 주소, 사업장(사옥·지사), 부서 근무 사업장

-- 협력사: 기존 address 에 우편번호·상세 주소·좌표 추가 (모두 선택)
ALTER TABLE `tb_party`
    ADD COLUMN `zip_code`       VARCHAR(10)    NULL AFTER `phone`,
    ADD COLUMN `address_detail` VARCHAR(100)   NULL AFTER `address`,
    ADD COLUMN `latitude`       DECIMAL(10, 7) NULL AFTER `address_detail`,
    ADD COLUMN `longitude`      DECIMAL(10, 7) NULL AFTER `latitude`;

-- 사업장 (본사·지사·연구소 등)
CREATE TABLE `tb_site` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(50)    NOT NULL,
    `site_type`      VARCHAR(20)    NOT NULL,
    `phone`          VARCHAR(20)    NULL,
    `zip_code`       VARCHAR(10)    NULL,
    `address`        VARCHAR(255)   NULL,
    `address_detail` VARCHAR(100)   NULL,
    `latitude`       DECIMAL(10, 7) NULL,
    `longitude`      DECIMAL(10, 7) NULL,
    `memo`           TEXT           NULL,
    `active_name`    VARCHAR(50)    GENERATED ALWAYS AS (CASE WHEN `deleted` = 0 THEN `name` END) STORED,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_SITE_ACTIVE_NAME` UNIQUE (`active_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '사업장';

-- 부서 근무 사업장 (선택)
ALTER TABLE `tb_department`
    ADD COLUMN `site_id` BIGINT NULL AFTER `parent_id`,
    ADD INDEX `IDX_DEPARTMENT_SITE_ID` (`site_id`),
    ADD CONSTRAINT `FK_DEPARTMENT_SITE_ID` FOREIGN KEY (`site_id`) REFERENCES `tb_site` (`id`);
