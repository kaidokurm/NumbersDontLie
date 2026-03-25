import { useAuth0 } from "@auth0/auth0-react";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { useLocalAuth } from "../../shared/auth/useLocalAuth";

// AuthButton: Shows login/logout button and user info
// Uses centralized AuthContext for auth state
export function AuthButton() {
    const { isAuthenticated, isLoading, authMethod } = useAppAuth();
    const { loginWithRedirect, logout, user } = useAuth0();
    const { logout: localLogout } = useLocalAuth();

    if (isLoading) return <span>Loading...</span>;

    const handleLogout = () => {
        if (authMethod === 'oauth') {
            logout({ logoutParams: { returnTo: window.location.origin } });
        } else if (authMethod === 'local') {
            localLogout();
        }
    };

    const displayName = user?.name || user?.email || "User";

    return (
        <div className="flex items-center gap-2">
            {!isAuthenticated ? (
                <button className="px-3 py-1 rounded bg-blue-600 text-white" onClick={() => loginWithRedirect()}>
                    Log In
                </button>
            ) : (
                <>
                    <span className="text-sm text-gray-700">{displayName}</span>
                    <button
                        className="px-3 py-1 rounded bg-gray-200 text-gray-800"
                        onClick={handleLogout}
                    >
                        Log Out
                    </button>
                </>
            )}
        </div>
    );
}
