import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getWeightHistory } from "../../shared/api/weight";
import { getPrimaryActiveGoal } from "../../shared/api/goals";
import { getGoalMilestones } from "../../shared/api/goalProgress";
import type { WeightEntry, PaginatedResponse } from "../../shared/types";
import { useAppAuth } from "../../shared/auth/AuthContext";

export type WeightRange = "30" | "90" | "all";

export interface ChartDataPoint {
    date: string; // YYYY-MM-DD
    weight: number; // kg
    daysAgo: number;
}

export interface MilestoneMarker {
    daysAgo: number;
    percentage: number;
    date: string;
}

export interface WeightChartState {
    points: ChartDataPoint[];
    milestones: MilestoneMarker[];
    targetWeight: number | null;
    minWeight: number;
    maxWeight: number;
    isLoading: boolean;
    error: Error | null;
}

export function useWeightChartData(range: WeightRange = "90"): WeightChartState {
    const { isAuthenticated } = useAppAuth();
    const sizeByRange: Record<WeightRange, number> = {
        "30": 30,
        "90": 90,
        all: 1000,
    };

    // Fetch weight history by selected range
    const weightQ = useAuthedQuery(
        `weightHistory-${range}`,
        (token: string) => getWeightHistory({ size: sizeByRange[range] }, token),
        isAuthenticated
    );

    // Fetch active goal for target weight reference
    const activeGoalQ = useAuthedQuery("activeGoal", getPrimaryActiveGoal, isAuthenticated);
    const activeGoalId = activeGoalQ.data?.id;
    const milestonesQ = useAuthedQuery(
        `goalMilestones-${activeGoalId || "none"}`,
        (token: string) => getGoalMilestones(activeGoalId as string, token),
        isAuthenticated && !!activeGoalId
    );

    // Process weight data into chart points
    const points: ChartDataPoint[] = [];
    let minWeight = Infinity;
    let maxWeight = -Infinity;

    if (weightQ.data) {
        const entries = (weightQ.data as unknown as PaginatedResponse<WeightEntry>).content || [];

        // Sort by date ascending for proper plotting
        const sorted = [...entries].sort(
            (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
        );

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        sorted.forEach((entry) => {
            const entryDate = new Date(entry.date);
            entryDate.setHours(0, 0, 0, 0);
            const daysAgo = Math.floor(
                (today.getTime() - entryDate.getTime()) / (1000 * 60 * 60 * 24)
            );

            points.push({
                date: entry.date,
                weight: entry.weight,
                daysAgo,
            });

            minWeight = Math.min(minWeight, entry.weight);
            maxWeight = Math.max(maxWeight, entry.weight);
        });
    }

    // Add 10% padding to min/max for chart spacing
    const padding = (maxWeight - minWeight) * 0.1 || 5;
    const adjustedMin = minWeight === Infinity ? 70 : Math.floor(minWeight - padding);
    const adjustedMax = maxWeight === -Infinity ? 80 : Math.ceil(maxWeight + padding);
    const milestones: MilestoneMarker[] = [];
    const unique = new Set<string>();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (const milestone of milestonesQ.data || []) {
        const dateObj = new Date(milestone.completedAt);
        if (Number.isNaN(dateObj.getTime())) continue;
        dateObj.setHours(0, 0, 0, 0);
        const daysAgo = Math.floor((today.getTime() - dateObj.getTime()) / (1000 * 60 * 60 * 24));
        if (daysAgo < 0) continue;
        const key = `${milestone.percentage}-${daysAgo}`;
        if (unique.has(key)) continue;
        unique.add(key);
        milestones.push({
            daysAgo,
            percentage: milestone.percentage,
            date: dateObj.toISOString().split("T")[0],
        });
    }

    return {
        points,
        milestones,
        targetWeight: activeGoalQ.data?.targetValue || null,
        minWeight: adjustedMin,
        maxWeight: adjustedMax,
        isLoading: weightQ.loading || activeGoalQ.loading || milestonesQ.loading,
        error: weightQ.error || activeGoalQ.error || milestonesQ.error || null,
    };
}
