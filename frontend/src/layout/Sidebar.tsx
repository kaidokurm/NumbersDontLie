import { NavLink } from "react-router-dom";
import {
    HomeIcon,
    PlusCircleIcon,
    ChartBarIcon,
    AdjustmentsHorizontalIcon,
    UserCircleIcon,
    Cog6ToothIcon,
} from "@heroicons/react/24/outline";
import { AuthButton } from "../features/auth/AuthButton";

// Sidebar: Neutral white background, slate grays, soft green accents.
// Modern, professional navigation with clear active states.
export default function Sidebar() {
    return (
        <aside className="hidden md:flex md:flex-col md:w-56 md:h-screen md:fixed md:left-0 md:top-0 bg-white border-r border-slate-200 z-20">
            <div className="flex flex-col gap-1 p-4 flex-1">
                <NavLink
                    to="/"
                    end
                    className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? "bg-green-50 text-green-700" : "text-slate-600 hover:bg-slate-50"}`
                    }
                >
                    <HomeIcon className="h-5 w-5" /> Dashboard
                </NavLink>
                <NavLink
                    to="/checkin"
                    className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? "bg-green-50 text-green-700" : "text-slate-600 hover:bg-slate-50"}`
                    }
                >
                    <PlusCircleIcon className="h-5 w-5" /> Check In
                </NavLink>
                <NavLink
                    to="/trends"
                    className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? "bg-green-50 text-green-700" : "text-slate-600 hover:bg-slate-50"}`
                    }
                >
                    <ChartBarIcon className="h-5 w-5" /> Trends
                </NavLink>
                <NavLink
                    to="/goals"
                    className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? "bg-green-50 text-green-700" : "text-slate-600 hover:bg-slate-50"}`
                    }
                >
                    <AdjustmentsHorizontalIcon className="h-5 w-5" /> Goals
                </NavLink>
                <NavLink
                    to="/profile"
                    className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? "bg-green-50 text-green-700" : "text-slate-600 hover:bg-slate-50"}`
                    }
                >
                    <UserCircleIcon className="h-5 w-5" /> Health Profile
                </NavLink>
                <NavLink
                    to="/settings"
                    className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? "bg-green-50 text-green-700" : "text-slate-600 hover:bg-slate-50"}`
                    }
                >
                    <Cog6ToothIcon className="h-5 w-5" /> Settings
                </NavLink>
            </div>
            {/* Auth controls at the bottom */}
            <div className="p-4 border-t border-slate-200">
                <AuthButton />
            </div>
        </aside>
    );
}
