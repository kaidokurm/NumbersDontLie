import React from "react";
import { Card } from "../../../shared/ui/Card";
import type { HealthProfile } from "../../../shared/types";

interface WellnessScoreCardProps {
    profile: HealthProfile | null;
    isLoading?: boolean;
}

/**
 * Get wellness level label and color based on score
 */
function getWellnessLevel(score: number) {
    if (score >= 80) return { label: "Excellent", color: "text-green-600", bgColor: "bg-green-50", borderColor: "border-green-200" };
    if (score >= 60) return { label: "Good", color: "text-blue-600", bgColor: "bg-blue-50", borderColor: "border-blue-200" };
    if (score >= 40) return { label: "Fair", color: "text-yellow-600", bgColor: "bg-yellow-50", borderColor: "border-yellow-200" };
    return { label: "Needs Work", color: "text-red-600", bgColor: "bg-red-50", borderColor: "border-red-200" };
}

/**
 * Calculate the circumference offset for the circular progress
 */
function getStrokeOffset(score: number) {
    const radius = 45;
    const circumference = 2 * Math.PI * radius;
    return circumference - (score / 100) * circumference;
}

export const WellnessScoreCard: React.FC<WellnessScoreCardProps> = ({ profile, isLoading = false }) => {
    if (isLoading) {
        return (
            <Card>
                <div className="p-4">
                    <div className="text-gray-400 text-sm mb-3">Wellness Score</div>
                    <div className="h-40 bg-gray-200 animate-pulse rounded"></div>
                </div>
            </Card>
        );
    }

    if (!profile || profile.wellnessScore === undefined) {
        return (
            <Card>
                <div className="p-4 text-center text-gray-500">
                    <div className="text-sm text-gray-400 mb-2">Wellness Score</div>
                    <p className="text-xs">Your wellness score will appear here</p>
                </div>
            </Card>
        );
    }

    const score = profile.wellnessScore;
    const level = getWellnessLevel(score);
    const strokeOffset = getStrokeOffset(score);
    const radius = 45;
    const circumference = 2 * Math.PI * radius;

    return (
        <Card>
            <div className={`p-6 border rounded-lg ${level.bgColor} border-blue-200`}>
                <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400 mb-4">Wellness Score</h3>

                <div className="flex items-center justify-center">
                    <svg width="120" height="120" className="transform -rotate-90">
                        {/* Background circle */}
                        <circle
                            cx="60"
                            cy="60"
                            r={radius}
                            fill="none"
                            stroke="rgba(0,0,0,0.1)"
                            strokeWidth="8"
                        />

                        {/* Progress circle (filled based on score) */}
                        <circle
                            cx="60"
                            cy="60"
                            r={radius}
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="8"
                            strokeDasharray={circumference}
                            strokeDashoffset={strokeOffset}
                            strokeLinecap="round"
                            className={level.color}
                            style={{ transition: "stroke-dashoffset 0.5s ease" }}
                        />

                        {/* Center text */}
                        <text
                            x="60"
                            y="70"
                            textAnchor="middle"
                            fontSize="28"
                            fontWeight="bold"
                            className={level.color}
                            transform="rotate(90 60 60)"
                        >
                            {score}
                        </text>
                    </svg>
                </div>

                <div className="mt-4 text-center">
                    <p className={`text-sm font-semibold ${level.color}`}>{level.label}</p>
                    <p className="text-xs text-gray-500 mt-1">
                        {score >= 80
                            ? "Keep up your healthy habits!"
                            : score >= 60
                                ? "Good progress! Small improvements can help."
                                : score >= 40
                                    ? "Focus on key health areas to improve."
                                    : "Start with small, manageable changes."}
                    </p>
                </div>
            </div>
        </Card>
    );
};
