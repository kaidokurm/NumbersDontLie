import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import type { Insight } from "../../../shared/types";
import { Link } from "react-router-dom";

interface InsightCardProps {
    insight: Insight | null;
    consentRequired?: boolean;
}

export function InsightCard({ insight, consentRequired = false }: InsightCardProps) {
    if (consentRequired) {
        return (
            <Card>
                <CardTitle>✨ AI Insight</CardTitle>
                <CardBody>
                    <p className="text-sm text-slate-700 mb-3">
                        AI insights are disabled until data usage consent is enabled.
                    </p>
                    <Link
                        to="/settings"
                        className="inline-block px-3 py-2 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700 transition"
                    >
                        Open Settings →
                    </Link>
                </CardBody>
            </Card>
        );
    }

    if (!insight) {
        return null;
    }

    const { payload, source, createdAt } = insight;
    const sourceLabel =
        source === "openai" ? "AI Generated" :
            source === "cache" || source === "cached" ? "Cached" :
                "Generic Wellness Tip";

    const priorityForIndex = (index: number) => {
        if (index === 0) return { label: "High", style: "bg-red-100 text-red-800" };
        if (index === 1) return { label: "Medium", style: "bg-amber-100 text-amber-800" };
        return { label: "Low", style: "bg-blue-100 text-blue-800" };
    };

    return (
        <Card>
            <div className="flex items-center justify-between">
                <CardTitle>✨ AI Insight</CardTitle>
                <span className="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-1 rounded">
                    {sourceLabel}
                </span>
            </div>
            <CardBody>
                <p className="text-sm text-slate-900 mb-3">{payload.summary}</p>

                {payload.recommendations.length > 0 && (
                    <div className="mb-4">
                        <h4 className="text-xs font-semibold text-slate-700 mb-2">
                            Recommendations
                        </h4>
                        <ul className="space-y-1">
                            {payload.recommendations.map((rec, idx) => (
                                <li key={idx} className="text-xs text-slate-600">
                                    <details className="rounded border border-slate-200 p-2 bg-slate-50">
                                        <summary className="flex items-center justify-between cursor-pointer list-none">
                                            <span className="flex items-center gap-2">
                                                <span className="text-green-600">→</span>
                                                <span>{rec}</span>
                                            </span>
                                            <span
                                                className={`px-2 py-0.5 rounded text-[10px] font-semibold ${priorityForIndex(idx).style}`}
                                            >
                                                {priorityForIndex(idx).label}
                                            </span>
                                        </summary>
                                        <p className="mt-2 text-[11px] text-slate-500">
                                            Priority is based on recommendation order in the latest AI insight.
                                        </p>
                                    </details>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}

                {payload.reflection_question && (
                    <div className="p-2 bg-green-50 rounded border border-green-100">
                        <p className="text-xs italic text-slate-700">
                            💭 <span className="font-medium">Reflect:</span>{" "}
                            {payload.reflection_question}
                        </p>
                    </div>
                )}

                <div className="text-xs text-slate-500 mt-3">
                    {new Date(createdAt).toLocaleDateString()}
                </div>
            </CardBody>
        </Card>
    );
}
