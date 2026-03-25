package ee.kaidokurm.ndl.common.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private record CounterWindow(long windowStartMs, int count) {
    }

    private final ConcurrentHashMap<String, CounterWindow> counters = new ConcurrentHashMap<>();

    public void check(String scope, String subjectKey, int limit, Duration window) {
        if (subjectKey == null || subjectKey.isBlank()) {
            subjectKey = "anonymous";
        }
        long now = System.currentTimeMillis();
        long windowMs = window.toMillis();
        String key = scope + ":" + subjectKey.toLowerCase();

        while (true) {
            CounterWindow current = counters.get(key);
            if (current == null || now - current.windowStartMs >= windowMs) {
                CounterWindow fresh = new CounterWindow(now, 1);
                if (current == null) {
                    if (counters.putIfAbsent(key, fresh) == null) {
                        return;
                    }
                } else if (counters.replace(key, current, fresh)) {
                    return;
                }
                continue;
            }

            if (current.count >= limit) {
                long retryAfterMs = windowMs - (now - current.windowStartMs);
                long retryAfterSeconds = Math.max(1, (retryAfterMs + 999) / 1000);
                throw new RateLimitExceededException(
                        "Too many requests. Please retry in " + retryAfterSeconds + " seconds.",
                        retryAfterSeconds);
            }

            CounterWindow next = new CounterWindow(current.windowStartMs, current.count + 1);
            if (counters.replace(key, current, next)) {
                return;
            }
        }
    }
}
