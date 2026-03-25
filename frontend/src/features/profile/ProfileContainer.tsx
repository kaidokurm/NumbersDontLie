import { useAppAuth } from "../../shared/auth/AuthContext";
import { ProfileContent } from "./ProfileContent";
import { useProfileData } from "./useProfileData";

export function ProfileContainer() {
    const { isAuthenticated } = useAppAuth();
    const data = useProfileData();

    return <ProfileContent isAuthenticated={isAuthenticated} data={data} />;
}
