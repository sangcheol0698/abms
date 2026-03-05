
## 테스트구현 예시

**`src/views/__tests__/departmentView.spec.ts`**
```ts
import { describe, it, expect, vi } from "vitest";
import { mount, flushPromises } from "@vue/test-utils";
import { getDepartments } from "@/api/departmentApi";
import DepartmentView from "@/views/DepartmentView.vue";
import type { Department } from "@/models/department.ts";

// 1. API 모킹 (실제 네트워크 요청 차단)
vi.mock("@/api/departmentApi");

describe("DepartmentView", () => {
  it("API를 호출하고 응답받은 데이터를 화면에 그려야 한다", async () => {
    // Given: Mock 데이터 설정
    const mockDepartments: Partial<Department>[] = [
      { departmentId: 1, departmentName: "IT" },
      { departmentId: 2, departmentName: "Marketing" },
    ];
    // TypeScript 환경에선 vi.mocked()로 감싸줘야 타입 추론이 됩니다.
    vi.mocked(getDepartments).mockResolvedValue(mockDepartments as Department[]);

    // When: 컴포넌트 마운트
    const wrapper = mount(DepartmentView);

    // API 호출이 완료될 때까지 기다림 (Microtask Queue 비우기)
    await flushPromises();

    // Then 2: 데이터가 화면에 표시되었는지 확인
    expect(wrapper.text()).toContain("IT");
    expect(wrapper.text()).toContain("Marketing");
  });
});
```

> 참고
> `Partial<T>`을 쓰면 일부 필드만 적어도 에러가 안 난다.

