import React from "react";
import { Card } from "../../../shared/ui/Card";
import { Skeleton } from "../../../shared/ui/Skeleton";

export const BMICardSkeleton: React.FC = () => {
    return (
        <Card>
            <div className="p-5 border rounded-lg border-gray-200 bg-gray-50">
                <div className="flex items-start justify-between mb-4">
                    <div>
                        <Skeleton width="w-16" height="h-3" className="mb-2" />
                        <Skeleton width="w-20" height="h-10" />
                    </div>
                    <Skeleton width="w-24" height="h-8" className="rounded-full" />
                </div>

                <div className="space-y-2">
                    <Skeleton width="w-12" height="h-2" />
                    <div className="flex gap-1 h-2">
                        <Skeleton width="flex-1" height="h-2" />
                        <Skeleton width="flex-1" height="h-2" />
                        <Skeleton width="flex-1" height="h-2" />
                        <Skeleton width="flex-1" height="h-2" />
                    </div>
                </div>

                <div className="mt-3 pt-3 border-t border-gray-300">
                    <Skeleton width="w-32" height="h-3" />
                </div>

                <div className="mt-3 pt-3 border-t border-gray-300 space-y-1">
                    <Skeleton width="w-40" height="h-3" />
                    <Skeleton width="w-40" height="h-3" />
                </div>
            </div>
        </Card>
    );
};

export const WellnessScoreCardSkeleton: React.FC = () => {
    return (
        <Card>
            <div className="p-6 border rounded-lg border-gray-200 bg-gray-50">
                <Skeleton width="w-28" height="h-3" className="mb-4" />

                <div className="flex items-center justify-center py-6">
                    <Skeleton width="w-24" height="h-24" circle={true} />
                </div>

                <div className="mt-4 text-center space-y-2">
                    <Skeleton width="w-20 mx-auto" height="h-4" />
                    <Skeleton width="w-48 mx-auto" height="h-3" />
                </div>
            </div>
        </Card>
    );
};

export const GoalCardSkeleton: React.FC = () => {
    return (
        <Card>
            <div className="p-4 space-y-3">
                <Skeleton width="w-20" height="h-3" />
                <Skeleton width="w-full" height="h-8" />
                <div className="space-y-2">
                    <Skeleton width="w-full" height="h-3" />
                    <Skeleton width="w-2/3" height="h-3" />
                </div>
                <Skeleton width="w-32" height="h-8" className="mt-4" />
            </div>
        </Card>
    );
};

export const InsightCardSkeleton: React.FC = () => {
    return (
        <Card>
            <div className="p-4 space-y-3">
                <div className="flex items-center justify-between">
                    <Skeleton width="w-24" height="h-3" />
                    <Skeleton width="w-16" height="h-5" className="rounded-full" />
                </div>
                <div className="space-y-2">
                    <Skeleton width="w-full" height="h-3" />
                    <Skeleton width="w-full" height="h-3" />
                    <Skeleton width="w-2/3" height="h-3" />
                </div>
                <Skeleton width="w-full" height="h-10" className="mt-4" />
            </div>
        </Card>
    );
};
