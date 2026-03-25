import { useState } from "react";
import { Alert } from "../../shared/ui/Alert";
import { Spinner } from "../../shared/ui/Spinner";
import { useWeightChartData } from "./useWeightChartData";
import { WeightChart } from "./components/WeightChart";
import { Link } from "react-router-dom";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { getWellnessHistory } from "../../shared/api/wellness";
import { getActivityHistory } from "../../shared/api/activity";
import { WellnessEvolutionChart } from "./components/WellnessEvolutionChart";
import { WellnessComponentsStackedChart } from "./components/WellnessComponentsStackedChart";
import { ActivityHeatmap } from "./components/ActivityHeatmap";

export default function TrendsPage() {
    const [range, setRange] = useState<"30" | "90" | "all">("90");
    const chartData = useWeightChartData(range);
    const { isAuthenticated } = useAppAuth();
    const weeksByRange = { "30": 4, "90": 12, all: 24 } as const;
    const wellnessQ = useAuthedQuery(
        `wellnessHistory-${range}`,
        (token: string) => getWellnessHistory(token, weeksByRange[range]),
        isAuthenticated
    );
    const activityQ = useAuthedQuery(
        `activityHistory-${range}`,
        (token: string) => getActivityHistory({ size: range === "30" ? 80 : range === "90" ? 220 : 500 }, token),
        isAuthenticated
    );
    const activityDates = (activityQ.data?.content || []).map((item) =>
        new Date(item.checkinAt).toISOString().split("T")[0]
    );

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Trends</h1>
                <p className="text-slate-600">Track your progress over time</p>
            </div>

            <div className="inline-flex rounded-lg border border-slate-200 bg-white p-1">
                {(["30", "90", "all"] as const).map((option) => (
                    <button
                        key={option}
                        onClick={() => setRange(option)}
                        className={`px-3 py-1.5 text-sm rounded-md transition ${range === option
                            ? "bg-blue-600 text-white"
                            : "text-slate-700 hover:bg-slate-100"
                            }`}
                    >
                        {option === "all" ? "All" : `${option}d`}
                    </button>
                ))}
            </div>

            {chartData.isLoading && (
                <div className="flex justify-center py-12">
                    <Spinner />
                </div>
            )}

            {chartData.error && (
                <Alert
                    tone="error"
                    title="Error Loading Trends"
                    message={chartData.error.message || "Failed to load weight data"}
                />
            )}
            {wellnessQ.error && (
                <Alert
                    tone="error"
                    title="Wellness Trend Unavailable"
                    message={wellnessQ.error.message || "Failed to load wellness history"}
                />
            )}
            {activityQ.error && (
                <Alert
                    tone="error"
                    title="Activity Trend Unavailable"
                    message={activityQ.error.message || "Failed to load activity history"}
                />
            )}

            {!chartData.isLoading && !chartData.error && (
                <>
                    <WeightChart
                        points={chartData.points}
                        milestones={chartData.milestones}
                        targetWeight={chartData.targetWeight}
                        minWeight={chartData.minWeight}
                        maxWeight={chartData.maxWeight}
                    />
                    <WellnessEvolutionChart points={wellnessQ.data || []} />
                    <WellnessComponentsStackedChart points={wellnessQ.data || []} />
                    <ActivityHeatmap dates={activityDates} />
                    {!activityQ.error && activityDates.length === 0 && (
                        <Alert
                            tone="info"
                            title="No Activity Check-Ins Yet"
                            message="Add activity check-ins to populate the weekly heatmap."
                        />
                    )}

                    {chartData.points.length === 0 && (
                        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                            <p className="text-blue-900">
                                Start recording your weight to see your progress over time. Visit the
                                <Link to="/checkin" className="font-medium underline">
                                    {" "}
                                    Check In
                                </Link>{" "}
                                page to add your first entry.
                            </p>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
