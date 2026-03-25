import { HeartIcon } from "@heroicons/react/24/outline";

// Header: Minimal, sleek branding. Small icon + short text, left-aligned.
// Neutral white background with subtle border. Modern, professional aesthetic.
export default function Header() {
    return (
        <header className="w-full bg-white border-b border-slate-200 shadow-sm sticky top-0 z-30">
            <div className="max-w-md md:max-w-full mx-auto flex items-center px-4 py-3 gap-2">
                {/* Heart icon - wellness theme */}
                <HeartIcon className="h-5 w-5 text-green-700 shrink-0" />
                {/* Short brand text, subtle color */}
                <span className="text-sm font-semibold text-slate-700 tracking-tight select-none">
                    NDL
                </span>
            </div>
        </header>
    );
}
