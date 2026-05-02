import HttpError from '@/core/http/HttpError';

export function getPartyErrorMessage(error: unknown, fallbackMessage: string): string {
  if (error instanceof HttpError) {
    return error.message;
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }

  return fallbackMessage;
}
