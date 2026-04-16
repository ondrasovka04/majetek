/** API service for write-off method (způsob vyřazení) dictionary CRUD operations. */
import authorizedRequest from "@/utils/authorizedRequest";
import { WriteOffMethod } from "@/types/writeOffMethod";

export const getWriteOffMethods = async (): Promise<WriteOffMethod[]> => {
  const res = await authorizedRequest.get("/write-off-method");
  return res.data;
};

export const createWriteOffMethod = async (method: WriteOffMethod): Promise<WriteOffMethod> => {
  const res = await authorizedRequest.post("/write-off-method", method);
  return res.data;
};

export const updateWriteOffMethod = async (id: string, name: string | null): Promise<WriteOffMethod> => {
  const res = await authorizedRequest.patch(`/write-off-method/${encodeURIComponent(id)}`, { name });
  return res.data;
};

export const deleteWriteOffMethod = async (id: string): Promise<void> => {
  await authorizedRequest.delete(`/write-off-method/${encodeURIComponent(id)}`);
};
