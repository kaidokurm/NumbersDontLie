import React from "react";
import { Card, CardBody } from "../../../shared/ui/Card";
import { Button } from "../../../shared/ui/Button";
import type { Goal } from "../../../shared/types";

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

interface GoalHistoryListProps {
    goals: Goal[];
    onDelete: (goalId: string) => void;
    isDeleting?: boolean;
}

export const GoalHistoryList: React.FC<GoalHistoryListProps> = ({ goals, onDelete, isDeleting = false }) => {
    if (goals.length === 0) {
        return (
            <Card>
                <CardBody>
                    <p className="text-center text-gray-500 text-sm">No past goals yet.</p>
                </CardBody>
            </Card>
        );
    }

    const formatDate = (dateString: string | undefined): string => {
        if (!dateString) return "Unknown";
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return "Unknown";
            return date.toLocaleDateString();
        } catch {
            return "Unknown";
        }
    };

    return (
        <div className="space-y-2">
            {goals.map((goal) => {
                const label = GOAL_TYPE_LABELS[goal.goalType] || goal.goalType;
                const createdDate = formatDate(goal.createdAt);

                return (
                    <Card key={goal.id}>
                        <CardBody>
                            <div className="flex items-start justify-between">
                                <div className="flex-1">
                                    <p className="font-semibold text-sm">{label}</p>
                                    <p className="text-xs text-gray-500 mt-1">Created: {createdDate}</p>
                                    {goal.targetValue && <p className="text-xs text-gray-600 mt-1">Target: {goal.targetValue} kg</p>}
                                </div>
                                <Button
                                    onClick={() => onDelete(goal.id)}
                                    disabled={isDeleting}
                                    className="text-red-600 bg-red-50 hover:bg-red-100"
                                >
                                    Delete
                                </Button>
                            </div>
                        </CardBody>
                    </Card>
                );
            })}
        </div>
    );
};
