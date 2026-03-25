import { useCallback } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { AUTH0_AUDIENCE } from "../config";
import { useLocalAuth } from "./useLocalAuth";

export function useAuthToken() {
  const { isAuthenticated, getAccessTokenSilently } = useAuth0();
  const { getAccessToken: getLocalToken } = useLocalAuth();

  return useCallback(async (): Promise<string | null> => {
    // Check for local email/password JWT first
    const localToken = getLocalToken();
    if (localToken) return localToken;

    // Fall back to Auth0 token
    if (!isAuthenticated) return null;

    return getAccessTokenSilently({
      authorizationParams: {
        audience: AUTH0_AUDIENCE,
        scope: "openid profile email",
      },
    });
  }, [isAuthenticated, getAccessTokenSilently, getLocalToken]);
}
