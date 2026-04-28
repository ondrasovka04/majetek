/**
 * Safely extract an error message from an unknown caught value.
 * Handles Axios errors, standard Error instances, and arbitrary values.
 */
export function extractErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    // Axios errors have response.data.message, but also extend Error
    const axiosLike = error as {
      response?: { data?: { message?: string } };
    };
    if (axiosLike.response?.data?.message) {
      return axiosLike.response.data.message;
    }
    return error.message;
  }

  if (typeof error === "object" && error !== null) {
    const obj = error as {
      response?: { data?: { message?: string } };
      message?: string;
    };
    if (obj.response?.data?.message) return obj.response.data.message;
    if (obj.message) return obj.message;
  }

  if (typeof error === "string") return error;
  if (typeof error === "number" || typeof error === "boolean") return error.toString();
  return "Unknown error";
}
