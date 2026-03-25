import { Card, CardBody, CardTitle, CardSubtitle } from "../../../shared/ui/Card";
import { TextField } from "../../../shared/ui/TextField";
import { SelectField } from "../../../shared/ui/SelectField";
import { Alert } from "../../../shared/ui/Alert";
import { Button } from "../../../shared/ui/Button";
import { Spinner } from "../../../shared/ui/Spinner";
import type { FormData, ValidationErrors } from "../useProfileData";
import type { HealthProfile } from "../../../shared/types";

interface HealthProfileSectionProps {
    profile: HealthProfile | null;
    isLoading: boolean;
    isEditing: boolean;
    isSaving: boolean;
    formData: FormData;
    loadError: Error | null;
    saveError: string | null;
    saveSuccess: boolean;
    validationErrors: ValidationErrors;
    onInputChange: (field: keyof FormData, value: string | number) => void;
    onEdit: () => void;
    onSave: () => Promise<void>;
    onCancel: () => void;
}

export function HealthProfileSection({
    profile,
    isLoading,
    isEditing,
    isSaving,
    formData,
    loadError,
    saveError,
    saveSuccess,
    validationErrors,
    onInputChange,
    onEdit,
    onSave,
    onCancel,
}: HealthProfileSectionProps) {
    const formatLabel = (value: string | number | "") =>
        value === "" ? "—" : String(value).replace(/_/g, " ");
    const formatCommaList = (value: string) => (value.trim() ? value : "—");
    const formatRunTime = () =>
        formData.run3kmMinutes !== "" || formData.run3kmSeconds !== ""
            ? `${formData.run3kmMinutes || 0}m ${formData.run3kmSeconds || 0}s`
            : "—";

    return (
        <Card>
            <CardTitle>Health Profile</CardTitle>
            <CardSubtitle>
                {isEditing ? "Update your health information" : "Your health metrics and fitness goals"}
            </CardSubtitle>
            <CardBody>
                {isLoading && <Spinner label="Loading profile..." />}
                {loadError && (
                    <Alert tone="error" title="Error" message={`Failed to load profile: ${loadError.message}`} />
                )}

                {saveError && <Alert tone="error" title="Error" message={saveError} />}
                {saveSuccess && (
                    <Alert tone="success" title="Success" message="Profile saved successfully!" />
                )}

                {!isEditing && profile ? (
                    // View Mode
                    <div className="space-y-3">
                        <div className="grid grid-cols-2 gap-3">
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Height</div>
                                <div className="text-lg font-semibold text-slate-900">
                                    {profile.height} cm
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Current Weight</div>
                                <div className="text-lg font-semibold text-slate-900">
                                    {formData.weight ? `${formData.weight} kg` : "—"}
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Age</div>
                                <div className="text-lg font-semibold text-slate-900">
                                    {formData.age || "—"}
                                </div>
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-3 text-sm">
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Gender</div>
                                <div className="font-medium text-slate-900">
                                    {profile.gender}
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Activity Level</div>
                                <div className="font-medium text-slate-900">
                                    {profile.activityLevel ? profile.activityLevel.replace(/_/g, " ") : "—"}
                                </div>
                            </div>
                        </div>
                        <div className="grid grid-cols-1 gap-3 text-sm">
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Occupation Type</div>
                                <div className="font-medium text-slate-900">{formData.occupationType || "—"}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Dietary Preferences</div>
                                <div className="font-medium text-slate-900">{formData.dietaryPreferencesText || "—"}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Dietary Restrictions</div>
                                <div className="font-medium text-slate-900">{formData.dietaryRestrictionsText || "—"}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Weekly Activity Frequency</div>
                                <div className="font-medium text-slate-900">
                                    {formData.activityFrequency !== "" ? `${formData.activityFrequency} days` : "—"}
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Exercise Types</div>
                                <div className="font-medium text-slate-900">{formatCommaList(formData.exerciseTypesText)}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Average Session Duration</div>
                                <div className="font-medium text-slate-900">{formatLabel(formData.sessionDuration)}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Self-Assessed Fitness Level</div>
                                <div className="font-medium text-slate-900">{formatLabel(formData.fitnessLevel)}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Preferred Exercise Environment</div>
                                <div className="font-medium text-slate-900">{formatLabel(formData.exerciseEnvironment)}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Preferred Time of Day</div>
                                <div className="font-medium text-slate-900">{formatLabel(formData.exerciseTimePreference)}</div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Current Endurance Level</div>
                                <div className="font-medium text-slate-900">
                                    {formData.enduranceMinutes !== "" ? `${formData.enduranceMinutes} min` : "—"}
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">Strength Indicators</div>
                                <div className="font-medium text-slate-900">
                                    Pushups: {formData.pushups !== "" ? formData.pushups : "—"} | Situps: {formData.situps !== "" ? formData.situps : "—"} | Pullups: {formData.pullups !== "" ? formData.pullups : "—"}
                                </div>
                            </div>
                            <div className="p-3 bg-slate-50 rounded">
                                <div className="text-xs text-slate-600">3km Run Time</div>
                                <div className="font-medium text-slate-900">
                                    {formatRunTime()}
                                </div>
                            </div>
                        </div>
                        <Button fullWidth onClick={onEdit}>
                            Edit Profile
                        </Button>
                    </div>
                ) : (
                    // Edit Mode
                    <div className="space-y-4">
                        <TextField
                            label="Height (cm)"
                            type="number"
                            value={formData.height}
                            onChange={(e) => onInputChange("height", e.target.value)}
                            error={validationErrors.height}
                            min="50"
                            max="300"
                            placeholder="180"
                        />
                        <TextField
                            label="Current Weight (kg)"
                            type="number"
                            value={formData.weight}
                            onChange={(e) => onInputChange("weight", e.target.value)}
                            error={validationErrors.weight}
                            min="20"
                            max="300"
                            placeholder="75"
                            step="0.1"
                        />
                        <TextField
                            label="Age"
                            type="number"
                            value={formData.age}
                            onChange={(e) => onInputChange("age", e.target.value)}
                            error={validationErrors.age}
                            min="13"
                            max="120"
                            placeholder="30"
                        />
                        <SelectField
                            label="Gender"
                            value={formData.gender}
                            onChange={(e) =>
                                onInputChange("gender", e.target.value as FormData["gender"])
                            }
                            options={[
                                { value: "MALE", label: "Male" },
                                { value: "FEMALE", label: "Female" },
                                { value: "OTHER", label: "Other" },
                            ]}
                        />
                        <SelectField
                            label="Activity Level"
                            value={formData.activityLevel}
                            onChange={(e) =>
                                onInputChange(
                                    "activityLevel",
                                    e.target.value as FormData["activityLevel"]
                                )
                            }
                            options={[
                                { value: "SEDENTARY", label: "Sedentary (little exercise)" },
                                { value: "LIGHTLY_ACTIVE", label: "Lightly active (1-3 days/week)" },
                                { value: "MODERATELY_ACTIVE", label: "Moderately active (3-5 days/week)" },
                                { value: "VERY_ACTIVE", label: "Very active (6-7 days/week)" },
                                { value: "EXTREMELY_ACTIVE", label: "Extremely active (twice per day)" },
                            ]}
                        />
                        <TextField
                            label="Occupation Type"
                            value={formData.occupationType}
                            onChange={(e) => onInputChange("occupationType", e.target.value)}
                            placeholder="Office / Manual / Shift work / Student..."
                        />
                        <TextField
                            label="Dietary Preferences (comma-separated)"
                            value={formData.dietaryPreferencesText}
                            onChange={(e) => onInputChange("dietaryPreferencesText", e.target.value)}
                            placeholder="Vegetarian, High-protein"
                        />
                        <TextField
                            label="Dietary Restrictions (comma-separated)"
                            value={formData.dietaryRestrictionsText}
                            onChange={(e) => onInputChange("dietaryRestrictionsText", e.target.value)}
                            placeholder="Lactose-free, Gluten-free"
                        />

                        <h3 className="text-sm font-semibold text-slate-900 pt-2">Fitness Assessment</h3>
                        <TextField
                            label="Current Weekly Activity Frequency (0-7)"
                            type="number"
                            value={formData.activityFrequency}
                            onChange={(e) => onInputChange("activityFrequency", e.target.value)}
                            error={validationErrors.activityFrequency}
                            min="0"
                            max="7"
                        />
                        <TextField
                            label="Exercise Types (comma-separated)"
                            value={formData.exerciseTypesText}
                            onChange={(e) => onInputChange("exerciseTypesText", e.target.value)}
                            placeholder="Cardio, Strength, Flexibility, Sports"
                        />
                        <SelectField
                            label="Average Session Duration"
                            value={formData.sessionDuration}
                            onChange={(e) => onInputChange("sessionDuration", e.target.value as FormData["sessionDuration"])}
                            options={[
                                { value: "", label: "Select..." },
                                { value: "15_30", label: "15-30 min" },
                                { value: "30_60", label: "30-60 min" },
                                { value: "60_plus", label: "60+ min" },
                            ]}
                        />
                        <SelectField
                            label="Self-Assessed Fitness Level"
                            value={formData.fitnessLevel}
                            onChange={(e) => onInputChange("fitnessLevel", e.target.value as FormData["fitnessLevel"])}
                            options={[
                                { value: "", label: "Select..." },
                                { value: "BEGINNER", label: "Beginner" },
                                { value: "INTERMEDIATE", label: "Intermediate" },
                                { value: "ADVANCED", label: "Advanced" },
                            ]}
                        />
                        <SelectField
                            label="Preferred Exercise Environment"
                            value={formData.exerciseEnvironment}
                            onChange={(e) =>
                                onInputChange("exerciseEnvironment", e.target.value as FormData["exerciseEnvironment"])
                            }
                            options={[
                                { value: "", label: "Select..." },
                                { value: "HOME", label: "Home" },
                                { value: "GYM", label: "Gym" },
                                { value: "OUTDOORS", label: "Outdoors" },
                            ]}
                        />
                        <SelectField
                            label="Preferred Time of Day"
                            value={formData.exerciseTimePreference}
                            onChange={(e) =>
                                onInputChange("exerciseTimePreference", e.target.value as FormData["exerciseTimePreference"])
                            }
                            options={[
                                { value: "", label: "Select..." },
                                { value: "MORNING", label: "Morning" },
                                { value: "AFTERNOON", label: "Afternoon" },
                                { value: "EVENING", label: "Evening" },
                            ]}
                        />
                        <TextField
                            label="Current Endurance Level (minutes)"
                            type="number"
                            value={formData.enduranceMinutes}
                            onChange={(e) => onInputChange("enduranceMinutes", e.target.value)}
                            error={validationErrors.enduranceMinutes}
                            min="0"
                        />
                        <TextField
                            label="Pushups (count)"
                            type="number"
                            value={formData.pushups}
                            onChange={(e) => onInputChange("pushups", e.target.value)}
                            error={validationErrors.pushups}
                            min="0"
                        />
                        <TextField
                            label="Situps (count)"
                            type="number"
                            value={formData.situps}
                            onChange={(e) => onInputChange("situps", e.target.value)}
                            error={validationErrors.situps}
                            min="0"
                        />
                        <TextField
                            label="Pullups (count)"
                            type="number"
                            value={formData.pullups}
                            onChange={(e) => onInputChange("pullups", e.target.value)}
                            error={validationErrors.pullups}
                            min="0"
                        />
                        <div className="grid grid-cols-2 gap-2">
                            <TextField
                                label="3km Run Minutes"
                                type="number"
                                value={formData.run3kmMinutes}
                                onChange={(e) => onInputChange("run3kmMinutes", e.target.value)}
                                error={validationErrors.run3kmMinutes}
                                min="0"
                            />
                            <TextField
                                label="3km Run Seconds"
                                type="number"
                                value={formData.run3kmSeconds}
                                onChange={(e) => onInputChange("run3kmSeconds", e.target.value)}
                                error={validationErrors.run3kmSeconds}
                                min="0"
                                max="59"
                            />
                        </div>

                        {/* Edit Form Actions */}
                        <div className="flex gap-2 pt-4">
                            <Button onClick={onSave} disabled={isSaving} fullWidth>
                                {isSaving ? "Saving..." : "Save Profile"}
                            </Button>
                            <Button
                                onClick={onCancel}
                                disabled={isSaving}
                                className="bg-slate-600 hover:bg-slate-700"
                            >
                                Cancel
                            </Button>
                        </div>
                    </div>
                )}
            </CardBody>
        </Card>
    );
}
