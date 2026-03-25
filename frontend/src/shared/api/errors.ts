import { ApiError } from "./client";

export function isApiError(e: unknown): e is ApiError {
    return e instanceof ApiError;
}

export function explainApiError(e: unknown): string {
    if (e instanceof ApiError) {
        const msg = e.bodyJson?.message || e.message || "";
        if (e.status === 401) return "You are not authorized. Please log in again.";
        if (e.status === 403) return "Forbidden. You do not have permission for this action.";
        if (e.status === 404) return msg || "Data not found. You may need to complete setup.";
        if (e.status === 400) return msg || "Validation failed or invalid request. Please check your input.";
        if (e.status >= 500) return "Server error. Please try again in a moment.";
        return msg || `Request failed (HTTP ${e.status}).`;
    }
    return e instanceof Error ? e.message : "Unknown error";
}

export function fieldErrorsByName(e: unknown): Record<string, string> {
    if (!(e instanceof ApiError)) return {};
    const fes = e.bodyJson?.fieldErrors ?? [];
    // if multiple errors per field later, last wins; can change to join("\n")
    return Object.fromEntries(fes.map((fe: { field: any; message: any; }) => [fe.field, fe.message]));
}