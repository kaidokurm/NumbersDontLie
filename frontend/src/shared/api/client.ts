// API client wrapper for backend communication
// Handles base URL, JSON, error handling, and token injection
import { API_BASE_URL } from "../config";
import type { ApiErrorBody } from "./errorTypes";


// Custom error class for API errors
export class ApiError extends Error {
    status: number;
    bodyText?: string;
    bodyJson?: ApiErrorBody;

    constructor(status: number, message: string, bodyText?: string, bodyJson?: ApiErrorBody) {
        super(message);
        this.status = status;
        this.bodyText = bodyText;
        this.bodyJson = bodyJson;
    }
}

// Helper to safely parse JSON error bodies
async function tryParseJson(text: string): Promise<ApiErrorBody | undefined> {
    try {
        return JSON.parse(text) as ApiErrorBody;
    } catch {
        return undefined;
    }
}

// Main API fetch function
export async function apiFetch<T>(
    path: string,
    options: RequestInit & { token?: string | null } = {}
): Promise<T> {
    const url = `${API_BASE_URL}${path}`;

    const headers = new Headers(options.headers);
    if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json");
    if (options.token) headers.set("Authorization", `Bearer ${options.token}`);

    const res = await fetch(url, { ...options, headers });

    if (!res.ok) {
        const bodyText = await res.text().catch(() => "");
        const bodyJson = bodyText ? await tryParseJson(bodyText) : undefined;
        // Prefer backend "message" when available
        const message = bodyJson?.message || bodyText || `HTTP ${res.status}`;
        throw new ApiError(res.status, message, bodyText, bodyJson);
    }

    if (res.status === 204) return undefined as unknown as T;

    // Read response body once and handle empty/null cases
    const bodyText = await res.text().catch(() => "");

    // Return null for empty responses
    if (!bodyText || bodyText === "null") {
        return null as unknown as T;
    }

    // Parse and return JSON
    return JSON.parse(bodyText) as T;
}

// Typed API helpers for common HTTP verbs
export const api = {
    get: <T>(path: string, token?: string | null) => apiFetch<T>(path, { method: "GET", token }),
    post: <T>(path: string, body: unknown, token?: string | null) =>
        apiFetch<T>(path, { method: "POST", body: JSON.stringify(body), token }),
    put: <T>(path: string, body: unknown, token?: string | null) =>
        apiFetch<T>(path, { method: "PUT", body: JSON.stringify(body), token }),
    patch: <T>(path: string, body: unknown, token?: string | null) =>
        apiFetch<T>(path, { method: "PATCH", body: JSON.stringify(body), token }),
    del: <T>(path: string, token?: string | null) => apiFetch<T>(path, { method: "DELETE", token }),
};

// Example usage:
// const user = await api.get<User>("/api/me", token);
