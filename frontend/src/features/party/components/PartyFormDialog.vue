<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="max-w-md">
      <DialogHeader>
        <DialogTitle>{{ mode === 'create' ? '협력사 등록' : '협력사 수정' }}</DialogTitle>
        <DialogDescription>
          {{ mode === 'create' ? '새로운 협력사 정보를 입력하세요.' : '협력사 정보를 수정합니다.' }}
        </DialogDescription>
      </DialogHeader>

      <form class="space-y-4" @submit.prevent="handleSubmit">
        <div class="space-y-2">
          <Label for="name">협력사명 *</Label>
          <Input id="name" v-model="form.name" placeholder="협력사명을 입력하세요" required />
        </div>

        <div class="space-y-2">
          <Label for="ceoName">대표자</Label>
          <Input id="ceoName" v-model="form.ceoName" placeholder="대표자명" />
        </div>

        <div class="space-y-2">
          <Label for="salesRepName">담당자</Label>
          <Input id="salesRepName" v-model="form.salesRepName" placeholder="담당자명" />
        </div>

        <div class="space-y-2">
          <Label for="salesRepPhone">연락처</Label>
          <Input id="salesRepPhone" v-model="form.salesRepPhone" placeholder="010-0000-0000" />
        </div>

        <div class="space-y-2">
          <Label for="salesRepEmail">이메일</Label>
          <Input id="salesRepEmail" v-model="form.salesRepEmail" type="email" placeholder="email@example.com" />
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" @click="emit('update:open', false)">취소</Button>
          <Button type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? '저장 중...' : (mode === 'create' ? '등록' : '저장') }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { appContainer } from '@/core/di/container';
import PartyRepository from '@/features/party/repository/PartyRepository';
import type { PartyDetail } from '@/features/party/models/partyDetail';
import { toast } from 'vue-sonner';

interface Props {
  open: boolean;
  mode: 'create' | 'edit';
  party?: PartyDetail | null;
}

const props = withDefaults(defineProps<Props>(), {
  party: null,
});

const emit = defineEmits<{
  'update:open': [value: boolean];
  created: [];
  updated: [];
}>();

const repository = appContainer.resolve(PartyRepository);

const form = reactive({
  name: '',
  ceoName: '',
  salesRepName: '',
  salesRepPhone: '',
  salesRepEmail: '',
});

const isSubmitting = ref(false);

watch(
  () => [props.open, props.party],
  () => {
    if (props.open && props.mode === 'edit' && props.party) {
      form.name = props.party.name;
      form.ceoName = props.party.ceo ?? '';
      form.salesRepName = props.party.manager ?? '';
      form.salesRepPhone = props.party.contact ?? '';
      form.salesRepEmail = props.party.email ?? '';
    } else if (props.open && props.mode === 'create') {
      form.name = '';
      form.ceoName = '';
      form.salesRepName = '';
      form.salesRepPhone = '';
      form.salesRepEmail = '';
    }
  },
  { immediate: true },
);

async function handleSubmit() {
  if (!form.name.trim()) {
    toast.error('협력사명을 입력하세요.');
    return;
  }

  isSubmitting.value = true;
  try {
    const data = {
      name: form.name,
      ceoName: form.ceoName || null,
      salesRepName: form.salesRepName || null,
      salesRepPhone: form.salesRepPhone || null,
      salesRepEmail: form.salesRepEmail || null,
    };

    if (props.mode === 'create') {
      await repository.create(data);
      emit('created');
    } else if (props.party?.partyId) {
      await repository.update(props.party.partyId, data);
      emit('updated');
    }
  } catch {
    toast.error(props.mode === 'create' ? '협력사 등록에 실패했습니다.' : '협력사 수정에 실패했습니다.');
  } finally {
    isSubmitting.value = false;
  }
}
</script>
