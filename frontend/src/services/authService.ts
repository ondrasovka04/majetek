import axios from 'axios';
import { LoginResponse, User } from '../types/auth';
import { ChangePasswordDto, RegisterUserDto } from '../types/auth';
import authorizedRequest from '../utils/authorizedRequest';

const API_URL = process.env.REACT_APP_API_URL;

export async function login(username: string, password: string): Promise<LoginResponse> {
  const response = await axios.post<LoginResponse>(`${API_URL}/auth/login`, {}, {
    headers : {
      'authorization': `Basic ${btoa(`${username}:${password}`)}`,
    }
  });
  return response.data;
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