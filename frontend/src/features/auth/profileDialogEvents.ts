const OPEN_PROFILE_DIALOG_EVENT = 'auth:open-profile-dialog';

export interface OpenProfileDialogDetail {
  openSelfProfileEditor?: boolean;
}

export function dispatchOpenProfileDialogEvent(detail: OpenProfileDialogDetail = {}): void {
  if (typeof window === 'undefined') {
    return;
  }

  window.dispatchEvent(new CustomEvent<OpenProfileDialogDetail>(OPEN_PROFILE_DIALOG_EVENT, { detail }));
}

export function addOpenProfileDialogListener(
  listener: (detail: OpenProfileDialogDetail | undefined) => void,
): () => void {
  if (typeof window === 'undefined') {
    return () => {};
  }

  const handler = (event: Event) => {
    listener((event as CustomEvent<OpenProfileDialogDetail>).detail);
  };
  window.addEventListener(OPEN_PROFILE_DIALOG_EVENT, handler);
  return () => window.removeEventListener(OPEN_PROFILE_DIALOG_EVENT, handler);
}
