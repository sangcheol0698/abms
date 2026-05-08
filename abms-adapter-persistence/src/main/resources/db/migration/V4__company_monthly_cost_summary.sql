CREATE TABLE IF NOT EXISTS `tb_company_monthly_cost_summary` (
    `id`                                  BIGINT      NOT NULL AUTO_INCREMENT,
    `target_month`                        DATE        NOT NULL,
    `calculated_at`                       DATETIME(6) NOT NULL,
    `total_full_time_employee_cost`       DECIMAL(19, 0) NOT NULL,
    `allocated_full_time_employee_cost`   DECIMAL(19, 0) NOT NULL,
    `unallocated_full_time_employee_cost` DECIMAL(19, 0) NOT NULL,

    `created_at`                          DATETIME(6) NOT NULL,
    `updated_at`                          DATETIME(6) NOT NULL,
    `created_by`                          BIGINT      NULL,
    `updated_by`                          BIGINT      NULL,
    `deleted`                             TINYINT(1)  NOT NULL,
    `deleted_at`                          DATETIME(6) NULL,
    `deleted_by`                          BIGINT      NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `UK_COMPANY_MONTHLY_COST_SUMMARY_TARGET_MONTH` UNIQUE (`target_month`),
    INDEX `IDX_COMPANY_MONTHLY_COST_SUMMARY_TARGET_MONTH` (`target_month`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
