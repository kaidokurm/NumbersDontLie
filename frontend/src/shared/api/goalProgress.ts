import { api } from "./client";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

type GoalProgressBackendResponse = {
    current_value: number;
    progress_percentage: number;
    is_on_track: boolean;
    days_remaining: number | null;
    milestones_completed: number;
    milestone_details: Array<Record<string, unknown>> | null;
    recorded_at: string;
    created_at: string;
    updated_at: string;
};

export type GoalProgress = {
    currentValue: number;
    progressPercentage: number;
    isOnTrack: boolean;
    daysRemaining: number | null;
    milestonesCompleted: number;
    milestoneDetails: Array<Record<string, unknown>>;
    recordedAt: string;
    createdAt: string;
    updatedAt: string;
};

export type GoalMilestone = {
    percentage: number;
    completedAt: string;
};

function transformGoalProgress(data: GoalProgressBackendResponse): GoalProgress {
    return {
        currentValue: data.current_value,
        progressPercentage: data.progress_percentage ?? 0,
        isOnTrack: data.is_on_track,
        daysRemaining: data.days_remaining ?? null,
        milestonesCompleted: data.milestones_completed ?? 0,
        milestoneDetails: data.milestone_details ?? [],
        recordedAt: data.recorded_at,
        createdAt: data.created_at,
        updatedAt: data.updated_at,
    };
}

export async function getCurrentGoalProgress(goalId: string, token: string): Promise<GoalProgress | null> {
    try {
        const response = await api.get<GoalProgressBackendResponse | ApiResponse<GoalProgressBackendResponse>>(
            `/api/goals/${goalId}/progress`,
            token
        );
        return transformGoalProgress(unwrapApiData(response));
    } catch {
        return null;
    }
}

function toMilestones(data: GoalProgressBackendResponse): GoalMilestone[] {
    const details = data.milestone_details ?? [];
    const milestones: GoalMilestone[] = [];
    for (const item of details) {
        const percentageRaw = item.percentage;
        const completedAtRaw = item.completed_at;
        const percentage = typeof percentageRaw === "number" ? percentageRaw : Number(percentageRaw);
        const completedAt = typeof completedAtRaw === "string" ? completedAtRaw : data.recorded_at;
        if (Number.isFinite(percentage) && completedAt) {
            milestones.push({ percentage, completedAt });
        }
    }
    return milestones;
}

export async function getGoalMilestones(goalId: string, token: string): Promise<GoalMilestone[]> {
    try {
        const response = await api.get<GoalProgressBackendResponse[] | ApiResponse<GoalProgressBackendResponse[]>>(
            `/api/goals/${goalId}/progress/history`,
            token
        );
        const history = unwrapApiData(response);
        if (!Array.isArray(history)) {
            return [];
        }
        return history.flatMap(toMilestones);
    } catch {
        return [];
    }
}
