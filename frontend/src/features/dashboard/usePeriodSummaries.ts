import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getWeeklySummary, getMonthlySummary } from "../../shared/api/summaries";
import type { PeriodSummary } from "../../shared/types";
import { useAppAuth } from "../../shared/auth/AuthContext";

export interface SummaryState {
    weekly: PeriodSummary | null;
    monthly: PeriodSummary | null;
    isLoading: boolean;
    error: Error | null;
}

export function usePeriodSummaries(): SummaryState {
    const { isAuthenticated } = useAppAuth();

    const weeklyQ = useAuthedQuery("weeklySummary", getWeeklySummary, isAuthenticated);
    const monthlyQ = useAuthedQuery("monthlySummary", getMonthlySummary, isAuthenticated);

    return {
        weekly: weeklyQ.data || null,
        monthly: monthlyQ.data || null,
        isLoading: weeklyQ.loading || monthlyQ.loading,
        error: weeklyQ.error || monthlyQ.error || null,
    };
}
