import React from "react";
import { Card, CardBody } from "../../../shared/ui/Card";
import { Button } from "../../../shared/ui/Button";
import type { Goal } from "../../../shared/types";

// Goal type labels
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

interface ActiveGoalCardProps {
    goal: Goal;
    onArchive: (goalId: string) => void;
    onEdit: (goal: Goal) => void;
    isArchiving?: boolean;
}

export const ActiveGoalCard: React.FC<ActiveGoalCardProps> = ({ goal, onArchive, onEdit, isArchiving = false }) => {
    const label = GOAL_TYPE_LABELS[goal.goalType] || goal.goalType;

    return (
        <Card>
            <CardBody>
                <div className="space-y-4">
                    <div>
                        <h3 className="text-sm font-medium text-gray-600">Active Goal</h3>
                        <p className="text-2xl font-bold mt-1">{label}</p>
                    </div>

                    {goal.description && (
                        <div>
                            <p className="text-xs text-gray-500 mb-1">Notes</p>
                            <p className="text-sm text-gray-700">{goal.description}</p>
                        </div>
                    )}

                    {goal.targetValue && (
                        <div>
                            <p className="text-xs text-gray-500 mb-1">Target Weight</p>
                            <p className="text-sm font-semibold">{goal.targetValue} kg</p>
                        </div>
                    )}

                    {goal.progress !== undefined && (
                        <div>
                            <p className="text-xs text-gray-500 mb-1">Progress</p>
                            <div className="w-full bg-gray-200 rounded-full h-2">
                                <div
                                    className="bg-blue-600 h-2 rounded-full transition-all"
                                    style={{ width: `${Math.min(goal.progress, 100)}%` }}
                                ></div>
                            </div>
                            <p className="text-xs text-gray-600 mt-1">{Math.round(goal.progress)}% complete</p>
                        </div>
                    )}

                    <div className="flex gap-2 pt-2">
                        <Button
                            onClick={() => onEdit(goal)}
                            className="flex-1 bg-blue-600 hover:bg-blue-700"
                        >
                            Edit Goal
                        </Button>
                        <Button
                            onClick={() => onArchive(goal.id)}
                            disabled={isArchiving}
                            className="flex-1 bg-amber-600 hover:bg-amber-700"
                        >
                            {isArchiving ? "Archiving..." : "Archive Goal"}
                        </Button>
                    </div>
                </div>
            </CardBody>
        </Card>
    );
};
