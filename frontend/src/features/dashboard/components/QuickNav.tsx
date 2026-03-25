import { Button } from "../../../shared/ui/Button";
import { Link } from "react-router-dom";

export function QuickNav() {
    return (
        <div className="grid grid-cols-2 gap-3">
            <Link to="/profile">
                <Button fullWidth className="text-center">
                    👤 Profile
                </Button>
            </Link>
            <Link to="/goals">
                <Button fullWidth className="text-center">
                    🎯 Goals
                </Button>
            </Link>
            <Link to="/trends">
                <Button fullWidth className="text-center">
                    📈 Trends
                </Button>
            </Link>
            <Link to="/checkin">
                <Button fullWidth className="text-center">
                    ✅ Check In
                </Button>
            </Link>
        </div>
    );
}
