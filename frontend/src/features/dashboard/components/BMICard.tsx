import React from "react";
import { Card } from "../../../shared/ui/Card";
import type { HealthSummary } from "../../../shared/types";
import { getBMIClassification } from "../../../shared/api/summary";

interface BMICardProps {
    summary: HealthSummary | null;
    isLoading?: boolean;
}

export const BMICard: React.FC<BMICardProps> = ({ summary, isLoading = false }) => {
    if (isLoading) {
        return (
            <Card>
                <div className="p-4">
                    <div className="text-gray-400 text-sm mb-3">BMI</div>
                    <div className="h-16 bg-gray-200 animate-pulse rounded"></div>
                </div>
            </Card>
        );
    }

    if (!summary) {
        return (
            <Card>
                <div className="p-4 text-center text-gray-500">
                    <div className="text-sm text-gray-400 mb-2">BMI</div>
                    <p className="text-xs">Complete your profile to see BMI</p>
                </div>
            </Card>
        );
    }

    const classification = getBMIClassification(summary.bmi);

    // Color mapping
    const colorClasses = {
        blue: "bg-blue-50 border-blue-200 text-blue-900",
        green: "bg-green-50 border-green-200 text-green-900",
        yellow: "bg-yellow-50 border-yellow-200 text-yellow-900",
        red: "bg-red-50 border-red-200 text-red-900",
    };

    const badgeColors = {
        blue: "bg-blue-100 text-blue-800",
        green: "bg-green-100 text-green-800",
        yellow: "bg-yellow-100 text-yellow-800",
        red: "bg-red-100 text-red-900",
    };

    return (
        <Card>
            <div className={`p-5 border rounded-lg ${colorClasses[classification.color]}`}>
                <div className="flex items-start justify-between mb-4">
                    <div>
                        <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">BMI</h3>
                        <p className="text-3xl font-bold mt-2">{summary.bmi}</p>
                    </div>
                    <span
                        className={`px-3 py-1 rounded-full text-sm font-medium ${badgeColors[classification.color]
                            }`}
                    >
                        {classification.label}
                    </span>
                </div>

                {/* BMI Range Indicator */}
                <div className="space-y-2">
                    <div className="text-xs text-gray-600">Range</div>
                    <div className="flex gap-1 h-2 bg-white rounded overflow-hidden">
                        <div className="flex-1 bg-blue-400"></div>
                        <div className="flex-1 bg-green-500"></div>
                        <div className="flex-1 bg-yellow-400"></div>
                        <div className="flex-1 bg-red-500"></div>
                    </div>
                </div>

                {/* Weight Delta */}
                {summary.weightDelta7d !== null && (
                    <div className="mt-3 pt-3 border-t border-current border-opacity-10">
                        <div className="text-xs text-gray-600">
                            7-day change:{" "}
                            <span
                                className={`font-semibold ${summary.weightDelta7d > 0
                                    ? "text-red-600"
                                    : summary.weightDelta7d < 0
                                        ? "text-green-600"
                                        : "text-gray-600"
                                    }`}
                            >
                                {summary.weightDelta7d > 0 ? "+" : ""}
                                {summary.weightDelta7d.toFixed(1)} kg
                            </span>
                        </div>
                    </div>
                )}

                {/* Display height and weight */}
                <div className="mt-3 pt-3 border-t border-current border-opacity-10 text-xs text-gray-600">
                    <div className="flex justify-between">
                        <span>Height: {summary.heightCm} cm</span>
                        <span>Weight: {summary.latestWeightKg} kg</span>
                    </div>
                </div>
            </div>
        </Card>
    );
};
