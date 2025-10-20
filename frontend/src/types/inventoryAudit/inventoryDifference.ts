export type InventoryDifferenceStatus = "CORRECT" | "WARN" | "WRONG";

// Akce které může uživatel zvolit na daném řádku
export type InventoryDifferenceAction = "KEEP" | "SU" | "OU" | "WRITE_OFF" | null;

export interface InventoryDifferenceEntry {
  cardId: number;
  cardName: string;
  realLocationId: string | null;
  realLocationName: string | null;
  expectedLocationId: string;
  expectedLocationName: string | null;
  status: InventoryDifferenceStatus;
  action?: InventoryDifferenceAction;
  writeOffDate?: Date | null;
  writeOffMethodId?: string | null;
}
