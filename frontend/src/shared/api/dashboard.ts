/**
 * Dashboard API
 * Aggregates multiple data sources for the dashboard view
 */
import { api } from "./client";
import type { DashboardSummary } from "../types";

/**
 * Get dashboard summary (all data needed for dashboard view)
 * This is a convenience endpoint that aggregates:
 * - Current user profile
 * - Health profile
 * - Latest weight entry
 * - Active goal
 * - Recent insight
 * - Weight trend (current vs 7 days ago)
 */
export function getDashboardSummary(token: string): Promise<DashboardSummary> {
    return api.get<DashboardSummary>("/api/dashboard", token);
}
