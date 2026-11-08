import { describe, it, expect } from "vite-plus/test";
import { extractErrorMessage } from "@/utils/extractErrorMessage";

describe("extractErrorMessage", () => {
  it("extracts message from standard Error", () => {
    expect(extractErrorMessage(new Error("something broke"))).toBe("something broke");
  });

  it("extracts message from Axios-like error (response.data.message)", () => {
    const axiosError = Object.assign(new Error("Request failed"), {
      response: { data: { message: "Server validation error" } },
    });
    expect(extractErrorMessage(axiosError)).toBe("Server validation error");
  });

  it("extracts message from plain object with response.data.message", () => {
    const obj = { response: { data: { message: "Not found" } } };
    expect(extractErrorMessage(obj)).toBe("Not found");
  });

  it("extracts message from plain object with message property", () => {
    expect(extractErrorMessage({ message: "oops" })).toBe("oops");
  });

  it("converts string to itself", () => {
    expect(extractErrorMessage("raw string error")).toBe("raw string error");
  });

  it("converts number to string", () => {
    expect(extractErrorMessage(404)).toBe("404");
  });

  it("converts null to string", () => {
    expect(extractErrorMessage(null)).toBe("null");
  });

  it("converts undefined to string", () => {
    expect(extractErrorMessage(undefined)).toBe("undefined");
  });

  it("prefers Axios response over Error.message", () => {
    const err = Object.assign(new Error("generic"), {
      response: { data: { message: "specific backend error" } },
    });
    expect(extractErrorMessage(err)).toBe("specific backend error");
  });
});
