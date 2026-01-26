import { computed, ref } from 'vue';
import { toast } from 'vue-sonner';
import HttpError from '@/core/http/HttpError';
import { appContainer } from '@/core/di/container';
import ProjectRepository from '@/features/project/repository/ProjectRepository';

export function useProjectDeletion(onDeleted: () => Promise<void> | void) {
  const projectRepository = appContainer.resolve(ProjectRepository);

  const isDialogOpen = ref(false);
  const candidateId = ref<number | null>(null);
  const candidateName = ref<string | null>(null);
  const isProcessing = ref(false);

  const description = computed(() => {
    const name = candidateName.value;
    return name
      ? `'${name}' 프로젝트를 삭제하면 복구할 수 없습니다.`
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
      await projectRepository.delete(id);
      toast.success('프로젝트를 삭제했습니다.', {
        description: name ?? undefined,
      });
      await onDeleted();
      close();
    } catch (error) {
      const message =
        error instanceof HttpError ? error.message : '프로젝트 삭제 중 오류가 발생했습니다.';
      toast.error('프로젝트 삭제에 실패했습니다.', {
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
