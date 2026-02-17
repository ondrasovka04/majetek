/** API service functions for authentication — login, token refresh, password change, and user registration. */
import axios from "axios";
import { LoginResponse, User } from "../types/auth";
import { ChangePasswordDto, RegisterUserDto } from "../types/auth";
import authorizedRequest from "../utils/authorizedRequest";

const API_URL = import.meta.env.VITE_API_URL;

export async function login(username: string, password: string): Promise<LoginResponse> {
  const response = await axios.post<LoginResponse>(
    `${API_URL}/auth/login`,
    {},
    {
      headers: { authorization: `Basic ${btoa(`${username}:${password}`)}` },
      withCredentials: true,
    },
  );
  return response.data;
}

export async function refresh(): Promise<LoginResponse> {
  const res = await axios.post<LoginResponse>(`${API_URL}/auth/refresh`, {}, { withCredentials: true });
  return res.data;
}

export const changePassword = async (data: ChangePasswordDto): Promise<void> => {
  await authorizedRequest.put("/auth/change-password", data);
};

export const registerUser = async (data: RegisterUserDto): Promise<void> => {
  await authorizedRequest.post("/auth/register", data);
};

export const getUsers = async (): Promise<User[]> => {
  const res = await authorizedRequest.get("/auth");
  return res.data;
};
