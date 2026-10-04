-- 첨부 파일 (프로젝트·협력사의 계약서, 세금계산서, 산출물 등). 파일 본문은 저장소(로컬 디스크 등)에 두고 메타데이터만 저장한다.
CREATE TABLE `tb_attachment` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `owner_type`    VARCHAR(20)  NOT NULL COMMENT 'PROJECT / PARTY',
    `owner_id`      BIGINT       NOT NULL,
    `category`      VARCHAR(20)  NOT NULL COMMENT 'CONTRACT / INVOICE / DELIVERABLE / ETC',
    `original_name` VARCHAR(255) NOT NULL,
    `stored_path`   VARCHAR(200) NOT NULL COMMENT '저장소 안의 상대 경로',
    `content_type`  VARCHAR(100) NULL,
    `size`          BIGINT       NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_ATTACHMENT_OWNER` (`owner_type`, `owner_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '첨부 파일';
