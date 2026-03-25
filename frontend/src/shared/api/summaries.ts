/**
 * Period Summaries API
 * Handles weekly and monthly health summaries
 */
import { api } from "./client";
import type { PeriodSummary } from "../types";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

// Backend response structure (snake_case)
type PeriodSummaryBackendResponse = {
    period_type: "weekly" | "monthly";
    start_date: string;
    end_date: string;
    weight_start_kg: number | null;
    weight_end_kg: number | null;
    weight_change_kg: number | null;
    avg_wellness_score: number;
    activity_level: string;
    goal_progress_percentage: number;
    days_tracked: number;
    weight_entries_count: number;
    generated_at: string;
};

// Transform backend response to frontend type
function transformPeriodSummary(data: PeriodSummaryBackendResponse): PeriodSummary {
    return {
        periodType: data.period_type,
        startDate: data.start_date,
        endDate: data.end_date,
        weightStartKg: data.weight_start_kg,
        weightEndKg: data.weight_end_kg,
        weightChangeKg: data.weight_change_kg,
        avgWellnessScore: data.avg_wellness_score,
        activityLevel: data.activity_level,
        goalProgressPercentage: data.goal_progress_percentage,
        daysTracked: data.days_tracked,
        weightEntriesCount: data.weight_entries_count,
        generatedAt: data.generated_at,
    };
}

/**
 * Get weekly health summary (last 7 days)
 */
export function getWeeklySummary(token: string): Promise<PeriodSummary> {
    return api
        .get<PeriodSummaryBackendResponse | ApiResponse<PeriodSummaryBackendResponse>>("/api/summary/weekly", token)
        .then((response) => transformPeriodSummary(unwrapApiData(response)));
}

/**
 * Get monthly health summary (last 30 days)
 */
export function getMonthlySummary(token: string): Promise<PeriodSummary> {
    return api
        .get<PeriodSummaryBackendResponse | ApiResponse<PeriodSummaryBackendResponse>>("/api/summary/monthly", token)
        .then((response) => transformPeriodSummary(unwrapApiData(response)));
}
