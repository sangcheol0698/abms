# ABMS v2 ERD

`abms-v2/src/main/resources/db/migration` 의 Flyway 마이그레이션(V1~V11)을 모두 적용한 뒤의 스키마를 그린 문서입니다.
스키마의 기준은 마이그레이션 SQL이므로, 테이블이나 컬럼을 바꾸는 마이그레이션을 추가할 때 이 문서도 함께 고쳐 주세요.

## 읽는 법

- **실선**(`──`)은 DB에 실제 FK 제약이 걸린 관계이고, **점선**(`┄┄`)은 FK 없이 ID 값으로만 참조하는 논리적 관계입니다.
- 공통 컬럼은 다이어그램에서 생략했습니다. 아래 표에 정리되어 있습니다.
- `PK`, `FK`, `UK`(유니크) 표시가 붙어 있고, `UK`는 복합 유니크의 구성 컬럼에도 붙어 있습니다.

### 공통 컬럼 (감사·소프트 삭제)

`tb_audit_log`, `tb_audit_log_change`, `tb_chat_memory_message`, `tb_notice_receipt` 를 뺀 모든 테이블에 있습니다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `created_at` / `updated_at` | `DATETIME(6)` | 생성·수정 시각 |
| `created_by` / `updated_by` | `BIGINT` NULL | 생성·수정한 계정(`tb_account.id`), 시스템 작업이면 NULL |
| `deleted` | `BOOLEAN` | 소프트 삭제 여부 |
| `deleted_at` / `deleted_by` | `DATETIME(6)` / `BIGINT` NULL | 삭제 시각·삭제한 계정 |

### `active_*` 생성 컬럼

`tb_department.active_code`, `tb_project.active_code`, `tb_party.active_name`, `tb_site.active_name`, `tb_project_revenue_plan.active_sequence` 는
`deleted = 0` 일 때만 값을 갖는 STORED 생성 컬럼입니다. 여기에 유니크 제약을 걸어, 삭제된 행과는 코드나 이름이 겹쳐도 되도록 했습니다.

---

## 1. 전체 관계도

테이블 사이의 관계만 보여 주는 지도입니다. 컬럼은 아래 도메인별 다이어그램에서 볼 수 있습니다.

```mermaid
erDiagram
    tb_site ||--o{ tb_department : "근무 사업장"
    tb_department |o--o{ tb_department : "상위 부서"
    tb_department ||--o{ tb_employee : "소속"
    tb_employee |o--o{ tb_department : "부서장"
    tb_employee ||--o{ tb_payroll : "연봉 이력"
    tb_employee ||--o{ tb_position_history : "직급 이력"
    tb_employee ||--o{ tb_employee_monthly_cost : "월 원가"
    tb_employee ||--o| tb_account : "로그인 계정"

    tb_party ||--o{ tb_party_contact : "담당자"
    tb_party ||--o{ tb_project : "발주"
    tb_department ||--o{ tb_project : "주관 부서"
    tb_project ||--o{ tb_project_revenue_plan : "매출 계획"
    tb_project ||--o{ tb_project_assignment : "투입"
    tb_employee ||--o{ tb_project_assignment : "투입"
    tb_project ||--o{ tb_project_expense : "직접비"
    tb_project ||..o{ tb_monthly_revenue_summary : "월 손익"

    tb_account ||--o{ tb_account_group_assignment : "할당"
    tb_permission_group ||--o{ tb_account_group_assignment : "할당"
    tb_permission_group ||--o{ tb_group_permission_grant : "부여"
    tb_permission ||--o{ tb_group_permission_grant : "부여"

    tb_account ||--o{ tb_notification : "수신"
    tb_notice ||--o{ tb_notice_receipt : "수신 상태"
    tb_account ||..o{ tb_notice_receipt : "수신자"
    tb_account ||--o{ tb_chat_session : "대화"
    tb_chat_session ||--o{ tb_chat_message : "메시지"
    tb_audit_log ||--o{ tb_audit_log_change : "변경 상세"
```

관계선이 없는 테이블은 아래와 같습니다. 단독으로 쓰이거나, ID나 다형 키로만 참조합니다.

| 테이블 | 참조 방식 |
| --- | --- |
| `tb_employee_cost_policy` | `tb_employee.type` 과 적용 연도로 조회 |
| `tb_company_monthly_cost_summary` | 월(`target_month`) 단위 전사 집계 |
| `tb_revenue_month_closing` | 월 단위 마감, `closed_by` 는 계정 ID |
| `tb_weekly_report` | `author_account_id` 는 계정 ID |
| `tb_attachment` | `owner_type`(PROJECT / PARTY) + `owner_id` 다형 참조 |
| `tb_audit_log` | `entity_type` + `entity_id` 다형 참조, `actor_account_id` 는 계정 ID |
| `tb_chat_memory_message` | `conversation_id` 로 `tb_chat_session` 과 연결 (Spring AI 메모리) |

---

## 2. 조직·인사

```mermaid
erDiagram
    tb_site ||--o{ tb_department : "근무 사업장"
    tb_department |o--o{ tb_department : "상위 부서"
    tb_department ||--o{ tb_employee : "소속"
    tb_employee |o--o{ tb_department : "부서장"
    tb_employee ||--o{ tb_payroll : "연봉 이력"
    tb_employee ||--o{ tb_position_history : "직급 이력"

    tb_site {
        bigint id PK
        varchar name UK "active_name 기준"
        varchar site_type "본사·지사·연구소 등"
        varchar phone
        varchar zip_code
        varchar address
        varchar address_detail
        decimal latitude
        decimal longitude
        text memo
    }

    tb_department {
        bigint id PK
        varchar code UK "active_code 기준"
        varchar name
        varchar type
        varchar description
        bigint parent_id FK "NULL = 최상위"
        bigint site_id FK
        bigint leader_employee_id FK
    }

    tb_employee {
        bigint id PK
        bigint department_id FK
        varchar name
        varchar email UK
        varchar phone
        date join_date
        date career_start_date "총 경력 산정"
        date birth_date
        varchar position "직급"
        varchar type "고용 유형"
        varchar status
        varchar grade "등급"
        varchar job "직무"
        varchar skills
        varchar work_type "근무 형태"
        varchar avatar "사용 안 함"
        varchar photo_path "프로필 사진"
        date resignation_date
        text memo
    }

    tb_payroll {
        bigint id PK
        bigint employee_id FK
        decimal annual_salary
        date start_date
        date end_date "NULL = 현재"
    }

    tb_position_history {
        bigint id PK
        bigint employee_id FK
        varchar position
        varchar grade
        date start_date
        date end_date "NULL = 현재"
    }
```

---

## 3. 협력사·프로젝트

```mermaid
erDiagram
    tb_party ||--o{ tb_party_contact : "담당자"
    tb_party ||--o{ tb_project : "발주"
    tb_department ||--o{ tb_project : "주관 부서"
    tb_project ||--o{ tb_project_revenue_plan : "매출 계획"
    tb_project ||--o{ tb_project_assignment : "투입"
    tb_employee ||--o{ tb_project_assignment : "투입"
    tb_project ||--o{ tb_project_expense : "직접비"
    tb_project ||..o{ tb_attachment : "owner_type = PROJECT"
    tb_party ||..o{ tb_attachment : "owner_type = PARTY"

    tb_party {
        bigint id PK
        varchar name UK "active_name 기준"
        varchar party_type "기본 CLIENT"
        varchar business_number "사업자등록번호"
        varchar industry
        varchar ceo_name
        varchar phone
        varchar zip_code
        varchar address
        varchar address_detail
        decimal latitude
        decimal longitude
        varchar website
        varchar sales_rep_name "사용 안 함"
        varchar sales_rep_phone "사용 안 함"
        varchar sales_rep_email "사용 안 함"
        text memo
    }

    tb_party_contact {
        bigint id PK
        bigint party_id FK
        varchar name
        varchar role "SALES·CONTRACT·BILLING·TECH·ETC"
        varchar title "부서·직책"
        varchar phone
        varchar email
        varchar memo
        boolean is_primary "대표 담당자"
    }

    tb_project {
        bigint id PK
        bigint party_id FK
        bigint lead_department_id FK
        varchar code UK "active_code 기준"
        varchar name
        varchar description
        varchar status
        decimal contract_amount
        date start_date
        date end_date
        varchar work_place "수행 장소"
        varchar work_zip_code
        varchar work_address
        varchar work_address_detail
        decimal work_latitude
        decimal work_longitude
    }

    tb_project_revenue_plan {
        bigint id PK
        bigint project_id FK,UK
        int plan_sequence UK "active_sequence 기준"
        date revenue_date
        varchar revenue_type
        decimal amount
        boolean issued "청구 발행 여부"
        varchar memo
    }

    tb_project_assignment {
        bigint id PK
        bigint project_id FK
        bigint employee_id FK
        varchar assignment_role
        date start_date
        date end_date
        int allocation_rate "투입률 1~100%"
    }

    tb_project_expense {
        bigint id PK
        bigint project_id FK
        date expense_date "귀속일"
        varchar category
        decimal amount "공급가액"
        varchar description
        varchar memo
    }

    tb_attachment {
        bigint id PK
        varchar owner_type "PROJECT·PARTY"
        bigint owner_id
        varchar category "CONTRACT·INVOICE·DELIVERABLE·ETC"
        varchar original_name
        varchar stored_path "저장소 상대 경로"
        varchar content_type
        bigint size
    }

    tb_department {
        bigint id PK
    }

    tb_employee {
        bigint id PK
    }
```

---

## 4. 원가·손익 집계

손익 집계 테이블은 FK 없이 프로젝트와 부서의 ID, 코드, 이름을 **집계 시점 값으로 복사해** 저장합니다.
원본이 바뀌거나 삭제되어도 마감된 월의 숫자가 유지되도록 하기 위해서입니다.

```mermaid
erDiagram
    tb_employee ||--o{ tb_employee_monthly_cost : "월 원가"
    tb_employee_cost_policy ||..o{ tb_employee_monthly_cost : "연도·고용유형별 비율 적용"
    tb_project ||..o{ tb_monthly_revenue_summary : "project_id"
    tb_department ||..o{ tb_monthly_revenue_summary : "lead_department_id"

    tb_employee_cost_policy {
        bigint id PK
        int apply_year UK
        varchar employee_type UK
        decimal overhead_rate "제경비율"
        decimal sga_rate "판관비율"
    }

    tb_employee_monthly_cost {
        bigint id PK
        bigint employee_id FK,UK
        date target_month UK
        decimal monthly_salary "월 기본급"
        decimal overhead_cost "제경비"
        decimal sga_cost "판관비"
        decimal total_cost
    }

    tb_monthly_revenue_summary {
        bigint id PK
        bigint project_id UK "FK 없음"
        varchar project_code "스냅샷"
        varchar project_name "스냅샷"
        bigint lead_department_id "FK 없음"
        varchar lead_department_name "스냅샷"
        date target_month UK
        datetime calculated_at
        decimal revenue_amount "청구 기준 매출"
        decimal managed_revenue_amount "진행 기준 매출"
        decimal cost_amount "총비용"
        decimal labor_cost_amount "인건비"
        decimal direct_cost_amount "직접비"
        decimal profit_amount
    }

    tb_company_monthly_cost_summary {
        bigint id PK
        date target_month UK
        datetime calculated_at
        decimal total_full_time_cost
        decimal allocated_full_time_cost
        decimal unallocated_full_time_cost
    }

    tb_revenue_month_closing {
        bigint id PK
        date target_month UK
        boolean closed
        datetime closed_at
        bigint closed_by "계정 ID"
    }

    tb_employee {
        bigint id PK
        varchar type "고용 유형"
    }

    tb_project {
        bigint id PK
    }

    tb_department {
        bigint id PK
    }
```

---

## 5. 계정·권한

권한은 `계정 → 권한 그룹 → (권한, 범위)` 구조입니다. 자세한 규칙은 [권한가이드](권한가이드.md)를 참고하세요.

```mermaid
erDiagram
    tb_employee ||--o| tb_account : "로그인 계정"
    tb_account ||--o{ tb_account_group_assignment : "할당"
    tb_permission_group ||--o{ tb_account_group_assignment : "할당"
    tb_permission_group ||--o{ tb_group_permission_grant : "부여"
    tb_permission ||--o{ tb_group_permission_grant : "부여"

    tb_account {
        bigint id PK
        bigint employee_id FK,UK
        varchar username UK
        varchar password "해시"
        datetime password_changed_at
        boolean enabled
        int login_fail_count
        datetime last_login_at
    }

    tb_permission_group {
        bigint id PK
        varchar name
        varchar description
        varchar group_type "SYSTEM·CUSTOM"
    }

    tb_permission {
        bigint id PK
        varchar code UK "예: project.read"
        varchar name
        varchar description
    }

    tb_group_permission_grant {
        bigint id PK
        bigint permission_group_id FK,UK
        bigint permission_id FK,UK
        varchar scope UK "권한 범위"
    }

    tb_account_group_assignment {
        bigint id PK
        bigint account_id FK,UK
        bigint permission_group_id FK,UK
    }

    tb_employee {
        bigint id PK
    }
```

---

## 6. 커뮤니케이션 (알림·공지·AI 어시스턴트·주간 보고서)

```mermaid
erDiagram
    tb_account ||--o{ tb_notification : "수신"
    tb_notice ||--o{ tb_notice_receipt : "수신 상태"
    tb_account ||..o{ tb_notice_receipt : "account_id"
    tb_account ||--o{ tb_chat_session : "대화"
    tb_chat_session ||--o{ tb_chat_message : "메시지"
    tb_chat_session ||..o{ tb_chat_memory_message : "conversation_id"
    tb_account ||..o{ tb_weekly_report : "author_account_id"

    tb_notification {
        bigint id PK
        bigint account_id FK
        varchar title
        varchar description
        varchar notification_type
        boolean is_read
        varchar link
    }

    tb_notice {
        bigint id PK
        varchar title
        text body "마크다운"
        varchar importance "NORMAL·IMPORTANT·URGENT"
        boolean pinned "상단 고정"
        boolean popup "안내 팝업"
        datetime starts_at "NULL = 즉시"
        datetime ends_at "NULL = 무기한"
    }

    tb_notice_receipt {
        bigint id PK
        bigint notice_id FK,UK
        bigint account_id UK "FK 없음"
        datetime read_at
        datetime popup_hidden_until "오늘 하루 보지 않기"
        boolean popup_hidden_forever "다시 보지 않기"
    }

    tb_chat_session {
        bigint id PK
        bigint account_id FK
        varchar conversation_id UK
        varchar title
        boolean favorite
        datetime last_message_at
    }

    tb_chat_message {
        bigint id PK
        bigint session_id FK
        varchar role
        text content
    }

    tb_chat_memory_message {
        bigint id PK
        varchar conversation_id "Spring AI 메모리"
        int seq
        varchar message_type
        text content
    }

    tb_weekly_report {
        bigint id PK
        varchar title
        date week_start
        date week_end
        mediumtext content
        varchar generator
        boolean edited
        bigint author_account_id "FK 없음"
    }

    tb_account {
        bigint id PK
    }
```

---

## 7. 감사 로그

엔티티 변경 이력을 남깁니다. `tb_audit_log` 하나가 변경 이벤트 하나이고, `tb_audit_log_change` 에 속성별 이전 값과 이후 값이 들어갑니다.

```mermaid
erDiagram
    tb_audit_log ||--o{ tb_audit_log_change : "변경 상세"
    tb_account |o..o{ tb_audit_log : "actor_account_id (NULL = 시스템)"

    tb_audit_log {
        bigint id PK
        varchar entity_type "엔티티 클래스명"
        bigint entity_id
        varchar entity_label "표시명"
        varchar entity_name "변경 당시 이름"
        varchar parent_type "상위 엔티티"
        bigint parent_id
        varchar action "CREATE·UPDATE·DELETE·RESTORE"
        bigint actor_account_id
        varchar actor_name
        datetime created_at
    }

    tb_audit_log_change {
        bigint id PK
        bigint audit_log_id FK
        varchar field "속성명"
        varchar before_value
        varchar after_value
    }

    tb_account {
        bigint id PK
    }
```
