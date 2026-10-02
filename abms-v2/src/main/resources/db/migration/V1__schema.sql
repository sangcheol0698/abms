-- ABMS 스키마
-- 모든 업무 테이블은 감사 컬럼(created/updated)과 소프트 삭제 컬럼(deleted)을 가진다.

-- 부서
CREATE TABLE `tb_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code`               VARCHAR(32)  NOT NULL,
    `name`               VARCHAR(50)  NOT NULL,
    `type`               VARCHAR(20)  NOT NULL,
    `parent_id`          BIGINT       NULL,
    `leader_employee_id` BIGINT       NULL,
    `active_code`        VARCHAR(32)  GENERATED ALWAYS AS (CASE WHEN `deleted` = 0 THEN `code` END) STORED,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_DEPARTMENT_ACTIVE_CODE` UNIQUE (`active_code`),
    INDEX `IDX_DEPARTMENT_PARENT_ID` (`parent_id`),
    CONSTRAINT `FK_DEPARTMENT_PARENT_ID` FOREIGN KEY (`parent_id`) REFERENCES `tb_department` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '부서';

-- 직원
CREATE TABLE `tb_employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `department_id`    BIGINT       NOT NULL,
    `name`             VARCHAR(30)  NOT NULL,
    `email`            VARCHAR(255) NOT NULL,
    `join_date`        DATE         NOT NULL,
    `birth_date`       DATE         NOT NULL,
    `position`         VARCHAR(30)  NOT NULL,
    `type`             VARCHAR(20)  NOT NULL,
    `status`           VARCHAR(20)  NOT NULL,
    `grade`            VARCHAR(20)  NOT NULL,
    `avatar`           VARCHAR(40)  NOT NULL,
    `resignation_date` DATE         NULL,
    `memo`             TEXT         NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_EMPLOYEE_EMAIL` UNIQUE (`email`),
    INDEX `IDX_EMPLOYEE_DEPARTMENT_ID` (`department_id`),
    INDEX `IDX_EMPLOYEE_NAME` (`name`),
    CONSTRAINT `FK_EMPLOYEE_DEPARTMENT_ID` FOREIGN KEY (`department_id`) REFERENCES `tb_department` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '직원';

ALTER TABLE `tb_department`
    ADD CONSTRAINT `FK_DEPARTMENT_LEADER_EMPLOYEE_ID` FOREIGN KEY (`leader_employee_id`) REFERENCES `tb_employee` (`id`);

-- 직원 연봉 이력
CREATE TABLE `tb_payroll` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `employee_id`   BIGINT         NOT NULL,
    `annual_salary` DECIMAL(19, 0) NOT NULL,
    `start_date`    DATE           NOT NULL,
    `end_date`      DATE           NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    INDEX `IDX_PAYROLL_EMPLOYEE_ID` (`employee_id`, `start_date`),
    CONSTRAINT `FK_PAYROLL_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '직원 연봉 이력';

-- 직급/등급 이력
CREATE TABLE `tb_position_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `employee_id` BIGINT      NOT NULL,
    `position`    VARCHAR(30) NOT NULL,
    `grade`       VARCHAR(20) NOT NULL,
    `start_date`  DATE        NOT NULL,
    `end_date`    DATE        NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    INDEX `IDX_POSITION_HISTORY_EMPLOYEE_ID` (`employee_id`),
    CONSTRAINT `FK_POSITION_HISTORY_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '직급/등급 이력';

-- 협력사
CREATE TABLE `tb_party` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(50)  NOT NULL,
    `ceo_name`        VARCHAR(30)  NULL,
    `sales_rep_name`  VARCHAR(30)  NULL,
    `sales_rep_phone` VARCHAR(20)  NULL,
    `sales_rep_email` VARCHAR(100) NULL,
    `active_name`     VARCHAR(50)  GENERATED ALWAYS AS (CASE WHEN `deleted` = 0 THEN `name` END) STORED,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_PARTY_ACTIVE_NAME` UNIQUE (`active_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '협력사';

-- 프로젝트
CREATE TABLE `tb_project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `party_id`           BIGINT         NOT NULL,
    `lead_department_id` BIGINT         NOT NULL,
    `code`               VARCHAR(50)    NOT NULL,
    `name`               VARCHAR(100)   NOT NULL,
    `description`        VARCHAR(1000)  NULL,
    `status`             VARCHAR(20)    NOT NULL,
    `contract_amount`    DECIMAL(19, 0) NOT NULL,
    `start_date`         DATE           NOT NULL,
    `end_date`           DATE           NULL,
    `active_code`        VARCHAR(50)    GENERATED ALWAYS AS (CASE WHEN `deleted` = 0 THEN `code` END) STORED,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_PROJECT_ACTIVE_CODE` UNIQUE (`active_code`),
    INDEX `IDX_PROJECT_PARTY_ID` (`party_id`),
    INDEX `IDX_PROJECT_LEAD_DEPARTMENT_ID` (`lead_department_id`),
    INDEX `IDX_PROJECT_PERIOD` (`start_date`, `end_date`),
    CONSTRAINT `FK_PROJECT_PARTY_ID` FOREIGN KEY (`party_id`) REFERENCES `tb_party` (`id`),
    CONSTRAINT `FK_PROJECT_LEAD_DEPARTMENT_ID` FOREIGN KEY (`lead_department_id`) REFERENCES `tb_department` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '프로젝트';

-- 프로젝트 매출(청구) 계획
CREATE TABLE `tb_project_revenue_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id`    BIGINT         NOT NULL,
    `plan_sequence` INT            NOT NULL,
    `revenue_date`  DATE           NOT NULL,
    `revenue_type`  VARCHAR(30)    NOT NULL,
    `amount`        DECIMAL(19, 0) NOT NULL,
    `issued`        BOOLEAN        NOT NULL DEFAULT FALSE,
    `memo`          VARCHAR(255)   NULL,
    `active_sequence` INT          GENERATED ALWAYS AS (CASE WHEN `deleted` = 0 THEN `plan_sequence` END) STORED,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_REVENUE_PLAN_PROJECT_SEQUENCE` UNIQUE (`project_id`, `active_sequence`),
    INDEX `IDX_REVENUE_PLAN_DATE` (`revenue_date`, `issued`),
    CONSTRAINT `FK_REVENUE_PLAN_PROJECT_ID` FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '프로젝트 매출(청구) 계획';

-- 프로젝트 투입
CREATE TABLE `tb_project_assignment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id`      BIGINT      NOT NULL,
    `employee_id`     BIGINT      NOT NULL,
    `assignment_role` VARCHAR(20) NULL,
    `start_date`      DATE        NOT NULL,
    `end_date`        DATE        NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    INDEX `IDX_ASSIGNMENT_PROJECT_ID` (`project_id`),
    INDEX `IDX_ASSIGNMENT_EMPLOYEE_ID` (`employee_id`),
    INDEX `IDX_ASSIGNMENT_PERIOD` (`start_date`, `end_date`),
    CONSTRAINT `FK_ASSIGNMENT_PROJECT_ID` FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`),
    CONSTRAINT `FK_ASSIGNMENT_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '프로젝트 투입';

-- 연도·고용유형별 원가 정책 (제경비율, 판관비율)
CREATE TABLE `tb_employee_cost_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `apply_year`    INT           NOT NULL,
    `employee_type` VARCHAR(20)   NOT NULL,
    `overhead_rate` DECIMAL(6, 4) NOT NULL,
    `sga_rate`      DECIMAL(6, 4) NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_COST_POLICY_YEAR_TYPE` UNIQUE (`apply_year`, `employee_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '연도·고용유형별 원가 정책 (제경비율, 판관비율)';

-- 직원 월 원가 (월 기본급 + 제경비 + 판관비)
CREATE TABLE `tb_employee_monthly_cost` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `employee_id`    BIGINT         NOT NULL,
    `target_month`   DATE           NOT NULL,
    `monthly_salary` DECIMAL(19, 0) NOT NULL,
    `overhead_cost`  DECIMAL(19, 0) NOT NULL,
    `sga_cost`       DECIMAL(19, 0) NOT NULL,
    `total_cost`     DECIMAL(19, 0) NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_EMPLOYEE_MONTHLY_COST` UNIQUE (`employee_id`, `target_month`),
    INDEX `IDX_EMPLOYEE_MONTHLY_COST_MONTH` (`target_month`),
    CONSTRAINT `FK_EMPLOYEE_MONTHLY_COST_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '직원 월 원가 (월 기본급 + 제경비 + 판관비)';

-- 프로젝트별 월 손익 집계
CREATE TABLE `tb_monthly_revenue_summary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id`           BIGINT         NOT NULL,
    `project_code`         VARCHAR(50)    NOT NULL,
    `project_name`         VARCHAR(100)   NOT NULL,
    `lead_department_id`   BIGINT         NOT NULL,
    `lead_department_name` VARCHAR(50)    NOT NULL,
    `target_month`         DATE           NOT NULL,
    `calculated_at`        DATETIME(6)    NOT NULL,
    `revenue_amount`       DECIMAL(19, 0) NOT NULL,
    `cost_amount`          DECIMAL(19, 0) NOT NULL,
    `profit_amount`        DECIMAL(19, 0) NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_MONTHLY_REVENUE_SUMMARY` UNIQUE (`project_id`, `target_month`),
    INDEX `IDX_MONTHLY_REVENUE_SUMMARY_MONTH` (`target_month`),
    INDEX `IDX_MONTHLY_REVENUE_SUMMARY_DEPT_MONTH` (`lead_department_id`, `target_month`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '프로젝트별 월 손익 집계';

-- 전사 월 정직원 비용 집계
CREATE TABLE `tb_company_monthly_cost_summary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `target_month`              DATE           NOT NULL,
    `calculated_at`             DATETIME(6)    NOT NULL,
    `total_full_time_cost`      DECIMAL(19, 0) NOT NULL,
    `allocated_full_time_cost`  DECIMAL(19, 0) NOT NULL,
    `unallocated_full_time_cost` DECIMAL(19, 0) NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_COMPANY_MONTHLY_COST_MONTH` UNIQUE (`target_month`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '전사 월 정직원 비용 집계';

-- 손익 집계 월 마감
CREATE TABLE `tb_revenue_month_closing` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `target_month` DATE        NOT NULL,
    `closed`       BOOLEAN     NOT NULL,
    `closed_at`    DATETIME(6) NULL,
    `closed_by`    BIGINT      NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_REVENUE_MONTH_CLOSING_MONTH` UNIQUE (`target_month`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '손익 집계 월 마감';

-- 로그인 계정
CREATE TABLE `tb_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `employee_id`         BIGINT       NOT NULL,
    `username`            VARCHAR(100) NOT NULL,
    `password`            VARCHAR(255) NOT NULL,
    `password_changed_at` DATETIME(6)  NOT NULL,
    `enabled`             BOOLEAN      NOT NULL,
    `login_fail_count`    INT          NOT NULL DEFAULT 0,
    `last_login_at`       DATETIME(6)  NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_ACCOUNT_USERNAME` UNIQUE (`username`),
    CONSTRAINT `UK_ACCOUNT_EMPLOYEE_ID` UNIQUE (`employee_id`),
    CONSTRAINT `FK_ACCOUNT_EMPLOYEE_ID` FOREIGN KEY (`employee_id`) REFERENCES `tb_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '로그인 계정';

-- 권한
CREATE TABLE `tb_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(100) NOT NULL,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_PERMISSION_CODE` UNIQUE (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '권한';

-- 권한 그룹
CREATE TABLE `tb_permission_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(50)  NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `group_type`  VARCHAR(20)  NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '권한 그룹';

-- 권한 그룹별 부여 권한/범위
CREATE TABLE `tb_group_permission_grant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `permission_group_id` BIGINT      NOT NULL,
    `permission_id`       BIGINT      NOT NULL,
    `scope`               VARCHAR(30) NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_GROUP_PERMISSION_GRANT` UNIQUE (`permission_group_id`, `permission_id`, `scope`),
    CONSTRAINT `FK_GRANT_PERMISSION_GROUP_ID` FOREIGN KEY (`permission_group_id`) REFERENCES `tb_permission_group` (`id`),
    CONSTRAINT `FK_GRANT_PERMISSION_ID` FOREIGN KEY (`permission_id`) REFERENCES `tb_permission` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '권한 그룹별 부여 권한/범위';

-- 계정-권한 그룹 할당
CREATE TABLE `tb_account_group_assignment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id`          BIGINT NOT NULL,
    `permission_group_id` BIGINT NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_ACCOUNT_GROUP_ASSIGNMENT` UNIQUE (`account_id`, `permission_group_id`),
    CONSTRAINT `FK_ACCOUNT_GROUP_ACCOUNT_ID` FOREIGN KEY (`account_id`) REFERENCES `tb_account` (`id`),
    CONSTRAINT `FK_ACCOUNT_GROUP_PERMISSION_GROUP_ID` FOREIGN KEY (`permission_group_id`) REFERENCES `tb_permission_group` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '계정-권한 그룹 할당';

-- 알림
CREATE TABLE `tb_notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id`        BIGINT       NOT NULL,
    `title`             VARCHAR(120) NOT NULL,
    `description`       VARCHAR(500) NULL,
    `notification_type` VARCHAR(20)  NOT NULL,
    `is_read`           BOOLEAN      NOT NULL DEFAULT FALSE,
    `link`              VARCHAR(255) NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    INDEX `IDX_NOTIFICATION_ACCOUNT` (`account_id`, `is_read`, `created_at`),
    CONSTRAINT `FK_NOTIFICATION_ACCOUNT_ID` FOREIGN KEY (`account_id`) REFERENCES `tb_account` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '알림';

-- AI 어시스턴트 대화 세션
CREATE TABLE `tb_chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id`      BIGINT       NOT NULL,
    `conversation_id` VARCHAR(64)  NOT NULL,
    `title`           VARCHAR(100) NOT NULL,
    `favorite`        BOOLEAN      NOT NULL DEFAULT FALSE,
    `last_message_at` DATETIME(6)  NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `UK_CHAT_SESSION_CONVERSATION_ID` UNIQUE (`conversation_id`),
    INDEX `IDX_CHAT_SESSION_ACCOUNT` (`account_id`, `last_message_at`),
    CONSTRAINT `FK_CHAT_SESSION_ACCOUNT_ID` FOREIGN KEY (`account_id`) REFERENCES `tb_account` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 어시스턴트 대화 세션';

-- AI 어시스턴트 대화 메시지
CREATE TABLE `tb_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` BIGINT      NOT NULL,
    `role`       VARCHAR(20) NOT NULL,
    `content`    TEXT        NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    INDEX `IDX_CHAT_MESSAGE_SESSION_ID` (`session_id`),
    CONSTRAINT `FK_CHAT_MESSAGE_SESSION_ID` FOREIGN KEY (`session_id`) REFERENCES `tb_chat_session` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 어시스턴트 대화 메시지';

-- AI 대화 메모리 (Spring AI ChatMemoryRepository)
CREATE TABLE `tb_chat_memory_message` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `conversation_id` VARCHAR(64) NOT NULL,
    `seq`             INT         NOT NULL,
    `message_type`    VARCHAR(20) NOT NULL,
    `content`         TEXT        NOT NULL,

    PRIMARY KEY (`id`),
    INDEX `IDX_CHAT_MEMORY_CONVERSATION` (`conversation_id`, `seq`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 대화 메모리';

-- 주간 운영 보고서
CREATE TABLE `tb_weekly_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title`             VARCHAR(200) NOT NULL,
    `week_start`        DATE         NOT NULL,
    `week_end`          DATE         NOT NULL,
    `content`           MEDIUMTEXT   NOT NULL,
    `generator`         VARCHAR(20)  NOT NULL,
    `edited`            BOOLEAN      NOT NULL DEFAULT FALSE,
    `author_account_id` BIGINT       NOT NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    INDEX `IDX_WEEKLY_REPORT_WEEK` (`week_start`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '주간 운영 보고서';
