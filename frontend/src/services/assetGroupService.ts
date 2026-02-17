/** API service for asset group (skupina majetku) dictionary CRUD operations. */
import authorizedRequest from "../utils/authorizedRequest";
import { AssetGroup } from "../types/assetGroup";

export const getAssetGroups = async (): Promise<AssetGroup[]> => {
  const res = await authorizedRequest.get("/asset-group");
  return res.data;
};

export const createAssetGroup = async (assetGroup: AssetGroup): Promise<AssetGroup> => {
  const res = await authorizedRequest.post("/asset-group", assetGroup);
  return res.data;
};

export const updateAssetGroup = async (id: string, name: string | null): Promise<AssetGroup> => {
  const res = await authorizedRequest.patch(`/asset-group/${encodeURIComponent(id)}`, { name });
  return res.data;
};

export const deleteAssetGroup = async (id: string): Promise<void> => {
  await authorizedRequest.delete(`/asset-group/${encodeURIComponent(id)}`);
};
