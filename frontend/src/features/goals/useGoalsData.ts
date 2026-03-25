import { useState, useCallback, useRef } from "react";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getAllGoals, createGoal, updateGoal, deleteGoal } from "../../shared/api/goals";
import { useAppAuth } from "../../shared/auth/AuthContext";
import type { Goal } from "../../shared/types";

export interface GoalsState {
    allGoals: Goal[];
    activeGoals: Goal[];
    isLoading: boolean;
    error: Error | null;
    isCreating: boolean;
    isUpdating: boolean;
    isDeleting: boolean;
}

export function useGoalsData() {
    const { isAuthenticated } = useAppAuth();
    const [isCreating, setIsCreating] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const refetchCounterRef = useRef(0);
    const [refetchCounter, setRefetchCounter] = useState(0);

    // Fetch all goals (re-fetches when refetchCounter changes)
    const allGoalsQ = useAuthedQuery(`allGoals-${refetchCounter}`, getAllGoals, isAuthenticated);
    const isLoading = allGoalsQ.loading;
    const error = allGoalsQ.error || null;
    const allGoals = Array.isArray(allGoalsQ.data) ? allGoalsQ.data : [];
    const activeGoals = allGoals.filter((goal) => goal.isActive);

    // Refetch both queries
    const refetch = useCallback(() => {
        refetchCounterRef.current++;
        setRefetchCounter(refetchCounterRef.current);
    }, []);

    // Action: Create new goal
    const createNewGoal = useCallback(
        async (goalType: string, targetWeightKg?: number, targetActivityDays?: number, notes?: string, targetDate?: string, token?: string) => {
            if (!token) return;

            setIsCreating(true);
            try {
                const newGoal: Omit<Goal, "id" | "userId" | "createdAt" | "updatedAt"> = {
                    title: goalType,
                    description: notes,
                    goalType: goalType as Goal["goalType"],
                    targetValue: targetWeightKg,
                    targetActivityDaysPerWeek: targetActivityDays,
                    targetDate: targetDate || new Date(Date.now() + 90 * 24 * 60 * 60 * 1000).toISOString().split("T")[0],
                    isActive: true,
                    progress: 0,
                };

                await createGoal(newGoal, token);
                // Refetch goals to show the new goal immediately
                refetch();
            } finally {
                setIsCreating(false);
            }
        },
        [refetch]
    );

    // Action: Update goal
    const updateExistingGoal = useCallback(
        async (goalId: string, updates: Partial<Goal>, token?: string) => {
            if (!token) return;

            setIsUpdating(true);
            try {
                await updateGoal(goalId, updates, token);
                // Refetch goals to show the updated goal immediately
                refetch();
            } finally {
                setIsUpdating(false);
            }
        },
        [refetch]
    );

    // Action: Delete goal
    const deleteExistingGoal = useCallback(
        async (goalId: string, token?: string) => {
            if (!token) return;

            setIsDeleting(true);
            try {
                await deleteGoal(goalId, token);
                // Refetch goals to remove the deleted goal immediately
                refetch();
            } finally {
                setIsDeleting(false);
            }
        },
        [refetch]
    );

    return {
        allGoals,
        activeGoals,
        isLoading,
        error,
        isCreating,
        isUpdating,
        isDeleting,
        createNewGoal,
        updateExistingGoal,
        deleteExistingGoal,
    };
}
