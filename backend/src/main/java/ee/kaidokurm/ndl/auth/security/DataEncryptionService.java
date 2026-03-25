package ee.kaidokurm.ndl.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataEncryptionService {

    private static final String PREFIX = "enc:v1:";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;

    private final SecretKeySpec keySpec;
    private final SecureRandom random = new SecureRandom();

    public DataEncryptionService(@Value("${app.security.data-encryption-key:change-me-in-env}") String keyMaterial) {
        this.keySpec = new SecretKeySpec(deriveKey(keyMaterial), "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] packed = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(ciphertext, 0, packed, iv.length, ciphertext.length);

            return PREFIX + Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        if (!encrypted.startsWith(PREFIX)) {
            // Backward compatibility for older base64-obfuscated 2FA secrets.
            try {
                return new String(Base64.getDecoder().decode(encrypted), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                return encrypted;
            }
        }
        try {
            String encoded = encrypted.substring(PREFIX.length());
            byte[] packed = Base64.getDecoder().decode(encoded);
            if (packed.length <= IV_LEN) {
                throw new IllegalArgumentException("Invalid encrypted payload");
            }

            byte[] iv = new byte[IV_LEN];
            byte[] ciphertext = new byte[packed.length - IV_LEN];
            System.arraycopy(packed, 0, iv, 0, IV_LEN);
            System.arraycopy(packed, IV_LEN, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private byte[] deriveKey(String keyMaterial) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest((keyMaterial == null ? "" : keyMaterial).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive encryption key", e);
        }
    }
}
