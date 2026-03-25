/**
 * Shared TypeScript types for Numbers Don't Lie
 * Maps to backend DTOs and entity models
 */

// User & Auth
export type UserProfile = {
    id: string;
    auth0Sub?: string;
    email: string;
    createdAt: string;
    updatedAt: string;
};

// Health Profile (captured at setup)
export type HealthProfile = {
    userId: string;
    height: number; // cm
    weight: number; // kg
    age: number;
    gender: "MALE" | "FEMALE" | "OTHER";
    activityLevel: "SEDENTARY" | "LIGHTLY_ACTIVE" | "MODERATELY_ACTIVE" | "VERY_ACTIVE" | "EXTREMELY_ACTIVE";
    targetWeight: number; // kg (optional goal)
    wellnessScore?: number; // 0-100 wellness score
    dietaryPreferences?: string[];
    dietaryRestrictions?: string[];
    fitnessAssessment?: Record<string, unknown> | null;
    fitnessGoals?: string[];
    createdAt: string;
    updatedAt: string;
};

// Weight entry (daily check-ins)
export type WeightEntry = {
    id: string;
    userId: string;
    weight: number; // kg
    date: string; // ISO date YYYY-MM-DD
    notes?: string;
    createdAt: string;
};

export type ActivityCheckin = {
    id: string;
    activityType: string;
    durationMinutes?: number;
    intensity?: string;
    note?: string;
    checkinAt: string;
};

// Goal (fitness/wellness targets)
export type Goal = {
    id: string;
    userId: string;
    title: string;
    description?: string;
    goalType: "WEIGHT_LOSS" | "WEIGHT_GAIN" | "MAINTENANCE" | "STRENGTH" | "ENDURANCE" | "GENERAL_WELLNESS";
    targetValue?: number; // e.g., target weight in kg
    targetActivityDaysPerWeek?: number; // For activity goals, target days per week
    targetDate: string; // ISO date
    isActive: boolean;
    progress?: number; // 0-100 percentage
    createdAt: string;
    updatedAt: string;
};

// AI-generated insight
export type Insight = {
    payload: {
        recommendations: string[]; // 3 items, max 220 chars each
        reflection_question: string; // max 220 chars
        summary: string; // max 220 chars
    };
    source: "fallback" | "cached" | "cache" | "openai"; // where the insight came from
    createdAt: string; // ISO timestamp
};

// Period summary (weekly/monthly)
export type PeriodSummary = {
    periodType: "weekly" | "monthly";
    startDate: string; // YYYY-MM-DD
    endDate: string; // YYYY-MM-DD
    weightStartKg: number | null;
    weightEndKg: number | null;
    weightChangeKg: number | null;
    avgWellnessScore: number;
    activityLevel: string;
    goalProgressPercentage: number;
    daysTracked: number;
    weightEntriesCount: number;
    generatedAt: string; // ISO timestamp
};

// Health summary with BMI info
export type HealthSummary = {
    heightCm: number;
    latestWeightKg: number;
    bmi: number; // rounded to 1 decimal
    weightDelta7d: number | null; // weight change in last 7 days, kg
};

// Dashboard summary (aggregated view)
export type DashboardSummary = {
    user: UserProfile;
    profile: HealthProfile | null;
    currentWeight: WeightEntry | null;
    activeGoal: Goal | null;
    recentInsight: Insight | null;
    weightTrend: {
        current: number; // kg
        previous: number; // kg (7 days ago)
        change: number; // kg
    } | null;
};

// Response wrappers for API responses
export type ApiResponse<T> = {
    data: T;
    timestamp?: string;
};

export type PaginatedResponse<T> = {
    content: T[];
    page_number: number;
    page_size: number;
    total_elements: number;
    total_pages: number;
    is_first: boolean;
    is_last: boolean;
    has_previous: boolean;
    has_next: boolean;
};
