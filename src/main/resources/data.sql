-- ---------------------------------------------------------
-- 1. 조직도 (Department) - 1단계: 부서 생성 (리더는 일단 NULL)
-- ---------------------------------------------------------
-- [Level 1] Root Company
INSERT INTO tb_department (id, department_code, department_name, department_type, parent_department_id, leader_id, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 'ABC0000', '(주)애버커스', 'COMPANY', NULL, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [Level 2] 본부 (Division)
INSERT INTO tb_department (id, department_code, department_name, department_type, parent_department_id, leader_id, created_at, updated_at, created_by, updated_by, deleted) VALUES
(2, 'ABC1000', '경영기획본부', 'DIVISION', 1, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(3, 'ABC2000', '통신사업본부', 'DIVISION', 1, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(4, 'ABC3000', '미래사업본부', 'DIVISION', 1, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(5, 'ABC4000', '연구개발본부', 'DIVISION', 1, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [Level 3] 담당/연구소 (Group/Lab)
INSERT INTO tb_department (id, department_code, department_name, department_type, parent_department_id, leader_id, created_at, updated_at, created_by, updated_by, deleted) VALUES
(6, 'ABC4100', '기술연구소', 'LAB', 5, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(7, 'ABC3100', '전략사업담당', 'GROUP', 4, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(8, 'ABC3200', 'AI/Data사업담당', 'GROUP', 4, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(9, 'ABC2100', '통신이행담당', 'GROUP', 3, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(10, 'ABC2200', '경영빌링담당', 'GROUP', 3, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [Level 4] 팀 (Team)
INSERT INTO tb_department (id, department_code, department_name, department_type, parent_department_id, leader_id, created_at, updated_at, created_by, updated_by, deleted) VALUES
(100, 'ABC4101', '플랫폼연구개발팀', 'TEAM', 6, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(101, 'ABC3101', '컨버전스사업팀', 'TEAM', 7, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(102, 'ABC3102', '핀테크사업팀', 'TEAM', 7, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(103, 'ABC3103', 'IoT융합사업팀', 'TEAM', 7, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(104, 'ABC3104', '융합서비스사업팀', 'TEAM', 7, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(105, 'ABC3201', 'Data플랫폼사업팀', 'TEAM', 8, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(106, 'ABC3202', '지능플랫폼사업팀', 'TEAM', 8, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(107, 'ABC3203', 'Data서비스운영팀', 'TEAM', 8, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(108, 'ABC3204', 'AX플랫폼사업팀', 'TEAM', 8, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(109, 'ABC3205', 'UX STUDIO TF', 'TEAM', 4, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(110, 'ABC2101', '고객정보팀', 'TEAM', 9, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(111, 'ABC2102', '가입정보팀', 'TEAM', 9, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(112, 'ABC2103', '빌링시스템팀', 'TEAM', 9, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(113, 'ABC2104', '영업정보팀', 'TEAM', 9, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(114, 'ABC2105', '기반기술팀', 'TEAM', 9, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(115, 'ABC2201', '경영플랫폼팀', 'TEAM', 10, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(116, 'ABC2202', 'CRM팀', 'TEAM', 10, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(117, 'ABC2203', '경영정보팀', 'TEAM', 10, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(118, 'ABC2204', 'NMS사업팀', 'TEAM', 10, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(119, 'ABC2205', '융합데이터분석팀', 'TEAM', 10, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- ---------------------------------------------------------
-- 2. 협력사 (Party)
-- ---------------------------------------------------------
INSERT INTO tb_party (id, party_name, ceo_name, sales_rep_name, sales_rep_phone, sales_rep_email, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, '네이버클라우드', '이준호', '김영업', '010-1234-5678', 'sales@navercloud.com', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(2, '삼성SDS', '박철수', '최담당', '010-2345-6789', 'contact@samsungsds.com', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(3, '카카오', '정여진', '이매니저', '010-3456-7890', 'partner@kakao.com', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(4, 'LG CNS', '강대리', '송과장', '010-4567-8901', 'business@lgcns.com', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(5, 'SK텔레콤', '윤사장', '임대리', '010-5678-9012', 'sales@sktelecom.com', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(6, '토스', '토사장', '토대리', '010-5600-9012', 'sales@toss.com', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- ---------------------------------------------------------
-- 3. 직원 (Employee)
-- ---------------------------------------------------------
-- [임원진]
INSERT INTO tb_employee (id, department_id, email_address, name, position, type, status, grade, avatar, join_date, birth_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 1, 'lyt@abms.co', '임회장', 'CHAIRMAN', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SKY_GLOW', '2001-01-30', '1960-05-12', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(2, 1, 'chaesungsoo@abms.co', '채사장', 'PRESIDENT', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SUNSET_BREEZE', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(3, 3, 'ydp@abms.co', '유부사장', 'VICE_PRESIDENT', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'CORAL_SPARK', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(4, 2, 'chin@abms.co', '진부사장', 'VICE_PRESIDENT', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'CORAL_SPARK', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(5, 4, 'jhkim@abms.co', '김상무', 'MANAGING_DIRECTOR', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'FOREST_MINT', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(6, 10, 'hades@abms.co', '허담당', 'DIRECTOR', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'FOREST_MINT', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(7, 4, 'hyosang@abms.co', '홍이사', 'TECHNICAL_DIRECTOR', 'FULL_TIME', 'ACTIVE', 'EXPERT', 'FOREST_MINT', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(8, 4, 'harryson@abms.co', '손이사', 'TECHNICAL_DIRECTOR', 'FULL_TIME', 'ACTIVE', 'EXPERT', 'FOREST_MINT', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(9, 5, 'yjeepark@abms.co', '박상무', 'MANAGING_DIRECTOR', 'FULL_TIME', 'ACTIVE', 'EXPERT', 'FOREST_MINT', '2007-06-01', '1968-10-10', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [중간 관리자 - 팀장급 (ID 10 ~ 19)]
INSERT INTO tb_employee (id, department_id, email_address, name, position, type, status, grade, avatar, join_date, birth_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(10, 102, 'ysfl@abms.co', '고팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'GOLDEN_RAY', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(11, 105, 'krinsa@abms.co', '류팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SAGE_GUARD', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(12, 114, 'sh3817@abms.co', '김팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SAGE_GUARD', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(13, 106, 'babopuding@abms.co', '금팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SAGE_GUARD', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(14, 118, 'zase98@abms.co', '박팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SAGE_GUARD', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(15, 101, 'uhchae@abms.co', '채팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SAGE_GUARD', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(16, 111, 'higo100@abms.co', '양팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SAGE_GUARD', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(17, 110, 'ladder@abms.co', '김팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SAGE_GUARD', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(18, 107, 'jyc939393@abms.co', '지팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'SKY_GLOW', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(19, 119, 'sogarib@abms.co', '봉팀장', 'TEAM_LEADER', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'COBALT_WAVE', '2026-02-05', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [실무진 - 책임급]
INSERT INTO tb_employee (id, department_id, email_address, name, position, type, status, grade, avatar, join_date, birth_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(20, 118, 'cidlist@abms.co', '권책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'AQUA_SPLASH', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(21, 117, 'bsaba@abms.co', '김책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2002-03-01', '1962-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(22, 2, 'azure00@abms.co', '김책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2002-03-01', '1969-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(23, 110, 'breadco@abms.co', '류책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2002-03-01', '1971-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(24, 7, 'ojpark2010@abms.co', '박책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2012-04-11', '1979-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(25, 118, 'needless2@abms.co', '오책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2016-03-13', '1988-08-08', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(26, 113, 'springbi@abms.co', '임책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2020-02-19', '1977-04-04', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(27, 112, 'dogbank@abms.co', '이책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2009-12-11', '1980-03-03', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(28, 108, 'myride@abms.co', '장책임', 'PRINCIPAL', 'FULL_TIME', 'ACTIVE', 'SENIOR', 'BLOSSOM_SMILE', '2012-03-01', '1972-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [실무진 - 선임급]
INSERT INTO tb_employee (id, department_id, email_address, name, position, type, status, grade, avatar, join_date, birth_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(29, 109, 'jason0814@abms.co', '방선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'BLOSSOM_SMILE', '2020-03-01', '1993-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(30, 115, 'sdi4931@abms.co', '신선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'MIDNIGHT_WINK', '2020-03-01', '1996-10-27', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(31, 111, 'wessuh11@abms.co', '서선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'MIDNIGHT_WINK', '2021-09-05', '1995-01-15', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(32, 113, 'woong22@abms.co', '이선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'MIDNIGHT_WINK', '2021-09-06', '1992-08-20', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(33, 103, 'dyd5795@abms.co', '장선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'MIDNIGHT_WINK', '2021-09-06', '1991-10-14', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(34, 101, 'hoonjoo@abms.co', '장선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'ORANGE_BURST', '2025-07-02', '1998-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(35, 116, '3097mk@abms.co', '최선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'LAVENDER_MOON', '2024-03-01', '1987-06-12', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(36, 117, 'moomstone2@abms.co', '허선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'SAGE_GUARD', '2019-05-10', '1991-01-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(37, 119, 'mjs6350@abms.co', '문선임', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'MID_LEVEL', 'LAVENDER_MOON', '2017-01-06', '1990-07-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [실무진 - 사원급]
INSERT INTO tb_employee (id, department_id, email_address, name, position, type, status, grade, avatar, join_date, birth_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(38, 104, 'cy950315@abms.co', '오사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'COBALT_WAVE', '2023-11-05', '1995-03-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(39, 103, 'dlwltn6604@abms.co', '이사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'COBALT_WAVE', '2024-11-21', '1999-02-01', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(40, 103, 'woouk747@abms.co', '이사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'COBALT_WAVE', '2024-09-21', '1998-02-12', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(41, 101, 'heeseokoh@abms.co', '오사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'COBALT_WAVE', '2025-09-21', '1997-12-08', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(42, 104, 'dhflgkwls@abms.co', '윤사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'COBALT_WAVE', '2023-09-21', '1996-04-17', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(43, 110, 'vicent77@abms.co', '임사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'SKY_GLOW', '2024-11-21', '1996-09-29', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(44, 110, 'ABC3202@abms.co', '정사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'SKY_GLOW', '2024-11-21', '1996-10-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(45, 2, 'zyz9229@abms.co', '제사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'SKY_GLOW', '2022-11-21', '1996-05-05', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(46, 113, 'rainbow2774@abms.co', '차사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'SKY_GLOW', '2022-11-21', '1996-06-16', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(47, 105, 'sychoi9089@abms.co', '최사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'SKY_GLOW', '2022-11-21', '1996-07-03', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(48, 100, 'kidsan20@abms.co', '윤사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'SKY_GLOW', '2023-11-05', '1996-02-22', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(49, 115, 'sdi4932@abms.co', '신사원', 'SENIOR_ASSOCIATE', 'FULL_TIME', 'ACTIVE', 'JUNIOR', 'SKY_GLOW', '2023-11-05', '2002-12-07', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- ---------------------------------------------------------
-- 4. 조직도 리더 매핑 (Update Leader) - 3단계: 직원 생성 후 리더 지정
-- ---------------------------------------------------------
-- [Root]
UPDATE tb_department SET leader_id = 1 WHERE id = 1;

-- [Division]
UPDATE tb_department SET leader_id = 4 WHERE id = 2; -- 경영기획본부 (진부사장)
UPDATE tb_department SET leader_id = 3 WHERE id = 3; -- 통신사업본부 (유부사장)
UPDATE tb_department SET leader_id = 5 WHERE id = 4; -- 미래사업본부 (김상무)

-- [Group]
UPDATE tb_department SET leader_id = 6 WHERE id = 10; -- 경영빌링담당 (허담당)

-- [Team]
UPDATE tb_department SET leader_id = 15 WHERE id = 101; -- 컨버전스사업팀 (채팀장)
UPDATE tb_department SET leader_id = 10 WHERE id = 102; -- 핀테크사업팀 (고팀장)
UPDATE tb_department SET leader_id = 11 WHERE id = 105; -- Data플랫폼사업팀 (류팀장)
UPDATE tb_department SET leader_id = 13 WHERE id = 106; -- 지능플랫폼사업팀 (금팀장)
UPDATE tb_department SET leader_id = 18 WHERE id = 107; -- Data서비스운영팀 (지팀장)
UPDATE tb_department SET leader_id = 17 WHERE id = 110; -- 고객정보팀 (김팀장)
UPDATE tb_department SET leader_id = 16 WHERE id = 111; -- 가입정보팀 (양팀장)
UPDATE tb_department SET leader_id = 12 WHERE id = 114; -- 기반기술팀 (김팀장)
UPDATE tb_department SET leader_id = 14 WHERE id = 118; -- NMS사업팀 (박팀장)
UPDATE tb_department SET leader_id = 19 WHERE id = 119; -- 융합데이터분석팀 (봉팀장)


-- ---------------------------------------------------------
-- 5. 비용 정책 (Employee Cost Policy)
-- ---------------------------------------------------------
INSERT INTO tb_employee_cost_policy (id, apply_year, employee_type, overhead_rate, sga_rate) VALUES
(1, 2025, 'FULL_TIME', 0.1, 0.07),
(2, 2026, 'FULL_TIME', 0.1, 0.05),
(3, 2025, 'FREELANCER', 0.02, 0.03),
(4, 2026, 'FREELANCER', 0.00, 0.03);


-- ---------------------------------------------------------
-- 6. 급여 정보 (Payroll) - 2026년 기준 (팀장~사원 전체)
-- ---------------------------------------------------------
-- [임원진] (ID 1 ~ 9)
INSERT INTO tb_payroll (employee_id, annual_salary, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 300000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 임회장
(2, 250000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 채사장
(3, 200000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 유부사장
(4, 200000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 진부사장
(5, 160000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 김상무
(6, 140000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 허담당
(7, 130000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 홍이사
(8, 130000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 손이사
(9, 160000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0); -- 박상무

-- [팀장급] (ID 10 ~ 19) : 연봉 약 9,600 ~ 1.05억
INSERT INTO tb_payroll (employee_id, annual_salary, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(10, 105000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 고팀장
(11, 102000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 류팀장
(12, 98000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),  -- 김팀장
(13, 99000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),  -- 금팀장
(14, 97000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),  -- 박팀장
(15, 101000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 채팀장
(16, 96000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),  -- 양팀장
(17, 96000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),  -- 김팀장
(18, 98500000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),  -- 지팀장
(19, 96000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);  -- 봉팀장

-- [책임급] (ID 20 ~ 28) : 연봉 약 7,200 ~ 8,000
INSERT INTO tb_payroll (employee_id, annual_salary, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(20, 78000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 권책임
(21, 76000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 김책임
(22, 80000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 김책임(본부)
(23, 79000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 류책임
(24, 75000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 박책임
(25, 74000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 오책임
(26, 73000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 임책임
(27, 72000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 이책임
(28, 42000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0); -- 장책임 (요청: 4200만)

-- [선임급] (ID 29 ~ 37) : 연봉 약 5,400 ~ 6,000
INSERT INTO tb_payroll (employee_id, annual_salary, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(29, 58000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 방선임
(30, 56000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 신선임
(31, 55000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 서선임
(32, 57000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 이선임
(33, 59000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 장선임
(34, 54000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 장선임
(35, 60000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 최선임
(36, 58000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 허선임
(37, 59500000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0); -- 문선임

-- [사원급] (ID 38 ~ 49) : 연봉 약 3,800 ~ 4,500
INSERT INTO tb_payroll (employee_id, annual_salary, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(38, 40000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 오사원
(39, 38000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 이사원
(40, 39000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 이사원
(41, 38000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 오사원
(42, 41000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 윤사원
(43, 40000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 임사원
(44, 40000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 정사원
(45, 43000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 제사원
(46, 44000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 차사원
(47, 45000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 최사원
(48, 41000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0), -- 윤사원
(49, 48000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0); -- 신사원


-- ---------------------------------------------------------
-- 7. 프로젝트 (Project)
-- 컬럼 매핑: lead_department_id 추가, contract_amount, project_code, project_name, project_status
-- ---------------------------------------------------------

-- [Project A] 차세대 AI 플랫폼 구축 (주관: 100 플랫폼연구개발팀, PM: 49 신사원)
INSERT INTO tb_project (id, party_id, lead_department_id, project_code, project_name, project_description, project_status, contract_amount, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 1, 100, 'PROJ-2026-AI', '2026 차세대 AI 플랫폼 구축', 'LLM 기반 사내 지식 관리 시스템 구축', 'IN_PROGRESS', 1000000000, '2026-01-01', '2026-06-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [Project B] 토스 결제 시스템 고도화 (주관: 102 핀테크사업팀, PM: 10 고팀장)
INSERT INTO tb_project (id, party_id, lead_department_id, project_code, project_name, project_description, project_status, contract_amount, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(2, 6, 102, 'PROJ-2026-TOSS', '2026 토스 결제 시스템 고도화', '대용량 트래픽 처리를 위한 결제 모듈 리팩토링', 'IN_PROGRESS', 500000000, '2026-02-01', '2026-11-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [Project C] SKT 빌링 시스템 유지보수 (주관: 112 빌링시스템팀, PM: 12 김팀장)
INSERT INTO tb_project (id, party_id, lead_department_id, project_code, project_name, project_description, project_status, contract_amount, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(3, 5, 112, 'PROJ-2026-SKT', '2026 SKT 빌링 시스템 유지보수', '통신 요금 정산 시스템 연간 운영 및 유지보수', 'IN_PROGRESS', 1200000000, '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- ---------------------------------------------------------
-- 8. 매출 계획 (Project Revenue Plan)
-- 컬럼 매핑: plan_sequence, revenue_type, amount, is_issued
-- ---------------------------------------------------------

-- [Project A] AI 플랫폼 (3회 분할)
INSERT INTO tb_project_revenue_plan (project_id, plan_sequence, revenue_date, revenue_type, amount, memo, is_issued, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 1, '2026-01-15', 'DOWN_PAYMENT', 300000000, '선수 착수금', 1, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(1, 2, '2026-02-10', 'INTERMEDIATE_PAYMENT', 400000000, '중도금', 1, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(1, 3, '2026-06-30', 'BALANCE_PAYMENT', 300000000, '최종 잔금', 0, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [Project B] 토스 고도화 (3회 분할)
INSERT INTO tb_project_revenue_plan (project_id, plan_sequence, revenue_date, revenue_type, amount, memo, is_issued, created_at, updated_at, created_by, updated_by, deleted) VALUES
(2, 1, '2026-02-15', 'DOWN_PAYMENT', 100000000, '선수 착수금', 1, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(2, 2, '2026-07-20', 'INTERMEDIATE_PAYMENT', 250000000, '중도금', 0, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(2, 3, '2026-11-30', 'BALANCE_PAYMENT', 150000000, '검수 완료금', 0, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);

-- [Project C] SKT 유지보수 (3회 분할 예시)
INSERT INTO tb_project_revenue_plan (project_id, plan_sequence, revenue_date, revenue_type, amount, memo, is_issued, created_at, updated_at, created_by, updated_by, deleted) VALUES
(3, 1, '2026-01-31', 'INTERMEDIATE_PAYMENT', 300000000, '1분기 유지보수료', 1, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(3, 2, '2026-06-30', 'INTERMEDIATE_PAYMENT', 300000000, '2분기 유지보수료', 0, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
(3, 3, '2026-09-30', 'INTERMEDIATE_PAYMENT', 300000000, '3분기 유지보수료', 0, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- ---------------------------------------------------------
-- 9. 인력 할당 (Project Assignment)
-- 컬럼 매핑: assignment_role (기존 role 아님 주의)
-- ---------------------------------------------------------

-- [Project A] AI 플랫폼 (4명 투입)
-- 1. 신사원(49): DEV, 전 기간
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 49, 'DEV', '2026-01-01', '2026-06-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 2. 장책임(28): DEV, 전 기간
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 28, 'DEV', '2026-01-01', '2026-06-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 3. 신선임(30): DEV, 1월~4월
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 30, 'DEV', '2026-01-01', '2026-04-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 4. 제사원(45): DEV, 2월~6월
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(1, 45, 'DEV', '2026-02-01', '2026-06-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- [Project B] 토스 고도화 (3명 투입)
-- 1. 고팀장(10): PM, 전 기간
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(2, 10, 'PM', '2026-02-01', '2026-11-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 2. 권책임(20): PL, 전 기간
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(2, 20, 'PL', '2026-02-01', '2026-11-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 3. 윤사원(48): DEV, 3월~10월
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(2, 48, 'DEV', '2026-03-01', '2026-10-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- [Project C] SKT 유지보수 (4명 투입)
-- 1. 김팀장(12): PM, 1년 전체
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(3, 12, 'PM', '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 2. 이책임(27): PL, 1년 전체
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(3, 27, 'PL', '2026-01-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 3. 서선임(31): DEV, 상반기
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(3, 31, 'DEV', '2026-01-01', '2026-06-30', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
-- 4. 임사원(43): DEV, 하반기
INSERT INTO tb_project_assignment (project_id, employee_id, assignment_role, start_date, end_date, created_at, updated_at, created_by, updated_by, deleted) VALUES
(3, 43, 'DEV', '2026-07-01', '2026-12-31', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);


-- ---------------------------------------------------------
-- 10. 월 매출 집계 (Monthly Revenue Summary)
-- ---------------------------------------------------------




-- ---------------------------------------------------------
-- 11. 알림 (Notification)
-- ---------------------------------------------------------
INSERT INTO tb_notification (
    id, notification_title, notification_description, notification_type, is_read, link_url,
    created_at, updated_at, created_by, updated_by, deleted
) VALUES
    (1, '조직 구조 변경', '부서 목록에서 경영기획실이 신설되었습니다.', 'INFO', 0, '/', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
    (2, '직원 정보 갱신', '인사팀이 직원 정보를 최신화했습니다.', 'SUCCESS', 0, '/employees', NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0),
    (3, '근태 정책 안내', '다음 주부터 신규 근태 정책이 적용됩니다.', 'WARNING', 1, NULL, NOW(), NOW(), 'ABMS_INIT', 'ABMS_INIT', 0);
