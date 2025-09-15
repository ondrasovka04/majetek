import authorizedRequest from "../utils/authorizedRequest";
import { Location } from "../types/location";

export const getLocations = async (): Promise<Location[]> => {
  const res = await authorizedRequest.get("/location");
  return res.data;
};

export const createLocation = async (location: Location): Promise<Location> => {
  const res = await authorizedRequest.post("/location", location);
  return res.data;
};

export const updateLocation = async (
  id: string,
  name: string | null
): Promise<Location> => {
  const res = await authorizedRequest.patch(
    `/location/${encodeURIComponent(id)}`,
    { name }
  );
  return res.data;
};

export const deleteLocation = async (id: string): Promise<void> => {
  await authorizedRequest.delete(`/location/${encodeURIComponent(id)}`);
};

export const printLocation = async (id: String): Promise<string> => {
  await authorizedRequest.post("/location/print", { id });
  return "Štítek byl úspěšně odeslán do tiskárny.";
};
