import { ref, computed } from 'vue';
import { toast } from 'vue-sonner';
import HttpError from '@/core/http/HttpError';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';

export function useEmployeeDeletion(onDeleted: () => Promise<void> | void) {
  const employeeRepository = appContainer.resolve(EmployeeRepository);

  const isDialogOpen = ref(false);
  const candidateId = ref<number | null>(null);
  const candidateName = ref<string | null>(null);
  const isProcessing = ref(false);

  const description = computed(() => {
    const name = candidateName.value;
    return name
      ? `'${name}' 직원을 삭제하면 복구할 수 없습니다.`
      : '삭제 후에는 복구할 수 없습니다.';
  });

  function open(id: number, name: string) {
    candidateId.value = id;
    candidateName.value = name;
    isDialogOpen.value = true;
  }

  function close() {
    isDialogOpen.value = false;
    candidateId.value = null;
    candidateName.value = null;
    isProcessing.value = false;
  }

  async function confirm() {
    const id = candidateId.value;
    const name = candidateName.value;
    if (!id) {
      return;
    }
    isProcessing.value = true;

    try {
      await employeeRepository.delete(id);

      toast.success('직원을 삭제했습니다.', {
        description: name ?? undefined,
        action: {
          label: '되돌리기',
          onClick: async () => {
            try {
              await employeeRepository.restore(id);
              toast.success('삭제를 취소했습니다.', {
                description: name ?? undefined,
              });
              await onDeleted();
            } catch (error) {
              const message =
                error instanceof HttpError
                  ? error.message
                  : '삭제 취소 중 오류가 발생했습니다.';
              toast.error('삭제 취소에 실패했습니다.', {
                description: message,
              });
            }
          },
        },
      });

      await onDeleted();
      close();
    } catch (error) {
      const message =
        error instanceof HttpError ? error.message : '직원 삭제 중 오류가 발생했습니다.';
      toast.error('직원을 삭제하지 못했습니다.', {
        description: message,
      });
    } finally {
      isProcessing.value = false;
    }
  }

  function cancel() {
    if (isProcessing.value) {
      return;
    }
    close();
  }

  return {
    isDialogOpen,
    candidateId,
    candidateName,
    isProcessing,
    description,
    open,
    confirm,
    cancel,
    close,
  };
}
