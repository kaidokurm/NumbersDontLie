package ee.kaidokurm.ndl.privacy;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivacyPreferencesService {

    private final PrivacyPreferencesRepository repository;

    public PrivacyPreferencesService(PrivacyPreferencesRepository repository) {
        this.repository = repository;
    }

    public PrivacyPreferencesEntity getOrDefault(UUID userId) {
        return repository.findByUserId(userId).orElseGet(() -> defaultForUser(userId));
    }

    public boolean hasDataUsageConsent(UUID userId) {
        return repository.findByUserId(userId).map(PrivacyPreferencesEntity::isDataUsageConsent).orElse(false);
    }

    @Transactional
    public PrivacyPreferencesEntity upsert(UUID userId, boolean dataUsageConsent, boolean allowAnonymizedAnalytics,
            boolean publicProfileVisible, boolean emailNotificationsEnabled) {
        OffsetDateTime now = OffsetDateTime.now();
        PrivacyPreferencesEntity entity = repository.findByUserId(userId).orElseGet(() -> defaultForUser(userId));

        entity.setDataUsageConsent(dataUsageConsent);
        entity.setConsentGivenAt(dataUsageConsent ? now : null);
        entity.setAllowAnonymizedAnalytics(dataUsageConsent && allowAnonymizedAnalytics);
        entity.setPublicProfileVisible(dataUsageConsent && publicProfileVisible);
        entity.setEmailNotificationsEnabled(emailNotificationsEnabled);
        entity.setUpdatedAt(now);

        return repository.save(entity);
    }

    private PrivacyPreferencesEntity defaultForUser(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new PrivacyPreferencesEntity(userId, false, null, false, false, true, now, now);
    }
}
