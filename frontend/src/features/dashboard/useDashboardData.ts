import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getMe, getHealthProfile } from "../../shared/api/profile";
import { getLatestWeight } from "../../shared/api/weight";
import { getLatestInsight } from "../../shared/api/insights";
import { getActiveGoals } from "../../shared/api/goals";
import { getCurrentGoalProgress } from "../../shared/api/goalProgress";
import { getHealthSummary } from "../../shared/api/summary";
import { getWeeklySummary, getMonthlySummary } from "../../shared/api/summaries";
import { getPrivacyPreferences } from "../../shared/api/privacy";
import { getActivityHistory } from "../../shared/api/activity";
import { useAppAuth } from "../../shared/auth/AuthContext";
import type { UserProfile, HealthProfile, WeightEntry, Goal, Insight, HealthSummary, PeriodSummary } from "../../shared/types";

export interface DashboardData {
    me: UserProfile | null;
    profile: HealthProfile | null;
    latestWeight: WeightEntry | null;
    activeGoals: Goal[];
    insight: Insight | null;
    summary: HealthSummary | null;
    weeklySummary: PeriodSummary | null;
    monthlySummary: PeriodSummary | null;
    activeDaysLast7: number;
    insightConsentRequired: boolean;
}

export interface DashboardState extends DashboardData {
    isLoading: boolean;
    error: Error | null;
}

// Custom hook to orchestrate all dashboard data fetching
export function useDashboardData(): DashboardState {
    const { isAuthenticated } = useAppAuth();

    // Step 1: Establish user context first.
    const meQ = useAuthedQuery("me", getMe, isAuthenticated);
    const meReady = isAuthenticated && !!meQ.data && !meQ.loading && !meQ.error;

    // Step 2: Fetch independent user resources after /me succeeds.
    const profileQ = useAuthedQuery("profile", getHealthProfile, meReady);
    const latestWeightQ = useAuthedQuery("latestWeight", getLatestWeight, meReady);
    const privacyQ = useAuthedQuery("privacyPreferences", getPrivacyPreferences, meReady);
    const activeGoalsQ = useAuthedQuery(
        "activeGoalsDashboard",
        async (token: string) => {
            const goals = await getActiveGoals(token);
            const goalsWithProgress = await Promise.all(
                goals.map(async (goal) => {
                    const progress = await getCurrentGoalProgress(goal.id, token);
                    return {
                        ...goal,
                        progress: progress?.progressPercentage ?? goal.progress ?? 0,
                    };
                })
            );
            return goalsWithProgress;
        },
        meReady
    );
    const activityQ = useAuthedQuery(
        "activityDashboard",
        (token: string) => getActivityHistory({ size: 100 }, token),
        meReady
    );
    const hasProfile = !!profileQ.data;
    const hasWeight = !!latestWeightQ.data;
    const hasConsent = !!privacyQ.data?.data_usage_consent;

    // Step 3: Fetch dependent resources only when prerequisites are present.
    const summaryQ = useAuthedQuery("summary", getHealthSummary, meReady && hasProfile && hasWeight);
    const weeklySummaryQ = useAuthedQuery("weeklySummary", getWeeklySummary, meReady && hasProfile && hasWeight);
    const monthlySummaryQ = useAuthedQuery("monthlySummary", getMonthlySummary, meReady && hasProfile && hasWeight);

    // Insights require explicit data usage consent and a completed profile context.
    const insightQ = useAuthedQuery("latestInsight", getLatestInsight, meReady && hasProfile && hasConsent);

    // Determine overall loading state
    const isLoading =
        meQ.loading ||
        profileQ.loading ||
        latestWeightQ.loading ||
        privacyQ.loading ||
        activeGoalsQ.loading ||
        activityQ.loading ||
        summaryQ.loading ||
        weeklySummaryQ.loading ||
        monthlySummaryQ.loading ||
        insightQ.loading;

    // Determine overall error state (return first error found, ignore summary errors)
    const insightConsentRequired =
        (hasProfile && !hasConsent) || (insightQ.error?.message || "").toLowerCase().includes("consent");

    const error =
        meQ.error ||
        profileQ.error ||
        latestWeightQ.error ||
        privacyQ.error ||
        activeGoalsQ.error ||
        activityQ.error ||
        (insightConsentRequired ? null : insightQ.error) ||
        null;

    // Extract data from individual queries
    const latestWeight = latestWeightQ.data;
    const activeGoals = activeGoalsQ.data || [];

    const activeDaysLast7 = (() => {
        const since = Date.now() - 7 * 24 * 60 * 60 * 1000;
        const unique = new Set<string>();
        for (const item of activityQ.data?.content || []) {
            const ts = new Date(item.checkinAt).getTime();
            if (!Number.isFinite(ts) || ts < since) continue;
            unique.add(new Date(item.checkinAt).toISOString().split("T")[0]);
        }
        return unique.size;
    })();

    return {
        me: meQ.data || null,
        profile: profileQ.data || null,
        latestWeight,
        activeGoals,
        insight: insightQ.data || null,
        summary: summaryQ.data || null,
        weeklySummary: weeklySummaryQ.data || null,
        monthlySummary: monthlySummaryQ.data || null,
        activeDaysLast7,
        insightConsentRequired,
        isLoading,
        error,
    };
}
