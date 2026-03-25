package ee.kaidokurm.ndl.config;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * Composite JWT decoder that supports multiple issuers. Routes token validation
 * to the appropriate decoder based on the "iss" (issuer) claim.
 * 
 * Useful for supporting: - Auth0 OAuth2 tokens (issuer: Auth0 domain) - Local
 * email/password tokens (issuer: "numbers-dont-lie")
 */
public class MultiIssuerJwtDecoder implements JwtDecoder {

    private static final Logger log = LoggerFactory.getLogger(MultiIssuerJwtDecoder.class);
    private final Map<String, JwtDecoder> decoders = new HashMap<>();

    public MultiIssuerJwtDecoder(String auth0Issuer, String localIssuer, JwtDecoder auth0Decoder,
            JwtDecoder localDecoder) {
        // Register decoders for each issuer
        this.decoders.put(auth0Issuer, auth0Decoder);
        this.decoders.put(localIssuer, localDecoder);
        log.info("MultiIssuerJwtDecoder initialized with decoders for: {}", decoders.keySet());
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        // Try to extract issuer from token without full validation
        String issuer = extractIssuerWithoutValidation(token);

        if (issuer == null) {
            log.warn("Could not extract issuer from token");
            throw new JwtException("Token issuer not recognized: null");
        }

        if (!decoders.containsKey(issuer)) {
            log.warn("Token issuer not registered: {}. Available: {}", issuer, decoders.keySet());
            throw new JwtException("Token issuer not recognized: " + issuer);
        }

        log.debug("Routing token to decoder for issuer: {}", issuer);
        // Use the appropriate decoder for this issuer
        JwtDecoder decoder = decoders.get(issuer);
        try {
            return decoder.decode(token);
        } catch (JwtException e) {
            log.error("JWT validation failed for issuer {}: {}", issuer, e.getMessage());
            throw e;
        }
    }

    /**
     * Quick issuer extraction by parsing JWT parts without validation. Returns null
     * if unable to extract.
     */
    private String extractIssuerWithoutValidation(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Token does not have 3 parts");
                return null;
            }

            // Decode the payload (second part)
            String payload = parts[1];
            byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(payload);
            String payloadJson = new String(decodedBytes);

            // Simple JSON extraction for "iss" claim
            int issStart = payloadJson.indexOf("\"iss\":");
            if (issStart == -1) {
                log.warn("No iss claim found in token payload");
                return null;
            }

            issStart = payloadJson.indexOf("\"", issStart + 6) + 1;
            int issEnd = payloadJson.indexOf("\"", issStart);

            String extractedIssuer = payloadJson.substring(issStart, issEnd);
            log.debug("Extracted issuer from token: {}", extractedIssuer);
            return extractedIssuer;
        } catch (Exception e) {
            log.error("Error extracting issuer from token: {}", e.getMessage());
            return null;
        }
    }
}
