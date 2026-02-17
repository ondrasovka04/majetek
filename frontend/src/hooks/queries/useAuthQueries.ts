/** TanStack Query mutation hooks for authentication actions (change password, register user). */
import { useMutation } from "@tanstack/react-query";
import { changePassword, registerUser } from "../../services/authService";
import { ChangePasswordDto, RegisterUserDto } from "../../types/auth";

export function useChangePassword() {
  return useMutation({
    mutationFn: (data: ChangePasswordDto) => changePassword(data),
  });
}

export function useRegisterUser() {
  return useMutation({
    mutationFn: (data: RegisterUserDto) => registerUser(data),
  });
}
