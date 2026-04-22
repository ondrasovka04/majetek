/** TanStack Query hooks for inventory audit overview, differences, patching, and projection. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getInventoryAudit,
  getInventoryDifferences,
  patchInventoryDifference,
  projectInventoryDifferences,
} from "@/services/inventoryAuditService";
import { InventoryDifferenceAction, InventoryDifferenceEntry } from "@/types/inventoryAudit/inventoryDifference";

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
  const queryClient = useQueryClient();
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
    onSuccess: (_data, { cardId, body }) => {
      queryClient.setQueryData<InventoryDifferenceEntry[]>(["inventoryDifferences"], (old) => {
        if (!old) return old;
        return old.map((entry) =>
          entry.cardId === cardId
            ? {
                ...entry,
                ...body,
                writeOffDate: body.writeOffDate ? new Date(body.writeOffDate) : body.writeOffDate === null ? null : entry.writeOffDate,
              }
            : entry,
        );
      });
    },
  });
}

export function useProjectInventoryDifferences() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => projectInventoryDifferences(),
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: ["inventoryDifferences"] });
      queryClient.removeQueries({ queryKey: ["inventoryAudit"] });
    },
  });
}
