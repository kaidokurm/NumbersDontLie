import type { SelectHTMLAttributes } from "react";

type Option = { value: string; label: string };

type Props = SelectHTMLAttributes<HTMLSelectElement> & {
    label: string;
    options: Option[];
    error?: string;
};

export function SelectField({ label, options, error, className = "", ...props }: Props) {
    return (
        <div className="space-y-1">
            <label className="block text-sm font-medium text-slate-700">{label}</label>
            <select
                className={[
                    "w-full rounded-lg border bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none transition",
                    error ? "border-red-300 focus:ring-2 focus:ring-red-200" : "border-slate-300 focus:ring-2 focus:ring-green-200",
                    className,
                ].join(" ")}
                {...props}
            >
                {options.map((o) => (
                    <option key={o.value} value={o.value}>
                        {o.label}
                    </option>
                ))}
            </select>
            {error && <div className="text-sm text-red-700">{error}</div>}
        </div>
    );
}
