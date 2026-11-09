/** TanStack Query hooks for inventory audit overview, differences, patching, and projection. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getInventoryAudit,
  getInventoryDifferences,
  patchInventoryDifference,
  projectInventoryDifferences,
} from "@/services/inventoryAuditService";
import { InventoryDifferenceAction, InventoryDifferenceEntry } from "@/types/inventoryAudit/inventoryDifference";

const resolveWriteOffDate = (
  next: string | null | undefined,
  previous: Date | null | undefined,
): Date | null | undefined => {
  if (next) return new Date(next);
  if (next === null) return null;
  return previous;
};

// Audit data are uploaded from the scanners outside of this app – refetch on every visit
export function useInventoryAudit() {
  return useQuery({
    queryKey: ["inventoryAudit"],
    queryFn: getInventoryAudit,
    staleTime: 0,
  });
}

export function useInventoryDifferences() {
  return useQuery({
    queryKey: ["inventoryDifferences"],
    queryFn: getInventoryDifferences,
    staleTime: 0,
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
                writeOffDate: resolveWriteOffDate(body.writeOffDate, entry.writeOffDate),
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
