import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";

interface ActivityHeatmapProps {
    dates: string[];
}

type HeatCell = {
    date: string;
    count: number;
};

function buildHeatmap(dates: string[], days = 84): HeatCell[] {
    const counts = new Map<string, number>();
    for (const date of dates) {
        counts.set(date, (counts.get(date) || 0) + 1);
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const cells: HeatCell[] = [];

    for (let i = days - 1; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(today.getDate() - i);
        const key = d.toISOString().split("T")[0];
        cells.push({ date: key, count: counts.get(key) || 0 });
    }
    return cells;
}

function cellColor(count: number): string {
    if (count <= 0) return "bg-slate-100";
    if (count === 1) return "bg-emerald-200";
    if (count === 2) return "bg-emerald-400";
    return "bg-emerald-600";
}

export function ActivityHeatmap({ dates }: ActivityHeatmapProps) {
    const cells = buildHeatmap(dates, 84);
    const activeDays = cells.filter((c) => c.count > 0).length;

    return (
        <Card>
            <CardTitle>🗓️ Activity Heatmap</CardTitle>
            <CardBody>
                <div className="text-xs text-slate-500 mb-3">
                    Last 12 weeks of check-ins ({activeDays} active days)
                </div>
                <div className="overflow-x-auto">
                    <div className="grid grid-flow-col grid-rows-7 gap-1 w-max">
                        {cells.map((cell) => (
                            <div
                                key={cell.date}
                                className={`w-3 h-3 rounded-sm ${cellColor(cell.count)}`}
                                title={`${cell.date}: ${cell.count} check-in${cell.count === 1 ? "" : "s"}`}
                            />
                        ))}
                    </div>
                </div>
                <div className="mt-3 flex items-center gap-2 text-xs text-slate-500">
                    <span>Less</span>
                    <span className="w-3 h-3 rounded-sm bg-slate-100" />
                    <span className="w-3 h-3 rounded-sm bg-emerald-200" />
                    <span className="w-3 h-3 rounded-sm bg-emerald-400" />
                    <span className="w-3 h-3 rounded-sm bg-emerald-600" />
                    <span>More</span>
                </div>
            </CardBody>
        </Card>
    );
}
