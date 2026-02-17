/** Converts a Date, ISO string, null, or undefined to a "yyyy-MM-dd" string (or null). */
import { format } from "date-fns";

export const toIsoDateString = (d: Date | string | null | undefined): string | null => {
  if (!d) return null;
  if (d instanceof Date) return format(d, "yyyy-MM-dd");
  return d;
};
