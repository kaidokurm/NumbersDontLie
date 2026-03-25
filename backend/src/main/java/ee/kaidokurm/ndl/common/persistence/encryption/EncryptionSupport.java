package ee.kaidokurm.ndl.common.persistence.encryption;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class EncryptionSupport {

    private static final String PREFIX = "enc:v1:";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private EncryptionSupport() {
    }

    static String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] packed = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(ciphertext, 0, packed, iv.length, ciphertext.length);
            return PREFIX + Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    static String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        if (!encrypted.startsWith(PREFIX)) {
            // Legacy/plaintext fallback: passthrough.
            return encrypted;
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
            cipher.init(Cipher.DECRYPT_MODE, keySpec(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private static SecretKeySpec keySpec() {
        String keyMaterial = System.getenv().getOrDefault("APP_DATA_ENCRYPTION_KEY", "change-me-in-env");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] key = md.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive encryption key", e);
        }
    }
}
