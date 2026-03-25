// Hook to manage local email/password authentication
// Stores JWT in localStorage and syncs auth state across the app
import { useCallback, useEffect, useState } from 'react';

export interface LocalAuthState {
    isAuthenticated: boolean;
    accessToken: string | null;
    refreshToken: string | null;
}

export function useLocalAuth() {
    const getAuthState = useCallback((): LocalAuthState => {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');

        return {
            isAuthenticated: !!accessToken,
            accessToken,
            refreshToken,
        };
    }, []);

    // Track auth state changes - updates when localAuthChanged event fires
    const [authState, setAuthState] = useState(() => getAuthState());

    useEffect(() => {
        const handleAuthChange = () => {
            setAuthState(getAuthState());
        };

        window.addEventListener('localAuthChanged', handleAuthChange);
        return () => window.removeEventListener('localAuthChanged', handleAuthChange);
    }, [getAuthState]);

    const login = useCallback((accessToken: string, refreshToken: string) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        window.dispatchEvent(new Event('localAuthChanged'));
    }, []);

    const logout = useCallback(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.dispatchEvent(new Event('localAuthChanged'));
    }, []);

    const getAccessToken = useCallback((): string | null => {
        return localStorage.getItem('accessToken');
    }, []);

    return {
        ...authState,
        login,
        logout,
        getAccessToken,
        getAuthState,
    };
}
