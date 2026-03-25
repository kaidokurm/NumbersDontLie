package ee.kaidokurm.ndl.health.summary;

import ee.kaidokurm.ndl.health.weight.WeightEntryEntity;
import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HealthSummaryService {

    @SuppressWarnings("unused")
    private final WeightEntryRepository weightRepo;

    public HealthSummaryService(WeightEntryRepository weightRepo) {
        this.weightRepo = weightRepo;
    }

    public double bmi(int heightCm, double weightKg) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public Double weightDelta7d(List<WeightEntryEntity> entries) {
        if (entries.size() < 2)
            return null;

        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(7);

        WeightEntryEntity newest = entries.get(0);
        WeightEntryEntity oldest = null;

        for (WeightEntryEntity e : entries) {
            if (e.getMeasuredAt().isBefore(cutoff)) {
                oldest = e;
                break;
            }
        }

        return oldest != null ? newest.getWeightKg() - oldest.getWeightKg() : null;
    }

    /**
     * Calculate weight change over a date range
     */
    public Double weightDeltaForPeriod(List<WeightEntryEntity> entries, int daysAgo) {
        if (entries.isEmpty())
            return null;

        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(daysAgo);

        WeightEntryEntity newest = entries.get(0);
        WeightEntryEntity oldest = null;

        for (WeightEntryEntity e : entries) {
            if (e.getMeasuredAt().isBefore(cutoff)) {
                oldest = e;
                break;
            }
        }

        return oldest != null ? newest.getWeightKg() - oldest.getWeightKg() : null;
    }

    /**
     * Get weight entries within a date range
     */
    public List<WeightEntryEntity> entriesInRange(List<WeightEntryEntity> entries, LocalDate startDate,
            LocalDate endDate) {
        OffsetDateTime startDt = startDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
        OffsetDateTime endDt = endDate.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC);

        return entries.stream().filter(e -> e.getMeasuredAt().isAfter(startDt) && e.getMeasuredAt().isBefore(endDt))
                .toList();
    }
}
