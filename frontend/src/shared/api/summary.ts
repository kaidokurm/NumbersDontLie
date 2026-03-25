/**
 * Health Summary API
 * Retrieves BMI, weight, and health metrics
 */
import { api } from "./client";
import type { HealthSummary } from "../types";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

// Backend response structure (snake_case)
type HealthSummaryBackendResponse = {
    heightCm: number;
    latestWeightKg: number;
    bmi: number;
    weightDelta7d: number | null;
};

// Transform backend response to frontend type
function transformHealthSummary(data: HealthSummaryBackendResponse | null): HealthSummary | null {
    if (!data) return null;

    return {
        heightCm: data.heightCm,
        latestWeightKg: data.latestWeightKg,
        bmi: data.bmi,
        weightDelta7d: data.weightDelta7d,
    };
}

/**
 * Get health summary including BMI and weight metrics
 * Requires profile and weight data to exist
 */
export function getHealthSummary(token: string): Promise<HealthSummary | null> {
    return api
        .get<HealthSummaryBackendResponse | ApiResponse<HealthSummaryBackendResponse>>("/api/summary", token)
        .then((response) => transformHealthSummary(unwrapApiData(response)));
}

/**
 * Classify BMI into category
 */
export function getBMIClassification(bmi: number): {
    category: "underweight" | "normal" | "overweight" | "obese";
    color: "blue" | "green" | "yellow" | "red";
    label: string;
} {
    if (bmi < 18.5) {
        return { category: "underweight", color: "blue", label: "Underweight" };
    } else if (bmi < 25) {
        return { category: "normal", color: "green", label: "Normal weight" };
    } else if (bmi < 30) {
        return { category: "overweight", color: "yellow", label: "Overweight" };
    } else {
        return { category: "obese", color: "red", label: "Obese" };
    }
}
