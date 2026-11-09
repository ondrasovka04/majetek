/** Retry policy for TanStack Query: retry once on network/server errors, never on 4xx client errors. */
import { isAxiosError } from "axios";

const MAX_RETRIES = 1;

export const shouldRetryQuery = (failureCount: number, error: unknown): boolean => {
  if (failureCount >= MAX_RETRIES) return false;
  const status = isAxiosError(error) ? error.response?.status : undefined;
  return status === undefined || status >= 500;
};
