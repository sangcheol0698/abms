<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="gap-0 p-0 sm:max-w-[550px]">
      <DialogHeader class="px-6 pt-6 pb-4">
        <DialogTitle>부서 리더 임명</DialogTitle>
        <DialogDescription>
          <span class="font-medium text-foreground">{{ departmentName }}</span
          >의 새로운 리더를 선택하세요.
        </DialogDescription>
      </DialogHeader>

      <Command :filter-function="() => 1" class="border-t">
        <div class="flex h-12 items-center gap-2 border-b px-3" cmdk-input-wrapper>
          <Search class="mr-2 h-4 w-4 shrink-0 opacity-50" />
          <input
            class="flex h-11 w-full rounded-md bg-transparent py-3 text-sm outline-none placeholder:text-muted-foreground disabled:cursor-not-allowed disabled:opacity-50"
            placeholder="이름으로 직원 검색..."
            :value="searchQuery"
            @input="handleSearch"
          />
        </div>
        <CommandList class="max-h-[300px]">
          <CommandEmpty v-if="isLoading" class="py-6 text-center text-sm text-muted-foreground">
            검색 중...
          </CommandEmpty>
          <CommandEmpty
            v-else-if="employees.length === 0"
            class="py-6 text-center text-sm text-muted-foreground"
          >
            검색 결과가 없습니다.
          </CommandEmpty>

          <CommandGroup v-else heading="검색 결과">
            <CommandItem
              v-for="employee in employees"
              :key="employee.employeeId"
              :value="employee.employeeId"
              @select="selectEmployee(employee)"
              class="flex items-center gap-3 px-4 py-2 cursor-pointer"
            >
              <Avatar class="h-9 w-9 rounded-xl border border-border/60 bg-background">
                <AvatarImage :src="employee.avatarImageUrl" :alt="employee.name" />
                <AvatarFallback class="rounded-xl">{{ getInitials(employee.name) }}</AvatarFallback>
              </Avatar>

              <div class="flex flex-col flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-medium truncate">{{ employee.name }}</span>
                  <Badge
                    variant="outline"
                    class="text-[10px] px-1 py-0 h-4 font-normal text-muted-foreground"
                  >
                    {{ employee.positionLabel }}
                  </Badge>
                </div>
                <div class="flex items-center gap-2 text-xs text-muted-foreground">
                  <span class="truncate">{{ employee.departmentName }}</span>
                  <span v-if="employee.email" class="truncate opacity-70"
                    >· {{ employee.email }}</span
                  >
                </div>
              </div>

              <Check
                v-if="selectedEmployee?.employeeId === employee.employeeId"
                class="h-4 w-4 text-primary"
              />
            </CommandItem>
          </CommandGroup>
        </CommandList>
      </Command>

      <DialogFooter class="bg-muted/10 p-4 border-t">
        <Button variant="outline" @click="close">취소</Button>
        <Button :disabled="!selectedEmployee || isSubmitting" @click="confirmAssignment">
          <Loader2 v-if="isSubmitting" class="mr-2 h-4 w-4 animate-spin" />
          임명하기
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useDebounceFn } from '@vueuse/core';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import DepartmentRepository from '@/features/department/repository/DepartmentRepository';
import type { EmployeeListItem } from '@/features/employee/models/employeeListItem';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Check, Loader2, Search } from 'lucide-vue-next';
import { toast } from 'vue-sonner';

const props = defineProps<{
  open: boolean;
  departmentId: number;
  departmentName: string;
}>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'assigned'): void;
}>();

const employeeRepository = appContainer.resolve(EmployeeRepository);
const organizationRepository = appContainer.resolve(DepartmentRepository);

const searchQuery = ref('');
const employees = ref<EmployeeListItem[]>([]);
const selectedEmployee = ref<EmployeeListItem | null>(null);
const isLoading = ref(false);
const isSubmitting = ref(false);

const handleSearch = (e: Event) => {
  const value = (e.target as HTMLInputElement).value;
  searchQuery.value = value;
  debouncedSearch(value);
};

const debouncedSearch = useDebounceFn(async (query: string) => {
  if (!query.trim()) {
    employees.value = [];
    return;
  }

  isLoading.value = true;
  try {
    const response = await employeeRepository.search({
      page: 1,
      size: 20,
      name: query,
      sort: 'name,asc',
    });
    employees.value = response.content;
  } catch (error) {
    console.error('Failed to search employees:', error);
  } finally {
    isLoading.value = false;
  }
}, 300);

function selectEmployee(employee: EmployeeListItem) {
  selectedEmployee.value = employee;
}

function handleOpenChange(value: boolean) {
  if (!value) {
    resetState();
  }
  emit('update:open', value);
}

function close() {
  handleOpenChange(false);
}

function resetState() {
  searchQuery.value = '';
  employees.value = [];
  selectedEmployee.value = null;
}

async function confirmAssignment() {
  if (!selectedEmployee.value) return;

  isSubmitting.value = true;
  try {
    await organizationRepository.assignTeamLeader(
      props.departmentId,
      selectedEmployee.value.employeeId,
    );

    toast.success(`${selectedEmployee.value.name}님이 리더로 임명되었습니다.`);
    emit('assigned');
    close();
  } catch (error) {
    console.error('Failed to assign leader:', error);
    toast.error('리더 임명에 실패했습니다.');
  } finally {
    isSubmitting.value = false;
  }
}

function getInitials(name?: string) {
  if (!name) return '';
  return name.slice(0, 2).toUpperCase();
}

// Initial load (optional, maybe showing recent employees or empty state is fine)
</script>
