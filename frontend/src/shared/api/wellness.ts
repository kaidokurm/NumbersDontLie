import { api } from "./client";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

export type WellnessScore = {
    score: number;
    description: string;
};

export type WellnessHistoryPoint = {
    weekStart: string;
    weekEnd: string;
    score: number;
    bmiScore: number;
    activityScore: number;
    goalScore: number;
    habitsScore: number;
};

type WellnessScoreBackendResponse = {
    score: number;
    description: string;
};

type WellnessHistoryBackendResponse = {
    week_start: string;
    week_end: string;
    score: number;
    bmi_score: number;
    activity_score: number;
    goal_score: number;
    habits_score: number;
};

function transformWellnessScore(data: WellnessScoreBackendResponse): WellnessScore {
    return {
        score: data.score,
        description: data.description,
    };
}

function transformWellnessHistoryPoint(data: WellnessHistoryBackendResponse): WellnessHistoryPoint {
    return {
        weekStart: data.week_start,
        weekEnd: data.week_end,
        score: data.score,
        bmiScore: data.bmi_score ?? 0,
        activityScore: data.activity_score ?? 0,
        goalScore: data.goal_score ?? 0,
        habitsScore: data.habits_score ?? 0,
    };
}

export function getWellnessScore(token: string): Promise<WellnessScore> {
    return api
        .get<WellnessScoreBackendResponse | ApiResponse<WellnessScoreBackendResponse>>("/api/wellness-score", token)
        .then((response) => transformWellnessScore(unwrapApiData(response)));
}

export function calculateWellnessScore(token: string): Promise<WellnessScore> {
    return api
        .post<WellnessScoreBackendResponse | ApiResponse<WellnessScoreBackendResponse>>(
            "/api/wellness-score/calculate",
            {},
            token
        )
        .then((response) => transformWellnessScore(unwrapApiData(response)));
}

export function getWellnessHistory(token: string, weeks = 12): Promise<WellnessHistoryPoint[]> {
    return api
        .get<WellnessHistoryBackendResponse[] | ApiResponse<WellnessHistoryBackendResponse[]>>(
            `/api/wellness-score/history?weeks=${weeks}`,
            token
        )
        .then((response) => {
            const data = unwrapApiData(response);
            return Array.isArray(data) ? data.map(transformWellnessHistoryPoint) : [];
        });
}
