/** Single inventory audit entry representing a scanned card and its real location. */
export interface InventoryAuditEntry {
  cardId: number;
  cardName: string;
  realLocationId: string;
  realLocationName: string;
  timestamp: Date;
}
