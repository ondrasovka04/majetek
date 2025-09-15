import authorizedRequest from "../utils/authorizedRequest";
import { AttachmentDto } from "../types/attachment";

export const getAttachments = async (
  cardId: string
): Promise<AttachmentDto[]> => {
  const res = await authorizedRequest.get(`card/${cardId}/attachments`);
  return res.data;
};

export const downloadAttachment = async (
  cardId: string,
  id: number
): Promise<Blob> => {
  const res = await authorizedRequest.get(`card/${cardId}/attachments/${id}`, {
    responseType: "blob",
  });
  return res.data;
};

export const deleteAttachment = async (cardId: string, id: number) => {
  await authorizedRequest.delete(`card/${cardId}/attachments/${id}`);
};

export const uploadAttachment = async (cardId: string, file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  await authorizedRequest.post(`card/${cardId}/attachments`, formData);
};
