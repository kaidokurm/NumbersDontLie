import { api } from "./client";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

export type PrivacyPreferences = {
    data_usage_consent: boolean;
    consent_given_at: string | null;
    allow_anonymized_analytics: boolean;
    public_profile_visible: boolean;
    email_notifications_enabled: boolean;
    updated_at: string;
};

export function getPrivacyPreferences(token: string): Promise<PrivacyPreferences> {
    return api
        .get<PrivacyPreferences | ApiResponse<PrivacyPreferences>>("/api/privacy-preferences", token)
        .then((response) => unwrapApiData(response));
}

export function upsertPrivacyPreferences(data: {
    data_usage_consent: boolean;
    allow_anonymized_analytics: boolean;
    public_profile_visible: boolean;
    email_notifications_enabled: boolean;
}, token: string): Promise<PrivacyPreferences> {
    return api
        .post<PrivacyPreferences | ApiResponse<PrivacyPreferences>>("/api/privacy-preferences", data, token)
        .then((response) => unwrapApiData(response));
}
