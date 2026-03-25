import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import type { WellnessHistoryPoint } from "../../../shared/api/wellness";

interface WellnessEvolutionChartProps {
    points: WellnessHistoryPoint[];
}

export function WellnessEvolutionChart({ points }: WellnessEvolutionChartProps) {
    if (!points.length) {
        return (
            <Card>
                <CardTitle>💚 Wellness Evolution</CardTitle>
                <CardBody>
                    <p className="text-slate-600 text-center py-6">Not enough data for wellness trend yet.</p>
                </CardBody>
            </Card>
        );
    }

    const width = 400;
    const height = 220;
    const padding = { top: 20, right: 20, bottom: 35, left: 35 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    const minScore = Math.max(0, Math.min(...points.map((p) => p.score)) - 5);
    const maxScore = Math.min(100, Math.max(...points.map((p) => p.score)) + 5);
    const scoreRange = Math.max(1, maxScore - minScore);

    const scaleX = (index: number) =>
        padding.left + (index / Math.max(1, points.length - 1)) * chartWidth;
    const scaleY = (score: number) =>
        padding.top + chartHeight - ((score - minScore) / scoreRange) * chartHeight;

    const linePoints = points
        .map((p, idx) => `${scaleX(idx)},${scaleY(p.score)}`)
        .join(" ");

    return (
        <Card>
            <CardTitle>💚 Wellness Evolution</CardTitle>
            <CardBody>
                <div className="overflow-x-auto pb-3">
                    <svg
                        width={width}
                        height={height}
                        viewBox={`0 0 ${width} ${height}`}
                        className="border border-slate-200 rounded bg-white w-full min-w-[400px]"
                    >
                        <line
                            x1={padding.left}
                            y1={padding.top}
                            x2={padding.left}
                            y2={height - padding.bottom}
                            stroke="#0f172a"
                            strokeWidth="1"
                        />
                        <line
                            x1={padding.left}
                            y1={height - padding.bottom}
                            x2={width - padding.right}
                            y2={height - padding.bottom}
                            stroke="#0f172a"
                            strokeWidth="1"
                        />

                        <polyline
                            points={linePoints}
                            fill="none"
                            stroke="#10b981"
                            strokeWidth="2"
                            vectorEffect="non-scaling-stroke"
                        />

                        {points.map((p, idx) => (
                            <circle
                                key={`${p.weekEnd}-${idx}`}
                                cx={scaleX(idx)}
                                cy={scaleY(p.score)}
                                r="3"
                                fill="#10b981"
                            />
                        ))}

                        {points.map((p, idx) => {
                            if (idx % Math.max(1, Math.floor(points.length / 4)) !== 0 && idx !== points.length - 1) {
                                return null;
                            }
                            return (
                                <text
                                    key={`label-${p.weekEnd}`}
                                    x={scaleX(idx)}
                                    y={height - padding.bottom + 16}
                                    textAnchor="middle"
                                    className="text-[10px] fill-slate-500"
                                >
                                    {new Date(p.weekEnd).toLocaleDateString(undefined, { month: "numeric", day: "numeric" })}
                                </text>
                            );
                        })}
                    </svg>
                </div>
                <div className="text-xs text-slate-500">
                    {points.length} weekly points
                </div>
            </CardBody>
        </Card>
    );
}
