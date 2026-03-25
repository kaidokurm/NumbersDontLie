// SplashScreen: Calming, branded loading overlay for app-wide use
export function SplashScreen() {
    return (
        <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-blue-100/80 backdrop-blur-sm">
            <span className="font-bold text-2xl text-blue-700 mb-2 select-none">Numbers Don't Lie</span>
            <div className="w-8 h-8 border-4 border-blue-300 border-t-blue-600 rounded-full animate-spin mb-4"></div>
            <p className="text-gray-600">Loading...</p>
        </div>
    );
}
