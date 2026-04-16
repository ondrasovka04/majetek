/** API service functions for inventory audit — overview, differences, patching actions, and projecting results. */
import authorizedRequest from "@/utils/authorizedRequest";
import { InventoryAuditEntry } from "@/types/inventoryAudit/inventoryAudit";
import { InventoryDifferenceEntry, InventoryDifferenceAction } from "@/types/inventoryAudit/inventoryDifference";

export const getInventoryAudit = async (): Promise<InventoryAuditEntry[]> => {
  const res = await authorizedRequest.get("/inventory-audit");
  return res.data.map((e: InventoryAuditEntry) => ({
    ...e,
    timestamp: new Date(e.timestamp),
    realLocationName: e.realLocationName ?? "",
  }));
};

export const getInventoryDifferences = async (): Promise<InventoryDifferenceEntry[]> => {
  const res = await authorizedRequest.get("/inventory-audit/differences");
  const data = res.data;
  return data.map((d: InventoryDifferenceEntry) => ({
    ...d,
    writeOffDate: d.writeOffDate ? new Date(d.writeOffDate) : null,
  }));
};

export interface PatchDifferenceBody {
  action?: InventoryDifferenceAction | null;
  writeOffDate?: string | null;
  writeOffMethodId?: string | null;
}

export const patchInventoryDifference = async (cardId: number, body: PatchDifferenceBody): Promise<void> => {
  await authorizedRequest.patch(`/inventory-audit/${cardId}`, body);
};

export const projectInventoryDifferences = async (): Promise<void> => {
  await authorizedRequest.post("/inventory-audit/project");
};
