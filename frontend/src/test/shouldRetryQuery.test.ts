import { describe, it, expect } from "vite-plus/test";
import { AxiosError, AxiosHeaders } from "axios";
import { shouldRetryQuery } from "@/utils/shouldRetryQuery";

const axiosErrorWithStatus = (status: number) =>
  new AxiosError("error", undefined, undefined, undefined, {
    status,
    statusText: "",
    data: {},
    headers: {},
    config: { headers: new AxiosHeaders() },
  });

describe("shouldRetryQuery", () => {
  it("does not retry client errors (4xx)", () => {
    expect(shouldRetryQuery(0, axiosErrorWithStatus(400))).toBe(false);
    expect(shouldRetryQuery(0, axiosErrorWithStatus(404))).toBe(false);
  });

  it("retries server errors (5xx) once", () => {
    expect(shouldRetryQuery(0, axiosErrorWithStatus(503))).toBe(true);
    expect(shouldRetryQuery(1, axiosErrorWithStatus(503))).toBe(false);
  });

  it("retries network errors without a response once", () => {
    expect(shouldRetryQuery(0, new AxiosError("Network Error"))).toBe(true);
    expect(shouldRetryQuery(1, new AxiosError("Network Error"))).toBe(false);
  });

  it("retries non-axios errors once", () => {
    expect(shouldRetryQuery(0, new Error("boom"))).toBe(true);
    expect(shouldRetryQuery(1, new Error("boom"))).toBe(false);
  });
});
