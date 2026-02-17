/** Application user returned by the auth API. */
export interface User {
  id: string;
  name: string;
  username: string;
  role: 'ADMIN' | 'USER';
}

/** Successful login response containing JWT token and user info. */
export interface LoginResponse {
  token: string;
  user: User;
  expiresIn: string;
}

/** DTO for changing the current user's password. */
export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}

/** DTO for registering a new user (admin only). */
export interface RegisterUserDto {
  id: string;
  username: string;
  name: string;
  password: string;
}

