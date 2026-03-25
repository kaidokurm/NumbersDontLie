import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import type { WellnessHistoryPoint } from "../../../shared/api/wellness";

interface WellnessComponentsStackedChartProps {
    points: WellnessHistoryPoint[];
}

export function WellnessComponentsStackedChart({ points }: WellnessComponentsStackedChartProps) {
    if (!points.length) {
        return (
            <Card>
                <CardTitle>🧩 Wellness Components</CardTitle>
                <CardBody>
                    <p className="text-slate-600 text-center py-6">Not enough data for component breakdown yet.</p>
                </CardBody>
            </Card>
        );
    }

    const latest = points[points.length - 1];
    const weighted = {
        bmi: Math.round(latest.bmiScore * 0.3),
        activity: Math.round(latest.activityScore * 0.3),
        goal: Math.round(latest.goalScore * 0.2),
        habits: Math.round(latest.habitsScore * 0.2),
    };
    const total = Math.max(1, weighted.bmi + weighted.activity + weighted.goal + weighted.habits);

    const segments = [
        { key: "bmi", label: "BMI", value: weighted.bmi, color: "bg-blue-500" },
        { key: "activity", label: "Activity", value: weighted.activity, color: "bg-emerald-500" },
        { key: "goal", label: "Goal", value: weighted.goal, color: "bg-amber-500" },
        { key: "habits", label: "Habits", value: weighted.habits, color: "bg-violet-500" },
    ];

    return (
        <Card>
            <CardTitle>🧩 Wellness Components</CardTitle>
            <CardBody>
                <div className="text-xs text-slate-500 mb-2">
                    Latest week ending {new Date(latest.weekEnd).toLocaleDateString()}
                </div>

                <div className="w-full h-6 rounded overflow-hidden border border-slate-200 flex">
                    {segments.map((segment) => (
                        <div
                            key={segment.key}
                            className={`${segment.color} h-full`}
                            style={{ width: `${(segment.value / total) * 100}%` }}
                            title={`${segment.label}: ${segment.value}`}
                        />
                    ))}
                </div>

                <div className="mt-3 grid grid-cols-2 gap-2 text-sm">
                    {segments.map((segment) => (
                        <div key={segment.key} className="flex items-center gap-2">
                            <span className={`inline-block w-3 h-3 rounded ${segment.color}`} />
                            <span className="text-slate-700">{segment.label}: {segment.value}</span>
                        </div>
                    ))}
                </div>

                <div className="mt-2 text-xs text-slate-500">
                    Weighted total: {latest.score}/100
                </div>
            </CardBody>
        </Card>
    );
}
