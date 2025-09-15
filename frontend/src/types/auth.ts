export interface User {
  id: string;
  name: string;
  username: string;
  role: 'ADMIN' | 'USER';
}

export interface LoginResponse {
  token: string;
  user: User;
  expiresIn: string;
}

export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}

export interface RegisterUserDto {
  id: string;
  username: string;
  name: string;
  password: string;
}

