export const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || (import.meta.env.DEV ? "http://localhost:8080" : "");

export const AUTH0_AUDIENCE =
    import.meta.env.VITE_AUTH0_AUDIENCE ?? "https://numbers-dont-lie-api";

export const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === "true";