import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import { useAppAuth } from "../../../shared/auth/AuthContext";
import { useAuthToken } from "../../../shared/auth/useAuthToken";
import { useLocalAuth } from "../../../shared/auth/useLocalAuth";
import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import { Alert } from "../../../shared/ui/Alert";
import {
    disableTwoFactor,
    enableTwoFactor,
    getTwoFactorStatus,
    setupTwoFactor,
    type TwoFactorSetupResponse,
} from "../../../shared/api/twofactor";
import {
    getPrivacyPreferences,
    upsertPrivacyPreferences,
    type PrivacyPreferences,
} from "../../../shared/api/privacy";
import { downloadDataExport } from "../../../shared/api/export";
import {
    deleteMyAccount,
    getAccountIdentities,
    linkCurrentIdentityByEmail,
    type AccountAuthMethods,
} from "../../../shared/api/account";

export function AccountSettings() {
    const { logout: auth0Logout } = useAuth0();
    const { authMethod } = useAppAuth();
    const { logout: localLogout } = useLocalAuth();
    const getToken = useAuthToken();
    const [isLoading, setIsLoading] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [twoFactorEnabled, setTwoFactorEnabled] = useState(false);
    const [setupData, setSetupData] = useState<TwoFactorSetupResponse | null>(null);
    const [code, setCode] = useState("");
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [privacy, setPrivacy] = useState<PrivacyPreferences | null>(null);
    const [privacySaving, setPrivacySaving] = useState(false);
    const [isExporting, setIsExporting] = useState(false);
    const [exportError, setExportError] = useState<string | null>(null);
    const [deleteConfirmation, setDeleteConfirmation] = useState("");
    const [isDeletingAccount, setIsDeletingAccount] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [authMethods, setAuthMethods] = useState<AccountAuthMethods | null>(null);
    const [isLinkingIdentity, setIsLinkingIdentity] = useState(false);
    const [identityError, setIdentityError] = useState<string | null>(null);
    const [identityMessage, setIdentityMessage] = useState<string | null>(null);

    useEffect(() => {
        if (authMethod !== "local") return;

        const loadStatus = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const token = await getToken();
                if (!token) return;
                const status = await getTwoFactorStatus(token);
                setTwoFactorEnabled(status.enabled);
            } catch (e) {
                setError(e instanceof Error ? e.message : "Failed to load 2FA status");
            } finally {
                setIsLoading(false);
            }
        };

        loadStatus();
    }, [authMethod, getToken]);

    useEffect(() => {
        const loadPrivacy = async () => {
            setError(null);
            try {
                const token = await getToken();
                if (!token) return;
                const prefs = await getPrivacyPreferences(token);
                setPrivacy(prefs);
            } catch (e) {
                setError(e instanceof Error ? e.message : "Failed to load privacy preferences");
            }
        };
        loadPrivacy();
    }, [getToken]);

    useEffect(() => {
        const loadAuthMethods = async () => {
            setIdentityError(null);
            try {
                const token = await getToken();
                if (!token) return;
                const methods = await getAccountIdentities(token);
                setAuthMethods(methods);
            } catch (e) {
                setIdentityError(e instanceof Error ? e.message : "Failed to load linked accounts");
            }
        };
        loadAuthMethods();
    }, [getToken]);

    const handleLogout = () => {
        if (authMethod === 'oauth') {
            // Auth0 logout
            auth0Logout({ logoutParams: { returnTo: window.location.origin } });
        } else if (authMethod === 'local') {
            // Local auth logout
            localLogout();
        }
    };

    const handleSetup = async () => {
        setIsSaving(true);
        setError(null);
        setMessage(null);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            const data = await setupTwoFactor(token);
            setSetupData(data);
            setMessage("2FA secret generated. Add it to your authenticator app, then enter the 6-digit code below.");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Failed to set up 2FA");
        } finally {
            setIsSaving(false);
        }
    };

    const handleEnable = async () => {
        setIsSaving(true);
        setError(null);
        setMessage(null);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            await enableTwoFactor(code.trim(), token);
            setTwoFactorEnabled(true);
            setSetupData(null);
            setCode("");
            setMessage("2FA enabled. You will need an authenticator code at login.");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Failed to enable 2FA");
        } finally {
            setIsSaving(false);
        }
    };

    const handleDisable = async () => {
        setIsSaving(true);
        setError(null);
        setMessage(null);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            await disableTwoFactor(code.trim(), token);
            setTwoFactorEnabled(false);
            setSetupData(null);
            setCode("");
            setMessage("2FA disabled.");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Failed to disable 2FA");
        } finally {
            setIsSaving(false);
        }
    };

    const updatePrivacy = async (next: PrivacyPreferences) => {
        setPrivacySaving(true);
        setError(null);
        setMessage(null);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            const saved = await upsertPrivacyPreferences({
                data_usage_consent: next.data_usage_consent,
                allow_anonymized_analytics: next.allow_anonymized_analytics,
                public_profile_visible: next.public_profile_visible,
                email_notifications_enabled: next.email_notifications_enabled,
            }, token);
            setPrivacy(saved);
            setMessage("Privacy preferences updated.");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Failed to update privacy preferences");
        } finally {
            setPrivacySaving(false);
        }
    };

    const handleExportData = async () => {
        setIsExporting(true);
        setExportError(null);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            await downloadDataExport(token);
        } catch (e) {
            setExportError(e instanceof Error ? e.message : "Failed to export data");
        } finally {
            setIsExporting(false);
        }
    };

    const handleDeleteAccount = async () => {
        setDeleteError(null);
        setIsDeletingAccount(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            await deleteMyAccount(token, deleteConfirmation.trim());
            handleLogout();
        } catch (e) {
            setDeleteError(e instanceof Error ? e.message : "Failed to delete account");
        } finally {
            setIsDeletingAccount(false);
        }
    };

    const handleLinkIdentityByEmail = async () => {
        setIdentityError(null);
        setIdentityMessage(null);
        setIsLinkingIdentity(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            const result = await linkCurrentIdentityByEmail(token);
            setIdentityMessage(result.message || "Identity linked successfully");
            const methods = await getAccountIdentities(token);
            setAuthMethods(methods);
        } catch (e) {
            setIdentityError(e instanceof Error ? e.message : "Failed to link identity");
        } finally {
            setIsLinkingIdentity(false);
        }
    };

    const formatProviderName = (provider: string) => {
        if (provider.toLowerCase() === "google-oauth2") return "Google";
        if (provider.toLowerCase() === "github") return "GitHub";
        return provider;
    };

    return (
        <Card>
            <CardTitle>Account</CardTitle>
            <CardBody>
                <p className="text-sm text-slate-600 mb-4">
                    Manage your account settings and sign out.
                </p>

                {authMethod === "local" && (
                    <div className="mb-6 rounded border border-slate-200 p-4 space-y-3">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm font-semibold text-slate-900">Two-Factor Authentication (2FA)</p>
                                <p className="text-xs text-slate-600">
                                    Optional but recommended. Codes come from an authenticator app.
                                </p>
                            </div>
                            <span
                                className={`px-2 py-1 rounded text-xs font-semibold ${twoFactorEnabled ? "bg-green-100 text-green-800" : "bg-slate-100 text-slate-700"}`}
                            >
                                {isLoading ? "Loading..." : twoFactorEnabled ? "Enabled" : "Disabled"}
                            </span>
                        </div>

                        {message && <Alert tone="success" title="2FA" message={message} />}
                        {error && <Alert tone="error" title="2FA Error" message={error} />}

                        {!twoFactorEnabled && (
                            <button
                                className="px-3 py-2 rounded bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-50"
                                onClick={handleSetup}
                                disabled={isSaving || isLoading}
                            >
                                {isSaving ? "Setting up..." : "Set Up 2FA"}
                            </button>
                        )}

                        {setupData && !twoFactorEnabled && (
                            <div className="space-y-2 rounded bg-slate-50 p-3 border border-slate-200">
                                <div className="flex flex-col items-center gap-2 py-2">
                                    <p className="text-xs text-slate-700">Scan with your authenticator app:</p>
                                    <div className="rounded bg-white p-2 border border-slate-200">
                                        <QRCodeSVG value={setupData.otpauthUri} size={160} includeMargin />
                                    </div>
                                </div>
                                <p className="text-xs text-slate-700">Add this secret to your authenticator app:</p>
                                <p className="font-mono text-xs break-all text-slate-900">{setupData.secret}</p>
                                <p className="text-xs text-slate-700">Or use this OTP URI:</p>
                                <p className="font-mono text-xs break-all text-slate-900">{setupData.otpauthUri}</p>
                                <input
                                    type="text"
                                    value={code}
                                    onChange={(e) => setCode(e.target.value)}
                                    placeholder="Enter 6-digit code"
                                    className="w-full px-3 py-2 border border-slate-300 rounded text-sm"
                                />
                                <button
                                    className="px-3 py-2 rounded bg-green-600 text-white text-sm font-semibold hover:bg-green-700 disabled:opacity-50"
                                    onClick={handleEnable}
                                    disabled={isSaving || code.trim().length !== 6}
                                >
                                    {isSaving ? "Enabling..." : "Enable 2FA"}
                                </button>
                            </div>
                        )}

                        {twoFactorEnabled && (
                            <div className="space-y-2">
                                <input
                                    type="text"
                                    value={code}
                                    onChange={(e) => setCode(e.target.value)}
                                    placeholder="Enter current 6-digit code to disable"
                                    className="w-full px-3 py-2 border border-slate-300 rounded text-sm"
                                />
                                <button
                                    className="px-3 py-2 rounded bg-amber-600 text-white text-sm font-semibold hover:bg-amber-700 disabled:opacity-50"
                                    onClick={handleDisable}
                                    disabled={isSaving || code.trim().length !== 6}
                                >
                                    {isSaving ? "Disabling..." : "Disable 2FA"}
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {privacy && (
                    <div className="mb-6 rounded border border-slate-200 p-4 space-y-3">
                        <p className="text-sm font-semibold text-slate-900">Privacy & Data Usage</p>
                        <p className="text-xs text-slate-600">
                            We collect health profile details, weight check-ins, and goal progress to calculate BMI/wellness metrics,
                            generate summaries, and provide personalized AI insights. You can withdraw consent at any time in this section.
                        </p>
                        <label className="flex items-start gap-2 text-sm text-slate-700">
                            <input
                                type="checkbox"
                                checked={privacy.data_usage_consent}
                                disabled={privacySaving}
                                onChange={(e) =>
                                    updatePrivacy({
                                        ...privacy,
                                        data_usage_consent: e.target.checked,
                                        allow_anonymized_analytics: e.target.checked && privacy.allow_anonymized_analytics,
                                        public_profile_visible: e.target.checked && privacy.public_profile_visible,
                                    })
                                }
                            />
                            <span>
                                I explicitly consent to collecting and processing my health data for analytics, recommendations, and AI insights.
                            </span>
                        </label>

                        <label className="flex items-start gap-2 text-sm text-slate-700">
                            <input
                                type="checkbox"
                                checked={privacy.allow_anonymized_analytics}
                                disabled={privacySaving || !privacy.data_usage_consent}
                                onChange={(e) =>
                                    updatePrivacy({
                                        ...privacy,
                                        allow_anonymized_analytics: e.target.checked,
                                    })
                                }
                            />
                            <span>Allow anonymized analytics usage.</span>
                        </label>

                        <label className="flex items-start gap-2 text-sm text-slate-700">
                            <input
                                type="checkbox"
                                checked={privacy.public_profile_visible}
                                disabled={privacySaving || !privacy.data_usage_consent}
                                onChange={(e) =>
                                    updatePrivacy({
                                        ...privacy,
                                        public_profile_visible: e.target.checked,
                                    })
                                }
                            />
                            <span>Allow public profile visibility.</span>
                        </label>

                        <label className="flex items-start gap-2 text-sm text-slate-700">
                            <input
                                type="checkbox"
                                checked={privacy.email_notifications_enabled}
                                disabled={privacySaving}
                                onChange={(e) =>
                                    updatePrivacy({
                                        ...privacy,
                                        email_notifications_enabled: e.target.checked,
                                    })
                                }
                            />
                            <span>Receive email notifications.</span>
                        </label>
                    </div>
                )}

                <div className="mb-6 rounded border border-slate-200 p-4 space-y-2">
                    <p className="text-sm font-semibold text-slate-900">Export Your Data</p>
                    <p className="text-xs text-slate-600">
                        Download your account, health profile, privacy settings, goals, progress, weight history, and
                        AI insights in JSON format.
                    </p>
                    <button
                        className="px-3 py-2 rounded bg-slate-800 text-white text-sm font-semibold hover:bg-slate-900 disabled:opacity-50"
                        onClick={handleExportData}
                        disabled={isExporting}
                    >
                        {isExporting ? "Exporting..." : "Export Data (JSON)"}
                    </button>
                    {exportError && <Alert tone="error" title="Export Error" message={exportError} />}
                </div>

                <div className="mb-6 rounded border border-red-200 bg-red-50 p-4 space-y-2">
                    <p className="text-sm font-semibold text-red-900">Delete Account</p>
                    <p className="text-xs text-red-800">
                        This permanently deletes your account and associated data (profile, goals, weights, insights,
                        and preferences). This action cannot be undone.
                    </p>
                    <p className="text-xs text-red-800">Type <span className="font-mono font-semibold">DELETE MY ACCOUNT</span> to confirm.</p>
                    <input
                        type="text"
                        value={deleteConfirmation}
                        onChange={(e) => setDeleteConfirmation(e.target.value)}
                        placeholder="DELETE MY ACCOUNT"
                        className="w-full px-3 py-2 border border-red-300 rounded text-sm"
                    />
                    <button
                        className="px-3 py-2 rounded bg-red-700 text-white text-sm font-semibold hover:bg-red-800 disabled:opacity-50"
                        onClick={handleDeleteAccount}
                        disabled={isDeletingAccount || deleteConfirmation.trim() !== "DELETE MY ACCOUNT"}
                    >
                        {isDeletingAccount ? "Deleting..." : "Permanently Delete Account"}
                    </button>
                    {deleteError && <Alert tone="error" title="Delete Error" message={deleteError} />}
                </div>

                {authMethod === "oauth" && (
                    <div className="mb-6 rounded border border-slate-200 p-4">
                        <p className="text-sm text-slate-700">
                            2FA for OAuth users is managed by your identity provider.
                        </p>
                    </div>
                )}

                <div className="mb-6 rounded border border-slate-200 p-4 space-y-2">
                    <p className="text-sm font-semibold text-slate-900">Linked Sign-In Methods</p>
                    {authMethods?.identities?.length ? (
                        <ul className="text-sm text-slate-700 space-y-1">
                            {authMethods.identities.map((identity) => (
                                <li key={`${identity.provider}:${identity.providerSub}`}>
                                    {formatProviderName(identity.provider)}
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="text-xs text-slate-600">No linked OAuth providers yet.</p>
                    )}
                    {identityMessage && <Alert tone="success" title="Linked Accounts" message={identityMessage} />}
                    {identityError && <Alert tone="error" title="Linked Accounts Error" message={identityError} />}

                    {authMethod === "oauth" && (
                        <button
                            className="px-3 py-2 rounded bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-50"
                            onClick={handleLinkIdentityByEmail}
                            disabled={isLinkingIdentity}
                        >
                            {isLinkingIdentity ? "Linking..." : "Link Current OAuth Identity By Email"}
                        </button>
                    )}
                    <p className="text-xs text-slate-600">
                        If sign-in was blocked due to an existing account with the same email, use this action while logged in with
                        that provider to attach it to your existing account.
                    </p>
                </div>

                <div className="fixed bottom-0 left-0 right-0 md:static md:bg-transparent md:border-0 md:p-0 md:flex md:justify-start bg-white border-t border-slate-200 p-4 flex justify-center z-50 mb-16 md:mb-0">
                    <button
                        className="px-4 py-2 rounded bg-red-600 text-white font-semibold hover:bg-red-700 transition disabled:opacity-50"
                        onClick={handleLogout}
                    >
                        Log Out
                    </button>
                </div>
            </CardBody>
        </Card>
    );
}
