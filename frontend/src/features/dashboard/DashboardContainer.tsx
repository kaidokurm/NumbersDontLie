import { useState } from "react";
import { DashboardContent } from "./DashboardContent";
import { useDashboardData } from "./useDashboardData";
import { useAppAuth } from "../../shared/auth/AuthContext";
import { useAuthToken } from "../../shared/auth/useAuthToken";
import { isEmailCollisionErrorMessage, linkCurrentIdentityByEmail } from "../../shared/api/account";

export function DashboardContainer() {
    const { isAuthenticated } = useAppAuth();
    const getToken = useAuthToken();
    const data = useDashboardData();
    const [isResolvingCollision, setIsResolvingCollision] = useState(false);
    const [collisionResolveError, setCollisionResolveError] = useState<string | null>(null);
    const [collisionResolveMessage, setCollisionResolveMessage] = useState<string | null>(null);

    const collisionError = isEmailCollisionErrorMessage(data.error?.message) ? data.error?.message || null : null;

    const onResolveCollision = async () => {
        setCollisionResolveError(null);
        setCollisionResolveMessage(null);
        setIsResolvingCollision(true);
        try {
            const token = await getToken();
            if (!token) throw new Error("Not authenticated");
            const result = await linkCurrentIdentityByEmail(token);
            setCollisionResolveMessage(result.message || "Identity linked successfully. Reloading...");
            window.location.reload();
        } catch (e) {
            setCollisionResolveError(e instanceof Error ? e.message : "Failed to link current provider");
        } finally {
            setIsResolvingCollision(false);
        }
    };

    return (
        <DashboardContent
            isAuthenticated={isAuthenticated}
            data={data}
            collisionError={collisionError}
            isResolvingCollision={isResolvingCollision}
            collisionResolveError={collisionResolveError}
            collisionResolveMessage={collisionResolveMessage}
            onResolveCollision={onResolveCollision}
        />
    );
}
