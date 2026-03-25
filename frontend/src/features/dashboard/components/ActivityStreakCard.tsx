import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";

interface ActivityStreakCardProps {
    activeDaysLast7: number;
}

export function ActivityStreakCard({ activeDaysLast7 }: ActivityStreakCardProps) {
    const pct = Math.max(0, Math.min(100, Math.round((activeDaysLast7 / 7) * 100)));

    return (
        <Card>
            <CardTitle>Active Days (7d)</CardTitle>
            <CardBody>
                <div className="flex items-end justify-between">
                    <div>
                        <div className="text-4xl font-bold text-slate-900">{activeDaysLast7}<span className="text-xl text-slate-500">/7</span></div>
                        <div className="text-sm text-slate-600">days with activity check-ins</div>
                    </div>
                    <div className="text-sm font-semibold text-emerald-700">{pct}%</div>
                </div>
                <div className="mt-3 h-2 w-full rounded bg-slate-100">
                    <div className="h-2 rounded bg-emerald-500" style={{ width: `${pct}%` }} />
                </div>
            </CardBody>
        </Card>
    );
}
