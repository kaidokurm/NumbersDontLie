import { Button } from "../../../shared/ui/Button";
import { Card, CardBody, CardTitle } from "../../../shared/ui/Card";
import type { WeightEntry } from "../../../shared/types";

interface WeightCardProps {
    latestWeight: WeightEntry | null;
    isLoading: boolean;
}

export function WeightCard({ latestWeight, isLoading }: WeightCardProps) {
    if (isLoading && !latestWeight) {
        return null;
    }

    if (latestWeight) {
        return (
            <Card>
                <CardTitle>Current Weight</CardTitle>
                <CardBody>
                    <div className="flex items-end gap-4">
                        <div>
                            <div className="text-4xl font-bold text-slate-900">
                                {latestWeight.weight}
                            </div>
                            <div className="text-sm text-slate-600">kg</div>
                        </div>
                    </div>
                    <div className="mt-2 text-xs text-slate-500">
                        Last recorded: {new Date(latestWeight.date).toLocaleDateString()}
                    </div>
                    <a href="/checkin">
                        <Button fullWidth className="mt-3">
                            Record New Weight
                        </Button>
                    </a>
                </CardBody>
            </Card>
        );
    }

    return (
        <Card>
            <CardBody>
                <p className="text-slate-600 mb-4">
                    Start tracking by recording your first weight entry.
                </p>
                <a href="/checkin">
                    <Button fullWidth>Record Your Weight</Button>
                </a>
            </CardBody>
        </Card>
    );
}
