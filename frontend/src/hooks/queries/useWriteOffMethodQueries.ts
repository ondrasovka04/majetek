/** TanStack Query hooks for write-off method (způsob vyřazení) CRUD with cache invalidation. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getWriteOffMethods,
  createWriteOffMethod,
  updateWriteOffMethod,
  deleteWriteOffMethod,
} from "@/services/writeOffMethodService";
import { WriteOffMethod } from "@/types/writeOffMethod";

const QUERY_KEY = ["writeOffMethods"] as const;

export function useWriteOffMethods() {
  return useQuery({
    queryKey: QUERY_KEY,
    queryFn: getWriteOffMethods,
    staleTime: Infinity,
  });
}

export function useCreateWriteOffMethod() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (m: WriteOffMethod) => createWriteOffMethod(m),
    onSuccess: (created) => {
      qc.setQueryData<WriteOffMethod[]>(QUERY_KEY, (old) => (old ? [...old, created] : [created]));
    },
  });
}

export function useUpdateWriteOffMethod() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, name }: { id: string; name: string | null }) => updateWriteOffMethod(id, name),
    onSuccess: (updated) => {
      qc.setQueryData<WriteOffMethod[]>(QUERY_KEY, (old) => old?.map((m) => (m.id === updated.id ? updated : m)));
    },
  });
}

export function useDeleteWriteOffMethod() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteWriteOffMethod(id),
    onSuccess: (_data, id) => {
      qc.setQueryData<WriteOffMethod[]>(QUERY_KEY, (old) => old?.filter((m) => m.id !== id));
    },
  });
}
