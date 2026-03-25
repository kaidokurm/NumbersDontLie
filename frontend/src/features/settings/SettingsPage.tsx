import { UserCard } from "../profile/components/UserCard";
import { AccountSettings } from "../profile/components/AccountSettings";
import { useAppAuth } from "../../shared/auth/AuthContext";

export default function SettingsPage() {
    const { isAuthenticated } = useAppAuth();

    return (
        <div className="space-y-4 pb-32 md:pb-4">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Settings</h1>
                <p className="text-slate-600">Manage account security, privacy, exports, and linked sign-in methods.</p>
            </div>

            <UserCard isAuthenticated={isAuthenticated} />
            <AccountSettings />
        </div>
    );
}
