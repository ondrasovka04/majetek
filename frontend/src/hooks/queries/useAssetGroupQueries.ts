/** TanStack Query hooks for asset group (skupina majetku) CRUD with cache invalidation. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getAssetGroups, createAssetGroup, updateAssetGroup, deleteAssetGroup } from "../../services/assetGroupService";
import { AssetGroup } from "../../types/assetGroup";

const QUERY_KEY = ["assetGroups"] as const;

export function useAssetGroups() {
  return useQuery({
    queryKey: QUERY_KEY,
    queryFn: getAssetGroups,
  });
}

export function useCreateAssetGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (group: AssetGroup) => createAssetGroup(group),
    onSuccess: (created) => {
      qc.setQueryData<AssetGroup[]>(QUERY_KEY, (old) => (old ? [...old, created] : [created]));
    },
  });
}

export function useUpdateAssetGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, name }: { id: string; name: string | null }) => updateAssetGroup(id, name),
    onSuccess: (updated) => {
      qc.setQueryData<AssetGroup[]>(QUERY_KEY, (old) => old?.map((g) => (g.id === updated.id ? updated : g)));
    },
  });
}

export function useDeleteAssetGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteAssetGroup(id),
    onSuccess: (_data, id) => {
      qc.setQueryData<AssetGroup[]>(QUERY_KEY, (old) => old?.filter((g) => g.id !== id));
    },
  });
}
