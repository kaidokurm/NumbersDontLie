import { api } from "./client";
import type { ActivityCheckin, PaginatedResponse } from "../types";
import { unwrapApiData } from "./unwrap";
import type { ApiResponse } from "../types";

type ActivityBackendResponse = {
    id: string;
    activity_type: string;
    duration_minutes?: number;
    intensity?: string;
    note?: string;
    checkin_at: string;
};

function toActivityCheckin(data: ActivityBackendResponse): ActivityCheckin {
    return {
        id: data.id,
        activityType: data.activity_type,
        durationMinutes: data.duration_minutes,
        intensity: data.intensity,
        note: data.note,
        checkinAt: data.checkin_at,
    };
}

export async function recordActivityCheckin(
    data: { activityType: string; durationMinutes?: number; intensity?: string; note?: string; checkinAt?: string },
    token: string
): Promise<ActivityCheckin> {
    const response = await api.post<ActivityBackendResponse>("/api/activity", {
        activityType: data.activityType,
        durationMinutes: data.durationMinutes,
        intensity: data.intensity,
        note: data.note,
        checkinAt: data.checkinAt,
    }, token);
    return toActivityCheckin(response);
}

export async function getActivityHistory(
    params: { page?: number; size?: number },
    token: string
): Promise<PaginatedResponse<ActivityCheckin>> {
    const query = new URLSearchParams(
        Object.entries(params)
            .filter(([_, v]) => v !== undefined)
            .map(([k, v]) => [k, String(v)])
    );
    const queryStr = query.toString();
    const path = `/api/activity/history${queryStr ? `?${queryStr}` : ""}`;
    const response = await api.get<PaginatedResponse<ActivityBackendResponse>>(path, token);
    return {
        ...response,
        content: response.content.map(toActivityCheckin),
    };
}

export async function getLatestActivity(token: string): Promise<ActivityCheckin | null> {
    const response = await api.get<ActivityBackendResponse | null | ApiResponse<ActivityBackendResponse | null>>(
        "/api/activity/latest",
        token
    );
    const unwrapped = unwrapApiData(response);
    return unwrapped ? toActivityCheckin(unwrapped) : null;
}
