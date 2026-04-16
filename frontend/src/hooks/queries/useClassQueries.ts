/** TanStack Query hooks for class (třída) CRUD with cache invalidation. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getClasses, createClass, updateClass, deleteClass } from "@/services/classService";
import { Class } from "@/types/class";

const QUERY_KEY = ["classes"] as const;

export function useClasses() {
  return useQuery({
    queryKey: QUERY_KEY,
    queryFn: getClasses,
    staleTime: Infinity,
  });
}

export function useCreateClass() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cls: Class) => createClass(cls),
    onSuccess: (created) => {
      qc.setQueryData<Class[]>(QUERY_KEY, (old) => (old ? [...old, created] : [created]));
    },
  });
}

export function useUpdateClass() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, name }: { id: string; name: string | null }) => updateClass(id, name),
    onSuccess: (updated) => {
      qc.setQueryData<Class[]>(QUERY_KEY, (old) => old?.map((c) => (c.id === updated.id ? updated : c)));
    },
  });
}

export function useDeleteClass() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteClass(id),
    onSuccess: (_data, id) => {
      qc.setQueryData<Class[]>(QUERY_KEY, (old) => old?.filter((c) => c.id !== id));
    },
  });
}
