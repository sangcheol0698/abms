ALTER TABLE `tb_party`
    DROP INDEX `UK_PARTY_NAME`;

ALTER TABLE `tb_party`
    ADD COLUMN `active_party_name` VARCHAR(50)
        GENERATED ALWAYS AS (
            CASE
                WHEN `deleted` = 0 THEN `party_name`
                ELSE NULL
            END
        ) STORED;

CREATE UNIQUE INDEX `UK_PARTY_ACTIVE_NAME` ON `tb_party` (`active_party_name`);
