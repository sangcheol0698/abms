import { hasStoredPermission } from '@/features/auth/session';

const PARTY_READ = 'party.read';
const PARTY_WRITE = 'party.write';

export function canReadParties(): boolean {
  return hasStoredPermission(PARTY_READ);
}

export function canManageParties(): boolean {
  return hasStoredPermission(PARTY_WRITE);
}
