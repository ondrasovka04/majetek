/** TanStack Query hooks for inventory audit overview, differences, patching, and projection. */
import { useQuery, useMutation } from "@tanstack/react-query";
import {
  getInventoryAudit,
  getInventoryDifferences,
  patchInventoryDifference,
  projectInventoryDifferences,
} from "@/services/inventoryAuditService";
import { InventoryDifferenceAction } from "@/types/inventoryAudit/inventoryDifference";

export function useInventoryAudit() {
  return useQuery({
    queryKey: ["inventoryAudit"],
    queryFn: getInventoryAudit,
  });
}

export function useInventoryDifferences() {
  return useQuery({
    queryKey: ["inventoryDifferences"],
    queryFn: getInventoryDifferences,
  });
}

export function usePatchInventoryDifference() {
  return useMutation({
    mutationFn: ({
      cardId,
      body,
    }: {
      cardId: number;
      body: {
        action?: InventoryDifferenceAction | null;
        writeOffDate?: string | null;
        writeOffMethodId?: string | null;
      };
    }) => patchInventoryDifference(cardId, body),
  });
}

export function useProjectInventoryDifferences() {
  return useMutation({
    mutationFn: () => projectInventoryDifferences(),
  });
}
