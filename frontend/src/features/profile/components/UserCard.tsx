import { useAuth0 } from "@auth0/auth0-react";
import { useAppAuth } from "../../../shared/auth/AuthContext";
import { Card } from "../../../shared/ui/Card";
import { useEffect, useState } from "react";

interface UserCardProps {
    isAuthenticated: boolean;
}

interface UserInfo {
    name?: string;
    email?: string;
    picture?: string;
}

export function UserCard({ isAuthenticated }: UserCardProps) {
    const { user: auth0User } = useAuth0();
    const { authMethod } = useAppAuth();
    const [localUserEmail, setLocalUserEmail] = useState<string | null>(null);

    useEffect(() => {
        // For local auth, try to decode the JWT to get email
        const token = localStorage.getItem('accessToken');
        if (token && authMethod === 'local') {
            try {
                const parts = token.split('.');
                if (parts.length === 3) {
                    const payload = JSON.parse(atob(parts[1]));
                    setLocalUserEmail(payload.email);
                }
            } catch (e) {
                console.error('Failed to decode local auth token:', e);
            }
        }
    }, [authMethod]);

    if (!isAuthenticated) {
        return null;
    }

    // Use OAuth user info if available, otherwise use local auth email
    const displayUser: UserInfo = authMethod === 'oauth' ? {
        name: auth0User?.name,
        email: auth0User?.email,
        picture: auth0User?.picture,
    } : {
        name: localUserEmail?.split('@')[0] || 'User',
        email: localUserEmail || undefined,
    };

    return (
        <Card>
            <div className="flex flex-col items-center">
                {displayUser.picture && (
                    <img
                        src={displayUser.picture}
                        alt={displayUser.name}
                        className="w-16 h-16 rounded-full mb-3 border-2 border-slate-200"
                    />
                )}
                {!displayUser.picture && (
                    <div className="w-16 h-16 rounded-full mb-3 border-2 border-slate-200 bg-slate-200 flex items-center justify-center">
                        <span className="text-xl font-bold text-slate-600">
                            {displayUser.name?.[0]?.toUpperCase() || 'U'}
                        </span>
                    </div>
                )}
                <div className="font-semibold text-slate-900">{displayUser.name}</div>
                <div className="text-sm text-slate-500">{displayUser.email}</div>
            </div>
        </Card>
    );
}
