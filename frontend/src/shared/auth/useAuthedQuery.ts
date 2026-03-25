import { useCallback, useEffect, useState } from "react";
import { useAuthToken } from "./useAuthToken";
import { useApiQuery } from "../hooks/useApiQuery";
import { useAuth0 } from "@auth0/auth0-react";
import { useLocalAuth } from "./useLocalAuth";

export function useAuthedQuery<T>(key: string, fn: (token: string) => Promise<T>, enabledOverride?: boolean) {
  const { isAuthenticated: auth0Authenticated } = useAuth0();
  const { isAuthenticated: localAuthenticated } = useLocalAuth();
  const getToken = useAuthToken();
  const [isAuthenticated, setIsAuthenticated] = useState(auth0Authenticated || localAuthenticated);

  // Listen for local auth changes
  useEffect(() => {
    const handleAuthChange = () => {
      setIsAuthenticated(auth0Authenticated || localAuthenticated);
    };

    window.addEventListener('localAuthChanged', handleAuthChange);
    return () => window.removeEventListener('localAuthChanged', handleAuthChange);
  }, [auth0Authenticated, localAuthenticated]);

  const enabled = enabledOverride ?? isAuthenticated;

  const runner = useCallback(async () => {
    const token = await getToken();
    if (!token) throw new Error("Not authenticated");
    return fn(token);
  }, [fn, getToken]);

  return useApiQuery(key, runner, enabled);
}