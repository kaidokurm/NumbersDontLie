import React, { createContext, useContext, useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useLocalAuth } from './useLocalAuth';
import { AUTH0_AUDIENCE } from '../config';

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  authMethod: 'oauth' | 'local' | null; // Which auth method is active
  getAccessToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * AuthProvider: Unified authentication context for the entire app
 * Combines Auth0 OAuth and local email/password authentication
 * Wraps the entire app in main.tsx
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const { isAuthenticated: auth0Authenticated, isLoading: auth0Loading, getAccessTokenSilently } = useAuth0();
  const { isAuthenticated: localAuthenticated, getAccessToken: getLocalAccessToken } = useLocalAuth();
  const [isAuthenticated, setIsAuthenticated] = useState(auth0Authenticated || localAuthenticated);
  const [authMethod, setAuthMethod] = useState<'oauth' | 'local' | null>(null);

  // Determine which auth method is active and update state
  useEffect(() => {
    const newIsAuthenticated = auth0Authenticated || localAuthenticated;
    const newAuthMethod: 'oauth' | 'local' | null = auth0Authenticated ? 'oauth' : localAuthenticated ? 'local' : null;
    
    setIsAuthenticated(newIsAuthenticated);
    setAuthMethod(newAuthMethod);
  }, [auth0Authenticated, localAuthenticated]);

  // Listen for local auth changes
  useEffect(() => {
    const handleLocalAuthChange = () => {
      // Will be picked up by the dependency on localAuthenticated
    };

    window.addEventListener('localAuthChanged', handleLocalAuthChange);
    return () => window.removeEventListener('localAuthChanged', handleLocalAuthChange);
  }, []);

  // Get access token from appropriate auth method
  const getAccessToken = async (): Promise<string | null> => {
    if (authMethod === 'oauth') {
      try {
        return await getAccessTokenSilently({
          authorizationParams: {
            audience: AUTH0_AUDIENCE,
            scope: 'openid profile email',
          },
        });
      } catch (e) {
        console.error('Failed to get OAuth token:', e);
        return null;
      }
    } else if (authMethod === 'local') {
      return getLocalAccessToken();
    }
    return null;
  };

  const value: AuthContextType = {
    isAuthenticated,
    isLoading: auth0Loading,
    authMethod,
    getAccessToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * useAppAuth: Hook to access combined auth context
 * Use this throughout the app instead of useAuth0() or useLocalAuth()
 */
export function useAppAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAppAuth must be used within AuthProvider');
  }
  return context;
}
