/** TanStack Query hooks for card attachment operations (list, upload, delete). */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getAttachments, uploadAttachment, deleteAttachment } from "@/services/attachmentService";

export function useAttachments(cardId: string | undefined) {
  return useQuery({
    queryKey: ["attachments", cardId],
    queryFn: () => getAttachments(cardId!),
    enabled: !!cardId,
  });
}

export function useUploadAttachment(cardId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => uploadAttachment(cardId, file),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["attachments", cardId] });
    },
  });
}

export function useDeleteAttachment(cardId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteAttachment(cardId, id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["attachments", cardId] });
    },
  });
}
