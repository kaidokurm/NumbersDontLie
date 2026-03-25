import type { ApiResponse } from "../types";

export function unwrapApiData<T>(response: T | ApiResponse<T>): T {
    if (response && typeof response === "object" && "data" in (response as object)) {
        return (response as ApiResponse<T>).data;
    }
    return response as T;
}
