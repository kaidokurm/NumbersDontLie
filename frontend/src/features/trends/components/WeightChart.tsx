import type { ChartDataPoint, MilestoneMarker } from "../useWeightChartData";
import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";

interface WeightChartProps {
    points: ChartDataPoint[];
    milestones: MilestoneMarker[];
    targetWeight: number | null;
    minWeight: number;
    maxWeight: number;
}

export function WeightChart({
    points,
    milestones,
    targetWeight,
    minWeight,
    maxWeight,
}: WeightChartProps) {
    if (points.length === 0) {
        return (
            <Card>
                <CardTitle>📊 Weight Trend</CardTitle>
                <CardBody>
                    <p className="text-slate-600 text-center py-8">
                        No weight data yet. Start tracking to see your trend!
                    </p>
                </CardBody>
            </Card>
        );
    }

    // Chart dimensions
    const width = 400;
    const height = 300;
    const padding = { top: 20, right: 20, bottom: 40, left: 50 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // Calculate scaling
    const weightRange = maxWeight - minWeight;
    const daysRange = Math.max(...points.map((p) => p.daysAgo), 1);

    // Scale functions
    const scaleX = (daysAgo: number) => {
        return padding.left + ((daysRange - daysAgo) / daysRange) * chartWidth;
    };

    const scaleY = (weight: number) => {
        return padding.top + chartHeight - ((weight - minWeight) / weightRange) * chartHeight;
    };

    // Polyline expects points in "x,y x,y ..." format (no M/L commands).
    const polylinePoints = points
        .map((point) => {
            const x = scaleX(point.daysAgo);
            const y = scaleY(point.weight);
            return `${x},${y}`;
        })
        .join(" ");

    // Current weight (most recent)
    const currentWeight = points[points.length - 1]?.weight || 0;

    // Target weight line position
    const targetY = targetWeight ? scaleY(targetWeight) : null;

    // Grid lines - weight labels
    const gridLines = [];
    const gridStep = Math.ceil(weightRange / 5);
    for (let w = Math.ceil(minWeight / gridStep) * gridStep; w <= maxWeight; w += gridStep) {
        const y = scaleY(w);
        gridLines.push(
            <g key={`grid-${w}`}>
                <line
                    x1={padding.left}
                    y1={y}
                    x2={width - padding.right}
                    y2={y}
                    stroke="#e2e8f0"
                    strokeWidth="1"
                />
                <text
                    x={padding.left - 8}
                    y={y + 4}
                    textAnchor="end"
                    className="text-xs fill-slate-500"
                >
                    {w}kg
                </text>
            </g>
        );
    }

    // Date labels (every nth day)
    const dateLabels = [];
    const labelInterval = Math.ceil(points.length / 5); // Show ~5 labels
    for (let i = 0; i < points.length; i += labelInterval) {
        const point = points[i];
        const x = scaleX(point.daysAgo);
        const dateObj = new Date(point.date);
        const month = String(dateObj.getMonth() + 1).padStart(2, "0");
        const day = String(dateObj.getDate()).padStart(2, "0");

        dateLabels.push(
            <text
                key={`date-${i}`}
                x={x}
                y={height - padding.bottom + 20}
                textAnchor="middle"
                className="text-xs fill-slate-500"
            >
                {month}/{day}
            </text>
        );
    }

    // Recent point (last weight entry)
    const lastPoint = points[points.length - 1];
    const lastX = scaleX(lastPoint.daysAgo);
    const lastY = scaleY(lastPoint.weight);
    const visibleMilestones = milestones.filter((m) => m.daysAgo <= daysRange);

    return (
        <Card>
            <CardTitle>📊 Weight Trend</CardTitle>
            <CardBody>
                <div className="overflow-x-auto pb-4">
                    <svg
                        width={width}
                        height={height}
                        className="border border-slate-200 rounded bg-white w-full min-w-[400px]"
                        viewBox={`0 0 ${width} ${height}`}
                    >
                        {/* Grid lines */}
                        {gridLines}

                        {/* Target weight reference line */}
                        {targetY !== null && (
                            <>
                                <line
                                    x1={padding.left}
                                    y1={targetY}
                                    x2={width - padding.right}
                                    y2={targetY}
                                    stroke="#93c5fd"
                                    strokeWidth="2"
                                    strokeDasharray="5,5"
                                    opacity="0.7"
                                />
                                <text
                                    x={width - padding.right - 5}
                                    y={targetY - 5}
                                    textAnchor="end"
                                    className="text-xs fill-blue-500"
                                    fontWeight="bold"
                                >
                                    Target: {targetWeight}kg
                                </text>
                            </>
                        )}

                        {/* Milestone markers */}
                        {visibleMilestones.map((milestone, idx) => {
                            const x = scaleX(milestone.daysAgo);
                            return (
                                <g key={`milestone-${idx}`}>
                                    <line
                                        x1={x}
                                        y1={padding.top}
                                        x2={x}
                                        y2={height - padding.bottom}
                                        stroke="#f59e0b"
                                        strokeWidth="1"
                                        strokeDasharray="3,3"
                                        opacity="0.7"
                                    />
                                    <circle
                                        cx={x}
                                        cy={padding.top + 8}
                                        r="4"
                                        fill="#f59e0b"
                                    />
                                    <text
                                        x={x}
                                        y={padding.top + 22}
                                        textAnchor="middle"
                                        className="text-[10px] fill-amber-700"
                                        fontWeight="bold"
                                    >
                                        {milestone.percentage}%
                                    </text>
                                </g>
                            );
                        })}

                        {/* Weight line chart */}
                        <polyline
                            points={polylinePoints}
                            fill="none"
                            stroke="#3b82f6"
                            strokeWidth="2"
                            vectorEffect="non-scaling-stroke"
                        />

                        {/* Data points (circles) */}
                        {points.map((point, idx) => (
                            <circle
                                key={`point-${idx}`}
                                cx={scaleX(point.daysAgo)}
                                cy={scaleY(point.weight)}
                                r="3"
                                fill="#3b82f6"
                                opacity="0.6"
                            />
                        ))}

                        {/* Highlight current (most recent) point */}
                        <circle
                            cx={lastX}
                            cy={lastY}
                            r="5"
                            fill="#1e40af"
                            opacity="0.8"
                        />

                        {/* Axes */}
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

                        {/* Date labels */}
                        {dateLabels}
                    </svg>
                </div>

                {/* Chart legend and info */}
                <div className="mt-4 flex flex-col gap-2 text-sm">
                    <div className="flex items-center gap-2">
                        <div className="w-3 h-3 bg-blue-600 rounded"></div>
                        <span className="text-slate-700">Current: {currentWeight}kg</span>
                    </div>
                    {targetWeight && (
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-1 bg-blue-300" style={{ width: "12px" }}></div>
                            <span className="text-slate-700">Target: {targetWeight}kg</span>
                        </div>
                    )}
                    {visibleMilestones.length > 0 && (
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-1 bg-amber-500" style={{ width: "12px" }}></div>
                            <span className="text-slate-700">Milestones: {visibleMilestones.length}</span>
                        </div>
                    )}
                    {targetWeight && currentWeight < targetWeight && (
                        <div className="text-green-600 font-medium">
                            🎯 {(targetWeight - currentWeight).toFixed(1)}kg to goal
                        </div>
                    )}
                    {targetWeight && currentWeight > targetWeight && (
                        <div className="text-amber-600 font-medium">
                            📍 {(currentWeight - targetWeight).toFixed(1)}kg above target
                        </div>
                    )}
                    <div className="text-xs text-slate-500">
                        {points.length} entries over {Math.max(...points.map((p) => p.daysAgo))} days
                    </div>
                </div>
            </CardBody>
        </Card>
    );
}
