import { Link } from "react-router-dom";
import { Card, CardBody, CardTitle, CardSubtitle } from "../../../shared/ui/Card";
import type { Goal, HealthSummary, Insight, PeriodSummary } from "../../../shared/types";

interface ComparisonViewCardProps {
    summary: HealthSummary | null;
    weeklySummary: PeriodSummary | null;
    monthlySummary: PeriodSummary | null;
    activeGoals: Goal[];
    insight: Insight | null;
}

export function ComparisonViewCard({ summary, weeklySummary, monthlySummary, activeGoals, insight }: ComparisonViewCardProps) {
    const primaryWeightGoal = activeGoals.find((g) => g.targetValue != null);
    const currentWeight = summary?.latestWeightKg ?? null;
    const targetWeight = primaryWeightGoal?.targetValue ?? null;

    const weightGap = currentWeight != null && targetWeight != null
        ? Number((targetWeight - currentWeight).toFixed(1))
        : null;

    const weeklyChange = weeklySummary?.weightChangeKg ?? null;
    const monthlyChange = monthlySummary?.weightChangeKg ?? null;

    const firstRecommendation = insight?.payload?.recommendations?.[0] || null;

    if (!summary && !weeklySummary && !monthlySummary && !activeGoals.length && !insight) {
        return null;
    }

    return (
        <Card>
            <CardTitle>Comparison View</CardTitle>
            <CardSubtitle>Current vs target, weekly/monthly movement, and recommendation context</CardSubtitle>
            <CardBody>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div className="rounded border border-slate-200 p-3">
                        <div className="text-xs text-slate-500 mb-1">Current vs Target</div>
                        <div className="text-sm text-slate-800">
                            Current: <span className="font-semibold">{currentWeight != null ? `${currentWeight.toFixed(1)} kg` : "—"}</span>
                        </div>
                        <div className="text-sm text-slate-800">
                            Target: <span className="font-semibold">{targetWeight != null ? `${targetWeight.toFixed(1)} kg` : "—"}</span>
                        </div>
                        <div className="text-xs mt-1 text-slate-600">
                            Gap: {weightGap == null ? "—" : `${weightGap > 0 ? "+" : ""}${weightGap.toFixed(1)} kg`}
                        </div>
                    </div>

                    <div className="rounded border border-slate-200 p-3">
                        <div className="text-xs text-slate-500 mb-1">Weekly / Monthly Comparison</div>
                        <div className="text-sm text-slate-800">
                            Weekly: <span className="font-semibold">{weeklyChange == null ? "—" : `${weeklyChange > 0 ? "+" : ""}${weeklyChange.toFixed(1)} kg`}</span>
                        </div>
                        <div className="text-sm text-slate-800">
                            Monthly: <span className="font-semibold">{monthlyChange == null ? "—" : `${monthlyChange > 0 ? "+" : ""}${monthlyChange.toFixed(1)} kg`}</span>
                        </div>
                        <div className="text-xs mt-1 text-slate-600">Trend details in <Link className="underline" to="/trends">Trends</Link>.</div>
                    </div>
                </div>

                <div className="mt-3 rounded border border-slate-200 p-3">
                    <div className="text-xs text-slate-500 mb-1">AI Recommendation Context</div>
                    <div className="text-sm text-slate-800">{firstRecommendation || "No recommendation yet."}</div>
                </div>
            </CardBody>
        </Card>
    );
}
