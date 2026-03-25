import React from "react";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
    ({ label, className = "", ...props }, ref) => (
        <div className="mb-4">
            {label && <label className="block mb-1 text-sm font-medium text-gray-700">{label}</label>}
            <input
                ref={ref}
                className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring ${className}`}
                {...props}
            />
        </div>
    )
);
Input.displayName = "Input";
