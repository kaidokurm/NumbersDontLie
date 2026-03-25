import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { getPrivacyPreferences, upsertPrivacyPreferences } from "../../shared/api/privacy";
import { Alert } from "../../shared/ui/Alert";
import { Card, CardBody, CardTitle } from "../../shared/ui/Card";

export default function ConsentPage() {
    const navigate = useNavigate();
    const getToken = useAuthToken();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleAccept = async () => {
        setLoading(true);
        setError(null);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");

            const current = await getPrivacyPreferences(token);
            await upsertPrivacyPreferences({
                data_usage_consent: true,
                allow_anonymized_analytics: current.allow_anonymized_analytics,
                public_profile_visible: current.public_profile_visible,
                email_notifications_enabled: current.email_notifications_enabled,
            }, token);

            navigate("/", { replace: true });
        } catch (e) {
            setError(e instanceof Error ? e.message : "Failed to save consent");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <Card>
                <CardTitle>Data Usage Consent Required</CardTitle>
                <CardBody>
                    <p className="text-sm text-slate-700 mb-3">
                        To use this app, you must provide explicit consent for health data processing.
                    </p>
                    <p className="text-xs text-slate-600 mb-3">
                        We collect and process health profile data, weight check-ins, and goal progress to provide:
                    </p>
                    <ul className="list-disc pl-5 text-xs text-slate-600 space-y-1 mb-4">
                        <li>BMI and wellness score calculations</li>
                        <li>Progress summaries and visualizations</li>
                        <li>Personalized AI wellness insights</li>
                    </ul>
                    <p className="text-xs text-slate-600 mb-4">
                        You can adjust additional sharing preferences later in Settings. Without this consent, app features remain locked.
                    </p>

                    {error && <Alert tone="error" title="Consent Error" message={error} />}

                    <button
                        className="px-4 py-2 rounded bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-50"
                        onClick={handleAccept}
                        disabled={loading}
                    >
                        {loading ? "Saving..." : "I Agree And Continue"}
                    </button>
                </CardBody>
            </Card>
        </div>
    );
}

