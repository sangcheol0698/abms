export interface PartyListItem {
  partyId: string;
  name: string;
  ceo: string;
  manager: string;
  contact: string;
  email: string;
}

export function mapPartyListItem(input: any): PartyListItem {
  return {
    partyId: String(input?.partyId ?? ''),
    name: String(input?.name ?? ''),
    ceo: String(input?.ceo ?? ''),
    manager: String(input?.manager ?? ''),
    contact: String(input?.contact ?? ''),
    email: String(input?.email ?? ''),
  };
}
