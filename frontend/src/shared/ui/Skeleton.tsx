import React from "react";

interface SkeletonProps {
    width?: string;
    height?: string;
    className?: string;
    circle?: boolean;
}

/**
 * Reusable skeleton loader for showing placeholder content
 */
export const Skeleton: React.FC<SkeletonProps> = ({ width = "w-full", height = "h-4", className = "", circle = false }) => {
    const baseClasses = "bg-gray-200 animate-pulse";
    const circleClass = circle ? "rounded-full" : "rounded";
    return <div className={`${baseClasses} ${width} ${height} ${circleClass} ${className}`}></div>;
};

/**
 * Skeleton for a line of text
 */
export const TextSkeleton: React.FC<{ lines?: number }> = ({ lines = 1 }) => {
    return (
        <div className="space-y-2">
            {Array.from({ length: lines }).map((_, i) => (
                <Skeleton key={i} width={i === lines - 1 ? "w-2/3" : "w-full"} height="h-4" />
            ))}
        </div>
    );
};

/**
 * Skeleton for a card-like component
 */
export const CardSkeleton: React.FC = () => {
    return (
        <div className="bg-white rounded-lg border border-gray-200 p-4 space-y-3">
            <Skeleton width="w-1/3" height="h-3" />
            <Skeleton width="w-full" height="h-12" />
            <Skeleton width="w-full" height="h-8" />
        </div>
    );
};
