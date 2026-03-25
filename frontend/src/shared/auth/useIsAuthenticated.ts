import { useAuth0 } from '@auth0/auth0-react';
import { useEffect, useState } from 'react';

/**
 * Combined authentication hook that checks BOTH Auth0 OAuth AND local email/password auth
 * Returns true if user is authenticated via either method
 */
export function useIsAuthenticated(): boolean {
    const { isAuthenticated: auth0Authenticated } = useAuth0();
    const [localAuthenticated, setLocalAuthenticated] = useState(() => {
        return !!localStorage.getItem('accessToken');
    });

    useEffect(() => {
        const handleAuthChange = () => {
            setLocalAuthenticated(!!localStorage.getItem('accessToken'));
        };

        window.addEventListener('localAuthChanged', handleAuthChange);
        window.addEventListener('storage', handleAuthChange);
        return () => {
            window.removeEventListener('localAuthChanged', handleAuthChange);
            window.removeEventListener('storage', handleAuthChange);
        };
    }, []);

    return auth0Authenticated || localAuthenticated;
}
