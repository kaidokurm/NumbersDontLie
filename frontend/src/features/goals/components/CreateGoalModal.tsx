import React, { useState, useEffect } from "react";
import { Card, CardBody } from "../../../shared/ui/Card";
import { Button } from "../../../shared/ui/Button";
import { TextField } from "../../../shared/ui/TextField";
import { Alert } from "../../../shared/ui/Alert";
import type { Goal } from "../../../shared/types";

// Goal types matching backend enum
const GOAL_TYPES = [
    { value: "WEIGHT_LOSS", label: "Weight Loss" },
    { value: "MAINTAIN_WEIGHT", label: "Maintain Weight" },
    { value: "WEIGHT_GAIN", label: "Weight Gain" },
    { value: "IMPROVE_FITNESS", label: "Improve Fitness" },
    { value: "BUILD_MUSCLE", label: "Build Muscle" },
    { value: "ENHANCE_ENDURANCE", label: "Enhance Endurance" },
    { value: "IMPROVE_FLEXIBILITY", label: "Improve Flexibility" },
    { value: "REDUCE_STRESS", label: "Reduce Stress" },
    { value: "BETTER_SLEEP", label: "Better Sleep" },
];

// Goal types that need weight targets
const WEIGHT_GOAL_TYPES = ["WEIGHT_LOSS", "MAINTAIN_WEIGHT", "WEIGHT_GAIN"];

// Goal types that need activity frequency
const ACTIVITY_GOAL_TYPES = ["IMPROVE_FITNESS", "BUILD_MUSCLE", "ENHANCE_ENDURANCE", "IMPROVE_FLEXIBILITY"];

interface CreateGoalModalProps {
    isOpen: boolean;
    isLoading?: boolean;
    isEditing?: boolean;
    editingGoal?: Goal | null;
    onClose: () => void;
    onSubmit: (goalType: string, targetWeightKg?: number, targetActivityDays?: number, notes?: string, targetDate?: string) => Promise<void>;
}

export const CreateGoalModal: React.FC<CreateGoalModalProps> = ({
    isOpen,
    isLoading = false,
    isEditing = false,
    editingGoal,
    onClose,
    onSubmit
}) => {
    const [goalType, setGoalType] = useState("");
    const [targetWeight, setTargetWeight] = useState("");
    const [targetActivityDays, setTargetActivityDays] = useState("");
    const [targetDate, setTargetDate] = useState("");
    const [notes, setNotes] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Load editing data
    useEffect(() => {
        if (isEditing && editingGoal) {
            setGoalType(editingGoal.goalType);
            setTargetWeight(editingGoal.targetValue ? editingGoal.targetValue.toString() : "");
            setTargetActivityDays(editingGoal.targetActivityDaysPerWeek ? editingGoal.targetActivityDaysPerWeek.toString() : "");
            setTargetDate(editingGoal.targetDate || "");
            setNotes(editingGoal.description || "");
        } else {
            setGoalType("");
            setTargetWeight("");
            setTargetActivityDays("");
            // Default to 90 days from today
            const defaultDate = new Date();
            defaultDate.setDate(defaultDate.getDate() + 90);
            setTargetDate(defaultDate.toISOString().split("T")[0]);
            setNotes("");
        }
        setError(null);
    }, [isOpen, isEditing, editingGoal]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!goalType) {
            setError("Please select a goal type");
            return;
        }

        if (!targetDate) {
            setError("Target date is required");
            return;
        }

        setIsSubmitting(true);
        try {
            await onSubmit(
                goalType,
                targetWeight ? parseFloat(targetWeight) : undefined,
                targetActivityDays ? parseInt(targetActivityDays) : undefined,
                notes || undefined,
                targetDate
            );
            // Reset form
            setGoalType("");
            setTargetWeight("");
            setTargetActivityDays("");
            setTargetDate("");
            setNotes("");
            // Show success message briefly before closing
            setError(null);
            setTimeout(() => {
                onClose();
            }, 300);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to save goal");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    const needsWeight = WEIGHT_GOAL_TYPES.includes(goalType);
    const needsActivityDays = ACTIVITY_GOAL_TYPES.includes(goalType);

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <Card>
                <CardBody>
                    <div className="max-w-md w-full">
                        <h2 className="text-xl font-bold mb-4">{isEditing ? "Edit Goal" : "Create New Goal"}</h2>

                        {error && (
                            <Alert tone="error" title="Error" message={error} />
                        )}

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div className="space-y-1 mb-4">
                                <label className="block text-sm font-medium text-slate-700">Goal Type</label>
                                <select
                                    value={goalType}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setGoalType(e.target.value)}
                                    required
                                    disabled={isSubmitting || isLoading}
                                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-green-200 transition"
                                >
                                    <option value="">Select a goal type...</option>
                                    {GOAL_TYPES.map((type) => (
                                        <option key={type.value} value={type.value}>
                                            {type.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {needsWeight && (
                                <TextField
                                    label="Target Weight (kg)"
                                    type="number"
                                    step="0.1"
                                    value={targetWeight}
                                    onChange={(e) => setTargetWeight(e.target.value)}
                                    placeholder="e.g., 75"
                                    disabled={isSubmitting || isLoading}
                                />
                            )}

                            {needsActivityDays && (
                                <TextField
                                    label="Target Activity Days Per Week"
                                    type="number"
                                    min="0"
                                    max="7"
                                    value={targetActivityDays}
                                    onChange={(e) => setTargetActivityDays(e.target.value)}
                                    placeholder="e.g., 4"
                                    disabled={isSubmitting || isLoading}
                                />
                            )}

                            <TextField
                                label="Target Date"
                                type="date"
                                value={targetDate}
                                onChange={(e) => setTargetDate(e.target.value)}
                                disabled={isSubmitting || isLoading}
                            />

                            <TextField
                                label="Notes (Optional)"
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                placeholder="Add any notes about this goal"
                                disabled={isSubmitting || isLoading}
                            />

                            <div className="flex gap-2 pt-4">
                                <Button
                                    onClick={onClose}
                                    disabled={isSubmitting || isLoading}
                                    className="flex-1 bg-gray-500 hover:bg-gray-600"
                                >
                                    Cancel
                                </Button>
                                <Button
                                    type="submit"
                                    disabled={isSubmitting || isLoading || !goalType}
                                    className="flex-1"
                                >
                                    {isSubmitting || isLoading ? (isEditing ? "Updating..." : "Creating...") : (isEditing ? "Update Goal" : "Create Goal")}
                                </Button>
                            </div>
                        </form>
                    </div>
                </CardBody>
            </Card>
        </div>
    );
};
