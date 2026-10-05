-- 공지사항과 사용자별 수신 상태(읽음, 안내 팝업 숨김)
CREATE TABLE `tb_notice` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `title`      VARCHAR(100) NOT NULL,
    `body`       TEXT         NOT NULL COMMENT '마크다운',
    `importance` VARCHAR(20)  NOT NULL COMMENT 'NORMAL / IMPORTANT / URGENT',
    `pinned`     BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '목록 상단 고정',
    `popup`      BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '안내 팝업으로 제공',
    `starts_at`  DATETIME(6)  NULL     COMMENT '게시 시작 (NULL 이면 등록 즉시)',
    `ends_at`    DATETIME(6)  NULL     COMMENT '게시 종료 (NULL 이면 무기한)',

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_NOTICE_PERIOD` (`starts_at`, `ends_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '공지사항';

CREATE TABLE `tb_notice_receipt` (
    `id`                      BIGINT      NOT NULL AUTO_INCREMENT,
    `notice_id`               BIGINT      NOT NULL,
    `account_id`              BIGINT      NOT NULL,
    `read_at`                 DATETIME(6) NULL,
    `popup_hidden_until`      DATETIME(6) NULL COMMENT '오늘 하루 보지 않기: 이 시각까지 팝업 숨김',
    `popup_hidden_forever`    BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '다시 보지 않기',

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_NOTICE_RECEIPT` UNIQUE (`notice_id`, `account_id`),
    INDEX `IDX_NOTICE_RECEIPT_ACCOUNT` (`account_id`),
    CONSTRAINT `FK_NOTICE_RECEIPT_NOTICE` FOREIGN KEY (`notice_id`) REFERENCES `tb_notice` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '공지 수신 상태';
