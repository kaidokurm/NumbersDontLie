package ee.kaidokurm.ndl.auth.twofactor;

import ee.kaidokurm.ndl.auth.security.DataEncryptionService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TwoFactorService {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int WINDOW_STEPS = 1;

    private final TwoFactorSecretRepository repository;
    private final DataEncryptionService dataEncryptionService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.two-factor.issuer:NumbersDontLie}")
    private String issuer;

    public TwoFactorService(TwoFactorSecretRepository repository, DataEncryptionService dataEncryptionService) {
        this.repository = repository;
        this.dataEncryptionService = dataEncryptionService;
    }

    public record SetupResult(String secret, String otpauthUri) {
    }

    public record StatusResult(boolean enabled) {
    }

    @Transactional
    public SetupResult setup(UUID userId, String accountLabel) {
        String secret = generateBase32Secret();
        String encrypted = encryptSecret(secret);
        OffsetDateTime now = OffsetDateTime.now();

        TwoFactorSecretEntity entity = repository.findByUserId(userId)
                .map(existing -> {
                    existing.setSecretEncrypted(encrypted);
                    existing.setEnabled(false);
                    existing.setVerifiedAt(null);
                    existing.setUpdatedAt(now);
                    return existing;
                })
                .orElseGet(() -> new TwoFactorSecretEntity(userId, encrypted, false, null, now, now));

        repository.save(entity);

        String label = URLEncoder.encode(accountLabel, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String otpauth = "otpauth://totp/" + encodedIssuer + ":" + label
                + "?secret=" + secret
                + "&issuer=" + encodedIssuer
                + "&algorithm=SHA1"
                + "&digits=6"
                + "&period=30";

        return new SetupResult(secret, otpauth);
    }

    @Transactional
    public boolean enable(UUID userId, String code) {
        TwoFactorSecretEntity entity = repository.findByUserId(userId).orElse(null);
        if (entity == null) {
            return false;
        }
        String secret = decryptSecret(entity.getSecretEncrypted());
        if (!verifyTotpCode(secret, code)) {
            return false;
        }
        entity.setEnabled(true);
        entity.setVerifiedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        repository.save(entity);
        return true;
    }

    @Transactional
    public boolean disable(UUID userId, String code) {
        TwoFactorSecretEntity entity = repository.findByUserId(userId).orElse(null);
        if (entity == null || !entity.isEnabled()) {
            return false;
        }
        String secret = decryptSecret(entity.getSecretEncrypted());
        if (!verifyTotpCode(secret, code)) {
            return false;
        }
        entity.setEnabled(false);
        entity.setUpdatedAt(OffsetDateTime.now());
        repository.save(entity);
        return true;
    }

    public StatusResult status(UUID userId) {
        boolean enabled = repository.findByUserId(userId).map(TwoFactorSecretEntity::isEnabled).orElse(false);
        return new StatusResult(enabled);
    }

    public boolean isEnabled(UUID userId) {
        return repository.findByUserId(userId).map(TwoFactorSecretEntity::isEnabled).orElse(false);
    }

    public boolean verifyCode(UUID userId, String code) {
        TwoFactorSecretEntity entity = repository.findByUserId(userId).orElse(null);
        if (entity == null || !entity.isEnabled()) {
            return false;
        }
        String secret = decryptSecret(entity.getSecretEncrypted());
        return verifyTotpCode(secret, code);
    }

    private String generateBase32Secret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return base32Encode(bytes);
    }

    private boolean verifyTotpCode(String base32Secret, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long currentStep = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
        for (long i = -WINDOW_STEPS; i <= WINDOW_STEPS; i++) {
            String expected = generateTotp(base32Secret, currentStep + i);
            if (expected.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateTotp(String base32Secret, long timeStep) {
        try {
            byte[] secret = base32Decode(base32Secret);
            byte[] data = new byte[8];
            long value = timeStep;
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (value & 0xFF);
                value >>= 8;
            }

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%06d", otp);
        } catch (Exception e) {
            return "";
        }
    }

    private String base32Encode(byte[] data) {
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = data[0];
        int next = 1;
        int bitsLeft = 8;
        while (bitsLeft > 0 || next < data.length) {
            if (bitsLeft < 5) {
                if (next < data.length) {
                    buffer <<= 8;
                    buffer |= (data[next++] & 0xFF);
                    bitsLeft += 8;
                } else {
                    int pad = 5 - bitsLeft;
                    buffer <<= pad;
                    bitsLeft += pad;
                }
            }
            int index = (buffer >> (bitsLeft - 5)) & 0x1F;
            bitsLeft -= 5;
            result.append(BASE32_ALPHABET.charAt(index));
        }
        return result.toString();
    }

    private byte[] base32Decode(String base32) {
        String normalized = base32.replace("=", "").toUpperCase();
        byte[] bytes = new byte[normalized.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;
        for (char c : normalized.toCharArray()) {
            int val = BASE32_ALPHABET.indexOf(c);
            if (val < 0) {
                continue;
            }
            buffer <<= 5;
            buffer |= val & 0x1F;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[count++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        if (count == bytes.length) {
            return bytes;
        }
        byte[] out = new byte[count];
        System.arraycopy(bytes, 0, out, 0, count);
        return out;
    }

    private String encryptSecret(String raw) {
        return dataEncryptionService.encrypt(raw);
    }

    private String decryptSecret(String encrypted) {
        return dataEncryptionService.decrypt(encrypted);
    }
}
