import type { InputHTMLAttributes } from "react";

type Props = InputHTMLAttributes<HTMLInputElement> & {
    label: string;
    error?: string;
};

export function TextField({ label, error, className = "", ...props }: Props) {
    return (
        <div className="space-y-1">
            <label className="block text-sm font-medium text-slate-700">{label}</label>
            <input
                className={[
                    "w-full rounded-lg border bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none transition",
                    error ? "border-red-300 focus:ring-2 focus:ring-red-200" : "border-slate-300 focus:ring-2 focus:ring-green-200",
                    className,
                ].join(" ")}
                {...props}
            />
            {error && <div className="text-sm text-red-700">{error}</div>}
        </div>
    );
}
