import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import type { PeriodSummary } from "../../../shared/types";

interface PeriodSummaryCardProps {
    summary: PeriodSummary | null;
}

export function PeriodSummaryCard({ summary }: PeriodSummaryCardProps) {
    if (!summary) {
        return null;
    }

    const { periodType, startDate, endDate, weightChangeKg, avgWellnessScore, activityLevel, weightEntriesCount } =
        summary;

    const isWeekly = periodType === "weekly";
    const title = isWeekly ? "📅 This Week" : "📅 This Month";

    // Determine trend direction
    let trendEmoji = "→";
    if (weightChangeKg !== null) {
        if (weightChangeKg < -0.5) trendEmoji = "📉";
        else if (weightChangeKg > 0.5) trendEmoji = "📈";
    }

    return (
        <Card>
            <CardTitle>{title}</CardTitle>
            <CardBody>
                <div className="space-y-3">
                    {/* Date range */}
                    <div className="text-xs text-slate-500">
                        {startDate} to {endDate}
                    </div>

                    {/* Weight change */}
                    {weightChangeKg !== null && (
                        <div className="flex items-center justify-between p-2 bg-slate-50 rounded">
                            <span className="text-sm text-slate-700">Weight change</span>
                            <div className="flex items-center gap-2">
                                <span className="text-lg">{trendEmoji}</span>
                                <span className={`font-semibold ${weightChangeKg < 0 ? "text-green-600" : "text-amber-600"
                                    }`}>
                                    {weightChangeKg > 0 ? "+" : ""}
                                    {weightChangeKg.toFixed(1)} kg
                                </span>
                            </div>
                        </div>
                    )}

                    {/* Wellness score */}
                    <div className="flex items-center justify-between p-2 bg-slate-50 rounded">
                        <span className="text-sm text-slate-700">Avg wellness</span>
                        <div className="flex items-center gap-2">
                            <span className="text-lg">💚</span>
                            <span className="font-semibold text-blue-600">{avgWellnessScore.toFixed(0)}/100</span>
                        </div>
                    </div>

                    {/* Activity level */}
                    <div className="flex items-center justify-between p-2 bg-slate-50 rounded">
                        <span className="text-sm text-slate-700">Activity level</span>
                        <span className="text-sm font-medium text-slate-900">{activityLevel}</span>
                    </div>

                    {/* Entries tracked */}
                    <div className="text-xs text-slate-600 text-center">
                        {weightEntriesCount} weight entries
                    </div>
                </div>
            </CardBody>
        </Card>
    );
}
