import { UserCard } from "./components/UserCard";
import { HealthProfileSection } from "./components/HealthProfileSection";
import type { ProfileState } from "./useProfileData";

interface ProfileContentProps {
    isAuthenticated: boolean;
    data: ProfileState;
}

export function ProfileContent({ isAuthenticated, data }: ProfileContentProps) {
    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <h1 className="text-2xl font-bold text-slate-900">Health Profile</h1>
            <p className="text-slate-600">Manage your baseline health data and fitness context.</p>

            <UserCard isAuthenticated={isAuthenticated} />

            <HealthProfileSection
                profile={data.profile}
                isLoading={data.isLoading}
                isEditing={data.isEditing}
                isSaving={data.isSaving}
                formData={data.formData}
                loadError={data.loadError}
                saveError={data.saveError}
                saveSuccess={data.saveSuccess}
                validationErrors={data.validationErrors}
                onInputChange={data.handleInputChange}
                onEdit={data.handleEdit}
                onSave={data.handleSave}
                onCancel={data.handleCancel}
            />
        </div>
    );
}
