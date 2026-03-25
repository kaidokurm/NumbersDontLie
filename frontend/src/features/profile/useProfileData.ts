import { useState, useCallback, useEffect } from "react";
import { useAuthedQuery } from "../../shared/auth/useAuthedQuery";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { getHealthProfile, upsertHealthProfile } from "../../shared/api/profile";
import { recordWeight, getLatestWeight } from "../../shared/api/weight";
import { ApiError } from "../../shared/api/client";
import type { HealthProfile } from "../../shared/types";

export type FormData = {
    height: number | "";
    weight: number | "";
    age: number | "";
    gender: "MALE" | "FEMALE" | "OTHER";
    activityLevel: "SEDENTARY" | "LIGHTLY_ACTIVE" | "MODERATELY_ACTIVE" | "VERY_ACTIVE" | "EXTREMELY_ACTIVE";
    occupationType: string;
    dietaryPreferencesText: string;
    dietaryRestrictionsText: string;
    activityFrequency: number | "";
    exerciseTypesText: string;
    sessionDuration: "" | "15_30" | "30_60" | "60_plus";
    fitnessLevel: "" | "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
    exerciseEnvironment: "" | "HOME" | "GYM" | "OUTDOORS";
    exerciseTimePreference: "" | "MORNING" | "AFTERNOON" | "EVENING";
    enduranceMinutes: number | "";
    pushups: number | "";
    situps: number | "";
    pullups: number | "";
    run3kmMinutes: number | "";
    run3kmSeconds: number | "";
};

export type ValidationErrors = Partial<Record<keyof FormData, string>>;

export interface ProfileState {
    // Data
    profile: HealthProfile | null;
    formData: FormData;
    isEditing: boolean;

    // Loading & Errors
    isLoading: boolean;
    isSaving: boolean;
    loadError: Error | null;
    saveError: string | null;
    saveSuccess: boolean;
    validationErrors: ValidationErrors;

    // Actions
    handleInputChange: (field: keyof FormData, value: string | number | boolean) => void;
    handleEdit: () => void;
    handleCancel: () => void;
    handleSave: () => Promise<void>;
    clearSuccessMessage: () => void;
}

function validateForm(data: FormData): ValidationErrors {
    const errors: ValidationErrors = {};

    if (!data.height || data.height <= 0) errors.height = "Height is required and must be positive";
    if (!data.weight || data.weight <= 0) errors.weight = "Weight is required and must be positive";
    if (!data.age || data.age < 13 || data.age > 120) errors.age = "Age must be between 13 and 120";
    if (data.activityFrequency !== "" && (data.activityFrequency < 0 || data.activityFrequency > 7)) {
        errors.activityFrequency = "Activity frequency must be between 0 and 7";
    }
    if (data.enduranceMinutes !== "" && data.enduranceMinutes < 0) {
        errors.enduranceMinutes = "Endurance minutes must be 0 or more";
    }
    if (data.pushups !== "" && data.pushups < 0) {
        errors.pushups = "Pushups must be 0 or more";
    }
    if (data.situps !== "" && data.situps < 0) {
        errors.situps = "Situps must be 0 or more";
    }
    if (data.pullups !== "" && data.pullups < 0) {
        errors.pullups = "Pullups must be 0 or more";
    }
    if (data.run3kmMinutes !== "" && data.run3kmMinutes < 0) {
        errors.run3kmMinutes = "3km run minutes must be 0 or more";
    }
    if (data.run3kmSeconds !== "" && (data.run3kmSeconds < 0 || data.run3kmSeconds > 59)) {
        errors.run3kmSeconds = "3km run seconds must be between 0 and 59";
    }

    return errors;
}

function initializeFormData(profile: HealthProfile | null, latestWeight?: number): FormData {
    const fitness = profile?.fitnessAssessment || {};
    const getText = (key: string): string => {
        const value = (fitness as Record<string, unknown>)[key];
        return typeof value === "string" ? value : "";
    };
    const getNumber = (key: string): number | "" => {
        const value = (fitness as Record<string, unknown>)[key];
        if (typeof value === "number") return value;
        if (typeof value === "string" && value.trim() !== "") {
            const parsed = Number(value);
            return Number.isFinite(parsed) ? parsed : "";
        }
        return "";
    };
    const getArrayText = (key: string): string => {
        const value = (fitness as Record<string, unknown>)[key];
        if (Array.isArray(value)) return value.filter(Boolean).join(", ");
        return "";
    };
    const getRunTime = (): { minutes: number | ""; seconds: number | "" } => {
        const raw = (fitness as Record<string, unknown>)["run_3km_time_sec"];
        let totalSec: number | null = null;
        if (typeof raw === "number") totalSec = raw;
        if (typeof raw === "string" && raw.trim() !== "" && Number.isFinite(Number(raw))) totalSec = Number(raw);
        if (totalSec == null || totalSec < 0) return { minutes: "", seconds: "" };
        return { minutes: Math.floor(totalSec / 60), seconds: totalSec % 60 };
    };
    const runTime = getRunTime();

    if (profile) {
        return {
            height: profile.height || "",
            weight: latestWeight || profile.weight || "",
            age: profile.age || "",
            gender: profile.gender || "OTHER",
            activityLevel: profile.activityLevel || "MODERATELY_ACTIVE",
            occupationType: getText("occupation_type"),
            dietaryPreferencesText: (profile.dietaryPreferences || []).join(", "),
            dietaryRestrictionsText: (profile.dietaryRestrictions || []).join(", "),
            activityFrequency: getNumber("current_activity_frequency"),
            exerciseTypesText: getArrayText("exercise_types"),
            sessionDuration: (getText("average_session_duration") as FormData["sessionDuration"]) || "",
            fitnessLevel: (getText("self_assessed_fitness_level") as FormData["fitnessLevel"]) || "",
            exerciseEnvironment: (getText("preferred_exercise_environment") as FormData["exerciseEnvironment"]) || "",
            exerciseTimePreference: (getText("exercise_time_preference") as FormData["exerciseTimePreference"]) || "",
            enduranceMinutes: getNumber("current_endurance_minutes"),
            pushups: getNumber("pushups_count"),
            situps: getNumber("situps_count"),
            pullups: getNumber("pullups_count"),
            run3kmMinutes: runTime.minutes,
            run3kmSeconds: runTime.seconds,
        };
    }
    return {
        height: "",
        weight: latestWeight || "",
        age: "",
        gender: "OTHER",
        activityLevel: "MODERATELY_ACTIVE",
        occupationType: "",
        dietaryPreferencesText: "",
        dietaryRestrictionsText: "",
        activityFrequency: "",
        exerciseTypesText: "",
        sessionDuration: "",
        fitnessLevel: "",
        exerciseEnvironment: "",
        exerciseTimePreference: "",
        enduranceMinutes: "",
        pushups: "",
        situps: "",
        pullups: "",
        run3kmMinutes: "",
        run3kmSeconds: "",
    };
}

export function useProfileData(): ProfileState {
    const { isAuthenticated } = useAppAuth();
    const getToken = useAuthToken();

    const [isEditing, setIsEditing] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [saveSuccess, setSaveSuccess] = useState(false);
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    // Fetch health profile
    const profileQ = useAuthedQuery("healthProfile", getHealthProfile, isAuthenticated && !isEditing);

    // Fetch latest weight entry (shown in profile view)
    const weightQ = useAuthedQuery("latestWeight", getLatestWeight, isAuthenticated && !isEditing);

    // Initialize form as empty - will be populated by useEffect below
    const [formData, setFormData] = useState<FormData>({
        height: "",
        weight: "",
        age: "",
        gender: "OTHER",
        activityLevel: "MODERATELY_ACTIVE",
        occupationType: "",
        dietaryPreferencesText: "",
        dietaryRestrictionsText: "",
        activityFrequency: "",
        exerciseTypesText: "",
        sessionDuration: "",
        fitnessLevel: "",
        exerciseEnvironment: "",
        exerciseTimePreference: "",
        enduranceMinutes: "",
        pushups: "",
        situps: "",
        pullups: "",
        run3kmMinutes: "",
        run3kmSeconds: "",
    });

    const handleInputChange = useCallback(
        (field: keyof FormData, value: string | number | boolean) => {
            setFormData((prev) => ({ ...prev, [field]: value }));
            if (validationErrors[field]) {
                setValidationErrors((prev) => {
                    const next = { ...prev };
                    delete next[field];
                    return next;
                });
            }
        },
        [validationErrors]
    );

    const handleEdit = useCallback(() => {
        setIsEditing(true);
        if (profileQ.data) {
            setFormData(initializeFormData(profileQ.data, weightQ.data?.weight));
        }
    }, [profileQ.data, weightQ.data]);

    const handleCancel = useCallback(() => {
        setIsEditing(false);
        setSaveError(null);
        setValidationErrors({});
    }, []);

    // Update form data when profile or weight data loads
    useEffect(() => {
        if (!isEditing) {
            setFormData(
                initializeFormData(profileQ.data || null, weightQ.data?.weight)
            );
        }
    }, [profileQ.data, weightQ.data, isEditing]);

    // After successfully saving, fetch and preserve the latest weight
    useEffect(() => {
        if (saveSuccess && isAuthenticated) {
            const fetchAndUpdateWeight = async () => {
                try {
                    const token = await getToken();
                    if (!token) return;
                    const latestWeight = await getLatestWeight(token);
                    if (latestWeight) {
                        setFormData((prev) => ({
                            ...prev,
                            weight: latestWeight.weight || prev.weight,
                        }));
                    }
                } catch (error) {
                    console.warn("Failed to fetch latest weight:", error);
                }
            };
            fetchAndUpdateWeight();
        }
    }, [saveSuccess, isAuthenticated, getToken]);

    const handleSave = useCallback(async () => {
        setSaveError(null);
        setSaveSuccess(false);
        const errors = validateForm(formData);

        if (Object.keys(errors).length > 0) {
            setValidationErrors(errors);
            setSaveError("Please fix the highlighted fields and try again.");
            return;
        }

        setIsSaving(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");

            // Convert frontend form data to backend request format
            // Backend expects snake_case field names and birth_year (calculated from age)
            const currentYear = new Date().getFullYear();
            const birthYear = currentYear - Number(formData.age);

            // Map activityLevel enum to backend format (convert to lowercase snake_case)
            const activityLevelMap: Record<string, string> = {
                SEDENTARY: "sedentary",
                LIGHTLY_ACTIVE: "light",
                MODERATELY_ACTIVE: "moderate",
                VERY_ACTIVE: "active",
                EXTREMELY_ACTIVE: "very_active",
            };

            const backendRequest = {
                birth_year: birthYear,
                height_cm: Number(formData.height),
                baseline_activity_level: activityLevelMap[formData.activityLevel] || formData.activityLevel,
                gender: formData.gender,
                dietary_preferences: formData.dietaryPreferencesText
                    .split(",")
                    .map((x) => x.trim())
                    .filter(Boolean),
                dietary_restrictions: formData.dietaryRestrictionsText
                    .split(",")
                    .map((x) => x.trim())
                    .filter(Boolean),
                fitness_assessment: {
                    occupation_type: formData.occupationType || null,
                    current_activity_frequency:
                        formData.activityFrequency === "" ? null : Number(formData.activityFrequency),
                    exercise_types: formData.exerciseTypesText
                        .split(",")
                        .map((x) => x.trim())
                        .filter(Boolean),
                    average_session_duration: formData.sessionDuration || null,
                    self_assessed_fitness_level: formData.fitnessLevel || null,
                    preferred_exercise_environment: formData.exerciseEnvironment || null,
                    exercise_time_preference: formData.exerciseTimePreference || null,
                    current_endurance_minutes:
                        formData.enduranceMinutes === "" ? null : Number(formData.enduranceMinutes),
                    pushups_count: formData.pushups === "" ? null : Number(formData.pushups),
                    situps_count: formData.situps === "" ? null : Number(formData.situps),
                    pullups_count: formData.pullups === "" ? null : Number(formData.pullups),
                    run_3km_time_sec:
                        formData.run3kmMinutes === "" && formData.run3kmSeconds === ""
                            ? null
                            : (Number(formData.run3kmMinutes || 0) * 60) + Number(formData.run3kmSeconds || 0),
                },
                fitness_assessment_completed:
                    formData.occupationType.trim() !== "" ||
                    formData.activityFrequency !== "" ||
                    formData.exerciseTypesText.trim() !== "" ||
                    formData.sessionDuration !== "" ||
                    formData.fitnessLevel !== "" ||
                    formData.exerciseEnvironment !== "" ||
                    formData.exerciseTimePreference !== "" ||
                    formData.enduranceMinutes !== "" ||
                    formData.pushups !== "" ||
                    formData.situps !== "" ||
                    formData.pullups !== "" ||
                    formData.run3kmMinutes !== "" ||
                    formData.run3kmSeconds !== "",
            };

            await upsertHealthProfile(
                backendRequest as unknown as Partial<HealthProfile>,
                token
            );

            // After successfully saving profile, also record a weight entry with today's date
            try {
                const today = new Date().toISOString().split("T")[0]; // YYYY-MM-DD format
                await recordWeight(
                    { weight: Number(formData.weight), date: today },
                    token
                );
            } catch (weightError) {
                // Log but don't fail the entire save - profile was saved successfully
                console.warn("Failed to record weight entry:", weightError);
            }

            setSaveSuccess(true);
            setIsEditing(false);
            setTimeout(() => setSaveSuccess(false), 3000);
        } catch (error) {
            const message = error instanceof ApiError ? error.message : "Failed to save profile";
            setSaveError(message);
        } finally {
            setIsSaving(false);
        }
    }, [formData, getToken]);

    const clearSuccessMessage = useCallback(() => {
        setSaveSuccess(false);
    }, []);

    return {
        profile: profileQ.data || null,
        formData,
        isEditing,
        isLoading: profileQ.loading,
        isSaving,
        loadError: profileQ.error,
        saveError,
        saveSuccess,
        validationErrors,
        handleInputChange,
        handleEdit,
        handleCancel,
        handleSave,
        clearSuccessMessage,
    };
}
