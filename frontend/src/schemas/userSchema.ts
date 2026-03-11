/** Zod schemas and types for user-related forms (registration, password change). */
import { z } from "zod";

/** Schema for the "register new user" admin dialog. */
export const registerUserSchema = z.object({
  id: z
    .string()
    .min(1, "ID nesmí být prázdné.")
    .transform((v) => v.trim()),

  username: z
    .string()
    .min(1, "Uživatelské jméno nesmí být prázdné.")
    .transform((v) => v.trim()),

  name: z.string(),

  password: z
    .string()
    .min(1, "Heslo nesmí být prázdné.")
    .transform((v) => v.trim()),
});

export type RegisterUserFormValues = z.input<typeof registerUserSchema>;
export type RegisterUserFormOutput = z.output<typeof registerUserSchema>;

/** Schema for the "change password" form. */
export const changePasswordSchema = z.object({
  currentPassword: z.string().min(1, "Aktuální heslo musí být vyplněno."),

  newPassword: z.string().min(1, "Nové heslo musí být vyplněno."),
});

export type ChangePasswordFormValues = z.input<typeof changePasswordSchema>;
