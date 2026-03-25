import { Card, CardBody, CardTitle, CardSubtitle } from "../../../shared/ui/Card";
import type { Goal } from "../../../shared/types";

interface ActiveGoalsCardProps {
    goals: Goal[];
}

const GOAL_TYPE_LABELS: Record<string, string> = {
    WEIGHT_LOSS: "Weight Loss",
    MAINTAIN_WEIGHT: "Maintain Weight",
    WEIGHT_GAIN: "Weight Gain",
    IMPROVE_FITNESS: "Improve Fitness",
    BUILD_MUSCLE: "Build Muscle",
    ENHANCE_ENDURANCE: "Enhance Endurance",
    IMPROVE_FLEXIBILITY: "Improve Flexibility",
    REDUCE_STRESS: "Reduce Stress",
    BETTER_SLEEP: "Better Sleep",
};

export function ActiveGoalsCard({ goals }: ActiveGoalsCardProps) {
    if (!goals.length) {
        return null;
    }

    return (
        <Card>
            <CardTitle>Active Goals</CardTitle>
            <CardSubtitle>{goals.length} active goal{goals.length === 1 ? "" : "s"}</CardSubtitle>
            <CardBody>
                <div className="space-y-3">
                    {goals.map((goal) => {
                        const progress = Math.max(0, Math.min(100, Number(goal.progress ?? 0)));
                        const label = GOAL_TYPE_LABELS[goal.goalType] || goal.title || goal.goalType;
                        return (
                            <div key={goal.id} className="rounded border border-slate-200 p-3">
                                <div className="flex items-center justify-between gap-2">
                                    <div className="text-sm font-semibold text-slate-900">{label}</div>
                                    <div className="text-xs text-slate-600">{progress}%</div>
                                </div>
                                <div className="mt-2 w-full bg-slate-200 rounded-full h-2">
                                    <div
                                        className="bg-green-600 h-2 rounded-full transition-all"
                                        style={{ width: `${progress}%` }}
                                    />
                                </div>
                                <div className="mt-2 text-xs text-slate-500">
                                    Target date: {new Date(goal.targetDate).toLocaleDateString()}
                                </div>
                            </div>
                        );
                    })}
                </div>
            </CardBody>
        </Card>
    );
}
