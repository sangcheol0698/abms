-- 협력사 담당자 (여러 명, 역할별)
CREATE TABLE `tb_party_contact` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `party_id`   BIGINT       NOT NULL,
    `name`       VARCHAR(30)  NOT NULL,
    `role`       VARCHAR(20)  NOT NULL COMMENT 'SALES / CONTRACT / BILLING / TECH / ETC',
    `title`      VARCHAR(30)  NULL     COMMENT '부서·직책',
    `phone`      VARCHAR(20)  NULL,
    `email`      VARCHAR(100) NULL,
    `memo`       VARCHAR(500) NULL,
    `is_primary` BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '대표 담당자',

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_PARTY_CONTACT_PARTY_ID` (`party_id`),
    CONSTRAINT `FK_PARTY_CONTACT_PARTY_ID` FOREIGN KEY (`party_id`) REFERENCES `tb_party` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '협력사 담당자';

-- 기존 영업 담당자(단일)를 대표 영업 담당자로 옮긴다.
-- tb_party.sales_rep_* 컬럼은 더 이상 쓰지 않는다. (기존 데모 마이그레이션 호환을 위해 남겨 둠)
INSERT INTO `tb_party_contact` (`party_id`, `name`, `role`, `phone`, `email`, `is_primary`, `created_at`, `updated_at`, `created_by`, `updated_by`)
SELECT `id`, `sales_rep_name`, 'SALES', `sales_rep_phone`, `sales_rep_email`, TRUE, NOW(6), NOW(6), `updated_by`, `updated_by`
FROM `tb_party`
WHERE `sales_rep_name` IS NOT NULL AND `deleted` = FALSE;
