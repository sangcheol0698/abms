ALTER TABLE `tb_monthly_revenue_summary`
    ADD COLUMN `target_month` DATE NULL AFTER `lead_department_name`,
    ADD COLUMN `calculated_at` DATETIME(6) NULL AFTER `target_month`;

UPDATE `tb_monthly_revenue_summary`
SET `target_month` = DATE_SUB(`summary_date`, INTERVAL DAYOFMONTH(`summary_date`) - 1 DAY),
    `calculated_at` = COALESCE(`updated_at`, `created_at`);

DELETE FROM `tb_monthly_revenue_summary`
WHERE `deleted` = 1;

DELETE summary
FROM `tb_monthly_revenue_summary` summary
JOIN (
    SELECT `id`
    FROM (
        SELECT
            `id`,
            ROW_NUMBER() OVER (
                PARTITION BY `project_id`, `target_month`
                ORDER BY `summary_date` DESC, `id` DESC
            ) AS `row_number`
        FROM `tb_monthly_revenue_summary`
    ) ranked
    WHERE ranked.`row_number` > 1
) duplicate_summary ON duplicate_summary.`id` = summary.`id`;

ALTER TABLE `tb_monthly_revenue_summary`
    MODIFY COLUMN `target_month` DATE NOT NULL,
    MODIFY COLUMN `calculated_at` DATETIME(6) NOT NULL,
    DROP COLUMN `summary_date`;

ALTER TABLE `tb_monthly_revenue_summary`
    ADD CONSTRAINT `UK_MONTHLY_REVENUE_SUMMARY_PROJECT_MONTH` UNIQUE (`project_id`, `target_month`),
    ADD INDEX `IDX_MONTHLY_REVENUE_SUMMARY_TARGET_MONTH` (`target_month`),
    ADD INDEX `IDX_MONTHLY_REVENUE_SUMMARY_DEPT_MONTH` (`lead_department_id`, `target_month`);

CREATE TABLE IF NOT EXISTS `tb_revenue_month_closing` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `target_month`   DATE         NOT NULL,
    `closing_status` VARCHAR(20)  NOT NULL,
    `closed_at`      DATETIME(6)  NULL,
    `closed_by`      BIGINT       NULL,

    `created_at`     DATETIME(6)  NOT NULL,
    `updated_at`     DATETIME(6)  NOT NULL,
    `created_by`     BIGINT       NULL,
    `updated_by`     BIGINT       NULL,
    `deleted`        TINYINT(1)   NOT NULL,
    `deleted_at`     DATETIME(6)  NULL,
    `deleted_by`     BIGINT       NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_REVENUE_MONTH_CLOSING_TARGET_MONTH` UNIQUE (`target_month`),
    INDEX `IDX_REVENUE_MONTH_CLOSING_STATUS` (`closing_status`, `target_month`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
