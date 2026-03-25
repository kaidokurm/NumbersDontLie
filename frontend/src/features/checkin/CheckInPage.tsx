import { useMemo, useState } from "react";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { getActivityHistory, recordActivityCheckin } from "../../shared/api/activity";
import { getWeightHistory, recordWeight } from "../../shared/api/weight";
import { Button } from "../../shared/ui/Button";
import { Card, CardBody, CardTitle, CardSubtitle } from "../../shared/ui/Card";
import { TextField } from "../../shared/ui/TextField";
import { Alert } from "../../shared/ui/Alert";
import { ApiError } from "../../shared/api/client";
import { Spinner } from "../../shared/ui/Spinner";

type WeightValidationErrors = {
    weight?: string;
};

type ActivityValidationErrors = {
    activityType?: string;
    durationMinutes?: string;
};

function validateWeightForm(weight: string): WeightValidationErrors {
    const errors: WeightValidationErrors = {};
    const w = Number(weight);

    if (!weight || w <= 0) errors.weight = "Weight is required and must be positive";
    if (w > 300) errors.weight = "Weight seems too high, please check";

    return errors;
}

function validateActivityForm(activityType: string, durationMinutes: string): ActivityValidationErrors {
    const errors: ActivityValidationErrors = {};
    const minutes = Number(durationMinutes);

    if (!activityType.trim()) errors.activityType = "Activity type is required";
    if (durationMinutes && (!Number.isFinite(minutes) || minutes < 1 || minutes > 720)) {
        errors.durationMinutes = "Duration must be between 1 and 720 minutes";
    }

    return errors;
}

type TimelineItem = {
    id: string;
    type: "weight" | "activity";
    at: string;
    title: string;
    subtitle?: string;
};

export default function CheckInPage() {
    const { isAuthenticated } = useAppAuth();
    const getToken = useAuthToken();

    const [reloadKey, setReloadKey] = useState(0);

    const [weight, setWeight] = useState("");
    const [date, setDate] = useState(new Date().toISOString().split("T")[0]);
    const [notes, setNotes] = useState("");
    const [isWeightSubmitting, setIsWeightSubmitting] = useState(false);
    const [weightError, setWeightError] = useState<string | null>(null);
    const [weightSuccess, setWeightSuccess] = useState(false);
    const [weightValidationErrors, setWeightValidationErrors] = useState<WeightValidationErrors>({});

    const [activityType, setActivityType] = useState("walking");
    const [activityDurationMinutes, setActivityDurationMinutes] = useState("30");
    const [activityIntensity, setActivityIntensity] = useState("medium");
    const [activityNotes, setActivityNotes] = useState("");
    const [isActivitySubmitting, setIsActivitySubmitting] = useState(false);
    const [activityError, setActivityError] = useState<string | null>(null);
    const [activitySuccess, setActivitySuccess] = useState(false);
    const [activityValidationErrors, setActivityValidationErrors] = useState<ActivityValidationErrors>({});
    const activityTypes = ["walking", "running", "cycling", "strength", "cardio", "sports", "mobility", "yoga", "other"];
    const intensityLevels = ["low", "medium", "high"];

    const activityQ = useAuthedQuery(
        `activity-history-checkin-${reloadKey}`,
        (token: string) => getActivityHistory({ size: 20 }, token),
        isAuthenticated
    );
    const weightQ = useAuthedQuery(
        `weight-history-checkin-${reloadKey}`,
        (token: string) => getWeightHistory({ size: 20 }, token),
        isAuthenticated
    );

    const timeline = useMemo<TimelineItem[]>(() => {
        const fromActivity = (activityQ.data?.content || []).map((item) => ({
            id: `activity-${item.id}`,
            type: "activity" as const,
            at: item.checkinAt,
            title: `${item.activityType} ${item.durationMinutes ? `(${item.durationMinutes} min)` : ""}`.trim(),
            subtitle: item.intensity ? `Intensity: ${item.intensity}` : undefined,
        }));

        const fromWeight = (weightQ.data?.content || []).map((item) => ({
            id: `weight-${item.id}`,
            type: "weight" as const,
            at: item.createdAt,
            title: `${item.weight.toFixed(1)} kg`,
            subtitle: item.notes || undefined,
        }));

        return [...fromActivity, ...fromWeight]
            .sort((a, b) => new Date(b.at).getTime() - new Date(a.at).getTime())
            .slice(0, 12);
    }, [activityQ.data, weightQ.data]);

    const handleWeightSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setWeightError(null);
        setWeightSuccess(false);

        const errors = validateWeightForm(weight);
        if (Object.keys(errors).length > 0) {
            setWeightValidationErrors(errors);
            return;
        }

        setIsWeightSubmitting(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");

            await recordWeight(
                {
                    weight: Number(weight),
                    date,
                    notes: notes || undefined,
                },
                token
            );

            setWeight("");
            setDate(new Date().toISOString().split("T")[0]);
            setNotes("");
            setWeightSuccess(true);
            setWeightValidationErrors({});
            setReloadKey((v) => v + 1);
            setTimeout(() => setWeightSuccess(false), 3000);
        } catch (err) {
            const message = err instanceof ApiError ? err.message : "Failed to record weight";
            setWeightError(message);
        } finally {
            setIsWeightSubmitting(false);
        }
    };

    const handleActivitySubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setActivityError(null);
        setActivitySuccess(false);

        const errors = validateActivityForm(activityType, activityDurationMinutes);
        if (Object.keys(errors).length > 0) {
            setActivityValidationErrors(errors);
            return;
        }

        setIsActivitySubmitting(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");

            await recordActivityCheckin(
                {
                    activityType,
                    durationMinutes: activityDurationMinutes ? Number(activityDurationMinutes) : undefined,
                    intensity: activityIntensity,
                    note: activityNotes || undefined,
                    checkinAt: new Date().toISOString(),
                },
                token
            );

            setActivityType("walking");
            setActivityDurationMinutes("30");
            setActivityIntensity("medium");
            setActivityNotes("");
            setActivityValidationErrors({});
            setActivitySuccess(true);
            setReloadKey((v) => v + 1);
            setTimeout(() => setActivitySuccess(false), 3000);
        } catch (err) {
            const message = err instanceof ApiError ? err.message : "Failed to record activity";
            setActivityError(message);
        } finally {
            setIsActivitySubmitting(false);
        }
    };

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <h1 className="text-2xl font-bold text-slate-900">Check In</h1>
            <p className="text-slate-600">Record weight and activity, then review your recent timeline.</p>

            {!isAuthenticated && (
                <Alert tone="info" title="Not Authenticated" message="Please log in to record check-ins." />
            )}

            <Card>
                <CardTitle>Record Weight</CardTitle>
                <CardSubtitle>Quick weight entry for today or another date</CardSubtitle>
                <CardBody>
                    {weightError && <Alert tone="error" title="Error" message={weightError} />}
                    {weightSuccess && (
                        <Alert tone="success" title="Success" message="Weight recorded successfully!" />
                    )}

                    <form onSubmit={handleWeightSubmit} className="space-y-4">
                        <TextField
                            label="Weight (kg)"
                            type="number"
                            value={weight}
                            onChange={(e) => {
                                setWeight(e.target.value);
                                if (weightValidationErrors.weight) setWeightValidationErrors({});
                            }}
                            error={weightValidationErrors.weight}
                            min="20"
                            max="300"
                            step="0.1"
                            placeholder="75.5"
                            disabled={isWeightSubmitting || !isAuthenticated}
                            required
                        />

                        <TextField
                            label="Date"
                            type="date"
                            value={date}
                            onChange={(e) => setDate(e.target.value)}
                            disabled={isWeightSubmitting || !isAuthenticated}
                            required
                        />

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Notes (optional)</label>
                            <textarea
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                placeholder="How are you feeling?"
                                disabled={isWeightSubmitting || !isAuthenticated}
                                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-green-200 transition"
                                rows={3}
                            />
                        </div>

                        <Button type="submit" fullWidth disabled={isWeightSubmitting || !isAuthenticated}>
                            {isWeightSubmitting ? "Recording..." : "Record Weight"}
                        </Button>
                    </form>
                </CardBody>
            </Card>

            <Card>
                <CardTitle>Record Activity</CardTitle>
                <CardSubtitle>Use this for workout frequency goals and activity heatmap</CardSubtitle>
                <CardBody>
                    {activityError && <Alert tone="error" title="Error" message={activityError} />}
                    {activitySuccess && (
                        <Alert tone="success" title="Success" message="Activity recorded successfully!" />
                    )}

                    <form onSubmit={handleActivitySubmit} className="space-y-4">
                        <div className="space-y-1">
                            <label className="block text-sm font-medium text-slate-700">Activity Type</label>
                            <select
                                value={activityType}
                                onChange={(e) => {
                                    setActivityType(e.target.value);
                                    if (activityValidationErrors.activityType) setActivityValidationErrors({});
                                }}
                                disabled={isActivitySubmitting || !isAuthenticated}
                                required
                                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-green-200 transition"
                            >
                                {activityTypes.map((type) => (
                                    <option key={type} value={type}>{type}</option>
                                ))}
                            </select>
                            {activityValidationErrors.activityType && (
                                <div className="text-sm text-red-700">{activityValidationErrors.activityType}</div>
                            )}
                        </div>

                        <TextField
                            label="Duration (minutes)"
                            type="number"
                            value={activityDurationMinutes}
                            onChange={(e) => {
                                setActivityDurationMinutes(e.target.value);
                                if (activityValidationErrors.durationMinutes) setActivityValidationErrors({});
                            }}
                            error={activityValidationErrors.durationMinutes}
                            min="1"
                            max="720"
                            placeholder="30"
                            disabled={isActivitySubmitting || !isAuthenticated}
                        />

                        <div className="space-y-1">
                            <label className="block text-sm font-medium text-slate-700">Intensity</label>
                            <select
                                value={activityIntensity}
                                onChange={(e) => setActivityIntensity(e.target.value)}
                                disabled={isActivitySubmitting || !isAuthenticated}
                                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-green-200 transition"
                            >
                                {intensityLevels.map((level) => (
                                    <option key={level} value={level}>{level}</option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Notes (optional)</label>
                            <textarea
                                value={activityNotes}
                                onChange={(e) => setActivityNotes(e.target.value)}
                                placeholder="Session notes"
                                disabled={isActivitySubmitting || !isAuthenticated}
                                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-green-200 transition"
                                rows={3}
                            />
                        </div>

                        <Button type="submit" fullWidth disabled={isActivitySubmitting || !isAuthenticated}>
                            {isActivitySubmitting ? "Recording..." : "Record Activity"}
                        </Button>
                    </form>
                </CardBody>
            </Card>

            <Card>
                <CardTitle>Recent Timeline</CardTitle>
                <CardSubtitle>Latest activity and weight events</CardSubtitle>
                <CardBody>
                    {(activityQ.loading || weightQ.loading) && (
                        <div className="py-2">
                            <Spinner label="Loading recent check-ins..." />
                        </div>
                    )}
                    {(activityQ.error || weightQ.error) && (
                        <Alert
                            tone="error"
                            title="Timeline Unavailable"
                            message={activityQ.error?.message || weightQ.error?.message || "Failed to load timeline"}
                        />
                    )}
                    {timeline.length === 0 && (
                        <p className="text-sm text-slate-500">No entries yet. Add your first check-in above.</p>
                    )}
                    <div className="space-y-2">
                        {timeline.map((item) => (
                            <div key={item.id} className="rounded-md border border-slate-200 p-3">
                                <div className="flex items-center justify-between gap-3">
                                    <div className="text-sm font-medium text-slate-900">
                                        {item.type === "activity" ? "Activity" : "Weight"}: {item.title}
                                    </div>
                                    <div className="text-xs text-slate-500 whitespace-nowrap">
                                        {new Date(item.at).toLocaleString()}
                                    </div>
                                </div>
                                {item.subtitle && <div className="mt-1 text-xs text-slate-600">{item.subtitle}</div>}
                            </div>
                        ))}
                    </div>
                </CardBody>
            </Card>
        </div>
    );
}
