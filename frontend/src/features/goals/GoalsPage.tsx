import { useState } from "react";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { useGoalsData } from "./useGoalsData";
import { Alert } from "../../shared/ui/Alert";
import { Card, CardBody } from "../../shared/ui/Card";
import { Button } from "../../shared/ui/Button";
import { Spinner } from "../../shared/ui/Spinner";
import { ActiveGoalCard } from "./components/ActiveGoalCard";
import { GoalHistoryList } from "./components/GoalHistoryList";
import { CreateGoalModal } from "./components/CreateGoalModal";
import type { Goal } from "../../shared/types";

export default function GoalsPage() {
    const { isAuthenticated } = useAppAuth();
    const getToken = useAuthToken();
    const { activeGoals, allGoals, isLoading, error, isCreating, isDeleting, createNewGoal, updateExistingGoal, deleteExistingGoal } = useGoalsData();
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [editingGoal, setEditingGoal] = useState<Goal | null>(null);

    // Filter history: all goals that are either not active or are archived
    const activeGoalIds = new Set(activeGoals.map((g) => g.id));
    const inactiveGoals = allGoals.filter((goal) => !activeGoalIds.has(goal.id));

    const handleCreateGoal = async (goalType: string, targetWeightKg?: number, targetActivityDays?: number, notes?: string, targetDate?: string) => {
        const token = await getToken();
        if (!token) return;
        if (editingGoal) {
            await updateExistingGoal(editingGoal.id, { goalType: goalType as Goal["goalType"], targetValue: targetWeightKg, targetActivityDaysPerWeek: targetActivityDays, description: notes, targetDate }, token);
            setEditingGoal(null);
        } else {
            await createNewGoal(goalType, targetWeightKg, targetActivityDays, notes, targetDate, token);
        }
    };

    const handleArchiveGoal = async (goalId: string) => {
        const token = await getToken();
        if (!token) return;
        try {
            await updateExistingGoal(goalId, { isActive: false }, token);
        } catch (error) {
            console.error("Failed to archive goal:", error);
        }
    };

    const handleEditGoal = (goal: Goal) => {
        setEditingGoal(goal);
        setIsCreateModalOpen(true);
    };

    const handleDeleteGoal = async (goalId: string) => {
        const token = await getToken();
        if (!token) return;
        if (window.confirm("Are you sure you want to delete this goal?")) {
            await deleteExistingGoal(goalId, token);
        }
    };

    return (
        <div className="space-y-6 pb-32 md:pb-4">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900">Goals</h1>
                    <p className="text-slate-600">Set and track your wellness goals</p>
                </div>
            </div>

            {/* Authentication Check */}
            {!isAuthenticated && (
                <Alert tone="info" title="Not Authenticated" message="Please log in to view your goals." />
            )}

            {/* Loading State */}
            {isLoading && (
                <Card>
                    <CardBody>
                        <Spinner label="Loading your goals..." />
                    </CardBody>
                </Card>
            )}

            {/* Error State */}
            {error && !isLoading && (
                <Alert tone="error" title="Error Loading Goals" message={error.message || "Failed to load goals."} />
            )}

            {/* Content */}
            {isAuthenticated && !isLoading && !error && (
                <>
                    {/* Active Goal Section */}
                    {activeGoals.length > 0 ? (
                        <div>
                            <div className="space-y-3">
                                {activeGoals.map((goal) => (
                                    <ActiveGoalCard
                                        key={goal.id}
                                        goal={goal}
                                        onArchive={handleArchiveGoal}
                                        onEdit={handleEditGoal}
                                        isArchiving={isDeleting}
                                    />
                                ))}
                            </div>
                        </div>
                    ) : (
                        <Card>
                            <CardBody>
                                <div className="text-center space-y-3">
                                    <p className="text-gray-600">You don't have an active goal yet.</p>
                                    <Button onClick={() => setIsCreateModalOpen(true)} className="w-full">
                                        Create Your First Goal
                                    </Button>
                                </div>
                            </CardBody>
                        </Card>
                    )}

                    {/* Create Goal Button (when active goal exists) */}
                    {activeGoals.length > 0 && (
                        <Button onClick={() => setIsCreateModalOpen(true)} className="w-full bg-blue-600 hover:bg-blue-700">
                            Create Another Goal
                        </Button>
                    )}

                    {/* Goal History Section */}
                    {inactiveGoals.length > 0 && (
                        <div>
                            <h2 className="text-lg font-semibold text-slate-900 mb-3">Goal History</h2>
                            <GoalHistoryList goals={inactiveGoals} onDelete={handleDeleteGoal} isDeleting={isDeleting} />
                        </div>
                    )}
                    {activeGoals.length > 0 && inactiveGoals.length === 0 && (
                        <Card>
                            <CardBody>
                                <p className="text-sm text-slate-600">
                                    No archived goals yet. Your completed and archived goals will appear here.
                                </p>
                            </CardBody>
                        </Card>
                    )}
                </>
            )}

            {/* Create/Edit Goal Modal */}
            <CreateGoalModal
                isOpen={isCreateModalOpen}
                isLoading={isCreating}
                onClose={() => {
                    setIsCreateModalOpen(false);
                    setEditingGoal(null);
                }}
                onSubmit={handleCreateGoal}
                editingGoal={editingGoal}
                isEditing={editingGoal !== null}
            />
        </div>
    );
}
