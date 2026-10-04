-- 프로필 사진: 프리셋 아바타(avatar)를 없애고 사진 경로를 둔다. 사진이 없으면 화면에서 기본 아바타를 보여준다.
-- avatar 컬럼은 더 이상 쓰지 않는다. (새 DB 에서 데모 마이그레이션(V100)이 이 컬럼에 값을 넣으므로 남겨 두고 NULL 허용)
ALTER TABLE `tb_employee`
    MODIFY COLUMN `avatar` VARCHAR(40) NULL COMMENT '사용 안 함 (프로필 사진으로 대체)',
    ADD COLUMN `photo_path` VARCHAR(200) NULL COMMENT '프로필 사진 저장 경로' AFTER `avatar`;
