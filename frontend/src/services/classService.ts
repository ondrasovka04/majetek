import authorizedRequest from "../utils/authorizedRequest";
import { Class } from "../types/class";

export const getClasses = async (): Promise<Class[]> => {
  const res = await authorizedRequest.get("/class");
  return res.data;
};

export const createClass = async (cls: Class): Promise<Class> => {
  const res = await authorizedRequest.post("/class", cls);
  return res.data;
};

export const updateClass = async (id: string, name: string | null): Promise<Class> => {
  const res = await authorizedRequest.patch(`/class/${encodeURIComponent(id)}`, { name });
  return res.data;
};

export const deleteClass = async (id: string): Promise<void> => {
  await authorizedRequest.delete(`/class/${encodeURIComponent(id)}`);
};
