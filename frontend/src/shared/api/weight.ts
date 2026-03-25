/**
 * Weight API
 * Handles weight entry (check-in) operations
 */
import { api } from "./client";
import type { WeightEntry, PaginatedResponse } from "../types";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

// Backend response structure (snake_case)
type WeightEntryBackendResponse = {
    id: string;
    weight_kg: number;
    measured_at: string; // ISO timestamp
    note?: string;
};

// Transform backend response to frontend type
function transformWeightEntry(data: WeightEntryBackendResponse | null): WeightEntry | null {
    if (!data) return null;

    const date = data.measured_at ? new Date(data.measured_at).toISOString().split("T")[0] : new Date().toISOString().split("T")[0];

    return {
        id: data.id,
        userId: "", // Backend doesn't include userId in response
        weight: data.weight_kg,
        date,
        notes: data.note,
        createdAt: data.measured_at,
    };
}

/**
 * Record a weight check-in
 * Frontend format: { weight: number, date?: string (YYYY-MM-DD), notes?: string }
 * Backend expects: { weightKg: number, measuredAt?: OffsetDateTime, note?: string }
 */
export async function recordWeight(
    data: { weight: number; date?: string; notes?: string },
    token: string
): Promise<WeightEntry> {
    const todayIso = new Date().toISOString().split("T")[0];
    const measuredAt = data.date
        ? (data.date === todayIso
            ? new Date().toISOString()
            : new Date(`${data.date}T12:00:00Z`).toISOString())
        : undefined;
    // Transform frontend format to backend format
    const backendRequest = {
        weightKg: data.weight,
        measuredAt,
        note: data.notes,
    };

    const response = await api.post<WeightEntryBackendResponse>("/api/weight", backendRequest, token);
    return transformWeightEntry(response)!;
}

/**
 * Get latest weight entry
 */
export async function getLatestWeight(token: string): Promise<WeightEntry | null> {
    const response = await api.get<WeightEntryBackendResponse | null | ApiResponse<WeightEntryBackendResponse | null>>("/api/weight/latest", token);
    return transformWeightEntry(unwrapApiData(response));
}

/**
 * Get weight history (paginated)
 */
export async function getWeightHistory(
    params: { page?: number; size?: number; sort?: string },
    token: string
): Promise<PaginatedResponse<WeightEntry>> {
    const query = new URLSearchParams(
        Object.entries(params)
            .filter(([_, v]) => v !== undefined)
            .map(([k, v]) => [k, String(v)])
    );
    const queryStr = query.toString();
    const path = `/api/weight/history${queryStr ? "?" + queryStr : ""}`;
    const response = await api.get<PaginatedResponse<WeightEntryBackendResponse>>(path, token);
    return {
        ...response,
        content: response.content.map(transformWeightEntry).filter((w) => w !== null) as WeightEntry[],
    };
}

/**
 * Get weight entries for a specific date range
 */
export async function getWeightByDateRange(
    startDate: string,
    endDate: string,
    token: string
): Promise<WeightEntry[]> {
    const query = new URLSearchParams({ startDate, endDate }).toString();
    const response = await api.get<WeightEntryBackendResponse[]>(`/api/weight/range?${query}`, token);
    return response.map(transformWeightEntry).filter((w) => w !== null) as WeightEntry[];
}

/**
 * Update weight entry
 */
export async function updateWeightEntry(
    id: string,
    data: Partial<WeightEntry>,
    token: string
): Promise<WeightEntry> {
    const todayIso = new Date().toISOString().split("T")[0];
    const measuredAt = data.date
        ? (data.date === todayIso
            ? new Date().toISOString()
            : new Date(`${data.date}T12:00:00Z`).toISOString())
        : undefined;
    // Transform frontend format to backend format
    const backendRequest = {
        weightKg: data.weight,
        measuredAt,
        note: data.notes,
    };
    const response = await api.patch<WeightEntryBackendResponse>(`/api/weight/${id}`, backendRequest, token);
    return transformWeightEntry(response)!;
}

/**
 * Delete weight entry
 */
export function deleteWeightEntry(id: string, token: string): Promise<void> {
    return api.del<void>(`/api/weight/${id}`, token);
}
