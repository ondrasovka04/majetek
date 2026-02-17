/** TanStack Query hooks for purchase method (způsob pořízení) CRUD with cache invalidation. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getPurchaseMethods,
  createPurchaseMethod,
  updatePurchaseMethod,
  deletePurchaseMethod,
} from "../../services/purchaseMethodService";
import { PurchaseMethod } from "../../types/purchaseMethod";

const QUERY_KEY = ["purchaseMethods"] as const;

export function usePurchaseMethods() {
  return useQuery({
    queryKey: QUERY_KEY,
    queryFn: getPurchaseMethods,
  });
}

export function useCreatePurchaseMethod() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (m: PurchaseMethod) => createPurchaseMethod(m),
    onSuccess: (created) => {
      qc.setQueryData<PurchaseMethod[]>(QUERY_KEY, (old) =>
        old ? [...old, created] : [created],
      );
    },
  });
}

export function useUpdatePurchaseMethod() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, name }: { id: string; name: string | null }) =>
      updatePurchaseMethod(id, name),
    onSuccess: (updated) => {
      qc.setQueryData<PurchaseMethod[]>(QUERY_KEY, (old) =>
        old?.map((m) => (m.id === updated.id ? updated : m)),
      );
    },
  });
}

export function useDeletePurchaseMethod() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deletePurchaseMethod(id),
    onSuccess: (_data, id) => {
      qc.setQueryData<PurchaseMethod[]>(QUERY_KEY, (old) =>
        old?.filter((m) => m.id !== id),
      );
    },
  });
}
