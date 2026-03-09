-- ABMS MySQL DDL (derived from current JPA entities)
-- Generated at: 2026-03-03
-- Note: `kr.co.abacus.abms.domain.contract.Contract` is excluded because `@Entity` is currently commented out.

CREATE TABLE IF NOT EXISTS `tb_common_code_group` (
    `group_code`  VARCHAR(50)  NOT NULL,
    `group_name`  VARCHAR(50)  NOT NULL,
    `description` VARCHAR(500) NULL,

    `created_at`  DATETIME(6)  NOT NULL,
    `updated_at`  DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`     TINYINT(1)   NOT NULL,
    `deleted_at`  DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`group_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_department` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
    `department_code`      VARCHAR(32)  NOT NULL,
    `department_name`      VARCHAR(30)  NOT NULL,
    `department_type`      VARCHAR(10)  NOT NULL,
    `leader_id`            BIGINT       NULL,
    `parent_department_id` BIGINT       NULL,

    `created_at`           DATETIME(6)  NOT NULL,
    `updated_at`           DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`              TINYINT(1)   NOT NULL,
    `deleted_at`           DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_DEPARTMENT_CODE` UNIQUE (`department_code`),

    INDEX `IDX_DEPARTMENT_PARENT_DEPARTMENT_ID` (`parent_department_id`),
    INDEX `IDX_DEPARTMENT_LEADER_ID` (`leader_id`),

    CONSTRAINT `FK_DEPARTMENT_PARENT_DEPARTMENT_ID` FOREIGN KEY (`parent_department_id`) REFERENCES `tb_department` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_party` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `party_name`      VARCHAR(50)  NOT NULL,
    `ceo_name`        VARCHAR(30)  NULL,
    `sales_rep_name`  VARCHAR(30)  NULL,
    `sales_rep_phone` VARCHAR(20)  NULL,
    `sales_rep_email` VARCHAR(100) NULL,

    `created_at`      DATETIME(6)  NOT NULL,
    `updated_at`      DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`         TINYINT(1)   NOT NULL,
    `deleted_at`      DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_PARTY_NAME` UNIQUE (`party_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_chat_session` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `title`      VARCHAR(200) NOT NULL,
    `session_id` VARCHAR(100) NOT NULL,
    `account_id` BIGINT       NOT NULL,
    `favorite`   TINYINT(1)   NOT NULL,

    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`    TINYINT(1)   NOT NULL,
    `deleted_at` DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_CHAT_SESSION_ACCOUNT_SESSION_ID` UNIQUE (`account_id`, `session_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_employee` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `department_id`    BIGINT       NOT NULL,
    `name`             VARCHAR(10)  NOT NULL,
    `email_address`    VARCHAR(255) NOT NULL,
    `join_date`        DATE         NOT NULL,
    `birth_date`       DATE         NOT NULL,
    `position`         VARCHAR(20)  NOT NULL,
    `type`             VARCHAR(255) NOT NULL,
    `status`           VARCHAR(255) NOT NULL,
    `grade`            VARCHAR(255) NOT NULL,
    `avatar`           VARCHAR(40)  NOT NULL,
    `resignation_date` DATE         NULL,
    `memo`             TEXT         NULL,

    `created_at`       DATETIME(6)  NOT NULL,
    `updated_at`       DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`          TINYINT(1)   NOT NULL,
    `deleted_at`       DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_EMPLOYEE_EMAIL_ADDRESS` UNIQUE (`email_address`),
    CONSTRAINT `FK_EMPLOYEE_DEPARTMENT_ID` FOREIGN KEY (`department_id`) REFERENCES `tb_department` (`id`),
    INDEX `IDX_EMPLOYEE_DEPARTMENT_ID` (`department_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_account` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `employee_id`         BIGINT       NOT NULL,
    `username`            VARCHAR(100) NOT NULL,
    `password`            VARCHAR(255) NOT NULL,
    `password_changed_at` DATETIME(6)  NOT NULL,
    `is_valid`            TINYINT(1)   NOT NULL,
    `login_fail_count`    INT          NOT NULL,
    `created_at`          DATETIME(6)  NOT NULL,
    `updated_at`          DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`             TINYINT(1)   NOT NULL,
    `deleted_at`          DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_ACCOUNT_USERNAME` UNIQUE (`username`),
    CONSTRAINT `UK_ACCOUNT_EMPLOYEE_ID` UNIQUE (`employee_id`),
    CONSTRAINT `FK_ACCOUNT_EMPLOYEE_ID`FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_permission` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(255) NOT NULL,
    `name`        VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,

    `created_at`  DATETIME(6)  NOT NULL,
    `updated_at`  DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`     TINYINT(1)   NOT NULL,
    `deleted_at`  DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_PERMISSION_CODE` UNIQUE (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_permission_group` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `group_type`  VARCHAR(255) NOT NULL,

    `created_at`  DATETIME(6)  NOT NULL,
    `updated_at`  DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`     TINYINT(1)   NOT NULL,
    `deleted_at`  DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_account_group_assignment` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `account_id`          BIGINT       NOT NULL,
    `permission_group_id` BIGINT       NOT NULL,

    `created_at`          DATETIME(6)  NOT NULL,
    `updated_at`          DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`             TINYINT(1)   NOT NULL,
    `deleted_at`          DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_ACCOUNT_GROUP_ASSIGNMENT` UNIQUE (`account_id`, `permission_group_id`),
    INDEX `IDX_ACCOUNT_GROUP_ASSIGNMENT_ACCOUNT_ID` (`account_id`),
    INDEX `IDX_ACCOUNT_GROUP_ASSIGNMENT_PERMISSION_GROUP_ID` (`permission_group_id`),
    CONSTRAINT `FK_ACCOUNT_GROUP_ASSIGNMENT_ACCOUNT_ID` FOREIGN KEY (`account_id`) REFERENCES `tb_account` (`id`),
    CONSTRAINT `FK_ACCOUNT_GROUP_ASSIGNMENT_PERMISSION_GROUP_ID` FOREIGN KEY (`permission_group_id`) REFERENCES `tb_permission_group` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_group_permission_grant` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `permission_group_id` BIGINT       NOT NULL,
    `permission_id`       BIGINT       NOT NULL,
    `scope`               VARCHAR(255) NOT NULL,

    `created_at`          DATETIME(6)  NOT NULL,
    `updated_at`          DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`             TINYINT(1)   NOT NULL,
    `deleted_at`          DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_GROUP_PERMISSION_GRANT` UNIQUE (`permission_group_id`, `permission_id`, `scope`),
    INDEX `IDX_GROUP_PERMISSION_GRANT_PERMISSION_GROUP_ID` (`permission_group_id`),
    INDEX `IDX_GROUP_PERMISSION_GRANT_PERMISSION_ID` (`permission_id`),
    CONSTRAINT `FK_GROUP_PERMISSION_GRANT_PERMISSION_GROUP_ID` FOREIGN KEY (`permission_group_id`) REFERENCES `tb_permission_group` (`id`),
    CONSTRAINT `FK_GROUP_PERMISSION_GRANT_PERMISSION_ID` FOREIGN KEY (`permission_id`) REFERENCES `tb_permission` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_registration_token` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `employee_id` BIGINT       NOT NULL,
    `email`       VARCHAR(100) NOT NULL,
    `token`       VARCHAR(100) NOT NULL,
    `expires_at`  DATETIME(6)  NOT NULL,
    `used`        TINYINT(1)   NOT NULL,
    `used_at`     DATETIME(6)  NULL,
    `created_at`  DATETIME(6)  NOT NULL,
    `updated_at`  DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`     TINYINT(1)   NOT NULL,
    `deleted_at`  DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_REGISTRATION_TOKEN` UNIQUE (`token`),
    INDEX `IDX_REGISTRATION_TOKEN_EMPLOYEE_ID` (`employee_id`),
    CONSTRAINT `FK_REGISTRATION_TOKEN_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_chat_message` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `role`            INT          NOT NULL,
    `content`         TEXT         NOT NULL,
    `chat_session_id` BIGINT       NULL,
    `timestamp`       DATETIME(6)  NOT NULL,
    `created_at`      DATETIME(6)  NOT NULL,
    `updated_at`      DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`         TINYINT(1)   NOT NULL,
    `deleted_at`      DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_CHAT_MESSAGE_CHAT_SESSION_ID` (`chat_session_id`),
    CONSTRAINT `FK_CHAT_MESSAGE_CHAT_SESSION_ID` FOREIGN KEY (`chat_session_id`) REFERENCES `tb_chat_session` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_chat_memory_message` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `conversation_id` VARCHAR(100) NOT NULL,
    `message_type`    VARCHAR(255) NOT NULL,
    `content`         TEXT         NOT NULL,
    `timestamp`       DATETIME(6)  NOT NULL,

    `created_at`      DATETIME(6)  NOT NULL,
    `updated_at`      DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`         TINYINT(1)   NOT NULL,
    `deleted_at`      DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_common_code_detail` (
    `group_code` VARCHAR(50)  NOT NULL,
    `code`       VARCHAR(255) NOT NULL,
    `name`       VARCHAR(255) NOT NULL,
    `sort_order` INT          NOT NULL,

    `created_at` DATETIME(6)  NOT NULL,
    `updated_at` DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`    TINYINT(1)   NOT NULL,
    `deleted_at` DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`group_code`, `code`),
    INDEX `IDX_COMMON_CODE_DETAIL_GROUP_CODE` (`group_code`),
    CONSTRAINT `FK_COMMON_CODE_DETAIL_GROUP_CODE` FOREIGN KEY (`group_code`) REFERENCES `tb_common_code_group` (`group_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_employee_cost_policy` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `apply_year`    INT          NOT NULL,
    `employee_type` VARCHAR(255) NOT NULL,
    `overhead_rate` DOUBLE       NOT NULL,
    `sga_rate`      DOUBLE       NOT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_EMPLOYEE_COST_POLICY_APPLY_YEAR_EMPLOYEE_TYPE` UNIQUE (`apply_year`, `employee_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_employee_monthly_cost` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT,
    `employee_id`    BIGINT         NOT NULL,
    `cost_month`     VARCHAR(6)     NOT NULL,
    `monthly_salary` DECIMAL(19, 0) NOT NULL,
    `overhead_cost`  DECIMAL(19, 0) NOT NULL,
    `sga_cost`       DECIMAL(19, 0) NOT NULL,
    `total_cost`     DECIMAL(19, 0) NOT NULL,

    `created_at`     DATETIME(6)    NOT NULL,
    `updated_at`     DATETIME(6)    NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`        TINYINT(1)     NOT NULL,
    `deleted_at`     DATETIME(6)    NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_EMPLOYEE_MONTHLY_COST_EMPLOYEE_ID_COST_MONTH` UNIQUE (`employee_id`, `cost_month`),
    CONSTRAINT `FK_EMPLOYEE_MONTHLY_COST_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`),
    INDEX `IDX_EMPLOYEE_MONTHLY_COST_EMPLOYEE_ID` (`employee_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '월별 인력 비용 (월급 + 제경비 + 판관비)';

CREATE TABLE IF NOT EXISTS `tb_notification` (
    `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
    `account_id`               BIGINT       NOT NULL,
    `notification_title`       VARCHAR(120) NOT NULL,
    `notification_description` VARCHAR(500) NULL,
    `notification_type`        VARCHAR(20)  NOT NULL,
    `is_read`                  TINYINT(1)   NOT NULL,
    `link_url`                 VARCHAR(255) NULL,

    `created_at`               DATETIME(6)  NOT NULL,
    `updated_at`               DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`                  TINYINT(1)   NOT NULL,
    `deleted_at`               DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `FK_NOTIFICATION_ACCOUNT_ID` FOREIGN KEY (`account_id`) REFERENCES `tb_account` (`id`),
    INDEX `IDX_NOTIFICATION_ACCOUNT_ID_DELETED_CREATED_AT` (`account_id`, `deleted`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_payroll` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT,
    `employee_id`   BIGINT         NOT NULL,
    `annual_salary` DECIMAL(19, 0) NOT NULL,
    `start_date`    DATE           NOT NULL,
    `end_date`      DATE           NULL,

    `created_at`    DATETIME(6)    NOT NULL,
    `updated_at`    DATETIME(6)    NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`       TINYINT(1)     NOT NULL,
    `deleted_at`    DATETIME(6)    NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_PAYROLL_EMPLOYEE_ID` (`employee_id`),
    CONSTRAINT `FK_PAYROLL_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_position_history` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `employee_id` BIGINT       NOT NULL,
    `start_date`  DATE         NOT NULL,
    `end_date`    DATE         NULL,
    `position`    VARCHAR(20)  NOT NULL,

    `created_at`  DATETIME(6)  NOT NULL,
    `updated_at`  DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`     TINYINT(1)   NOT NULL,
    `deleted_at`  DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_POSITION_HISTORY_EMPLOYEE_ID` (`employee_id`),
    CONSTRAINT `FK_POSITION_HISTORY_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_project` (
    `id`                  BIGINT         NOT NULL AUTO_INCREMENT,
    `party_id`            BIGINT         NOT NULL,
    `lead_department_id`  BIGINT         NOT NULL,
    `project_code`        VARCHAR(50)    NOT NULL,
    `project_name`        VARCHAR(100)   NOT NULL,
    `project_description` VARCHAR(500)   NULL,
    `project_status`      VARCHAR(20)    NOT NULL,
    `contract_amount`     DECIMAL(19, 0) NOT NULL,
    `start_date`          DATE           NOT NULL,
    `end_date`            DATE           NOT NULL,

    `created_at`          DATETIME(6)    NOT NULL,
    `updated_at`          DATETIME(6)    NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`             TINYINT(1)     NOT NULL,
    `deleted_at`          DATETIME(6)    NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_PROJECT_CODE` UNIQUE (`project_code`),
    CONSTRAINT `FK_PROJECT_PARTY_ID` FOREIGN KEY (`party_id`) REFERENCES `tb_party` (`id`),
    CONSTRAINT `FK_PROJECT_LEAD_DEPARTMENT_ID` FOREIGN KEY (`lead_department_id`) REFERENCES `tb_department` (`id`),
    INDEX `IDX_PROJECT_PARTY_ID` (`party_id`),
    INDEX `IDX_PROJECT_LEAD_DEPARTMENT_ID` (`lead_department_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_project_revenue_plan` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT,
    `project_id`    BIGINT         NOT NULL,
    `plan_sequence` INT            NOT NULL,
    `revenue_date`  DATE           NOT NULL,
    `revenue_type`  VARCHAR(20)    NOT NULL,
    `amount`        DECIMAL(19, 0) NOT NULL,
    `is_issued`     TINYINT(1)     NOT NULL DEFAULT 0,
    `memo`          VARCHAR(255)   NULL,

    `created_at`    DATETIME(6)    NOT NULL,
    `updated_at`    DATETIME(6)    NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`       TINYINT(1)     NOT NULL,
    `deleted_at`    DATETIME(6)    NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_PROJECT_REVENUE_PLAN_PROJECT_ID` (`project_id`),
    CONSTRAINT `FK_PROJECT_REVENUE_PLAN_PROJECT_ID` FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_project_assignment` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `project_id`      BIGINT       NOT NULL,
    `employee_id`     BIGINT       NOT NULL,
    `assignment_role` VARCHAR(20)  NULL,
    `start_date`      DATE         NOT NULL,
    `end_date`        DATE         NULL,

    `created_at`      DATETIME(6)  NOT NULL,
    `updated_at`      DATETIME(6)  NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`         TINYINT(1)   NOT NULL,
    `deleted_at`      DATETIME(6)  NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_PROJECT_ASSIGNMENT_PROJECT_ID` (`project_id`),
    INDEX `IDX_PROJECT_ASSIGNMENT_EMPLOYEE_ID` (`employee_id`),
    CONSTRAINT `FK_PROJECT_ASSIGNMENT_PROJECT_ID` FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`),
    CONSTRAINT `FK_PROJECT_ASSIGNMENT_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tb_monthly_revenue_summary` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT,
    `project_id`     BIGINT         NOT NULL,
    `project_code`   VARCHAR(255)   NOT NULL,
    `project_name`   VARCHAR(255)   NOT NULL,
    `team_id`        BIGINT         NOT NULL,
    `team_code`      VARCHAR(255)   NOT NULL,
    `team_name`      VARCHAR(255)   NOT NULL,
    `summary_date`   DATE           NOT NULL,
    `revenue_amount` DECIMAL(19, 0) NOT NULL,
    `cost_amount`    DECIMAL(19, 0) NOT NULL,
    `profit_amount`  DECIMAL(19, 0) NOT NULL,

    `created_at`     DATETIME(6)    NOT NULL,
    `updated_at`     DATETIME(6)    NOT NULL,
    `created_by`  BIGINT       NULL,
    `updated_by`  BIGINT       NULL,
    `deleted`        TINYINT(1)     NOT NULL,
    `deleted_at`     DATETIME(6)    NULL,
    `deleted_by`  BIGINT       NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_MONTHLY_REVENUE_SUMMARY_PROJECT_ID` (`project_id`),
    INDEX `IDX_MONTHLY_REVENUE_SUMMARY_TEAM_ID` (`team_id`),
    CONSTRAINT `FK_MONTHLY_REVENUE_SUMMARY_PROJECT_ID` FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`),
    CONSTRAINT `FK_MONTHLY_REVENUE_SUMMARY_TEAM_ID` FOREIGN KEY (`team_id`) REFERENCES `tb_department` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- `tb_department.leader_id`는 순환 참조 문제를 피하기 위해 `tb_employee` 생성 후에 추가됩니다.
ALTER TABLE `tb_department`
    ADD CONSTRAINT `FK_DEPARTMENT_LEADER_ID`
        FOREIGN KEY (`leader_id`) REFERENCES `tb_employee` (`id`);
