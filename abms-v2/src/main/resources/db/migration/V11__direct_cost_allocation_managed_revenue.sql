-- 투입률: 한 직원을 여러 프로젝트에 나눠 투입할 수 있도록 기간 비율에 곱하는 비율(%)
ALTER TABLE `tb_project_assignment`
    ADD COLUMN `allocation_rate` INT NOT NULL DEFAULT 100 COMMENT '투입률(%) 1~100' AFTER `end_date`;

-- 프로젝트 직접비 (외주 용역비, 장비·자재, 라이선스, 출장비 등 인건비 외 비용)
CREATE TABLE `tb_project_expense` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id`   BIGINT         NOT NULL,
    `expense_date` DATE           NOT NULL COMMENT '귀속일 (이 날짜가 속한 월의 비용으로 집계)',
    `category`     VARCHAR(30)    NOT NULL,
    `amount`       DECIMAL(19, 0) NOT NULL COMMENT '공급가액',
    `description`  VARCHAR(100)   NOT NULL,
    `memo`         VARCHAR(255)   NULL,

    `created_at`  DATETIME(6) NOT NULL,
    `updated_at`  DATETIME(6) NOT NULL,
    `created_by`  BIGINT      NULL,
    `updated_by`  BIGINT      NULL,
    `deleted`     BOOLEAN     NOT NULL DEFAULT FALSE,
    `deleted_at`  DATETIME(6) NULL,
    `deleted_by`  BIGINT      NULL,

    PRIMARY KEY (`id`),

    INDEX `IDX_PROJECT_EXPENSE_PROJECT_ID` (`project_id`),
    INDEX `IDX_PROJECT_EXPENSE_DATE` (`expense_date`),
    CONSTRAINT `FK_PROJECT_EXPENSE_PROJECT_ID` FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '프로젝트 직접비';

-- 월 손익 집계: 진행 기준(관리) 매출, 비용 내역(인건비/직접비)
-- 기존 행은 다음 재집계 전까지 청구 매출 = 관리 매출, 비용 전액 = 인건비로 둔다.
ALTER TABLE `tb_monthly_revenue_summary`
    ADD COLUMN `managed_revenue_amount` DECIMAL(19, 0) NOT NULL DEFAULT 0 COMMENT '진행 기준 매출 (계약금액 기간 일할)' AFTER `revenue_amount`,
    ADD COLUMN `labor_cost_amount`      DECIMAL(19, 0) NOT NULL DEFAULT 0 COMMENT '인건비 (직원 월 원가 × 투입 M/M)' AFTER `cost_amount`,
    ADD COLUMN `direct_cost_amount`     DECIMAL(19, 0) NOT NULL DEFAULT 0 COMMENT '직접비' AFTER `labor_cost_amount`;

UPDATE `tb_monthly_revenue_summary`
SET `managed_revenue_amount` = `revenue_amount`,
    `labor_cost_amount`      = `cost_amount`;
