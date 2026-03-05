
## UI 컴포넌트 TDD

### BaseButton 업그레이드 (Loading 상태)

**`src/components/common/__tests__/BaseButton.spec.ts`**
```ts
import { describe, it, expect } from 'vitest'
import BaseButton from '@/components/common/BaseButton.vue'
import { mount } from '@vue/test-utils'

describe('BaseButton', () => {
  ...

  it('loading이 true일 때 버튼이 비활성화되도록 적절한 클래스가 적용되어야 한다.', () => {
    const wrapper = mount(BaseButton, {
      props: {
        label: '테스트 버튼',
        loading: true,
      },
    })

    // 1. loading이 true일 때는 버튼이 disabled 상태인지 확인
    expect(wrapper.attributes('disabled')).toBeDefined()

    // 2. 커서가 not-allowed 클래스를 가지고 있는지 확인
    expect(wrapper.classes()).toContain('cursor-not-allowed')

    // 3. 내용이 'Loading...'으로 바뀌는지 확인
    expect(wrapper.text()).toContain('Loading...')
  })
})
```

**TDD 사이클**
1. 위 테스트가 실패하는 지 확인
2. 테스트를 통과하도록 `src/components/BaseButton` 구현 및 테스트 통과 확인
```vue
<script setup lang="ts">
defineProps<{ label: string; loading?: boolean }>()
</script>

<template>
  <button :disabled="loading" :class="loading ? 'cursor-not-allowed' : ''">
    {{ loading ? 'Loading...' : label }}
  </button>
</template>
```
- `:disabled="loading"`
- `:class="loading ? 'cursor-not-allowed' : ''"`
- `{{ loading ? 'Loading...' : label }}`
- 테스트 확인 - 성공

3. 리팩토링


### BaseInput 생성

**`src/components/common/__tests__/BaseInput.spec.ts`**
```ts
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseInput from '@/components/common/BaseInput.vue'

describe('BaseInput', () => {
  it('v-model이 정상적으로 동작한다.', async () => {
    const wrapper = mount(BaseInput, {
      props: {
        modelValue: '초기값',
        'onUpdate:modelValue': e => wrapper.setProps({ modelValue: e }),
      },
    })

    const input = wrapper.find('input')
    await input.setValue('변경된 값')

    expect(wrapper.props('modelValue')).toBe('변경된 값')
  })
})
```

**`BaseInput.vue`**
```vue
<script setup lang="ts">
const model = defineModel<string>()
</script>

<template>
  <input
    v-model="model"
    class="border rounded px-3 py-2 border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
  />
</template>
```


### 요약
* **부모**: `v-model="email"`을 사용하여 자신의 데이터를 자식과 공유한다.
* **자식**: `const model = defineModel()`로 데이터를 수신한다.
* **동작**: 자식이 `model.value`의 값을 변경하면 부모의 `email` 변수도 자동으로 동기화된다.

이러한 구조를 통해 코드의 가독성이 높아지고 데이터 흐름을 더 직관적으로 제어할 수 있다.



## 참고 문법

1. props: `defineProps<{ ... }>()`
2. 양방향 바인딩 핵심: `defineModel<T>()`, `v-model`
3. 클래스/상태 바인딩: `:disabled`, `:class`
4. 조건 렌더링: `v-if`, `v-else`
5. 테스트에서의 이벤트/상태 검사: `mount`, `setValue`, `setProps`, `attributes`, `classes`



## 공식 문서

1. [Vue `defineProps`](https://ko.vuejs.org/guide/components/props.html)
2. [Vue `v-model` 문서](https://ko.vuejs.org/guide/essentials/forms.html#v-model)
3. [Vue `defineModel`](https://ko.vuejs.org/api/sfc-script-setup.html#definemodel)
4. [Vue Test Utils](https://test-utils.vuejs.org/guide/)
5. [Vitest `mock`/`expect`](https://vitest.dev/guide/mocking.html)
