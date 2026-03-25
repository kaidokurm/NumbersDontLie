import { Alert } from "../../shared/ui/Alert";
import { Card, CardBody } from "../../shared/ui/Card";
import { BMICard } from "./components/BMICard";
import { WellnessScoreCard } from "./components/WellnessScoreCard";
import { ActiveGoalsCard } from "./components/ActiveGoalsCard";
import { InsightCard } from "./components/InsightCard";
import { PeriodSummaryCard } from "./components/PeriodSummaryCard";
import { QuickNav } from "./components/QuickNav";
import { ActivityStreakCard } from "./components/ActivityStreakCard";
import { ComparisonViewCard } from "./components/ComparisonViewCard";
import { BMICardSkeleton, WellnessScoreCardSkeleton, GoalCardSkeleton, InsightCardSkeleton } from "./components/SkeletonCards";
import type { DashboardState } from "./useDashboardData";
import { Link } from "react-router-dom";

interface DashboardContentProps {
    isAuthenticated: boolean;
    data: DashboardState;
    collisionError: string | null;
    isResolvingCollision: boolean;
    collisionResolveError: string | null;
    collisionResolveMessage: string | null;
    onResolveCollision: () => Promise<void>;
}

export function DashboardContent({
    isAuthenticated,
    data,
    collisionError,
    isResolvingCollision,
    collisionResolveError,
    collisionResolveMessage,
    onResolveCollision,
}: DashboardContentProps) {
    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900">Dashboard</h1>
                    <p className="text-slate-600">Your wellness at a glance</p>
                </div>
            </div>

            {!isAuthenticated && (
                <Alert
                    tone="info"
                    title="Not Authenticated"
                    message="Please log in to view your dashboard."
                />
            )}

            {data.isLoading && (
                <>
                    <BMICardSkeleton />
                    <WellnessScoreCardSkeleton />
                    <GoalCardSkeleton />
                    <InsightCardSkeleton />
                </>
            )}

            {data.error && (
                <Alert
                    tone="error"
                    title="Error Loading Dashboard"
                    message={data.error.message || "Failed to load dashboard data"}
                />
            )}

            {collisionError && (
                <Alert
                    tone="warning"
                    title="Sign-In Method Conflict"
                    message={collisionError}
                >
                    <div className="mt-2">
                        <button
                            className="px-3 py-2 rounded bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-50"
                            onClick={() => void onResolveCollision()}
                            disabled={isResolvingCollision}
                        >
                            {isResolvingCollision ? "Linking..." : "Link Current Provider To Existing Account"}
                        </button>
                    </div>
                    {collisionResolveMessage && <div className="mt-2 text-green-800">{collisionResolveMessage}</div>}
                    {collisionResolveError && <div className="mt-2 text-red-800">{collisionResolveError}</div>}
                </Alert>
            )}

            {isAuthenticated && !data.isLoading && !data.error && !data.profile && (
                <Card>
                    <CardBody>
                        <div className="space-y-3">
                            <h3 className="text-lg font-semibold text-slate-900">Complete Your Profile</h3>
                            <p className="text-sm text-slate-600">
                                Please fill in your health profile to get started with tracking your wellness.
                            </p>
                            <Link
                                to="/profile"
                                className="inline-block px-4 py-2 bg-blue-600 text-white rounded font-medium hover:bg-blue-700 transition"
                            >
                                Go to Profile →
                            </Link>
                        </div>
                    </CardBody>
                </Card>
            )}

            {isAuthenticated && !data.isLoading && !data.error && data.profile && (
                <>
                    <BMICard summary={data.summary} isLoading={data.isLoading} />
                    <WellnessScoreCard profile={data.profile} isLoading={data.isLoading} />
                    <ActiveGoalsCard goals={data.activeGoals} />
                    <ActivityStreakCard activeDaysLast7={data.activeDaysLast7} />
                    <InsightCard insight={data.insight} consentRequired={data.insightConsentRequired} />
                    <ComparisonViewCard
                        summary={data.summary}
                        weeklySummary={data.weeklySummary}
                        monthlySummary={data.monthlySummary}
                        activeGoals={data.activeGoals}
                        insight={data.insight}
                    />
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <PeriodSummaryCard summary={data.weeklySummary} />
                        <PeriodSummaryCard summary={data.monthlySummary} />
                    </div>
                    <QuickNav />
                </>
            )}
        </div>
    );
}
