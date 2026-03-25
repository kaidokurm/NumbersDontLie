import { Navigate } from "react-router-dom";
import React from "react";
import { useAppAuth } from "../../shared/auth/AuthContext";

// ProtectedRoute: Restricts access to authenticated users only
export function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const { isLoading, isAuthenticated } = useAppAuth();
    if (isLoading) return <div>Loading...</div>;
    return isAuthenticated ? <>{children}</> : <Navigate to="/" />;
}
