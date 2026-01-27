export interface PartyListItem {
  partyId: number;
  name: string;
  ceo: string;
  manager: string;
  contact: string;
  email: string;
}

export interface PartySearchParams {
  page: number;
  size: number;
  name?: string;
  sort?: string;
}

export function mapPartyListItem(input: any): PartyListItem {
  return {
    partyId: Number(input?.partyId ?? 0),
    name: String(input?.name ?? ''),
    ceo: String(input?.ceo ?? ''),
    manager: String(input?.manager ?? ''),
    contact: String(input?.contact ?? ''),
    email: String(input?.email ?? ''),
  };
}
