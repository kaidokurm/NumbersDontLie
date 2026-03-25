import { useEffect, useState } from "react";

export function useApiQuery<T>(key: string, fn: () => Promise<T>, enabled: boolean) {
    const [data, setData] = useState<T | null>(null);
    const [error, setError] = useState<Error | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        let cancelled = false;

        if (!enabled) {
            setData(null);
            setError(null);
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);

        fn()
            .then((d) => {
                if (!cancelled) setData(d);
            })
            .catch((e) => {
                if (!cancelled) setError(e instanceof Error ? e : new Error("Unknown error"));
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [enabled, key]);

    return { data, error, loading };
}
