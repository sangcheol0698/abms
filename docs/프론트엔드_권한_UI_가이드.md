# 프론트엔드 권한 UI 가이드

본 문서는 **프론트엔드에서 실제로 어떤 화면/버튼/메뉴가 권한에 따라 제어되고 있는지**, 그리고 **새 화면에서 같은 방식으로 권한 제어를 적용하려면 어떻게 구현해야 하는지**를 정리한 문서입니다.

중요한 원칙은 아래 두 가지입니다.

- **실제 보안은 백엔드가 담당**한다.
- **프런트는 UX 차원에서 미리 숨기거나 막아준다.**

즉, 프런트에서 버튼을 숨겨도 보안이 완성되는 것은 아니며, 최종 권한 검사는 항상 서버가 기준입니다.

---

## 1. 현재 프런트에서 권한으로 제어되는 영역

### 1-1. 전역 메뉴 / 화면 진입

현재 프런트에서 메뉴와 직접 URL 진입까지 제어되는 항목은 아래와 같습니다.

| 대상 | 권한 코드 | 현재 프런트 동작 |
|---|---|---|
| `직원` 메뉴 | `employee.read` | 권한이 없으면 사이드바/커맨드 팔레트에서 숨김 |
| `/employees` | `employee.read` | 권한이 없으면 직접 URL 진입도 403 페이지로 이동 |
| `/employees/:employeeId` | `employee.read` | 권한이 없으면 직접 URL 진입도 403 페이지로 이동 |
| `권한 그룹 관리` 메뉴 | `permission.group.manage` | 권한이 없으면 사이드바/커맨드 팔레트에서 숨김 |
| `/system/permission-groups` | `permission.group.manage` | 권한이 없으면 직접 URL 진입도 403 페이지로 이동 |

현재는 `직원`, `권한 그룹 관리` 화면만 라우트 레벨 권한 가드가 붙어 있습니다.  
`project.read`, `dashboard.read`는 아직 프런트 메뉴/라우트 가드에 본격 반영되지 않았습니다.

### 1-2. 직원 목록 화면

대상 파일:
- [EmployeeListView.vue](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/employee/views/EmployeeListView.vue)
- [tableColumns.ts](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/employee/configs/tableColumns.ts)
- [EmployeeRowActions.vue](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/employee/components/EmployeeRowActions.vue)

현재 동작은 아래와 같습니다.

| 기능 | 권한 조건 | 프런트 동작 |
|---|---|---|
| 직원 추가 버튼 | `employee.write` + 자기 외 범위(`ALL`, `OWN_DEPARTMENT`, `OWN_DEPARTMENT_TREE`) | 권한 없으면 숨김 |
| 직원 엑셀 업로드 | `employee.write` + 자기 외 범위 | 권한 없으면 숨김 |
| 직원 엑셀 다운로드 | `employee.read` | 권한 없으면 숨김 |
| 샘플 다운로드 | 로그인 사용자 | 항상 노출 |
| 목록 이름 클릭 | `employee.read` + 대상 범위 일치 | 권한 없으면 링크 대신 일반 텍스트 |
| 행 액션 `직원 편집` | `employee.write` 범위 일치 | 권한 없으면 메뉴에서 숨김 |
| 행 액션 `직원 삭제` | `employee.write` + 자기 외 범위 | 권한 없으면 메뉴에서 숨김 |
| 행 액션 `이메일 복사` | 로그인 사용자 | 계속 노출 |

특히 `SELF` 사용자면:
- 자기 행에서는 `직원 편집` 대신 `내 정보 수정`으로 보입니다.
- 그 버튼은 일반 직원 수정 다이얼로그가 아니라 `내 계정` 다이얼로그 안의 자기 정보 수정 UI를 엽니다.

### 1-3. 부서 > 직원 탭

대상 파일:
- [DepartmentEmployeeList.vue](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/department/components/DepartmentEmployeeList.vue)

현재 부서 직원 탭도 직원 목록과 같은 권한 기준을 따릅니다.

| 기능 | 권한 조건 | 프런트 동작 |
|---|---|---|
| 직원 상세 링크 | `employee.read` + 대상 범위 일치 | 범위 밖이면 링크 차단 |
| 행 액션 `직원 편집` | `employee.write` 범위 일치 | 권한 없으면 숨김 |
| 행 액션 `직원 삭제` | `employee.write` + 자기 외 범위 | 권한 없으면 숨김 |
| `SELF` 수정 | 자기 행만 가능 | 라벨은 `내 정보 수정`, 프로필 다이얼로그로 이동 |

### 1-4. 직원 상세 화면

대상 파일:
- [EmployeeDetailView.vue](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/employee/views/EmployeeDetailView.vue)
- [EmployeeEmploymentPanel.vue](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/employee/components/EmployeeEmploymentPanel.vue)

현재 동작은 아래와 같습니다.

| 기능 | 권한 조건 | 프런트 동작 |
|---|---|---|
| 승진 버튼 | `employee.write` + 자기 외 범위 | 권한 없으면 숨김 |
| 직원 편집 버튼 | `employee.write` + 자기 외 범위 | 권한 없으면 숨김 |
| 내 정보 수정 버튼 | 자기 자신 + `employee.write` (`SELF` 포함) | 자기 상세에서만 노출 |
| 상태 변경 섹션 | `employee.write` + 자기 외 범위 | 권한 없으면 전체 숨김 |
| 직원 삭제 카드 | `employee.write` + 자기 외 범위 | 권한 없으면 숨김 |

즉, `SELF` 사용자는 자기 상세에서만 `내 정보 수정` 진입이 보이고,  
승진/삭제/휴직/복직/퇴사 같은 관리자 액션은 보이지 않습니다.

### 1-5. 내 계정 > 내 정보 수정

대상 파일:
- [ProfileDialog.vue](/Users/sangcheol/IdeaProjects/abms/frontend/src/components/business/ProfileDialog.vue)
- [EmployeeSelfProfileDialog.vue](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/employee/components/EmployeeSelfProfileDialog.vue)
- [profileDialogEvents.ts](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/auth/profileDialogEvents.ts)

현재 동작:

| 기능 | 권한 조건 | 프런트 동작 |
|---|---|---|
| `내 정보 수정` 카드 | `employee.write` | 권한 있으면 `내 계정` 다이얼로그의 `계정 정보` 탭에 노출 |
| 목록/상세/부서탭의 `내 정보 수정` | `employee.write` + 자기 자신 | 누르면 `내 계정` 다이얼로그를 열고 자기 수정 팝업까지 바로 엶 |
| 수정 가능 필드 | 서버 정책 반영 | 생년월일, 아바타만 수정 |
| 수정 불가 필드 | 서버 정책 반영 | 계정 이름, 계정 이메일 |

아바타 선택은 직원 생성/편집 팝업과 동일한 `EmployeeAvatarSelectDialog`를 재사용합니다.

---

## 2. 프런트에서 권한을 어떻게 판단하는가

대상 파일:
- [session.ts](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/auth/session.ts)
- [permissions.ts](/Users/sangcheol/IdeaProjects/abms/frontend/src/features/employee/permissions.ts)

현재 프런트는 `/api/auth/me` 응답을 세션(localStorage)에 저장하고,  
화면에서는 이를 직접 읽지 않고 helper 함수를 통해 판단합니다.

대표 helper:

| 함수 | 의미 |
|---|---|
| `canReadEmployeeDetail()` | `employee.read` 자체가 있는가 |
| `canViewEmployeeDetail(employeeEmail)` | 특정 직원 상세를 볼 수 있는가 |
| `canManageEmployees()` | 자기 외 직원까지 관리 가능한가 |
| `canManageEmployee(employeeEmail)` | 특정 직원을 수정할 수 있는가 |
| `canEditOwnProfile(employeeEmail)` | 자기 자신만 수정 가능한가 |
| `canAccessOwnProfileEditor()` | `내 계정`에서 자기 정보 수정 UI를 열 수 있는가 |
| `canManagePermissionGroups()` | 권한 그룹 관리 화면 접근 권한이 있는가 |

`SELF` 판정은 현재 세션 사용자 `email`과 대상 직원 `email` 비교로 처리합니다.

---

## 3. 새 화면에서 권한 UI 제어를 적용하는 방법

새 화면에 프런트 권한 제어를 붙일 때는 아래 순서를 따릅니다.

### 3-1. 라우트 가드가 필요한지 먼저 판단

**URL 직접 진입도 막아야 하는 화면**이면 라우터 메타에 권한을 붙입니다.

예:

```ts
meta: {
  requiredPermission: 'employee.read',
}
```

현재 라우터는 `requiredPermission`이 있으면 세션을 새로 검증한 뒤, 권한이 없을 때 403 페이지로 보냅니다.

### 3-2. 템플릿에서는 직접 권한 코드를 비교하지 말 것

다음처럼 helper를 거치게 합니다.

```ts
const canCreateEmployees = computed(() => canManageEmployees());
const canDownloadEmployees = computed(() => canReadEmployeeDetail());
```

그리고 템플릿에서는:

```vue
<Button v-if="canCreateEmployees" />
```

처럼 씁니다.

### 3-3. 행 단위 / 리소스 단위 권한은 helper 함수로 분기

직원처럼 대상에 따라 권한이 달라지는 경우:

```ts
canViewEmployeeDetail(employee.email)
canEditOwnProfile(employee.email)
```

처럼 **대상 데이터(email 등)를 넣는 함수**로 판단해야 합니다.  
단순 `hasPermission('employee.read')`만으로는 `SELF`를 처리할 수 없습니다.

### 3-4. 프런트는 UX만 담당

프런트에서 버튼을 숨겨도 **최종 보안은 서버가 담당**합니다.  
즉, 프런트에서 숨기는 이유는:

- 사용자가 눌러보고 403을 받는 경험을 줄이기 위해서
- 실제 가능한 액션만 보이게 하여 화면을 단순하게 만들기 위해서

입니다.

---

## 4. 현재 프런트 권한 제어의 한계

현재 구현은 실용적으로는 충분하지만, 아래 한계는 남아 있습니다.

### 4-1. 부서/하위부서 범위는 프런트에서 완전 복제하지 않음

`OWN_DEPARTMENT`, `OWN_DEPARTMENT_TREE`는 프런트에서 모든 대상에 대해 정밀 계산하지 않습니다.  
프런트는 주로 `SELF`만 명확히 분기하고, 나머지는 서버를 최종 기준으로 둡니다.

즉:
- 프런트는 과한 액션을 숨기는 수준
- 최종 허용/차단은 서버

### 4-2. 모든 도메인에 같은 수준으로 적용된 것은 아님

현재 프런트에서 실질적으로 가장 많이 적용된 권한 UI 제어는 `employee` 도메인입니다.  
`project.read`, `dashboard.read`는 아직 백엔드 적용 자체가 확장 중이므로 프런트도 부분 적용 상태입니다.

### 4-3. 일부 unit test는 mock 컬럼 구조 때문에 경고가 있음

`EmployeeListView.spec.ts`는 mock 테이블 컬럼이 단순해서  
`Column with id ... does not exist` 경고가 나오지만, 테스트 자체는 현재 통과합니다.

---

## 5. 권장 유지보수 원칙

현재 구조를 유지보수하기 위해서는 아래 원칙을 지키는 것이 좋습니다.

1. 새 화면에 권한 UI 제어를 붙일 때는 먼저 helper 함수부터 추가한다.
2. 템플릿 곳곳에서 권한 코드를 직접 비교하지 않는다.
3. `SELF` 판정은 가능한 한 공통 helper로만 처리한다.
4. 라우트 직접 진입을 막아야 하는 화면은 `requiredPermission`을 쓴다.
5. 프런트 권한 제어를 추가해도, 서버 권한 검사는 절대 제거하지 않는다.

---

## 6. 한 줄 정리

현재 프런트 권한 제어는 아래 정도까지 적용되어 있습니다.

- 메뉴 숨김
- 권한 있는 화면만 직접 URL 진입 허용
- 직원 목록/부서 직원 탭/직원 상세의 액션 노출 제어
- 자기 정보 수정 전용 UX
- 권한 그룹 관리 화면 접근 제어

즉, **프런트는 “보여줄 것만 보여주기”까지는 하고 있고, 실제 보안 판단은 여전히 서버가 최종 기준**입니다.
