/** Summary representation of an asset card (used in list views). */
export interface Card {
  id: string;
  name: string;
  writeOffDate: Date | null;
}