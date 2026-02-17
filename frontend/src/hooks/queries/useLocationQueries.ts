/** TanStack Query hooks for location CRUD and label printing. */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getLocations,
  createLocation,
  updateLocation,
  deleteLocation,
  printLocation,
} from "../../services/locationService";
import { Location } from "../../types/location";

const QUERY_KEY = ["locations"] as const;

export function useLocations() {
  return useQuery({
    queryKey: QUERY_KEY,
    queryFn: getLocations,
  });
}

export function useCreateLocation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (loc: Location) => createLocation(loc),
    onSuccess: (created) => {
      qc.setQueryData<Location[]>(QUERY_KEY, (old) =>
        old ? [...old, created] : [created],
      );
    },
  });
}

export function useUpdateLocation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, name }: { id: string; name: string | null }) =>
      updateLocation(id, name),
    onSuccess: (updated) => {
      qc.setQueryData<Location[]>(QUERY_KEY, (old) =>
        old?.map((l) => (l.id === updated.id ? updated : l)),
      );
    },
  });
}

export function useDeleteLocation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteLocation(id),
    onSuccess: (_data, id) => {
      qc.setQueryData<Location[]>(QUERY_KEY, (old) =>
        old?.filter((l) => l.id !== id),
      );
    },
  });
}

export function usePrintLocation() {
  return useMutation({
    mutationFn: (id: string) => printLocation(id),
  });
}
