/** Status indicating match between real and expected location. */
export type InventoryDifferenceStatus = "CORRECT" | "WARN" | "WRONG";

// Actions the user can assign to a given inventory difference row
export type InventoryDifferenceAction = "KEEP" | "RL" | "EL" | "WRITE_OFF" | null;

/** Single row in the inventory differences comparison table. */
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
