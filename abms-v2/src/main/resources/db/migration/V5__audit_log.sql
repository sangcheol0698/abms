-- 변경 이력 (감사 로그): 누가 언제 무엇을 바꿨는지 기록한다.

CREATE TABLE `tb_audit_log` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `entity_type`      VARCHAR(50)  NOT NULL COMMENT '엔티티 종류 (클래스명)',
    `entity_id`        BIGINT       NOT NULL,
    `entity_label`     VARCHAR(30)  NOT NULL COMMENT '엔티티 종류 표시명 (프로젝트, 매출 계획 …)',
    `entity_name`      VARCHAR(200) NULL     COMMENT '변경 당시 대상 이름',
    `parent_type`      VARCHAR(50)  NULL     COMMENT '상위 엔티티 (예: 매출 계획 → 프로젝트)',
    `parent_id`        BIGINT       NULL,
    `action`           VARCHAR(10)  NOT NULL COMMENT 'CREATE / UPDATE / DELETE / RESTORE',
    `actor_account_id` BIGINT       NULL     COMMENT 'NULL 이면 시스템',
    `actor_name`       VARCHAR(50)  NULL,
    `created_at`       DATETIME(6)  NOT NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_AUDIT_LOG_ENTITY` (`entity_type`, `entity_id`),
    INDEX `IDX_AUDIT_LOG_PARENT` (`parent_type`, `parent_id`),
    INDEX `IDX_AUDIT_LOG_CREATED_AT` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '변경 이력';

CREATE TABLE `tb_audit_log_change` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `audit_log_id` BIGINT       NOT NULL,
    `field`        VARCHAR(50)  NOT NULL COMMENT '속성명',
    `before_value` VARCHAR(500) NULL,
    `after_value`  VARCHAR(500) NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_AUDIT_LOG_CHANGE_LOG` (`audit_log_id`),
    CONSTRAINT `FK_AUDIT_LOG_CHANGE_LOG` FOREIGN KEY (`audit_log_id`) REFERENCES `tb_audit_log` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '변경 이력 상세 (속성별 이전/이후 값)';
