import authorizedRequest from "../utils/authorizedRequest";
import { PurchaseMethod } from "../types/purchaseMethod";

export const getPurchaseMethods = async (): Promise<PurchaseMethod[]> => {
  const res = await authorizedRequest.get("/purchase-method");
  return res.data;
};

export const createPurchaseMethod = async (data: PurchaseMethod): Promise<PurchaseMethod> => {
  const res = await authorizedRequest.post("/purchase-method", data);
  return res.data;
};

export const updatePurchaseMethod = async (id: string, name: string | null): Promise<PurchaseMethod> => {
  const res = await authorizedRequest.patch(`/purchase-method/${encodeURIComponent(id)}`, { name });
  return res.data;
};

export const deletePurchaseMethod = async (id: string): Promise<void> => {
  await authorizedRequest.delete(`/purchase-method/${encodeURIComponent(id)}`);
};
